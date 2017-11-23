package org.ergoplatform.board

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.ergoplatform.board.handlers.ElectionHandler
import org.ergoplatform.board.services.ElectionServiceImpl
import reactivemongo.api.{DefaultDB, MongoConnection}

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Try

object App extends Mongo {

  implicit val system = ActorSystem("board-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val config = ConfigFactory.load()

  def main(args: Array[String]) {
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
      new ElectionServiceImpl(db)
    }.map {service => new ElectionHandler(service).routes}
    electionRoute
  }
}
