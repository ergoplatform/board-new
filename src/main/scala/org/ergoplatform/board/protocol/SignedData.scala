package org.ergoplatform.board.protocol

import io.swagger.annotations.ApiModelProperty
import play.api.libs.json.Json

import scala.annotation.meta.field

case class SignedData(
                       @(ApiModelProperty @field)(value = "public key", dataType = "string")
                       publicKey: String,
                       @(ApiModelProperty @field)(value = "signed data", dataType = "string")
                       sign: String)

object SignedData {
  implicit val fmt = Json.format[SignedData]

  val empty = SignedData("none", "none")
}
