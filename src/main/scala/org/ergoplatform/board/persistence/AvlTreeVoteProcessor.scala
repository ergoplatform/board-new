package org.ergoplatform.board.persistence

import java.util.UUID

import akka.actor.Props
import akka.event.Logging
import akka.persistence.PersistentActor
import com.google.common.primitives.Longs
import org.ergoplatform.board.models.{AvlVote, KeysRecord, Proof}
import org.ergoplatform.board.persistence.AvlTreeVoteProcessor.ApplyVote
import org.ergoplatform.board.services.HashService
import org.ergoplatform.board.utils.RichBytes
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import scorex.crypto.authds.avltree.batch.{BatchAVLProver, Insert}
import scorex.crypto.authds.{ADKey, ADValue}
import scorex.crypto.hash.{Blake2b256Unsafe, Digest32, ThreadUnsafeHash}

import scala.util.{Failure, Success}

class AvlTreeVoteProcessor(electionId: String, keys: KeysRecord) extends PersistentActor with RichBytes {

  implicit val ec = context.dispatcher
  val logger = Logging(context.system, this)


  type D = Digest32
  type HF = ThreadUnsafeHash[D]
  implicit val hf: HF = new Blake2b256Unsafe
  val keyLength = 8
  val valueLength = 32
  var prover = new BatchAVLProver[D, HF](keyLength, Some(valueLength))(hf)
  var digest = prover.digest

  //this should be used to restore votes from original records, also useful for applying migrations.
  def restoreToDb(voteRecord: AvlVote): Unit = ()

  override def receiveRecover: Receive = {
    case bson: BSONDocument => {
      val json = JsObjectReader.read(bson)
      val v = AvlVote.fmt.reads(json).get
      restoreToDb(v)
      //rework. using get here is just for testing purposes
      val proof = createProof(lastSequenceNr, v.m).get

      if (proof == v.proof) {
        logger.info(s"Vote has correct proof: $proof")
      } else {
        logger.error("SOMETHING BAD HAS HAPPENED")
      }
    }
  }

  override def receiveCommand: Receive = {
    case v: ApplyVote => {
      println(lastSequenceNr)
      val timestamp = System.currentTimeMillis()
      createProof(lastSequenceNr + 1, v.m) match {
        case Some(proof) =>
          val id = UUID.randomUUID().toString
          val vote = AvlVote(id, v.electionId, v.m, proof, timestamp)
          val json = AvlVote.fmt.writes(vote)
          val bson: BSONDocument = JsObjectWriter.write(json)
          persist(bson) {
            _ => sender() ! vote
          }
        case None =>
          logger.error(s"Was unable to generate proof for $v")
      }
    }
    case "print" => sender ! digest.to64String
  }

  override def persistenceId: String = "avl_tree_chain_vote_processor_" + electionId

  //move logic to separate service.
  private def createProof(key: Long, m: String): Option[Proof] = {
    val d1 = digest.to64String
    val k = ADKey @@ Longs.toByteArray(key)
    val v = ADValue @@ HashService.hashify(m)
    val op = Insert(k, v)

    prover.performOneOperation(op) match {
      case Success(_) =>
      val proof = prover.generateProof().to64String
      digest = prover.digest
      val d2 = digest.to64String
      Some(Proof(d1, proof, d2))
      case Failure(t) =>
        logger.error(t.toString)
        None
    }
  }
}

object AvlTreeVoteProcessor {

  def props(electionId: String, keys: KeysRecord): Props = Props(new AvlTreeVoteProcessor(electionId, keys))

  case class ApplyVote(electionId: String, m: String)
}
