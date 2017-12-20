package org.ergoplatform.board.protocol

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import org.ergoplatform.board.models.VoterRecord
import org.ergoplatform.board.protocol.Voter._
import play.api.libs.json.Json

import scala.annotation.meta.field

@ApiModel(description = "voter model")
case class Voter(
                  @(ApiModelProperty @field)(value = idDesc, dataType = "string")
                  id: String,
                  @(ApiModelProperty @field)(value = electionIdDesc, dataType = "string")
                  electionId: String,
                  @(ApiModelProperty @field)(value = pkDesc, dataType = "string")
                  publicKey: String
                )

object Voter {

  implicit val fmt = Json.format[Voter]

  def fromVoterRecord(rec: VoterRecord): Voter = Voter(
    id = rec._id,
    electionId = rec.electionId,
    publicKey = rec.publicKey
  )

  final val idDesc = "unique id for voter model, string representation of UUID"
  final val electionIdDesc = "election id for which this voter has been created for"
  final val pkDesc = "public key of voter to verify signed data"
}