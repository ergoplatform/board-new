package org.ergoplatform.board

import reactivemongo.api.{DefaultDB, MongoConnection}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

import scala.language.postfixOps

trait Mongo { self: Setup =>

  val timeoutMongo = 10 seconds

  private val defaultMongoUri = "mongodb://localhost:27017"
  private lazy val dbName = Try(config.getString("mongodb.dbName")).getOrElse("board")
  private lazy val mongoUri = Try(config.getString("mongodb.uri")).getOrElse(defaultMongoUri)
  private val driver = reactivemongo.api.MongoDriver()

  lazy val connectionToMongoF: Future[MongoConnection] = for {
    parsedUri <- Future.fromTry(MongoConnection.parseURI(mongoUri))
    conn = driver.connection(parsedUri)
  } yield conn

  lazy val connectionToMongo = Await.result(connectionToMongoF, timeoutMongo)
  lazy val dbF: Future[DefaultDB] = connectionToMongo.database(dbName)
  lazy val db = Await.result(dbF, timeoutMongo)
}
