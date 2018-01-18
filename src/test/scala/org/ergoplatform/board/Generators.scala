package org.ergoplatform.board

import java.util.UUID

import org.ergoplatform.board.models.{ElectionRecord, VoteRecord, VoterRecord}
import org.ergoplatform.board.services.SignService

import scala.util.Random

trait Generators {

  def rndElection(electionId: String = uuid): ElectionRecord = {
    val id = electionId
    val start = System.currentTimeMillis()
    val end = start + Random.nextInt(Int.MaxValue)
    val desc = Some(Random.alphanumeric.take(20).toString())
    ElectionRecord(id, start, end, desc)
  }

  def rndVoter(electionId: String = uuid,
               publicKey: String = SignService.generateRandomKeyPair().publicKey): VoterRecord = VoterRecord(
    _id = uuid,
    electionId = electionId,
    publicKey = publicKey
  )

  def rndVote(electionId: String = uuid,
              voterId: String = uuid,
              publicKey: String = SignService.generateRandomKeyPair().publicKey): VoteRecord = {
    val id = uuid
    val index = 0L
    val timestamp = 0L
    val m = Random.alphanumeric.take(20).toString()
    VoteRecord(id, electionId, voterId, index, m, timestamp)
  }

  def uuid: String = UUID.randomUUID().toString

}
