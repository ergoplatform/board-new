package org.ergoplatform.board.models

import play.api.libs.json.{Json, OFormat}

case class VoteRecord(_id: String,
                      electionId: String,
                      voterId: String,
                      index: Long,
                      m: String,
                      timestamp: Long)

object VoteRecord {
  implicit val fmt: OFormat[VoteRecord] = Json.format[VoteRecord]
}
