package org.ergoplatform.board.models

import play.api.libs.json.{Json, OFormat}

case class AvlVote(_id: String, electionId: String, m: String, proof: Proof, timestamp: Long)

object AvlVote {
  implicit val fmt: OFormat[AvlVote] = Json.format[AvlVote]
}
