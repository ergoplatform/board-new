package org.ergoplatform.board.protocol

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.Json
import Election._

import scala.annotation.meta.field

@ApiModel(description = "command to create election")
case class ElectionCreate(
                           @(ApiModelProperty @field)(value = startDesc, dataType = "long")
                           start: Long,
                           @(ApiModelProperty @field)(value = endDesc, dataType = "long")
                           end: Long,
                           @(ApiModelProperty @field)(value = desc, dataType = "string", required = false)
                           description: Option[String])

object ElectionCreate {
  implicit val fmt = Json.format[ElectionCreate]
}
