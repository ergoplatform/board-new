package org.ergoplatform.board.services

import org.ergoplatform.board.utils.{RichBytes, RichString}
import scorex.crypto.hash.Blake2b256

object HashService extends RichBytes with RichString {

  def hash(m: String, oldHash: Option[String] = None): String = {
    val messageBytes: Array[Byte] = m.asBytes
    val oldHashBytes: Array[Byte] = oldHash.map(_.asBytes).getOrElse(Array[Byte]())
    val toHash: Array[Byte] = Array.concat(messageBytes, oldHashBytes)
    val hashBytes = Blake2b256.hash(toHash)
    hashBytes.to64String
  }

  def hashify(m: String): Array[Byte] = Blake2b256.hash(m.asBytes)

}
