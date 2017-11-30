package org.ergoplatform.board.services

import java.util.UUID

import org.ergoplatform.board.models.ElectionRecord
import org.ergoplatform.board.protocol._
import org.ergoplatform.board.stores.{ElectionStore, VoteStore}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

trait ElectionService {

  def create(cmd: ElectionCreate): Future[ElectionView]

  def find(id: String): Future[Option[ElectionView]]

  def get(id: String): Future[ElectionView]

  def exist(id: String): Future[Boolean]

  def extendDuration(id: String, extendFor: ElectionProlong): Future[ElectionView]

  def vote(electionId: String, cmd: VoteCreate): Future[VoteView]

  def getVotesCount(electionId: String): Future[Int]

  def getVotes(electionId: String, offset: Int = 0, limit: Int = 20): Future[List[VoteView]]

}

class ElectionServiceImpl(eStore: ElectionStore, vStore: VoteStore)
                         (implicit ec: ExecutionContext) extends ElectionService {

  def create(cmd: ElectionCreate): Future[ElectionView] = {
    val id = UUID.randomUUID().toString
    val keys = SignService.generateRandomKeyPair()
    val election = ElectionRecord(id, cmd.start, cmd.end, keys, cmd.description)
    eStore.create(election).map(_ => ElectionView.fromRecord(election))
  }

  def find(id: String): Future[Option[ElectionView]] = eStore.find(id).map(_.map(ElectionView.fromRecord))

  def get(id: String): Future[ElectionView] = eStore.get(id).map(ElectionView.fromRecord)

  def exist(id: String): Future[Boolean] = eStore.exist(id)

  def extendDuration(id: String, extendFor: ElectionProlong): Future[ElectionView] = eStore
    .extend(id, extendFor.prolongDuration)
    .map(ElectionView.fromRecord)

  def vote(electionId: String, cmd: VoteCreate): Future[VoteView] = for {
    _ <- SignService.validateFuture(cmd.signature, cmd.m)
    _ <- eStore.exist(electionId)
    election <- eStore.get(electionId)
    signedByBoardData = SignService.sign(Json.stringify(Json.toJson(cmd)), election.keys)
    vote <- vStore.create(electionId, cmd, signedByBoardData)
    voteView = VoteView.fromRecord(vote)
  } yield voteView

  def getVotesCount(electionId: String): Future[Int] = vStore.countByElectionId(electionId)

  def getVotes(electionId: String, offset: Int = 0, limit: Int = 20): Future[List[VoteView]] =
    vStore.getAllByElectionId(electionId, offset, limit).map(_.map(VoteView.fromRecord))
}
