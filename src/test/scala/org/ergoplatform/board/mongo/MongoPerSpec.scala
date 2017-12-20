package org.ergoplatform.board.mongo

import de.flapdoodle.embed.mongo.MongodExecutable
import org.scalatest.BeforeAndAfterAll
import reactivemongo.api.{DefaultDB, MongoConnection}

import scala.concurrent.duration._

trait MongoPerSpec extends EmbeddedMongoInstance with MongoClient { self: BeforeAndAfterAll =>

  val driver = new reactivemongo.api.MongoDriver
  var mEx: MongodExecutable = _
  var connection: MongoConnection = _
  var db: DefaultDB = _

  override def beforeAll(): Unit = {
    mEx = mongoEx()
    mEx.start()
    connection = getConnection(driver)
    db = getDb(connection)
  }

  override def afterAll(): Unit = {
    connection.close()
    driver.close(2 seconds)
    mEx.stop()
  }

}
