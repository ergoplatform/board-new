package org.ergoplatform.board.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.mongo.MongoPerSpec
import org.ergoplatform.board.protocol.{SignedData, Vote, VoteCreate}
import org.ergoplatform.board.services.{SignService, VoteServiceImpl}
import org.ergoplatform.board.stores.{ElectionStoreImpl, VoteStoreImpl, VoterStoreImpl}
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class VoteResourcesSpec extends FlatSpec
  with BeforeAndAfterAll
  with Matchers
  with ScalatestRouteTest
  with PlayJsonSupport
  with FutureHelpers
  with MongoPerSpec
  with Generators {

  override val port = 27020

  import akka.http.scaladsl.testkit.RouteTestTimeout
  import akka.testkit.TestDuration
  import org.ergoplatform.board.ApiErrorHandler._

  import scala.concurrent.duration._

  implicit val timeout = RouteTestTimeout(15.seconds dilated)

  lazy val eStore = new ElectionStoreImpl(db)
  lazy val vStore = new VoterStoreImpl(db)
  lazy val voteStore = new VoteStoreImpl(db)
  lazy val service = new VoteServiceImpl(eStore, voteStore, vStore)
  lazy val handler = new VoteResources(service)
  lazy val route  = Route.seal(handler.routes)

  it should "post and get vote" in {
    val election = eStore.save(rndElection()).await
    val electionId = election._id
    val voterKeys = SignService.generateRandomKeyPair()
    val voter = vStore.save(rndVoter(electionId, voterKeys.publicKey)).await

    val randomKeys = SignService.generateRandomKeyPair()
    val wrongSignedData = SignedData(randomKeys.publicKey, "hoho")
    val wrongVoteCmd = VoteCreate(electionId, "hey", wrongSignedData)

    Post("/votes", wrongVoteCmd) ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
    }

    val m = "test_message"
    val signature = SignService.sign(m, voterKeys)
    val cmd = VoteCreate(electionId, m, signature)


    Post("/votes", cmd) ~> route ~> check {
      status shouldBe StatusCodes.Created
      val data = responseAs[Vote]
      data.electionId shouldEqual electionId
    }

    val vote = voteStore.getAllByElectionId(electionId, 0, 10).await.head

    Get(s"/votes/${vote._id}") ~> route ~> check  {
      status shouldBe StatusCodes.OK
      val data = responseAs[Vote]
      data.m shouldBe vote.m
    }
  }
}
