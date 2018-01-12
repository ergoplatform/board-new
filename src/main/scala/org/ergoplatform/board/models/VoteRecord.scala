package org.ergoplatform.board.models

import play.api.libs.json.{Json, OFormat}

case class VoteRecord(_id: String,
                      electionId: String,
                      index: Long,
                      proof: String,
                      m: String,
                      signedDataByVoter: SignedData,
                      signedDataByBoard: SignedData,
                      timestamp: Long)

object VoteRecord {
  implicit val fmt: OFormat[VoteRecord] = Json.format[VoteRecord]
}
