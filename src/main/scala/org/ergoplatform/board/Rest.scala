package org.ergoplatform.board

import akka.http.scaladsl.server.Directives._
import org.ergoplatform.board.handlers.{ElectionResources, SwaggerSupport}
import org.ergoplatform.board.services.SwaggerDocService

trait Rest { self: Services with Setup =>

  lazy val electionRoutes = new ElectionResources(electionService).routes
  lazy val swaggerStaticClientRoute = new SwaggerSupport().assets
  lazy val swaggerJsonRoute = SwaggerDocService.routes

  lazy val routes = electionRoutes ~ swaggerStaticClientRoute ~ swaggerJsonRoute
}
