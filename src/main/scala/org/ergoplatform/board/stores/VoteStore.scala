package org.ergoplatform.board.stores

import java.util.UUID

import org.ergoplatform.board.models.{SignedData, VoteRecord}
import org.ergoplatform.board.protocol.VoteCreate
import org.ergoplatform.board.services.HashService
import play.api.libs.json.{JsValue, Json}
import reactivemongo.api.{Cursor, DefaultDB}

import scala.concurrent.{Await, ExecutionContext, Future}

trait VoteStore {

  def get(id: String): Future[VoteRecord]

  def create(electionId: String, cmd: VoteCreate, boardSign: SignedData): Future[VoteRecord]

  def save(rec: VoteRecord): Future[VoteRecord]

  def getAllByElectionId(electionId: String, offset: Int, limit: Int): Future[List[VoteRecord]]

  def countByElectionId(electionId: String): Future[Int]

//  def updateAll(list: List[VoteRecord]): Future[List[VoteRecord]]
}

class VoteStoreImpl(db: DefaultDB)
                   (implicit ec: ExecutionContext)
  extends BasicMongoStore[VoteRecord, String](db, "votes") with VoteStore {

  import reactivemongo.play.json._

  //TODO: remove all kind of side logic. store should only save prepared data without preparing it.
  override def create(electionId: String, cmd: VoteCreate, boardSign: SignedData) = {
    val id = UUID.randomUUID().toString
    getIndexAndHash(electionId).flatMap{ case (index, prevHash) =>
      val hash = HashService.hash(Json.stringify(Json.toJson(cmd)), Some(prevHash).filterNot(_.isEmpty))
      val timestamp = System.currentTimeMillis()
      val vote = VoteRecord(id,
        electionId,
        index + 1,
        hash,
        cmd.m,
        cmd.signature,
        boardSign,
        timestamp)
      insert(vote)
    }

  }

  override def getAllByElectionId(electionId: String, offset: Int, limit: Int) = {
    val query = Json.obj("electionId" -> electionId)
    findByQuery(query = query, offset = offset, limit = limit)
  }

  override def countByElectionId(electionId: String) = {
    val query = Json.obj("electionId" -> electionId)
    countByQuery(query)
  }

  def getIndexAndHash(electionId: String): Future[(Long, String)] = {
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
//TODO: WAIT TO FIX ERGO-54
//  def updateAll(list: List[VoteRecord]): Future[List[VoteRecord]] = {
//    import scala.concurrent.duration._
//
//    val update = collection.update(false)
//    val elements = Await.result(Future.sequence(list.map { vr =>
//      update.element(
//        q = Json.obj("_id" -> vr._id),
//        u = vr,
//        upsert = true,
//        multi = false
//      )
//    }), 2 seconds)
//
//    update.many(elements).map(_ => list)
//  }

  def get(id: String): Future[VoteRecord] = getById(id)

  def save(rec: VoteRecord): Future[VoteRecord] = insert(rec)
}
