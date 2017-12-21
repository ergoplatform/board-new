package org.ergoplatform.board.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import org.ergoplatform.board.models.VoteRecord
import org.ergoplatform.board.persistence.HashChainVoteProcessor
import org.ergoplatform.board.persistence.HashChainVoteProcessor.VoteMessage
import org.ergoplatform.board.protocol.{VoteCreate, VoteView}
import org.ergoplatform.board.stores.{ElectionStore, VoteStore, VoterStore}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

trait VoteService {

  def vote(voteCreate: VoteCreate): Future[VoteView]

  def get(id: String): Future[VoteView]
}

class VoteServiceImpl(eStore: ElectionStore, vStore: VoteStore, voterStore: VoterStore)
                     (implicit ec: ExecutionContext, ac: ActorSystem) extends VoteService {

  implicit val timeout = Timeout(10 seconds)

  override def vote(voteCreate: VoteCreate): Future[VoteView] = for {
    _ <- SignService.validateFuture(voteCreate.signature, voteCreate.m)
    e <- eStore.get(voteCreate.electionId).filter(_.end > System.currentTimeMillis())
    //TODO: Need to check here is this voter already voted or not
    _ <- voterStore.getByKeyAndElectionId(voteCreate.signature.publicKey, e._id)
    boardSignature = SignService.sign(voteCreate.m, e.keys)
    vote <- (ac.actorOf(HashChainVoteProcessor.props(e._id, e.keys)) ? VoteMessage(voteCreate, boardSignature)).mapTo[VoteRecord]
    saved <- vStore.save(vote)
  } yield VoteView.fromRecord(saved)

  override def get(id: String): Future[VoteView] = vStore.get(id).map(VoteView.fromRecord)
}
