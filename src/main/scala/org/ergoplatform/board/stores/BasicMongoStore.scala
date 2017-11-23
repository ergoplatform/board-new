package org.ergoplatform.board.stores

import play.api.libs.json._
import reactivemongo.api.DefaultDB
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

abstract class BasicMongoStore[Model, ID](db: DefaultDB, collectionName: String)
                                         (implicit ec: ExecutionContext,
                                          mFormat: OFormat[Model],
                                          idFormat: OFormat[ID]) {

  val collection: JSONCollection = db[JSONCollection](collectionName)

  def $id[T](id: T)(implicit w: OWrites[T]): JsObject = Json.obj("_id" → Json.toJson(id))

  def getById(id: ID): Future[Model] = findById(id).flatMap {
    case Some(or) => Future.successful(or)
    case None => Future.failed(new NoSuchElementException(s"Cannot find record with id = $id"))
  }

  def findById(id: ID): Future[Option[Model]] = collection.find($id(id)).one[Model]

  def insert(model: Model): Future[Model] = collection.insert(model).map(_ => model)

  def updateById(id: ID, update: JsObject): Future[Model] = collection.update($id(id), update).flatMap(_ ⇒ getById(id))
}
