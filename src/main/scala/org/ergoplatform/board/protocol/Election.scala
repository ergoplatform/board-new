package org.ergoplatform.board.protocol

import org.ergoplatform.board.models.ElectionRecord
import play.api.libs.json.Json

case class Election(id: String, start: Long, end: Long, description: Option[String])

object Election {
  implicit val fmt = Json.format[Election]

  def fromRecord(e: ElectionRecord): Election = Election(
    id = e._id.toString,
    start = e.start,
    end =  e.end,
    description = e.description
  )
}
