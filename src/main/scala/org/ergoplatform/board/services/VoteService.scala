package org.ergoplatform.board.services

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import org.ergoplatform.board.models.VoteRecord
import org.ergoplatform.board.persistence.HashChainVoteProcessor
import org.ergoplatform.board.persistence.HashChainVoteProcessor.VoteMessage
import org.ergoplatform.board.protocol.{VoteCreate, Vote}
import org.ergoplatform.board.stores.{ElectionStore, VoteStore, VoterStore}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait VoteService {

  def vote(voteCreate: VoteCreate): Future[Vote]

  def get(id: String): Future[Vote]
}

class VoteServiceImpl(eStore: ElectionStore, vStore: VoteStore, voterStore: VoterStore)
                     (implicit ec: ExecutionContext, ac: ActorSystem) extends VoteService {

  implicit val timeout = Timeout(10 seconds)

  override def vote(voteCreate: VoteCreate): Future[Vote] = for {
    _ <- SignService.validateFuture(voteCreate.signature, voteCreate.m)
    e <- eStore.get(voteCreate.electionId).filter(_.end > System.currentTimeMillis())
    //TODO: Need to check here is this voter already voted or not
    _ <- voterStore.getByKeyAndElectionId(voteCreate.signature.publicKey, e._id)
    boardSignature = SignService.sign(voteCreate.m, e.keys)
    actor = ac.actorOf(HashChainVoteProcessor.props(e._id, e.keys), e._id)
    vote <- (actor ? VoteMessage(voteCreate, boardSignature)).mapTo[VoteRecord]
    saved <- vStore.save(vote)
  } yield Vote.fromRecord(saved)

  override def get(id: String): Future[Vote] = vStore.get(id).map(Vote.fromRecord)
}
