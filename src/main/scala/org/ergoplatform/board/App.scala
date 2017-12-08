package org.ergoplatform.board

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.ergoplatform.board.handlers.{ElectionResources, SwaggerSupport}
import org.ergoplatform.board.services.ElectionServiceImpl
import org.ergoplatform.board.stores.{ElectionStoreImpl, VoteStoreImpl}
import reactivemongo.api.DefaultDB

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

    val routes = initRoutes(db)
    val bindingFuture = Http().bindAndHandle(routes, host, port)

    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete{_ => terminateMongo; system.terminate()} // and shutdown when done

  }

  def initRoutes(db: DefaultDB): Route = {
    val eStore = new ElectionStoreImpl(db)
    val vStore = new VoteStoreImpl(db)
    val service = new ElectionServiceImpl(eStore, vStore)
    val routes = new ElectionResources(service).routes ~ new SwaggerSupport().assets ~ SwaggerDocService.routes
    routes
  }
}
