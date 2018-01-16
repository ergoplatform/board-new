package org.ergoplatform.board.stores

import org.ergoplatform.board.models.VoteRecord
import play.api.libs.json.Json
import reactivemongo.api.DefaultDB

import scala.concurrent.{ExecutionContext, Future}

trait VoteStore {

  def get(id: String): Future[VoteRecord]

  def save(rec: VoteRecord): Future[VoteRecord]

  def exists(electionId: String, voterId: String): Future[Boolean]

  def getAllByElectionId(electionId: String, offset: Int, limit: Int): Future[List[VoteRecord]]

  def countByElectionId(electionId: String): Future[Int]

//  def updateAll(list: List[VoteRecord]): Future[List[VoteRecord]]
}

class VoteStoreImpl(db: DefaultDB)
                   (implicit ec: ExecutionContext)
  extends BasicMongoStore[VoteRecord, String](db, "votes") with VoteStore {

  override def getAllByElectionId(electionId: String, offset: Int, limit: Int) = {
    val query = Json.obj("electionId" -> electionId)
    findByQuery(query = query, offset = offset, limit = limit)
  }

  override def countByElectionId(electionId: String) = {
    val query = Json.obj("electionId" -> electionId)
    countByQuery(query)
  }

//  def getIndexAndHash(electionId: String): Future[(Long, String)] = {
//    collection
//      .find(Json.obj("electionId" -> electionId), Json.obj("index" -> 1, "hash" -> 1))
//      .sort(Json.obj("index" -> -1))
//      .cursor[JsValue]()
//      .collect[List](1, Cursor.FailOnError[List[JsValue]]())
//      .map {
//        _.headOption.map { v =>
//          val index = (v \ "index").asOpt[Long].getOrElse(0L)
//          val hash = (v \ "hash").asOpt[String].getOrElse("")
//
//          (index, hash)
//        }.getOrElse((0L, ""))
//      }
//  }

  def exists(electionId: String, voterId: String): Future[Boolean] = {
    val query = Json.obj("electionId" -> electionId, "voterId" -> voterId)
    countByQuery(query).map(_ > 0)
  }

  def get(id: String): Future[VoteRecord] = getById(id)

  def save(rec: VoteRecord): Future[VoteRecord] = insert(rec)

  //  SHOULD ONLY BE USED FOR SCHEMA EVOLUTIONS
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
}
