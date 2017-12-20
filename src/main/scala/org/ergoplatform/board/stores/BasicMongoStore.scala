package org.ergoplatform.board.stores

import play.api.libs.json._
import reactivemongo.api.{Cursor, DefaultDB, QueryOpts}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.reflect.runtime.universe.{TypeTag, typeOf}
import scala.concurrent.{ExecutionContext, Future}

abstract class BasicMongoStore[Model: TypeTag, ID](db: DefaultDB, collectionName: String)
                                         (implicit ec: ExecutionContext,
                                          mFormat: OFormat[Model],
                                          idFormat: Format[ID]) {

  val collection: JSONCollection = db[JSONCollection](collectionName)

  def name[T: TypeTag] = typeOf[T].typeSymbol.name.toString

  def $id[T](id: T)(implicit w: Writes[T]): JsObject = Json.obj("_id" → Json.toJson(id))

  def getById(id: ID): Future[Model] = findById(id).flatMap {
    case Some(or) => Future.successful(or)
    case None => Future.failed(new NoSuchElementException(s"Cannot find record(${name[Model]}) with id = $id"))
  }

  def findById(id: ID): Future[Option[Model]] = collection.find($id(id)).one[Model]

  def insert(model: Model): Future[Model] = collection.insert(model).map(_ => model)

  def updateById(id: ID, update: JsObject): Future[Model] = collection.update($id(id), update).flatMap(_ ⇒ getById(id))

  def findByQuery(query: JsObject, offset: Int = 0, limit: Int = 20): Future[List[Model]] = {
    collection
      .find(query)
      .options(QueryOpts(skipN = offset, batchSizeN = limit))
      .cursor[Model]()
      .collect[List](limit, Cursor.FailOnError[List[Model]]())
  }

  def countByQuery(query: JsObject): Future[Int] = collection.count(Some(query))

  def findOne(query: JsObject): Future[Option[Model]] = collection.find(query).one[Model]
}
