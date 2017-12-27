package org.ergoplatform.board.services

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import org.ergoplatform.board.handlers.{ElectionResources, VoteResources, VoterResources}

object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses = Set(classOf[ElectionResources], classOf[VoterResources], classOf[VoteResources])
  override val host = "127.0.0.1:8080"
  override val basePath = "/"
  override val apiDocsPath = "api-docs"
  override val info = Info(version = "1.0")
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}
