package org.ergoplatform.board.models

import play.api.libs.json.Json

case class VoteRecord(_id: MongoId,
                      electionId: MongoId,
                      groupId: String,
                      sectionId: String,
                      index: Long,
                      hash: String,
                      m: String,
                      signedDataByVoter: SignedData,
                      signedDataByBoard: SignedData,
                      timestamp: Long)

object VoteRecord {
  implicit val fmt = Json.format[VoteRecord]

}
