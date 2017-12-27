package org.ergoplatform.board.services

import java.util.UUID

import org.ergoplatform.board.models.ElectionRecord
import org.ergoplatform.board.protocol._
import org.ergoplatform.board.stores.ElectionStore

import scala.concurrent.{ExecutionContext, Future}

trait ElectionService {

  def create(cmd: ElectionCreate): Future[Election]

  def find(id: String): Future[Option[Election]]

  def get(id: String): Future[Election]

  def exists(id: String): Future[Boolean]

  def extendDuration(id: String, extendFor: ElectionProlong): Future[Election]
}

class ElectionServiceImpl(eStore: ElectionStore)(implicit ec: ExecutionContext) extends ElectionService {

  override def create(cmd: ElectionCreate): Future[Election] = {
    val id = UUID.randomUUID().toString
    val keys = SignService.generateRandomKeyPair()
    val election = ElectionRecord(id, cmd.start, cmd.end, keys, cmd.description)
    eStore.save(election).map(_ => Election.fromRecord(election))
  }

  override def find(id: String): Future[Option[Election]] = eStore.find(id).map(_.map(Election.fromRecord))

  override def get(id: String): Future[Election] = eStore.get(id).map(Election.fromRecord)

  override def exists(id: String): Future[Boolean] = eStore.exists(id)

  override def extendDuration(id: String, extendFor: ElectionProlong): Future[Election] = eStore
    .extend(id, extendFor.prolongDuration)
    .map(Election.fromRecord)
}
