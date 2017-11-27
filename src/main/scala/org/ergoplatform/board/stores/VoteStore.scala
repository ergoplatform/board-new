package org.ergoplatform.board.stores

import org.ergoplatform.board.models.{MongoId, SignedData, VoteRecord}
import org.ergoplatform.board.protocol.VoteCreate
import play.api.libs.json.{JsValue, Json}
import reactivemongo.api.{Cursor, DefaultDB, QueryOpts}

import scala.concurrent.{ExecutionContext, Future}

trait VoteStore {

  def create(cmd: VoteCreate, boardSign: SignedData): Future[VoteRecord]

  def getAllByElectionId(electionId: MongoId, offset: Int, limit: Int): Future[List[VoteRecord]]

  def countByElectionId(electionId: MongoId): Future[Int]

}

class VoteStoreImpl(db: DefaultDB)
                   (implicit ec: ExecutionContext)
  extends BasicMongoStore[VoteRecord, MongoId](db, "votes") with VoteStore {

  import reactivemongo.play.json._

  override def create(cmd: VoteCreate, boardSign: SignedData) = {
    val id = MongoId()
    getIndexFor(cmd.electionId).flatMap{ index =>
      val timestamp = System.currentTimeMillis()
      val vote = VoteRecord(id,
        cmd.electionId,
        cmd.groupId,
        cmd.sectionId,
        index,
        cmd.m,
        cmd.signature,
        boardSign,
        timestamp)
      insert(vote)
    }

  }

  override def getAllByElectionId(electionId: MongoId, offset: Int, limit: Int) = {
    collection
      .find(Json.obj("electionId" -> electionId))
      .options(QueryOpts(skipN = offset, batchSizeN = limit))
      .cursor[VoteRecord]()
      .collect[List](limit, Cursor.FailOnError[List[VoteRecord]]())
  }

  override def countByElectionId(electionId: MongoId) = {
    val query = Json.obj("electionId" -> electionId)
    collection.count(Some(query))
  }

  def getIndexFor(electionId: MongoId): Future[Long] = {
    collection
      .find(Json.obj("electionId" -> electionId), Json.obj("index" -> 1))
      .sort(Json.obj("index" -> -1))
      .cursor[JsValue]()
      .collect[List](1, Cursor.FailOnError[List[JsValue]]())
      .map {
        _.headOption
          .flatMap { v => (v \ "index").asOpt[Long] }
          .getOrElse(0L)
      }
  }
}
