package org.ergoplatform.board

import io.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field


/**
  * Only dummy classes for swagger modeling.
  */
package object protocol {

  @ApiModel(description = "boolean response")
  case class BooleanResultResponse(
                                    @(ApiModelProperty@field)(value = "result of a performed query", dataType = "boolean")
                                    result: Boolean
                                  )

}