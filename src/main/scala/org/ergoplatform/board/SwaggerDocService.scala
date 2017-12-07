package org.ergoplatform.board

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import org.ergoplatform.board.handlers.ElectionResources

object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses = Set(classOf[ElectionResources])
  override val host = "localhost:8080"
  override val basePath = "/"
  override val apiDocsPath = "api-docs"
  override val info = Info(version = "1.0")
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}
