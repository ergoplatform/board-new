package org.ergoplatform.board.stores

import java.util.UUID

import org.ergoplatform.board.FutureHelpers
import org.ergoplatform.board.models.ElectionRecord
import org.ergoplatform.board.mongo.MongoFixture
import org.ergoplatform.board.services.SignService
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class ElectionStoreSpec extends MongoFixture with Matchers with FutureHelpers {

  def generateRec: ElectionRecord = {
    val id = UUID.randomUUID().toString
    val start = Random.nextInt(Int.MaxValue)
    val end = start + Random.nextInt(Int.MaxValue)
    val keys = SignService.generateRandomKeyPair()
    val desc = Some(Random.alphanumeric.take(20).toString())
    ElectionRecord(id, start, end, keys, desc)
  }

  val rec1 = generateRec
  val rec2 = generateRec
  val rec3 = generateRec
  val recs = List(rec1, rec2, rec3)

  it should "check existence correctly" in  { db =>
    val store = new ElectionStoreImpl(db)
    recs.forall{rec => !store.exist(rec._id).await} shouldBe true
    recs.foreach( rec => store.create(rec).await)
    recs.forall{rec => store.exist(rec._id).await} shouldBe true
  }

  it should "find and get correctly" in { db =>
    val store = new ElectionStoreImpl(db)
    recs.foreach( rec => store.create(rec).await)

    val Some(found1) = store.find(rec1._id).await
    found1 shouldBe rec1

    val found2 = store.get(rec2._id).await
    found2 shouldBe rec2

    the[NoSuchElementException] thrownBy store.get(UUID.randomUUID().toString).await
  }

  it should "extend duration correctly" in {db =>
    val store = new ElectionStoreImpl(db)
    val extendFor = 200L

    store.create(rec1).await
    val updatedRec = store.extend(rec1._id, extendFor).await

    updatedRec._id shouldBe rec1._id
    updatedRec.start shouldBe rec1.start
    updatedRec.end shouldBe (rec1.end + extendFor)
    updatedRec.keys shouldBe rec1.keys
    updatedRec.description shouldBe rec1.description
  }

}
