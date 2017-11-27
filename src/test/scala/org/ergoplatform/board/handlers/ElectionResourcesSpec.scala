package org.ergoplatform.board.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.models.MongoId
import org.ergoplatform.board.{FakeElectionService, FutureHelpers}
import org.ergoplatform.board.protocol.{ElectionCreate, ElectionProlong, ElectionView}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, Json}

class ElectionResourcesSpec extends FlatSpec
  with Matchers
  with ScalatestRouteTest
  with PlayJsonSupport
  with FutureHelpers {

  import org.ergoplatform.board.ApiErrorHandler._
  val service = new FakeElectionService
  val handler = new ElectionResources(service)
  val route  = Route.seal(handler.routes)

  import ElectionCreate._
  import ElectionView._

  it should "create election correclty" in {
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

    service.data.remove(MongoId(election.id))

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

}
