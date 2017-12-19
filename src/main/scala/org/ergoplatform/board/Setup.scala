package org.ergoplatform.board

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

trait Setup {
  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer
  implicit val ec: ExecutionContext

  lazy val config = ConfigFactory.load()
  lazy val logger = Logging(system, getClass)
}
