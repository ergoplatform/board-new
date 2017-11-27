package org.ergoplatform.board.protocol

import org.ergoplatform.board.models.SignedData
import play.api.libs.json.Json

case class VoteCreate(groupId: String,
                      sectionId: String,
                      m: String,
                      signature: SignedData)

object VoteCreate {
  implicit val fmt = Json.format[VoteCreate]
}