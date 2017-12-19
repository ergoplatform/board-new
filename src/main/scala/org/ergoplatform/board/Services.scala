package org.ergoplatform.board

import org.ergoplatform.board.services.ElectionServiceImpl
import org.ergoplatform.board.stores.{ElectionStoreImpl, VoteStoreImpl}

trait Services { self: Mongo with Setup =>
  lazy val eStore = new ElectionStoreImpl(db)
  lazy val vStore = new VoteStoreImpl(db)
  lazy val electionService = new ElectionServiceImpl(eStore, vStore)
}
