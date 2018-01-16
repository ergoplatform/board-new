package org.ergoplatform.board.services

import org.ergoplatform.board.models._
import org.ergoplatform.board.protocol.{Keys, SignedData}
import org.ergoplatform.board.utils.{RichBytes, RichString}

import scala.concurrent.Future

object SignService extends RichBytes with RichString {

  import scorex.crypto.signatures._

  def validate(pk: String, message: String, sign: String): Boolean = {
    val pkBytes = PublicKey @@ pk.to64Bytes
    val messageBytes = message.asBytes
    val signatureBytes = Signature @@ sign.to64Bytes
    Curve25519.verify(signatureBytes, messageBytes, pkBytes)
  }

  def validate(signedData: SignedData, m: String): Boolean = validate(signedData.publicKey, m, signedData.sign)

  def validateFuture(signedData: SignedData, m: String): Future[Unit] = if (validate(signedData, m)) {
    Future.successful(())
  } else {
    Future.failed(new IllegalArgumentException("Cannot verify signed data"))
  }

  def sign(message: String, keys: Keys): SignedData = {
    val privKeyBytes = PrivateKey @@ keys.privateKey.to64Bytes
    val mBytes = message.asBytes
    val signedBytes = Curve25519.sign(privKeyBytes, mBytes)
    SignedData(keys.publicKey, signedBytes.to64String)
  }

  def generateRandomKeyPair(): Keys = {
    val seed = scorex.utils.Random.randomBytes()
    val (priv, pub) = Curve25519.createKeyPair(seed)
    Keys(privateKey = priv.to64String, publicKey = pub.to64String)
  }

}
