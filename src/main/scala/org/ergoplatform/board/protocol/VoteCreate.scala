package org.ergoplatform.board.protocol

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import org.ergoplatform.board.models.SignedData
import play.api.libs.json.Json

import scala.annotation.meta.field

@ApiModel(description = "command to vote")
case class VoteCreate(
                       @(ApiModelProperty@field)(value = "election id", dataType = "string")
                       electionId: String,
                       @(ApiModelProperty@field)(value = "vote message", dataType = "string")
                       m: String,
                       @(ApiModelProperty@field)(value = "signed data", dataType = "org.ergoplatform.board.models.SignedData")
                       signature: SignedData)

object VoteCreate {
  implicit val fmt = Json.format[VoteCreate]
}