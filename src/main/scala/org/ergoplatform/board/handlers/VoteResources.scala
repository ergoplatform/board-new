package org.ergoplatform.board.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.directives.CommonDirectives
import org.ergoplatform.board.protocol.VoteCreate
import org.ergoplatform.board.services.VoteService
import org.ergoplatform.board.utils.RichBoolean

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


class VoteResources(service: VoteService, timeout: Timeout = Timeout(3 seconds))
                   (implicit ec: ExecutionContext, mat: ActorMaterializer) extends PlayJsonSupport
  with CommonDirectives
  with RichBoolean {

  val routes = pathPrefix("votes") { postVote ~ getVote }

  def postVote = (post & entity(as[VoteCreate])) { cmd =>
    onSuccess(service.vote(cmd)) { r => complete(StatusCodes.Created -> r) }
  }

  def getVote = (get & uuidPath) { uuid => onSuccess(service.get(uuid)) { r => complete(r) } }
}
