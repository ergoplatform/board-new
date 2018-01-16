package org.ergoplatform.board.services

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import org.ergoplatform.board.persistence.AvlTreeVoteProcessor
import org.ergoplatform.board.persistence.AvlTreeVoteProcessor.{VoteApplication, VoteFailure, VoteResult, VoteSuccess}
import org.ergoplatform.board.protocol.{Vote, VoteCreate}
import org.ergoplatform.board.stores.{ElectionStore, VoteStore, VoterStore}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait VoteService {

  def vote(voteCreate: VoteCreate): Future[Vote]

  def get(id: String): Future[Vote]
}

class VoteServiceImpl(eStore: ElectionStore, voteStore: VoteStore, voterStore: VoterStore)
                     (implicit ec: ExecutionContext, ac: ActorSystem) extends VoteService {

  implicit val timeout = Timeout(10 seconds)

  def voteExistsValidation(electionId: String, voterId: String): Future[Unit] = voteStore
    .exists(electionId, voterId)
    .flatMap{
      case true => Future.failed(new IllegalStateException("You have voted already." +
        "You are not allowed to do it twice within same election."))
      case false => Future.successful(())
    }

  override def vote(voteCreate: VoteCreate): Future[Vote] = for {
    _ <- SignService.validateFuture(voteCreate.signature, voteCreate.m)
    electionId = voteCreate.electionId
    m = voteCreate.m
    e <- eStore.get(voteCreate.electionId).filter(_.end > System.currentTimeMillis())
    voter <- voterStore.getByKeyAndElectionId(voteCreate.signature.publicKey, e._id)
    voterId = voter._id
    _ <- voteExistsValidation(electionId, voterId)
    actor = ac.actorOf(AvlTreeVoteProcessor.props(e._id), e._id)
    voteSuccess <- (actor ? VoteApplication(electionId, voterId, m)).mapTo[VoteResult].flatMap {
      case x: VoteSuccess => Future.successful(x)
      case y: VoteFailure => Future.failed(y.reason)
    }
    saved <- voteStore.save(voteSuccess.vote)
  } yield Vote.fromRecord(saved, Some(voteSuccess.proof))

  override def get(id: String): Future[Vote] = voteStore.get(id).map(v => Vote.fromRecord(v))
}
