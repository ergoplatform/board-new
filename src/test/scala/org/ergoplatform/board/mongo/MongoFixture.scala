package org.ergoplatform.board.mongo

import java.util.concurrent.atomic.AtomicLong

import org.scalatest.fixture
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

trait MongoFixture extends fixture.FlatSpecLike with EmbeddedMongoInstance {

  override type FixtureParam = DefaultDB

  def withFixture(test: OneArgTest) = {
    val exec = mongoEx()
    val proc = exec.start()
    val driver = new reactivemongo.api.MongoDriver
    val connection = getConnection(driver)
    val db = getDb(connection)
    try {
      withFixture(test.toNoArgTest(db))
    } finally {
      connection.close()
      driver.close(2 seconds)
      proc.stop()
      exec.stop()
    }
  }

  private def getDb(connection: MongoConnection): DefaultDB = Await.result(connection.database(getDbName), 5 seconds)

  private def getConnection(driver: MongoDriver, uri: String = mongouri): MongoConnection = {
    val driver = new reactivemongo.api.MongoDriver
    val parsedUriF: Future[ParsedURI] = Future.fromTry(MongoConnection.parseURI(uri))
    Await.result(parsedUriF.map(v => driver.connection(v)), 5 seconds)
  }

  private def getDbName: String = "test_database_" + MongoFixture.counter.getAndIncrement().toString
}

object MongoFixture {
  val counter = new AtomicLong(0)
}
