package org.ergoplatform.board.directives

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import org.ergoplatform.board.models.MongoId

import scala.util.{Failure, Success, Try}

trait CommonDirectives {

  val objectId: Directive1[MongoId] = pathPrefix(Segment).flatMap { value =>
    Try(MongoId.fromString(value)) match {
      case Success(id) => provide(id)
      case Failure(_) => failWith(new IllegalArgumentException("Wrong id format"))
    }
  }

}
