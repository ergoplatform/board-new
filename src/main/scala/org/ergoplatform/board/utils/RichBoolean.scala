package org.ergoplatform.board.utils

import org.ergoplatform.board.protocol.ResultResponse

trait RichBoolean {

  implicit class BooleanResponse(b: Boolean) {
    def toResponse: ResultResponse[Boolean] = ResultResponse(b)
  }

}
