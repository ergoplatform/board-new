package org.ergoplatform.board.handlers

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import io.swagger.annotations._
import org.ergoplatform.board.directives.CommonDirectives
import org.ergoplatform.board.protocol.{ApiErrorResponse, Vote, VoteCreate, Voter}
import org.ergoplatform.board.services.VoteService
import org.ergoplatform.board.utils.RichBoolean

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


@Path("/votes")
@Api(value = "/votes", produces = "application/json")
class VoteResources(service: VoteService, timeout: Timeout = Timeout(3 seconds))
                   (implicit ec: ExecutionContext, mat: ActorMaterializer) extends PlayJsonSupport
  with CommonDirectives
  with RichBoolean {

  val routes = pathPrefix("votes") {
    postVote ~ getVote
  }


  @ApiOperation(httpMethod = "POST", code = 201, response = classOf[Vote], value = "Creates vote")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "VoteCreate",
        value = "VoteCreate Сommand",
        dataType = "org.ergoplatform.board.protocol.VoteCreate",
        paramType = "body")
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Return created vote model", response = classOf[Vote]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def postVote = (post & entity(as[VoteCreate])) { cmd =>
    onSuccess(service.vote(cmd)) { r =>
      complete(StatusCodes.Created -> r)
    }
  }

  @Path("{id}")
  @ApiOperation(httpMethod = "GET", code = 200, response = classOf[Vote], value = "Gets vote model by Id")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "id",
        value = "UUID",
        dataType = "string",
        paramType = "path",
        required = true)
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Not Found", response = classOf[ApiErrorResponse])
  ))
  def getVote = (get & uuidPath) { uuid => onSuccess(service.get(uuid)) { r => complete(r) } }

}
