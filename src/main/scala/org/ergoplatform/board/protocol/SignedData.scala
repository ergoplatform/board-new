package org.ergoplatform.board.protocol

import play.api.libs.json.Json

case class SignedData(publicKey: String, sign: String)

object SignedData {
  implicit val fmt = Json.format[SignedData]

  val empty = SignedData("none", "none")
}
