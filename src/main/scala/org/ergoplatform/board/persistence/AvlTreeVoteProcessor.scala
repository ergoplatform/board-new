package org.ergoplatform.board.persistence

import java.util.UUID

import akka.actor.Props
import akka.event.Logging
import akka.persistence.PersistentActor
import com.google.common.primitives.Longs
import org.ergoplatform.board.models.VoteRecord
import org.ergoplatform.board.persistence.AvlTreeVoteProcessor._
import org.ergoplatform.board.protocol.Proof
import org.ergoplatform.board.utils.{RichBytes, RichString}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import scorex.crypto.authds.avltree.batch.{BatchAVLProver, Insert}
import scorex.crypto.authds.{ADKey, ADValue}
import scorex.crypto.hash.{Blake2b256Unsafe, Digest32, ThreadUnsafeHash}

import scala.util.{Failure, Success, Try}

class AvlTreeVoteProcessor(electionId: String) extends PersistentActor with RichBytes with RichString {

  implicit val ec = context.dispatcher
  val logger = Logging(context.system, this)

  type D = Digest32
  type HF = ThreadUnsafeHash[D]
  implicit val hf: HF = new Blake2b256Unsafe
  val keyLength = 8
  var prover = new BatchAVLProver[D, HF](keyLength, None)(hf)
  var digest = prover.digest

  //this should be used to restore votes from original records, also useful for applying migrations.
  def restoreToDb(voteRecord: VoteRecord): Unit = ()

  override def receiveRecover: Receive = {
    case bson: BSONDocument => {
      val json = JsObjectReader.read(bson)
      val v = VoteRecord.fmt.reads(json).get
      //No need right now.
      //restoreToDb(v)
      createProof(v.index, v.m) match {
        case Success(_) => logger.debug(s"Vote ($v) has been restored.")
        case Failure(t) => logger.error(s"Unable to restore vote $v to tree, reason $t.")
      }
    }
  }

  override def receiveCommand: Receive = {
    case v: VoteApplication => {
      val timestamp = System.currentTimeMillis()
      val index = lastSequenceNr + 1
      createProof(index, v.m) match {
        case Success(proof) =>
          val id = UUID.randomUUID().toString
          val vote = VoteRecord(id, v.electionId, v.voterId, index, v.m, timestamp)
          val json = VoteRecord.fmt.writes(vote)
          val bson: BSONDocument = JsObjectWriter.write(json)
          persist(bson) {
            _ => sender() ! VoteSuccess(vote, proof)
          }
        case Failure(t) =>
          logger.error(s"Was unable to generate proof for $v")
          sender() ! VoteFailure(t)
      }
    }
    case g: GetVoteFromTree =>
      val index = g.index
      val k = ADKey @@ Longs.toByteArray(index)
      val result = prover.unauthenticatedLookup(k).map { v => new String(v, "UTF-8") }
      sender() ! VoteOption(result)

    case GetCurrentDigest =>
      sender() ! digest.to64String
  }

  override def persistenceId: String = "avl_tree_chain_vote_processor_" + electionId

  private def createProof(key: Long, m: String): Try[Proof] = {
    val d1 = digest.to64String
    val k = ADKey @@ Longs.toByteArray(key)
    val v = ADValue @@ m.asBytes("UTF-8")
    //cant insert if key is equals to 0, need to clarify is it bug or not
    val op = Insert(k, v)

    prover.performOneOperation(op).map { _ =>
      val proof = prover.generateProof().to64String
      digest = prover.digest
      val d2 = digest.to64String
      Proof(d1, proof, d2)
    }
  }
}

object AvlTreeVoteProcessor {

  def props(electionId: String): Props = Props(new AvlTreeVoteProcessor(electionId))

  case class VoteApplication(electionId: String, voterId: String, m: String)

  sealed trait VoteResult

  case class VoteSuccess(vote: VoteRecord, proof: Proof) extends VoteResult

  case class VoteFailure(reason: Throwable) extends VoteResult

  case class GetVoteFromTree(index: Long)

  case class VoteOption(voteMessage: Option[String])

  case object GetCurrentDigest
}
