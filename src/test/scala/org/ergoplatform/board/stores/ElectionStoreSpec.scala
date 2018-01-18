package org.ergoplatform.board.stores

import org.ergoplatform.board.mongo.MongoFixture
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class ElectionStoreSpec extends MongoFixture with Matchers with FutureHelpers with Generators {

  val rec1 = rndElection()
  val rec2 = rndElection()
  val rec3 = rndElection()
  val recs = List(rec1, rec2, rec3)

  it should "check existence correctly" in  { db =>
    val store = new ElectionStoreImpl(db)
    recs.forall{rec => !store.exists(rec._id).await} shouldBe true
    recs.foreach( rec => store.save(rec).await)
    recs.forall{rec => store.exists(rec._id).await} shouldBe true
  }

  it should "find and get correctly" in { db =>
    val store = new ElectionStoreImpl(db)
    recs.foreach( rec => store.save(rec).await)

    val Some(found1) = store.find(rec1._id).await
    found1 shouldBe rec1

    val found2 = store.get(rec2._id).await
    found2 shouldBe rec2


    the[NoSuchElementException] thrownBy store.get(uuid).await
    //check that message has entity name in it in case of not found error
    val ex =  intercept[NoSuchElementException] { store.get(uuid).await }
    ex.getMessage should include (rec1.getClass.getSimpleName)
  }

  it should "extend duration correctly" in {db =>
    val store = new ElectionStoreImpl(db)
    val extendFor = 200L

    store.save(rec1).await
    val updatedRec = store.extend(rec1._id, extendFor).await

    updatedRec._id shouldBe rec1._id
    updatedRec.start shouldBe rec1.start
    updatedRec.end shouldBe (rec1.end + extendFor)
    updatedRec.description shouldBe rec1.description
  }

}
