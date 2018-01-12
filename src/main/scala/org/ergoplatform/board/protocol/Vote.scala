package org.ergoplatform.board.protocol

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import org.ergoplatform.board.models.{SignedData, VoteRecord}
import play.api.libs.json.Json

import scala.annotation.meta.field

@ApiModel(description = "vote model")
case class Vote(
                 @(ApiModelProperty@field)(value = "election id", dataType = "string")
                 electionId: String,
                 @(ApiModelProperty@field)(value = "index of vote", dataType = "long")
                 index: Long,
                 @(ApiModelProperty@field)(value = "current hash in hashchain:", dataType = "string")
                 hash: String,
                 @(ApiModelProperty@field)(value = "vote message", dataType = "string")
                 m: String,
                 @(ApiModelProperty@field)(value = "signed by voter data", dataType = "org.ergoplatform.board.models.SignedData")
                 signedDataByVoter: SignedData,
                 @(ApiModelProperty@field)(value = "signed by board data", dataType = "org.ergoplatform.board.models.SignedData")
                 signedDataByBoard: SignedData,
                 @(ApiModelProperty@field)(value = "timestamp", dataType = "long")
                 timestamp: Long)

object Vote {
  implicit val fmt = Json.format[Vote]

  def fromRecord(vr: VoteRecord): Vote = Vote(
    electionId = vr.electionId.toString,
    index = vr.index,
    m = vr.m,
    hash = vr.proof,
    signedDataByVoter = vr.signedDataByVoter,
    signedDataByBoard = vr.signedDataByBoard,
    timestamp = vr.timestamp
  )
}
