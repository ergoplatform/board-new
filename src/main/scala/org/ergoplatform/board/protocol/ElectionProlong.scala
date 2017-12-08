package org.ergoplatform.board.protocol

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.Json

import scala.annotation.meta.field

@ApiModel(description = "command to extend election duration")
case class ElectionProlong(
                            @(ApiModelProperty @field)(
                              value = "number of millliseconds for which election should be prolonged",
                              dataType = "long"
                            )
                            prolongDuration: Long
                          )

object ElectionProlong {
  implicit val fmt = Json.format[ElectionProlong]
}
