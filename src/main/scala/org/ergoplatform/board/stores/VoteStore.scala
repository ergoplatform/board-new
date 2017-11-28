package org.ergoplatform.board.stores

import org.ergoplatform.board.models.{MongoId, SignedData, VoteRecord}
import org.ergoplatform.board.protocol.VoteCreate
import org.ergoplatform.board.services.HashService
import play.api.libs.json.{JsValue, Json}
import reactivemongo.api.{Cursor, DefaultDB, QueryOpts}

import scala.concurrent.{ExecutionContext, Future}

trait VoteStore {

  def create(electionId: MongoId, cmd: VoteCreate, boardSign: SignedData): Future[VoteRecord]

  def getAllByElectionId(electionId: MongoId, offset: Int, limit: Int): Future[List[VoteRecord]]

  def countByElectionId(electionId: MongoId): Future[Int]

}

class VoteStoreImpl(db: DefaultDB)
                   (implicit ec: ExecutionContext)
  extends BasicMongoStore[VoteRecord, MongoId](db, "votes") with VoteStore {

  import reactivemongo.play.json._

  override def create(electionId: MongoId, cmd: VoteCreate, boardSign: SignedData) = {
    val id = MongoId()
    getIndexAndHash(electionId).flatMap{ case (index, prevHash) =>
      val hash = HashService.hash(Json.stringify(Json.toJson(cmd)), Some(prevHash).filterNot(_.isEmpty))
      val timestamp = System.currentTimeMillis()
      val vote = VoteRecord(id,
        electionId,
        cmd.groupId,
        cmd.sectionId,
        index + 1,
        hash,
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

  def getIndexAndHash(electionId: MongoId): Future[(Long, String)] = {
    collection
      .find(Json.obj("electionId" -> electionId), Json.obj("index" -> 1, "hash" -> 1))
      .sort(Json.obj("index" -> -1))
      .cursor[JsValue]()
      .collect[List](1, Cursor.FailOnError[List[JsValue]]())
      .map {
        _.headOption.map { v =>
          val index = (v \ "index").asOpt[Long].getOrElse(0L)
          val hash = (v \ "hash").asOpt[String].getOrElse("")

          (index, hash)
        }.getOrElse((0L, ""))
      }
  }
}
