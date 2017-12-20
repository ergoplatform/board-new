package org.ergoplatform.board.stores

import org.ergoplatform.board.models.VoterRecord
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.DefaultDB

import scala.concurrent.{ExecutionContext, Future}

trait VoterStore {

  def create(rec: VoterRecord): Future[VoterRecord]

  def exists(publicKey: String): Future[Boolean]

  def get(id: String): Future[VoterRecord]

  def findByKey(publicKey: String): Future[Option[VoterRecord]]

  def findByKeyAndElectionId(publicKey: String, electionId: String): Future[Option[VoterRecord]]
}

class VoterStoreImpl(db: DefaultDB)
                    (implicit ec: ExecutionContext)
  extends BasicMongoStore[VoterRecord, String](db, "voters") with VoterStore {

  private def byKeyQuery(publicKey: String): JsObject = Json.obj("publicKey" -> publicKey)

  private def byKeyQueryAndElectionId(publicKey: String, electionId: String): JsObject = {
    byKeyQuery(publicKey) ++ Json.obj("electionId" -> electionId)
  }

  override def create(rec: VoterRecord): Future[VoterRecord] = insert(rec)

  override def exists(publicKey: String): Future[Boolean] = findOne(byKeyQuery(publicKey)).map(_.nonEmpty)

  override def get(id: String): Future[VoterRecord] = getById(id)

  override def findByKey(publicKey: String): Future[Option[VoterRecord]] = findOne(byKeyQuery(publicKey))

  override def findByKeyAndElectionId(publicKey: String, electionId: String): Future[Option[VoterRecord]] =
    findOne(byKeyQueryAndElectionId(publicKey, electionId))
}
