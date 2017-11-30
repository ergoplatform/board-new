package org.ergoplatform.board.utils

import play.api.libs.json._

trait CommonCodecs {

  implicit val booleanOWrites: OWrites[Boolean] = OWrites { b => Json.obj("result" -> JsBoolean.apply(b)) }

  implicit val countOWrites: OWrites[Int] = OWrites { b => Json.obj("count" -> JsNumber.apply(b)) }
}
