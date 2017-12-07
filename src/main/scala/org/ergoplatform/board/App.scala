package org.ergoplatform.board

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.ergoplatform.board.handlers.ElectionResources
import org.ergoplatform.board.models.{SignedData, VoteRecord}
import org.ergoplatform.board.persistence.HashChainVoteProcessor
import org.ergoplatform.board.protocol.VoteCreate
import org.ergoplatform.board.services.ElectionServiceImpl
import org.ergoplatform.board.stores.{ElectionStoreImpl, VoteStoreImpl}
import reactivemongo.api.{DefaultDB, Driver, MongoConnection, MongoDriver}

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Try

object App extends Mongo {

  implicit val system = ActorSystem("board-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val config = ConfigFactory.load()

  def main(args: Array[String]) {

//    val persistentActor = system.actorOf(HashChainVoteProcessor.props("test"))
//
//    val cmd1 = VoteCreate("group1", "section1", "some message1", SignedData.empty)
//    val cmd2 = VoteCreate("group1", "section1", "some message2", SignedData.empty)
//
//    persistentActor ! "print"
////    persistentActor ! cmd1
////    persistentActor ! "print"
////    persistentActor ! cmd2
////    persistentActor ! "print"
//
//    Thread.sleep(1000)
//    system.terminate()
    implicit def eh: ExceptionHandler = ApiErrorHandler.exceptionHandler

    val host = Try(config.getString("http.host")).getOrElse("localhost")
    val port = Try(config.getInt("http.port")).getOrElse(8080)

    val bindingFuture = connectionToMongo
      .flatMap(initRoutes)
      .flatMap { routes =>
        Http().bindAndHandle(routes, host, port)
      }

    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete{_ => terminateMongo; system.terminate()} // and shutdown when done

  }

  def initRoutes(connection: MongoConnection): Future[Route] = {
    val dbName = Try(config.getString("mongodb.dbName")).getOrElse("board")
    def dbF: Future[DefaultDB] = connection.database(dbName)
    val electionRoute = dbF.map { db =>
      val eStore = new ElectionStoreImpl(db)
      val vStore = new VoteStoreImpl(db)
      new ElectionServiceImpl(eStore, vStore)
    }.map {service => new ElectionResources(service).routes}
    electionRoute
  }
}
