package org.ergoplatform.board.services

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.ergoplatform.board.models.VoteRecord
import org.ergoplatform.board.mongo.MongoPerSpec
import org.ergoplatform.board.protocol.{Vote, VoteCreate}
import org.ergoplatform.board.stores.{ElectionStoreImpl, VoteStoreImpl, VoterStoreImpl}
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class VoteServiceSpec extends TestKit(ActorSystem("vote-spec"))
  with FlatSpecLike
  with MongoPerSpec
  with Matchers
  with FutureHelpers
  with Generators {

  override val port = 27020

  lazy val eStore = new ElectionStoreImpl(db)
  lazy val voteStore = new VoteStoreImpl(db)
  lazy val voterStore = new VoterStoreImpl(db)
  lazy val service = new VoteServiceImpl(eStore, voteStore, voterStore)

  it should "vote and look for vote correctly" in {
    val electionId = uuid
    val election = rndElection(electionId).copy(end = System.currentTimeMillis() + 10000000L)
    val keys = SignService.generateRandomKeyPair()
    val voter = rndVoter(electionId).copy(publicKey = keys.publicKey)
    val voterId = voter._id
    val m = "some message"
    val signedData = SignService.sign(m, keys)
    val voteCreate = VoteCreate(electionId, m, signedData)
    val ts = System.currentTimeMillis()
    val vote = VoteRecord(uuid, electionId, voterId, 1, m, ts)

    the[NoSuchElementException] thrownBy service.vote(voteCreate).await
    eStore.save(election).await
    the[NoSuchElementException] thrownBy service.vote(voteCreate).await
    voterStore.save(voter).await

    val result = service.vote(voteCreate).await
    result.p shouldBe defined
    val proof = result.p

    result shouldEqual Vote.fromRecord(vote.copy(timestamp = result.timestamp), proof)
  }

}
