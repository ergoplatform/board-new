package org.ergoplatform.board.protocol

import play.api.libs.json.{Json, OFormat}

case class Keys(privateKey: String, publicKey: String)

object Keys {
  implicit val keysFmt: OFormat[Keys] = OFormat.apply(Json.reads[Keys], Json.writes[Keys])
}
