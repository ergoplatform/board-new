package org.ergoplatform.board

import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.MongoConnection
import reactivemongo.api.MongoConnection.ParsedURI

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait Mongo extends StrictLogging {

  implicit val executionContext: ExecutionContext
  val config: Config

  private val defaultMongoUri = "mongodb://localhost:27017"
  private lazy val driver = new reactivemongo.api.MongoDriver
  private lazy val uri = Try(config.getString("mongodb.uri")).getOrElse(defaultMongoUri)
  private lazy val parsedUri: Future[ParsedURI] = Future.fromTry(MongoConnection.parseURI(uri))
  lazy val connectionToMongo: Future[MongoConnection] = parsedUri.map(v => driver.connection(v))

  def terminateMongo: Unit = {
    connectionToMongo.map(_.close())
    driver.close()
  }
}
