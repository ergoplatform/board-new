package org.ergoplatform.board.protocol

import play.api.libs.json.{Json, OFormat}

case class VoterCreate(electionId: String, publicKey: String)

object VoterCreate {
  implicit val fmt: OFormat[VoterCreate] = Json.format[VoterCreate]
}
