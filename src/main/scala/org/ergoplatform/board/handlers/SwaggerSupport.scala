package org.ergoplatform.board.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

/**
  * This class is only serving static swagger-ui
  */
class SwaggerSupport(implicit ec: ExecutionContext, mat: ActorMaterializer) {

  def assets = pathPrefix("swagger") {
    getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect))) }

}
