package org.ergoplatform.board.models

import play.api.libs.json.{Json, OFormat}

case class Proof(digest: String, proof: String, postDigest: String)

object Proof {
  implicit val fmt: OFormat[Proof] = Json.format[Proof]
}