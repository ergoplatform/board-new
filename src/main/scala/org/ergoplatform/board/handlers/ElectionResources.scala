package org.ergoplatform.board.handlers

import javax.ws.rs.Path

import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import io.swagger.annotations._
import org.ergoplatform.board.directives.CommonDirectives
import org.ergoplatform.board.protocol._
import org.ergoplatform.board.services.ElectionService
import org.ergoplatform.board.utils.RichBoolean

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Path("/elections")
@Api(value = "/elections", produces = "application/json")
class ElectionResources(service: ElectionService, timeout: Timeout = Timeout(3 seconds))
                       (implicit ec: ExecutionContext, mat: ActorMaterializer)
  extends PlayJsonSupport
  with CommonDirectives
  with RichBoolean {

  import akka.http.scaladsl.model.StatusCodes._

  val routes = pathPrefix("elections") { createElection ~ existElection ~ extendElection ~ getElection }

  @ApiOperation(httpMethod = "POST", code = 201, response = classOf[Election], value = "Creates election")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "ElectionCreate",
        value = "ElectionCreate Сommand",
        dataType = "org.ergoplatform.board.protocol.ElectionCreate",
        paramType = "body")
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Return created election model", response = classOf[Election]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def createElection = (post & entity(as[ElectionCreate])) { cmd =>
    onSuccess(service.create(cmd)) { v => complete(Created -> v) }
  }

  @Path("{id}")
  @ApiOperation(httpMethod = "GET", code = 200, response = classOf[Election], value = "Gets election model by Id")
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
  def getElection = (get & uuidPath) { uuid => onSuccess(service.get(uuid)) { v => complete(v) } }


  @Path("/{id}/exist")
  @ApiOperation(httpMethod = "GET", code = 200, response = classOf[BooleanResultResponse], value = "Checks if election with this id is exists")
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
  def existElection = (get & uuidPath & pathPrefix("exist")) { uuid => onSuccess(service.exist(uuid)) { v => complete(v.toResponse) } }

  @Path("/{id}")
  @ApiOperation(httpMethod = "PUT", code = 200, response = classOf[Election], value = "Prolongs election duration for provided number of milliseconds")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "id",
        value = "UUID",
        dataType = "string",
        paramType = "path",
        required = true),
      new ApiImplicitParam(
        name = "ElectionProlong",
        value = "ElectionProlong Сommand",
        dataType = "org.ergoplatform.board.protocol.ElectionProlong",
        paramType = "body")
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Not Found", response = classOf[ApiErrorResponse])
  ))
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
