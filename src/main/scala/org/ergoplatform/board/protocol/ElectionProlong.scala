package org.ergoplatform.board.protocol

import play.api.libs.json.Json

case class ElectionProlong(prolongDuration: Long)

object ElectionProlong {
  implicit val fmt = Json.format[ElectionProlong]
}
