package org.ergoplatform.board.protocol

import org.ergoplatform.board.models.VoterRecord
import play.api.libs.json.Json

case class Voter(id: String, electionId: String, publicKey: String)

object Voter {
  implicit val fmt = Json.format[Voter]

  def fromVoterRecord(rec: VoterRecord): Voter = Voter(
    id = rec._id,
    electionId = rec.electionId,
    publicKey = rec.publicKey
  )
}