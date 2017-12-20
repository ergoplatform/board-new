package org.ergoplatform.board.models

import play.api.libs.json.{Json, OFormat}

case class VoterRecord(_id: String, electionId: String, publicKey: String)

object VoterRecord {
  implicit val fmt: OFormat[VoterRecord] = Json.format[VoterRecord]
}
