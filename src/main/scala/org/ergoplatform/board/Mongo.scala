package org.ergoplatform.board

import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.{DefaultDB, MongoConnection}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

trait Mongo extends StrictLogging {

  val timeoutMongo = 10 seconds

  implicit val executionContext: ExecutionContext
  val config: Config

  private val defaultMongoUri = "mongodb://localhost:27017"
  private lazy val dbName = Try(config.getString("mongodb.dbName")).getOrElse("board")
  private lazy val mongoUri = Try(config.getString("mongodb.uri")).getOrElse(defaultMongoUri)
  private val driver = new reactivemongo.api.MongoDriver

  lazy val connectionToMongoF: Future[MongoConnection] = for {
    parsedUri <- Future.fromTry(MongoConnection.parseURI(mongoUri))
    conn = driver.connection(parsedUri)
  } yield conn

  lazy val connectionToMongo = Await.result(connectionToMongoF, timeoutMongo)
  lazy val dbF: Future[DefaultDB] = connectionToMongo.database(dbName)
  lazy val db = Await.result(dbF, timeoutMongo)

  def terminateMongo: Unit = {
    connectionToMongo.close()
    driver.close()
  }
}
