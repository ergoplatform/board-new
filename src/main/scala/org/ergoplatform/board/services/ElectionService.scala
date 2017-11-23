package org.ergoplatform.board.services

import org.ergoplatform.board.models.{ElectionRecord, MongoId}
import org.ergoplatform.board.protocol.{ElectionCreate, ElectionProlong, ElectionView}
import org.ergoplatform.board.stores.BasicMongoStore
import play.api.libs.json.Json
import reactivemongo.api.DefaultDB

import scala.concurrent.{ExecutionContext, Future}

trait ElectionService {

  def create(cmd: ElectionCreate): Future[ElectionView]

  def find(id: MongoId): Future[Option[ElectionView]]

  def get(id: MongoId): Future[ElectionView]

  def exist(id: MongoId): Future[Boolean]

  def extendDuration(id: MongoId, extendFor: ElectionProlong): Future[ElectionView]

}

class ElectionServiceImpl(db: DefaultDB)
                         (implicit ec: ExecutionContext)
  extends BasicMongoStore[ElectionRecord, MongoId](db, "elections") with ElectionService {

  def create(cmd: ElectionCreate): Future[ElectionView] = {
    val id = MongoId()
    val keys = SignService.generateRandomKeyPair()
    val election = ElectionRecord(id, cmd.start, cmd.end, keys, cmd.description)
    insert(election).map(_ => ElectionView.fromRecord(election))
  }

  def find(id: MongoId): Future[Option[ElectionView]] = findById(id).map(_.map(ElectionView.fromRecord))

  def get(id: MongoId): Future[ElectionView] = getById(id).map(ElectionView.fromRecord)

  def exist(id: MongoId): Future[Boolean] = find(id).map(_.nonEmpty)

  def extendDuration(id: MongoId, extendFor: ElectionProlong): Future[ElectionView] =
    getById(id).flatMap { e =>
      val newEnd = e.end + extendFor.prolongDuration
      updateById(id, Json.obj("$set" -> Json.obj("end" -> newEnd)))
    }.map {
      ElectionView.fromRecord
    }
}
