package org.ergoplatform.board.protocol

import org.ergoplatform.board.models.ElectionRecord
import play.api.libs.json.Json

case class ElectionView(id: String, start: Long, end: Long, publicKey: String, description: Option[String])

object ElectionView {
  implicit val fmt = Json.format[ElectionView]

  def fromRecord(e: ElectionRecord): ElectionView = ElectionView(
    id = e._id.toString,
    start = e.start,
    end =  e.end,
    publicKey = e.keys.publicKey,
    description = e.description
  )
}
