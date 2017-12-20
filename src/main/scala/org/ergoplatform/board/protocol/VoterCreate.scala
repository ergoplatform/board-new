package org.ergoplatform.board.protocol

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.{Json, OFormat}
import Voter._

import scala.annotation.meta.field

@ApiModel(description = "command to create voter")
case class VoterCreate(
                        @(ApiModelProperty @field)(value = electionIdDesc, dataType = "string")
                        electionId: String,
                        @(ApiModelProperty @field)(value = pkDesc, dataType = "string")
                        publicKey: String
                      )

object VoterCreate {
  implicit val fmt: OFormat[VoterCreate] = Json.format[VoterCreate]
}
