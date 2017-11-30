package org.ergoplatform.board.directives

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._

trait CommonDirectives {
  val uuidPath: Directive1[String] = pathPrefix(JavaUUID).map(_.toString)
}
