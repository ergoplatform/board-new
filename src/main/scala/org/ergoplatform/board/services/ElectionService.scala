package org.ergoplatform.board.services

import org.ergoplatform.board.models.{ElectionRecord, MongoId}
import org.ergoplatform.board.protocol._
import org.ergoplatform.board.stores.{ElectionStore, VoteStore}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

trait ElectionService {

  def create(cmd: ElectionCreate): Future[ElectionView]

  def find(id: MongoId): Future[Option[ElectionView]]

  def get(id: MongoId): Future[ElectionView]

  def exist(id: MongoId): Future[Boolean]

  def extendDuration(id: MongoId, extendFor: ElectionProlong): Future[ElectionView]

  def vote(electionId: MongoId, cmd: VoteCreate): Future[VoteView]

  def getVotesCount(electionId: MongoId): Future[Int]

  def getVotes(electionId: MongoId, offset: Int = 0, limit: Int = 20): Future[List[VoteView]]

}

class ElectionServiceImpl(eStore: ElectionStore, vStore: VoteStore)
                         (implicit ec: ExecutionContext) extends ElectionService {

  def create(cmd: ElectionCreate): Future[ElectionView] = {
    val id = MongoId()
    val keys = SignService.generateRandomKeyPair()
    val election = ElectionRecord(id, cmd.start, cmd.end, keys, cmd.description)
    eStore.create(election).map(_ => ElectionView.fromRecord(election))
  }

  def find(id: MongoId): Future[Option[ElectionView]] = eStore.find(id).map(_.map(ElectionView.fromRecord))

  def get(id: MongoId): Future[ElectionView] = eStore.get(id).map(ElectionView.fromRecord)

  def exist(id: MongoId): Future[Boolean] = eStore.exist(id)

  def extendDuration(id: MongoId, extendFor: ElectionProlong): Future[ElectionView] = eStore
    .extend(id, extendFor.prolongDuration)
    .map(ElectionView.fromRecord)

  def vote(electionId: MongoId, cmd: VoteCreate): Future[VoteView] = for {
    _ <- SignService.validateFuture(cmd.signature, cmd.m)
    _ <- eStore.exist(electionId)
    election <- eStore.get(electionId)
    signedByBoardData = SignService.sign(Json.stringify(Json.toJson(cmd)), election.keys)
    vote <- vStore.create(electionId, cmd, signedByBoardData)
    voteView = VoteView.fromRecord(vote)
  } yield voteView

  def getVotesCount(electionId: MongoId): Future[Int] = vStore.countByElectionId(electionId)

  def getVotes(electionId: MongoId, offset: Int = 0, limit: Int = 20): Future[List[VoteView]] =
    vStore.getAllByElectionId(electionId, offset, limit).map(_.map(VoteView.fromRecord))
}
