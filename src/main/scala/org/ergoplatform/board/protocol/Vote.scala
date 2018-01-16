package org.ergoplatform.board.protocol

import org.ergoplatform.board.models.VoteRecord
import play.api.libs.json.Json

case class Vote(electionId: String, index: Long, m: String, timestamp: Long, p: Option[Proof] = None)

object Vote {
  implicit val fmt = Json.format[Vote]

  def fromRecord(vr: VoteRecord, proof: Option[Proof] = None): Vote = Vote(
    electionId = vr.electionId.toString,
    index = vr.index,
    m = vr.m,
    timestamp = vr.timestamp,
    p = proof
  )
}
