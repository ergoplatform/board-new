package org.ergoplatform.board.utils

import scorex.crypto.encode.Base64


trait RichBytes {

  implicit class StringTo64BaseBytes(in: String) {
    def to64Bytes: Array[Byte] = Base64.decode(in)
  }

  implicit class Bytes64ToString(in: Array[Byte]) {
    def to64String: String = Base64.encode(in)
  }
}
