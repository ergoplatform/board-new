package org.ergoplatform.board

import org.ergoplatform.board.models.{ElectionRecord, MongoId}
import org.ergoplatform.board.protocol.{ElectionCreate, ElectionProlong, ElectionView, VoteCreate}
import org.ergoplatform.board.services.{ElectionService, SignService}

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global

class FakeElectionService extends ElectionService with FutureHelpers {

  val data = new TrieMap[MongoId, ElectionRecord]()

  override def create(cmd: ElectionCreate) = {
    val id = MongoId()
    val keys = SignService.generateRandomKeyPair()
    val election = ElectionRecord(id, cmd.start, cmd.end, keys, cmd.description)
    data.put(id, election)
    ElectionView.fromRecord(election).asFut
  }

  override def find(id: MongoId) = data.get(id).map(ElectionView.fromRecord).asFut

  override def get(id: MongoId) = find(id).flatMap{
    case Some(or) => or.asFut
    case None => notFound(id).asFut
  }

  override def exist(id: MongoId) = data.get(id).nonEmpty.asFut

  override def extendDuration(id: MongoId, extendFor: ElectionProlong) = data.get(id) match {
    case Some(e) =>
      data.put(e.id, e.copy(end = e.end + extendFor.prolongDuration))
      ElectionView.fromRecord(data(e.id)).asFut
    case None => notFound(id).asFut
  }

  override def vote(electionId: MongoId, cmd: VoteCreate) = ???

  override def getVotesCount(electionId: MongoId) = ???

  override def getVotes(electionId: MongoId, offset: Int, limit: Int) = ???
}
