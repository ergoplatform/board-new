package org.ergoplatform.board.protocol

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import org.ergoplatform.board.models.ElectionRecord
import play.api.libs.json.Json
import Election._

import scala.annotation.meta.field

@ApiModel(description = "election model")
case class Election(
                     @(ApiModelProperty @field)(value = idDesc, dataType = "string")
                     id: String,
                     @(ApiModelProperty @field)(value = startDesc, dataType = "long")
                     start: Long,
                     @(ApiModelProperty @field)(value = endDesc, dataType = "long")
                     end: Long,
                     @(ApiModelProperty @field)(value = pkDesc, dataType = "string")
                     publicKey: String,
                     @(ApiModelProperty @field)(value = desc, dataType = "string")
                     description: Option[String])

object Election {
  implicit val fmt = Json.format[Election]

  def fromRecord(e: ElectionRecord): Election = Election(
    id = e._id.toString,
    start = e.start,
    end =  e.end,
    publicKey = e.keys.publicKey,
    description = e.description
  )

  final val idDesc = "unique id for election model, string representation of UUID"
  final val startDesc = "timestamp of date when election will start"
  final val endDesc = "timestamp of date when election will close"
  final val pkDesc = "string representation of pk that will be used for signing votes"
  final val desc = "short description"
}
