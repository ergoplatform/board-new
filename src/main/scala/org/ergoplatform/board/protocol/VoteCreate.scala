package org.ergoplatform.board.protocol

import play.api.libs.json.Json

case class VoteCreate(electionId: String, m: String, signature: SignedData)

object VoteCreate {
  implicit val fmt = Json.format[VoteCreate]
}