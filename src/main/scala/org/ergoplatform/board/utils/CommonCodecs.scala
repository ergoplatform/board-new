package org.ergoplatform.board.utils

import play.api.libs.json.{JsBoolean, Json, OWrites}

trait CommonCodecs {


  implicit val booleanOWrites: OWrites[Boolean] = OWrites { b => Json.obj("result" -> JsBoolean.apply(b))}

}
