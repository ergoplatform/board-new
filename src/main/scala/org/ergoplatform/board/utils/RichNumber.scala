package org.ergoplatform.board.utils

import org.ergoplatform.board.protocol.ResultResponse

trait RichNumber {

  implicit class NumberResponse[T <: Numeric[T]](n: T) {
    def toResponse: ResultResponse[T] = ResultResponse(n)
  }

}
