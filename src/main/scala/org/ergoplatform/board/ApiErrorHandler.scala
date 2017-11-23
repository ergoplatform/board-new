package org.ergoplatform.board

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.protocol.ApiError
import play.api.libs.json.{Json, Writes}

object ApiErrorHandler extends PlayJsonSupport {
  implicit val authErrorEncoder: Writes[ApiError] =
    Writes(err => Json.obj("msg" -> err.msg, "status" -> err.statusCode))

  implicit val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: ApiError => complete(StatusCodes.custom(e.statusCode, e.msg) → e)
    case e: NoSuchElementException => complete(StatusCodes.NotFound -> Json.obj("msg" -> e.getMessage))
    case e: IllegalArgumentException => complete(StatusCodes.BadRequest -> Json.obj("msg" -> e.getMessage))
  }

}
