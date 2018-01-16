package org.ergoplatform.board.stores

import java.util.UUID

import org.ergoplatform.board.models.VoteRecord
import org.ergoplatform.board.mongo.MongoFixture
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class VoteStoreSpec extends MongoFixture with Matchers with FutureHelpers with Generators {

  val electionId: String = UUID.randomUUID().toString
  val offset: Int = 0
  val limit: Int = 10


  val vote1: VoteRecord = rndVote(electionId = electionId)
  val vote2: VoteRecord = rndVote(electionId = electionId)
  val vote3: VoteRecord = rndVote(electionId = electionId)
  val votes = List(vote1, vote2, vote3)

  it should "create and find correctly" in { db =>
    val store = new VoteStoreImpl(db)

    val res1 = store.save(vote1).await

    res1.electionId shouldBe electionId
    res1.m shouldBe vote1.m

    store.getAllByElectionId(electionId, offset, limit).await should contain only res1
    store.countByElectionId(electionId).await shouldBe 1

    val res2 = store.save(vote2).await
    val res3 = store.save(vote3).await

    store.getAllByElectionId(electionId, offset, limit).await should contain allOf (res1, res2, res3)
    store.countByElectionId(electionId).await shouldBe 3

    store.getAllByElectionId(electionId, offset + 1, limit).await should contain allOf (res2, res3)
    store.getAllByElectionId(electionId, offset + 2, limit).await should contain only res3
  }

}
