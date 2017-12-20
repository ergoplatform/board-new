package org.ergoplatform.board

import java.util.UUID

import org.ergoplatform.board.models.{ElectionRecord, VoterRecord}
import org.ergoplatform.board.services.SignService

import scala.util.Random

trait Generators {

  def rndElection(electionId: String = uuid): ElectionRecord = {
    val id = electionId
    val start = Random.nextInt(Int.MaxValue)
    val end = start + Random.nextInt(Int.MaxValue)
    val keys = SignService.generateRandomKeyPair()
    val desc = Some(Random.alphanumeric.take(20).toString())
    ElectionRecord(id, start, end, keys, desc)
  }

  def rndVoter(electionId: String = uuid): VoterRecord = VoterRecord(
    _id = uuid,
    electionId = electionId,
    publicKey = SignService.generateRandomKeyPair().publicKey
  )

  def uuid: String = UUID.randomUUID().toString

}
