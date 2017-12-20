package org.ergoplatform.board.stores

import org.ergoplatform.board.mongo.MongoFixture
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class VoterStoreSpec extends MongoFixture with Matchers with FutureHelpers with Generators {

  val electionId1 = uuid
  val electionId2 = uuid

  val rec1 = rndVoter(electionId1)
  val rec2 = rndVoter(electionId1)
  val rec3 = rndVoter(electionId2)

  val recs = List(rec1, rec2, rec3)

  it should "create records and check that their are exist" in { db =>
    val store = new VoterStoreImpl(db)

    recs.forall(r => !store.exists(r.publicKey).await)

    the[NoSuchElementException] thrownBy store.get(rec1._id).await

    val saved1 = store.create(rec1).await
    val saved2 = store.create(rec2).await
    val saved3 = store.create(rec3).await

    store.get(rec1._id).await shouldBe saved1

    saved1 shouldBe rec1
    saved2 shouldBe rec2
    saved3 shouldBe rec3

    recs.forall(r => store.exists(r.publicKey).await)
  }

  it should "find records by public keys" in { db =>
    val store = new VoterStoreImpl(db)

    recs.forall(r => !store.exists(r.publicKey).await)

    store.create(rec1).await
    store.create(rec2).await
    store.create(rec3).await

    recs.forall(r => store.findByKey(r.publicKey).await.nonEmpty)

    store.findByKeyAndElectionId(rec1.publicKey, electionId2).await shouldBe empty
    store.findByKeyAndElectionId(rec2.publicKey, electionId1).await shouldBe defined
    store.findByKeyAndElectionId(rec3.publicKey, electionId1).await shouldBe empty
  }
}
