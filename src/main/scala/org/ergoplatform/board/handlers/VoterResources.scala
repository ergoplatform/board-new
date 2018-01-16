package org.ergoplatform.board.handlers

import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.directives.CommonDirectives
import org.ergoplatform.board.protocol.VoterCreate
import org.ergoplatform.board.services.VoterService
import org.ergoplatform.board.utils.RichBoolean

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class VoterResources(service: VoterService, timeout: Timeout = Timeout(3 seconds))
                    (implicit ec: ExecutionContext, mat: ActorMaterializer) extends PlayJsonSupport
  with CommonDirectives
  with RichBoolean {

  import akka.http.scaladsl.model.StatusCodes._

  val routes = pathPrefix("voters") { createVoter ~ getVoter }

  def createVoter = (post & entity(as[VoterCreate])) { cmd =>
    onSuccess(service.register(cmd)) { v => complete(Created -> v) }
  }

  def getVoter = (get & uuidPath) { uuid => onSuccess(service.get(uuid)) { v => complete(v) } }
}
