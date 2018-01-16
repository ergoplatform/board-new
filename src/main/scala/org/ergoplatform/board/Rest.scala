package org.ergoplatform.board

import akka.http.scaladsl.server.Directives._
import org.ergoplatform.board.handlers.{ElectionResources, SwaggerSupport, VoteResources, VoterResources}

trait Rest { self: Services with Setup =>

  lazy val electionRoutes = new ElectionResources(electionService).routes
  lazy val voterRoutes = new VoterResources(voterService).routes
  lazy val voteRoutes = new VoteResources(voteService).routes
  lazy val swaggerStaticClientRoute = new SwaggerSupport().assets

  lazy val routes = electionRoutes ~ swaggerStaticClientRoute ~ voterRoutes ~ voteRoutes
}
