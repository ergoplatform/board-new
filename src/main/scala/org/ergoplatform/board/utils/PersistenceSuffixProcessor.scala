package org.ergoplatform.board.utils

import akka.contrib.persistence.mongodb.CanSuffixCollectionNames

class PersistenceSuffixProcessor extends CanSuffixCollectionNames {
  import PersistenceSuffixProcessor._

  override def getSuffixFromPersistenceId(persistenceId: String): String = persistenceId

  override def validateMongoCharacters(input: String): String =
    input.map { c => if (forbidden.contains(c)) '_' else c }

}

object PersistenceSuffixProcessor {
  val forbidden = List('/', '\\', '.', ' ', '\"', '$', '*', '<', '>', ':', '|', '?')
}
