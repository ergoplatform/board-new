package org.ergoplatform.board.stores

import org.ergoplatform.board.models.ElectionRecord
import play.api.libs.json.Json
import reactivemongo.api.DefaultDB

import scala.concurrent.{ExecutionContext, Future}

trait ElectionStore {

  def create(rec: ElectionRecord): Future[ElectionRecord]

  def find(id: String): Future[Option[ElectionRecord]]

  def get(id: String): Future[ElectionRecord]

  def exists(id: String): Future[Boolean]

  def extend(mongoId: String, extendFor: Long): Future[ElectionRecord]

}

class ElectionStoreImpl(db: DefaultDB)
                       (implicit ec: ExecutionContext)
  extends BasicMongoStore[ElectionRecord, String](db, "elections") with ElectionStore {


  def find(id: String): Future[Option[ElectionRecord]] = findById(id)

  def get(id: String): Future[ElectionRecord] = getById(id)

  def extend(id: String, extendFor: Long): Future[ElectionRecord] = getById(id).flatMap { e =>
    val newEnd = e.end + extendFor
    updateById(id, Json.obj("$set" -> Json.obj("end" -> newEnd)))
  }

  override def create(rec: ElectionRecord) = insert(rec)

  override def exists(id: String) = find(id).map(_.nonEmpty)
}
