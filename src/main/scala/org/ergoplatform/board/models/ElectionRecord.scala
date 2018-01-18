package org.ergoplatform.board.models

import play.api.libs.json._

case class ElectionRecord(_id: String, start: Long, end: Long, description: Option[String])

object ElectionRecord {
  implicit val fmt: OFormat[ElectionRecord] = Json.format[ElectionRecord]
}
