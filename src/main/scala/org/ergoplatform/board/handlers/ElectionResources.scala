package org.ergoplatform.board.handlers

import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.directives.CommonDirectives
import org.ergoplatform.board.protocol._
import org.ergoplatform.board.services.ElectionService
import org.ergoplatform.board.utils.{RichBoolean, RichString}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ElectionResources(service: ElectionService, timeout: Timeout = Timeout(3 seconds))
                       (implicit ec: ExecutionContext, mat: ActorMaterializer)
  extends PlayJsonSupport
  with CommonDirectives
  with RichString
  with RichBoolean {

  import akka.http.scaladsl.model.StatusCodes._

  val routes = pathPrefix("elections") { createElection ~ existElection ~ currentHash ~ extendElection ~ getElection }

  def createElection = (post & entity(as[ElectionCreate])) { cmd =>
    onSuccess(service.create(cmd)) { v => complete(Created -> v) }
  }

  def getElection = (get & uuidPath) { uuid => onSuccess(service.get(uuid)) { v => complete(v) } }

  def existElection = (get & uuidPath & pathPrefix("exist")) { uuid =>
    onSuccess(service.exists(uuid)) { v => complete(v.toResponse) }
  }

  def extendElection = (put & uuidPath & entity(as[ElectionProlong])) { (uuid, cmd) =>
    onSuccess(service.extendDuration(uuid, cmd)) { v => complete(v) }
  }

  def currentHash = (get & uuidPath & pathPrefix("currentHash")) { uuid =>
    onSuccess(service.currentHash(uuid)) { v => complete(v.toResponse) }
  }
}
