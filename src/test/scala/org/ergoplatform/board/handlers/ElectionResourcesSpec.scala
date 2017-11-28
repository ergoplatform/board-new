package org.ergoplatform.board.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.models.{MongoId, SignedData}
import org.ergoplatform.board.protocol._
import org.ergoplatform.board.services.{ElectionServiceImpl, HashService, SignService}
import org.ergoplatform.board.{FutureHelpers, InMemoryElectionStore, InMemoryVoteStore}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, Json}

class ElectionResourcesSpec extends FlatSpec
  with Matchers
  with ScalatestRouteTest
  with PlayJsonSupport
  with FutureHelpers {

  import ElectionCreate._
  import ElectionView._
  import VoteCreate._
  import VoteView._
  import org.ergoplatform.board.ApiErrorHandler._

  import scala.concurrent.duration._
  import akka.http.scaladsl.testkit.RouteTestTimeout
  import akka.testkit.TestDuration

  implicit val timeout = RouteTestTimeout(10.seconds dilated)

  val eStore = new InMemoryElectionStore
  val vStore = new InMemoryVoteStore
  val service = new ElectionServiceImpl(eStore, vStore)
  val handler = new ElectionResources(service)
  val route  = Route.seal(handler.routes)


  it should "create election correctly" in {
    val cmd = ElectionCreate(100L, 200L, Some("test"))

    Post("/elections",cmd)  ~> route ~> check {
      status shouldBe StatusCodes.Created
      val data = entityAs[ElectionView]
      data.start shouldEqual 100L
      data.end shouldEqual 200L
      data.description shouldBe Some("test")
    }

    val corruptedCmd = Json.obj("start" -> "100", "end" -> 200L)
    Post("/elections", corruptedCmd)  ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }

  it should "find and update and check for existence election by id" in {
    val cmd = ElectionCreate(100L, 200L, Some("test"))
    val election = service.create(cmd).await

    Get(s"/elections/${election.id}") ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[ElectionView]
      data.start shouldEqual election.start
      data.end shouldEqual election.end
      data.description shouldBe election.description
      data.publicKey shouldEqual election.publicKey
      data.id shouldEqual election.id
    }

    val prolongValue = 200L

    Post(s"/elections/${election.id}", ElectionProlong(prolongValue)) ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[ElectionView]
      data.end shouldEqual election.end + prolongValue
    }

    Get(s"/elections/${election.id}/exist") ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[JsObject]
      (data \ "result").as[Boolean] shouldBe true
    }

    eStore.data.remove(MongoId(election.id))

    Get(s"/elections/${election.id}") ~> route ~> check {
      status shouldBe StatusCodes.NotFound
    }

    Post(s"/elections/${election.id}", ElectionProlong(prolongValue)) ~> route ~> check {
      status shouldBe StatusCodes.NotFound
    }

    Get(s"/elections/${election.id}/exist") ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[JsObject]
      (data \ "result").as[Boolean] shouldBe false
    }
  }

  it should "work correctly with basic flow" in {
    val cmd = ElectionCreate(100L, 200L, Some("test"))

    Post("/elections", cmd) ~> route ~> check {
      status shouldBe StatusCodes.Created
      val data = entityAs[ElectionView]
      data.start shouldEqual 100L
      data.end shouldEqual 200L
      data.description shouldBe Some("test")
    }

    val election = eStore.data.head._2
    val electionId = election.id

    val keys1 = SignService.generateRandomKeyPair()
    val keys2 = SignService.generateRandomKeyPair()

    val gId = "1"
    val sId = "1"

    val m1 = "vote for 1"
    val m2 = "vote for 2"

    val m1Signed = SignService.sign(m1, keys1)
    val m2Signed = SignService.sign(m2, keys2)

    val broken = SignedData(keys2.publicKey, m1Signed.sign)
    val fraudCmd = VoteCreate(gId, sId, m1, broken)

    val cmd1 = VoteCreate(gId, sId, m1, m1Signed)
    val cmd2 = VoteCreate(gId, sId, m2, m2Signed)

    Post(s"/elections/${electionId.id}/votes", cmd1) ~> route ~> check {
      status shouldBe StatusCodes.Created
      val data = entityAs[VoteView]
      data.electionId shouldEqual electionId
      data.m shouldEqual m1
    }

    Post(s"/elections/${electionId.id}/votes", cmd2) ~> route ~> check {
      status shouldBe StatusCodes.Created
      val data = entityAs[VoteView]
      data.electionId shouldEqual electionId
      data.m shouldEqual m2
    }

    Post(s"/elections/${electionId.id}/votes", fraudCmd) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }

    val hash1 = HashService.hash(Json.stringify(Json.toJson(cmd1)))
    val hash2 = HashService.hash(Json.stringify(Json.toJson(cmd2)), Some(hash1))

    Get(s"/elections/${electionId.id}/votes") ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[List[VoteView]]

      //checking hash chain
      data should have length 2
      val sorted = data.sortBy(_.index)
      sorted(0).hash shouldEqual hash1
      sorted(1).hash shouldEqual hash2
    }
  }
}
