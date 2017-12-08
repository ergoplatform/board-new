package org.ergoplatform.board.protocol

import play.api.libs.json.{Json, OWrites, Writes}

case class ResultResponse[T](value: T)

object ResultResponse {
  implicit def writes[T](implicit tWrites: Writes[T]): OWrites[ResultResponse[T]] = OWrites[ResultResponse[T]] { r =>
    Json.obj("result" -> tWrites.writes(r.value))
  }
}
