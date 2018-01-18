package org.ergoplatform.board.utils

import org.ergoplatform.board.protocol.ResultResponse

trait RichString {


  implicit class StringToBytes(in: String) {
    def asBytes: Array[Byte] = in.getBytes("UTF-8")

    def asBytes(charset: String): Array[Byte] = in.getBytes(charset)
  }

  implicit class StringResponse(b: String) {
    def toResponse: ResultResponse[String] = ResultResponse(b)
  }
}
