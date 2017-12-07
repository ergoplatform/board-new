package org.ergoplatform.board.persistence

import java.util.UUID

import akka.actor.Props
import akka.event.Logging
import akka.persistence.PersistentActor
import org.ergoplatform.board.models.{SignedData, VoteRecord}
import org.ergoplatform.board.protocol.VoteCreate
import org.ergoplatform.board.services.HashService
import play.api.libs.json.Json
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

class HashChainVoteProcessor(electionId: String) extends PersistentActor{

  implicit val ec = context.dispatcher
  val logger = Logging(context.system, this)
  var prevHash = ""

  override def persistenceId: String = "hash_chain_vote_processor_" + electionId

  override def receiveRecover: Receive = {
    case bson: BSONDocument => {
      val json = JsObjectReader.read(bson)
      val voteRecord = VoteRecord.fmt.reads(json).get
      val cmd = VoteCreate(voteRecord.groupId, voteRecord.sectionId, voteRecord.m, voteRecord.signedDataByVoter)
      val hash = HashService.hash(Json.stringify(Json.toJson(cmd)), Some(prevHash).filterNot(_.isEmpty))
      if (hash == voteRecord.hash) {
        logger.info(s"HASH CORRECT FOR VOTE ID ${voteRecord._id} and hash is $hash")
        prevHash = hash
      } else {
        logger.error("SOMETHING BAD HAS HAPPENED")
      }
    }
  }

  override def receiveCommand: Receive = {
    case cmd: VoteCreate => {
      val hash = HashService.hash(Json.stringify(Json.toJson(cmd)), Some(prevHash).filterNot(_.isEmpty))
      val timestamp = System.currentTimeMillis()
      val vote = VoteRecord(
        UUID.randomUUID().toString,
        electionId,
        cmd.groupId,
        cmd.sectionId,
        lastSequenceNr,
        hash,
        cmd.m,
        cmd.signature,
        SignedData("none", "none"),
        timestamp)
      prevHash = hash
      val json = VoteRecord.fmt.writes(vote)
      val bson: BSONDocument = JsObjectWriter.write(json)
      persist(bson){_ => logger.error(s"persisted\n${Json.prettyPrint(json)}")}
    }
    case "print" => logger.info(s"Current hash is $prevHash and index is $lastSequenceNr" )
  }
}

object HashChainVoteProcessor {
  def props(electionId: String): Props = Props(new HashChainVoteProcessor(electionId))
}



