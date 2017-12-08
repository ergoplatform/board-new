package org.ergoplatform.board.services

import java.util.UUID

import org.ergoplatform.board.models.ElectionRecord
import org.ergoplatform.board.protocol._
import org.ergoplatform.board.stores.{ElectionStore, VoteStore}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

trait ElectionService {

  def create(cmd: ElectionCreate): Future[Election]

  def find(id: String): Future[Option[Election]]

  def get(id: String): Future[Election]

  def exist(id: String): Future[Boolean]

  def extendDuration(id: String, extendFor: ElectionProlong): Future[Election]

  def vote(electionId: String, cmd: VoteCreate): Future[VoteView]

  def getVotesCount(electionId: String): Future[Int]

  def getVotes(electionId: String, offset: Int = 0, limit: Int = 20): Future[List[VoteView]]

}

class ElectionServiceImpl(eStore: ElectionStore, vStore: VoteStore)
                         (implicit ec: ExecutionContext) extends ElectionService {

  def create(cmd: ElectionCreate): Future[Election] = {
    val id = UUID.randomUUID().toString
    val keys = SignService.generateRandomKeyPair()
    val election = ElectionRecord(id, cmd.start, cmd.end, keys, cmd.description)
    eStore.create(election).map(_ => Election.fromRecord(election))
  }

  def find(id: String): Future[Option[Election]] = eStore.find(id).map(_.map(Election.fromRecord))

  def get(id: String): Future[Election] = eStore.get(id).map(Election.fromRecord)

  def exist(id: String): Future[Boolean] = eStore.exist(id)

  def extendDuration(id: String, extendFor: ElectionProlong): Future[Election] = eStore
    .extend(id, extendFor.prolongDuration)
    .map(Election.fromRecord)


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
