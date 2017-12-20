package org.ergoplatform.board.handlers

import javax.ws.rs.Path

import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import io.swagger.annotations._
import org.ergoplatform.board.directives.CommonDirectives
import org.ergoplatform.board.protocol.{ApiErrorResponse, Election, Voter, VoterCreate}
import org.ergoplatform.board.services.VoterService
import org.ergoplatform.board.utils.RichBoolean

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

@Path("/voters")
@Api(value = "/voters", produces = "application/json")
class VoterResources(service: VoterService, timeout: Timeout = Timeout(3 seconds))
                    (implicit ec: ExecutionContext, mat: ActorMaterializer) extends PlayJsonSupport
  with CommonDirectives
  with RichBoolean {

  import akka.http.scaladsl.model.StatusCodes._

  val routes = pathPrefix("voters") { createVoter ~ getVoter }

  @ApiOperation(httpMethod = "POST", code = 201, response = classOf[Voter], value = "Creates voter")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "VoterCreate",
        value = "VoterCreate Сommand",
        dataType = "org.ergoplatform.board.protocol.VoterCreate",
        paramType = "body")
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Return created voter model", response = classOf[Voter]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def createVoter = (post & entity(as[VoterCreate])) { cmd =>
    onSuccess(service.register(cmd)) { v => complete(Created -> v) }
  }

  @Path("{id}")
  @ApiOperation(httpMethod = "GET", code = 200, response = classOf[Voter], value = "Gets voter model by Id")
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
  def getVoter = (get & uuidPath) { uuid => onSuccess(service.get(uuid)) { v => complete(v) } }



}
