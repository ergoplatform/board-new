package org.ergoplatform.board.utils

trait RichString {


  implicit class StringToBytes(in: String) {
    def asBytes: Array[Byte] = in.getBytes("UTF-8")

    def asBytes(charset: String): Array[Byte] = in.getBytes(charset)
  }
}
