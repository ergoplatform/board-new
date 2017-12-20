package org.ergoplatform.board.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.ergoplatform.board.mongo.MongoPerSpec
import org.ergoplatform.board.protocol.{Voter, VoterCreate}
import org.ergoplatform.board.services.{SignService, VoterServiceImpl}
import org.ergoplatform.board.stores.{ElectionStoreImpl, VoterStoreImpl}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import play.api.libs.json.Json

class VoterResourcesSpec extends FlatSpec
  with BeforeAndAfterAll
  with Matchers
  with ScalatestRouteTest
  with PlayJsonSupport
  with FutureHelpers
  with MongoPerSpec
  with Generators {

  import akka.http.scaladsl.testkit.RouteTestTimeout
  import akka.testkit.TestDuration
  import org.ergoplatform.board.ApiErrorHandler._

  import scala.concurrent.duration._

  implicit val timeout = RouteTestTimeout(10.seconds dilated)

  lazy val eStore = new ElectionStoreImpl(db)
  lazy val vStore = new VoterStoreImpl(db)
  lazy val service = new VoterServiceImpl(eStore, vStore)
  lazy val handler = new VoterResources(service)
  lazy val route  = Route.seal(handler.routes)

  it should "create voter correctly" in {
    val election = eStore.create(rndElection()).await
    val electionId = election._id
    val keys = SignService.generateRandomKeyPair()

    val cmd = VoterCreate(electionId, keys.publicKey)

    Post("/voters",cmd)  ~> route ~> check {
      status shouldBe StatusCodes.Created
      val data = entityAs[Voter]
      data.electionId shouldEqual electionId
      data.publicKey shouldEqual keys.publicKey
    }

    //check the case when we try to create voter for incorrect election id
    val corruptedCmd = VoterCreate(uuid, keys.publicKey)
    Post("/voters", corruptedCmd)  ~> route ~> check {
      status shouldBe StatusCodes.NotFound
    }
  }

  it should "get voter correctly" in {
    val election = eStore.create(rndElection()).await
    val electionId = election._id
    val voter = vStore.create(rndVoter(electionId)).await

    Get(s"/voters/$uuid")  ~> route ~> check {
      status shouldBe StatusCodes.NotFound
    }

    Get(s"/voters/${voter._id}")  ~> route ~> check {
      status shouldBe StatusCodes.OK
      val data = entityAs[Voter]
      data.electionId shouldEqual voter.electionId
      data.publicKey shouldEqual voter.publicKey
      data.id shouldEqual voter._id
    }
  }


}
