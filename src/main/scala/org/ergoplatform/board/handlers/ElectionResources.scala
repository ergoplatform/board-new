package org.ergoplatform.board.handlers

import javax.ws.rs.Path

import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import io.swagger.annotations._
import org.ergoplatform.board.directives.CommonDirectives
import org.ergoplatform.board.protocol.{ElectionCreate, ElectionProlong}
import org.ergoplatform.board.services.ElectionService
import org.ergoplatform.board.utils.CommonCodecs

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Path("/elections")
@Api(value = "/elections", produces = "application/json")
class ElectionResources(service: ElectionService, timeout: Timeout = Timeout(3 seconds))
                       (implicit ec: ExecutionContext, mat: ActorMaterializer)
  extends PlayJsonSupport
  with CommonDirectives
  with CommonCodecs {

  import akka.http.scaladsl.model.StatusCodes._

  val routes = pathPrefix("elections") { createElection ~ existElection ~ extendElection ~ getElection }

  @ApiOperation(httpMethod = "POST", value = "Creates election")
  def createElection = (post & entity(as[ElectionCreate])) { cmd =>
    onSuccess(service.create(cmd)) { v => complete(Created -> v) }
  }

  def getElection = (get & uuidPath) { uuid => onSuccess(service.get(uuid)) { v => complete(v) } }

  def existElection = (get & uuidPath & pathPrefix("exist")) { uuid => onSuccess(service.exist(uuid)) { v => complete(v) } }

  def extendElection = (put & uuidPath & entity(as[ElectionProlong])) { (uuid, cmd) =>
    onSuccess(service.extendDuration(uuid, cmd)) { v => complete(v) }
  }

//votes here
//  pathPrefix("votes") {
//    (pathPrefix("count") & get) {
//      onSuccess(service.getVotesCount(uuid)) { v => complete(v) }
//    } ~ (post & entity(as[VoteCreate])) { cmd =>
//      onSuccess(service.vote(uuid, cmd)) { v => complete(StatusCodes.Created -> v) }
//    } ~ (get & paging) { (o, l) =>
//      onSuccess(service.getVotes(uuid, o, l)) { v => complete(v) }
//    }
//  }
}
