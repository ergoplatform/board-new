package org.ergoplatform.board.models

import play.api.libs.json.{Json, OFormat}

case class KeysRecord(privateKey: String, publicKey: String)

object KeysRecord {
  implicit val keysFmt: OFormat[KeysRecord] = OFormat.apply(Json.reads[KeysRecord], Json.writes[KeysRecord])
}
