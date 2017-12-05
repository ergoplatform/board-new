package org.ergoplatform.board.mongo

import org.scalatest.fixture
import reactivemongo.api.DefaultDB

import scala.concurrent.duration._

trait MongoFixture extends fixture.FlatSpecLike with EmbeddedMongoInstance with MongoClient {

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
}
