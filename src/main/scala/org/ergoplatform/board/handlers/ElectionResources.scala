package org.ergoplatform.board.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.ergoplatform.board.directives.CommonDirectives
import org.ergoplatform.board.protocol.{ElectionCreate, ElectionProlong, VoteCreate}
import org.ergoplatform.board.services.ElectionService
import org.ergoplatform.board.utils.CommonCodecs

import scala.concurrent.ExecutionContext

class ElectionResources(service: ElectionService)(implicit ec: ExecutionContext, mat: ActorMaterializer)
  extends PlayJsonSupport with CommonDirectives with CommonCodecs {

  val routes = pathPrefix("elections") {
    pathEndOrSingleSlash {
      post {
        entity(as[ElectionCreate]) { cmd =>
          onSuccess(service.create(cmd)) { v => complete(StatusCodes.Created -> v) }
        }
      }
    } ~ uuidPath { uuid =>
      (get & pathPrefix("exist")) {
        onSuccess(service.exist(uuid)) { v => complete(v) }
      } ~ (post & entity(as[ElectionProlong])) { cmd =>
        onSuccess(service.extendDuration(uuid, cmd)) { v => complete(v) }
      } ~ pathPrefix("votes") {
        (pathPrefix("count") & get) {
          onSuccess(service.getVotesCount(uuid)) { v => complete(v) }
        } ~ (post & entity(as[VoteCreate])) { cmd =>
          onSuccess(service.vote(uuid, cmd)) { v => complete(StatusCodes.Created -> v) }
        } ~ (get & parameters("offset".as[Int] ? 0, "limit".as[Int] ? 20)) { (o, l) =>
          onSuccess(service.getVotes(uuid, o, l)) { v => complete(v) }
        }
      } ~ onSuccess(service.get(uuid)) { v => complete(v) }
    }
  }
}
