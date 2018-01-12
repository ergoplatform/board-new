package org.ergoplatform.board.persistence

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.ergoplatform.board.models.AvlVote
import org.ergoplatform.board.mongo.MongoPerSpec
import org.ergoplatform.board.persistence.AvlTreeVoteProcessor.ApplyVote
import org.ergoplatform.board.services.SignService
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

  it should "be alive!" in {

    val keys = SignService.generateRandomKeyPair()
    val electionId = uuid

    val ac1 = system.actorOf(AvlTreeVoteProcessor.props("test", keys))

    val av1 = ApplyVote(electionId, "1")
    val av2 = ApplyVote(electionId, "2")
    val av3 = ApplyVote(electionId, "3")
    val av4 = ApplyVote(electionId, "4")
    val av5 = ApplyVote(electionId, "5")

    val v1 = (ac1 ? av1).mapTo[AvlVote].await
    val v2 = (ac1 ? av2).mapTo[AvlVote].await
    val v3 = (ac1 ? av3).mapTo[AvlVote].await
    val v4 = (ac1 ? av4).mapTo[AvlVote].await
    val v5 = (ac1 ? av5).mapTo[AvlVote].await

    val ac2 = system.actorOf(AvlTreeVoteProcessor.props("test", keys))
    val digest1 = (ac1 ? "print").mapTo[String].await
    val digest2 = (ac2 ? "print").mapTo[String].await

    digest1 shouldBe digest2

    v1.proof.postDigest shouldBe v2.proof.digest
    v2.proof.postDigest shouldBe v3.proof.digest
    v3.proof.postDigest shouldBe v4.proof.digest
    v4.proof.postDigest shouldBe v5.proof.digest
  }
}
