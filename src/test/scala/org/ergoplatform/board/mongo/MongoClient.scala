package org.ergoplatform.board.mongo

import java.util.concurrent.atomic.AtomicLong

import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

trait MongoClient { self: EmbeddedMongoInstance =>

  def getDb(connection: MongoConnection): DefaultDB = Await.result(connection.database(getDbName), 5 seconds)

  def getConnection(driver: MongoDriver, uri: String = mongouri): MongoConnection = {
    val driver = new reactivemongo.api.MongoDriver
    val parsedUriF: Future[ParsedURI] = Future.fromTry(MongoConnection.parseURI(uri))
    Await.result(parsedUriF.map(v => driver.connection(v)), 5 seconds)
  }

  def getDbName: String = "test_database_" + MongoClient.counter.getAndIncrement().toString

}

object MongoClient {
  val counter = new AtomicLong(0)
}
