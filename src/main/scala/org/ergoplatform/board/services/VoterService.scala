package org.ergoplatform.board.services

import java.util.UUID

import org.ergoplatform.board.models.VoterRecord
import org.ergoplatform.board.protocol.{Voter, VoterCreate}
import org.ergoplatform.board.stores.{ElectionStore, VoterStore}

import scala.concurrent.{ExecutionContext, Future}

trait VoterService {

  def register(cmd: VoterCreate): Future[Voter]

  def get(id: String): Future[Voter]
}

class VoterServiceImpl(eStore: ElectionStore, vStore: VoterStore)(implicit ec: ExecutionContext) extends VoterService {

  override def register(cmd: VoterCreate): Future[Voter] = eStore.exists(cmd.electionId).flatMap {
    case true =>
      val id = UUID.randomUUID().toString
      val rec = VoterRecord(id, cmd.electionId, cmd.publicKey)
      vStore.create(rec).map(Voter.fromVoterRecord)
    case false =>
      Future.failed(new NoSuchElementException(s"Can't find election with id = ${cmd.electionId}"))
  }

  override def get(id: String): Future[Voter] = vStore.get(id).map(Voter.fromVoterRecord)
}
