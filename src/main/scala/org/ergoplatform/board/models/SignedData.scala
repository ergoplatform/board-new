package org.ergoplatform.board.models

import play.api.libs.json.Json

case class SignedData(publicKey: String, sign: String)

object SignedData {
  implicit val fmt = Json.format[SignedData]

  val empty = SignedData("none", "none")
}
