package org.ergoplatform.board.stores

import org.ergoplatform.board.models.{ElectionRecord, MongoId}
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.DefaultDB

import scala.concurrent.{ExecutionContext, Future}

trait ElectionStore {

  def create(rec: ElectionRecord): Future[ElectionRecord]

  def find(id: MongoId): Future[Option[ElectionRecord]]

  def get(id: MongoId): Future[ElectionRecord]

  def exist(id: MongoId): Future[Boolean]

  def extend(mongoId: MongoId, extendFor: Long): Future[ElectionRecord]

}

class ElectionStoreImpl(db: DefaultDB)
                       (implicit ec: ExecutionContext)
  extends BasicMongoStore[ElectionRecord, MongoId](db, "elections") with ElectionStore {


  def find(id: MongoId): Future[Option[ElectionRecord]] = findById(id)

  def get(id: MongoId): Future[ElectionRecord] = getById(id)

  def extend(id: MongoId, extendFor: Long): Future[ElectionRecord] = getById(id).flatMap { e =>
    val newEnd = e.end + extendFor
    updateById(id, Json.obj("$set" -> Json.obj("end" -> newEnd)))
  }

  override def create(rec: ElectionRecord) = insert(rec)

  override def exist(id: MongoId) = find(id).map(_.nonEmpty)
}
