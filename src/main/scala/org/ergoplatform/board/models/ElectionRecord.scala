package org.ergoplatform.board.models

import play.api.libs.json._

case class ElectionRecord(id: MongoId, start: Long, end: Long, keys: KeysRecord, description: Option[String])

object ElectionRecord {

  import MongoId._

  implicit val reads: Reads[ElectionRecord] = Reads[ElectionRecord] { json =>
      val id = (json \ "_id").as[MongoId]
      val start = (json \ "start").as[Long]
      val end = (json \ "end").as[Long]
      val keys = (json \ "keys").as[KeysRecord]
      val desc = (json \ "description").asOpt[String]
      JsSuccess(ElectionRecord(id, start, end, keys, desc))
  }

  implicit val oWrites: OWrites[ElectionRecord] = OWrites[ElectionRecord] { election =>
    val desc = election.description.fold(JsObject.empty) { desc => Json.obj("description" -> Json.toJson(desc))}
    val main = Json.obj(
      "_id" -> Json.toJson(election.id),
      "start" -> Json.toJson(election.start),
      "end" -> Json.toJson(election.end),
      "keys" -> Json.toJson(election.keys)
    )

   main ++ desc
  }

  implicit val fmt: OFormat[ElectionRecord] = OFormat[ElectionRecord](reads, oWrites)
}
