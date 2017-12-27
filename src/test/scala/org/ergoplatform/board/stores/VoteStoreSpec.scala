package org.ergoplatform.board.stores

import java.util.UUID

import org.ergoplatform.board.FutureHelpers
import org.ergoplatform.board.models.SignedData
import org.ergoplatform.board.mongo.MongoFixture
import org.ergoplatform.board.protocol.VoteCreate
import org.ergoplatform.board.services.SignService
import org.scalatest.Matchers
import play.api.libs.json.Json

import scala.util.Random

import scala.concurrent.ExecutionContext.Implicits.global

class VoteStoreSpec extends MongoFixture with Matchers with FutureHelpers {

  val electionId = UUID.randomUUID().toString
  val electionKeys = SignService.generateRandomKeyPair()
  val groupId = "GROUP1"
  val sectionId = "SECTION1"
  val keys = SignService.generateRandomKeyPair()
  val offset = 0
  val limit = 10

  def signByBoard(cmd: VoteCreate): SignedData = SignService.sign(Json.stringify(Json.toJson(cmd)), keys)

  def generateSignedData(m: String): SignedData = {
    val keys = SignService.generateRandomKeyPair()
    SignService.sign(m, keys)
  }

  def generateRec: VoteCreate = {
    val m = Random.alphanumeric(5).toString
    val voterSign = generateSignedData(m)
    VoteCreate(electionId, m, voterSign)
  }

  val cmd1 = generateRec
  val cmd2 = generateRec
  val cmd3 = generateRec
  val cmds = List(cmd1, cmd2, cmd3)

  it should "create and find correctly" in { db =>
    val store = new VoteStoreImpl(db)

    val boardSign1 = signByBoard(cmd1)
    val res1 = store.create(electionId, cmd1, boardSign1).await

    res1.electionId shouldBe electionId
    res1.m shouldBe cmd1.m

    store.getAllByElectionId(electionId, offset, limit).await should contain only res1
    store.countByElectionId(electionId).await shouldBe 1

    val boardSign2 = signByBoard(cmd2)
    val res2 = store.create(electionId, cmd1, boardSign2).await
    val boardSign3 = signByBoard(cmd3)
    val res3 = store.create(electionId, cmd1, boardSign3).await

    store.getAllByElectionId(electionId, offset, limit).await should contain allOf (res1, res2, res3)
    store.countByElectionId(electionId).await shouldBe 3

    store.getAllByElectionId(electionId, offset + 1, limit).await should contain allOf (res2, res3)
    store.getAllByElectionId(electionId, offset + 2, limit).await should contain only res3
  }

}
