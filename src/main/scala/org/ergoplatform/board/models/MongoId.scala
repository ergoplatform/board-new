package org.ergoplatform.board.models

import org.mongodb.scala.bson.BsonObjectId
import play.api.libs.json._

case class MongoId(id: String) extends AnyVal

object MongoId {

  implicit val mIdReads: Reads[MongoId] = Reads[MongoId] { json =>
    val value = (json \ "$oid").asOpt[String]
    value match {
      case Some(s) => JsSuccess(MongoId(s))
      case None => JsError()
    }
  }

  implicit val mIdOWrites: OWrites[MongoId] = OWrites[MongoId] { id =>
    Json.obj("$oid" -> id.id)
  }

  implicit val oFormat: OFormat[MongoId] = OFormat[MongoId](mIdReads, mIdOWrites)

  def apply(): MongoId = MongoId(BsonObjectId.apply().getValue.toString)

  def fromString(id: String) = MongoId(BsonObjectId.apply(id).getValue.toString)

}
