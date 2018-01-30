package org.ergoplatform.board.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.{ElectionProcessorProviderHelper, FutureHelpers}
import org.ergoplatform.board.mongo.MongoPerSpec
import org.ergoplatform.board.protocol._
import org.ergoplatform.board.services.ElectionServiceImpl
import org.ergoplatform.board.stores.{ElectionStoreImpl, VoteStoreImpl}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

class ElectionResourcesSpec extends FlatSpec
  with BeforeAndAfterAll
  with Matchers
  with ScalatestRouteTest
  with PlayJsonSupport
  with FutureHelpers
  with MongoPerSpec with ElectionProcessorProviderHelper {

  override val port = 27020

  import Election._
  import ElectionCreate._
  import akka.http.scaladsl.testkit.RouteTestTimeout
  import akka.testkit.TestDuration
  import org.ergoplatform.board.ApiErrorHandler._

  import scala.concurrent.duration._

  implicit val timeout = RouteTestTimeout(10.seconds dilated)

  lazy val eStore = new ElectionStoreImpl(db)
  lazy val vStore = new VoteStoreImpl(db)
  lazy val service = new ElectionServiceImpl(eStore, electionProcessorProvider)
  lazy val handler = new ElectionResources(service)
  lazy val route  = Route.seal(handler.routes)



  it should "create election correctly" in {
    val cmd = ElectionCreate(100L, 200L, Some("test"))

    Post("/elections",cmd)  ~> route ~> check {
      status shouldBe StatusCodes.Created
      val data = entityAs[Election]
      data.start shouldEqual 100L
      data.end shouldEqual 200L
      data.description shouldBe Some("test")
    }

    val election = eStore.findByQuery(JsObject.empty).await.head

    Get(s"/elections/${election._id}/currentHash") ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[JsObject]
      (data \ "result").as[String] should not be empty
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
      val data = entityAs[Election]
      data.start shouldEqual election.start
      data.end shouldEqual election.end
      data.description shouldBe election.description
      data.id shouldEqual election.id
    }

    val prolongValue = 200L

    Put(s"/elections/${election.id}", ElectionProlong(prolongValue)) ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[Election]
      data.end shouldEqual election.end + prolongValue
    }

    Get(s"/elections/${election.id}/exist") ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[JsObject]
      (data \ "result").as[Boolean] shouldBe true
    }

    db[JSONCollection]("elections").remove(Json.obj("_id" -> election.id)).await

    Get(s"/elections/${election.id}") ~> route ~> check {
      status shouldBe StatusCodes.NotFound
    }

    Put(s"/elections/${election.id}", ElectionProlong(prolongValue)) ~> route ~> check {
      status shouldBe StatusCodes.NotFound
    }

    Get(s"/elections/${election.id}/exist") ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[JsObject]
      (data \ "result").as[Boolean] shouldBe false
    }
  }

}
