package org.ergoplatform.board

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

import scala.language.postfixOps

object App extends Setup with Mongo with Services with Rest {

  override implicit val system = ActorSystem("board-system")
  override implicit val mat = ActorMaterializer()
  override implicit val ec = system.dispatcher

  def main(args: Array[String]) {
    sys.addShutdownHook{
      Await.result(connectionToMongo.actorSystem.terminate(), 2 seconds)
      Await.result(system.terminate(), 2 seconds)
    }

    implicit def eh: ExceptionHandler = ApiErrorHandler.exceptionHandler
    val host = Try(config.getString("http.host")).getOrElse("localhost")
    val port = Try(config.getInt("http.port")).getOrElse(8080)

    Http()
      .bindAndHandle(routes, host, port)
      .map { binding => logger.info(s"HTTP server started at ${binding.localAddress}") }
      .recover { case ex => logger.error("Could not start HTTP server", ex) }
  }
}
