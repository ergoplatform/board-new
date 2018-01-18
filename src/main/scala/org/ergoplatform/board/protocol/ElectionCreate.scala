package org.ergoplatform.board.protocol

import play.api.libs.json.Json

case class ElectionCreate(start: Long, end: Long, description: Option[String])

object ElectionCreate {
  implicit val fmt = Json.format[ElectionCreate]
}
