package org.ergoplatform.board.services

import org.ergoplatform.board.mongo.MongoFixture
import org.ergoplatform.board.protocol.VoterCreate
import org.ergoplatform.board.stores.{ElectionStoreImpl, VoterStoreImpl}
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class VoterServiceSpec extends MongoFixture with Matchers with FutureHelpers with Generators {

  it should "create and find voter" in { db =>
    val vStore = new VoterStoreImpl(db)
    val eStore = new ElectionStoreImpl(db)
    val service = new VoterServiceImpl(eStore, vStore)


    val electionId = uuid
    val keys = SignService.generateRandomKeyPair()
    val cmd = VoterCreate(electionId, keys.publicKey)

    the[NoSuchElementException] thrownBy service.register(cmd).await
    val ex = intercept[NoSuchElementException](service.register(cmd).await)
    ex.getMessage should include("Can't find election")

    val election = rndElection(electionId)
    eStore.save(election).await

    val created = service.register(cmd).await

    created.electionId shouldBe electionId
    created.publicKey shouldBe keys.publicKey

    service.get(created.id).await shouldBe created
    the[NoSuchElementException] thrownBy service.get(uuid).await
  }
}
