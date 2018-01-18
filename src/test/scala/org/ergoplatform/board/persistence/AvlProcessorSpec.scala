package org.ergoplatform.board.persistence

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.ergoplatform.board.mongo.MongoPerSpec
import org.ergoplatform.board.persistence.AvlTreeVoteProcessor._
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

class AvlProcessorSpec extends TestKit(ActorSystem("vote-spec"))
  with FlatSpecLike
  with MongoPerSpec
  with Matchers
  with FutureHelpers
  with Generators {

  override val port = 27020

  implicit val timeout = Timeout(30 seconds)

  it should "work in a simple flow" in {
    val electionId = uuid
    val ac1 = system.actorOf(AvlTreeVoteProcessor.props(s"test_$electionId"))

    val v1 = VoteApplication(electionId, uuid, "1")
    val v2 = VoteApplication(electionId, uuid, "2")
    val v3 = VoteApplication(electionId, uuid, "3")
    val v4 = VoteApplication(electionId, uuid, "4")
    val v5 = VoteApplication(electionId, uuid, "5")

    val vr1 = (ac1 ? v1).mapTo[VoteSuccess].await
    val vr2 = (ac1 ? v2).mapTo[VoteSuccess].await
    val vr3 = (ac1 ? v3).mapTo[VoteSuccess].await
    val vr4 = (ac1 ? v4).mapTo[VoteSuccess].await
    val vr5 = (ac1 ? v5).mapTo[VoteSuccess].await

    val ac2 = system.actorOf(AvlTreeVoteProcessor.props(s"test_$electionId"))
    val digest1 = (ac1 ? GetCurrentDigest).mapTo[String].await
    val digest2 = (ac2 ? GetCurrentDigest).mapTo[String].await

    digest1 shouldBe digest2

    vr1.proof.postDigest shouldBe vr2.proof.digest
    vr2.proof.postDigest shouldBe vr3.proof.digest
    vr3.proof.postDigest shouldBe vr4.proof.digest
    vr4.proof.postDigest shouldBe vr5.proof.digest
  }

  it should "retrieve data from tree back" in {
    val electionId = uuid
    val ac1 = system.actorOf(AvlTreeVoteProcessor.props(s"test_$uuid"))

    val v1 = VoteApplication(electionId, uuid, "1")
    val v2 = VoteApplication(electionId, uuid, "2")

    val vr1 = (ac1 ? v1).mapTo[VoteSuccess].await
    val vr2 = (ac1 ? v2).mapTo[VoteSuccess].await

    (ac1 ? GetVoteFromTree(vr1.vote.index)).mapTo[VoteOption].await shouldBe VoteOption(Some(v1.m))
    (ac1 ? GetVoteFromTree(vr2.vote.index)).mapTo[VoteOption].await shouldBe VoteOption(Some(v2.m))
    (ac1 ? GetVoteFromTree(vr2.vote.index + 1)).mapTo[VoteOption].await shouldBe VoteOption(None)

  }
}
