package org.ergoplatform.board

import akka.actor.Props
import org.ergoplatform.board.actors.ActiveElectionStore
import org.ergoplatform.board.persistence.AvlTreeVoteProcessor
import org.ergoplatform.board.services.{ElectionProcessorProviderImpl, ElectionServiceImpl, VoteServiceImpl, VoterServiceImpl}
import org.ergoplatform.board.stores.{ElectionStoreImpl, VoteStoreImpl, VoterStoreImpl}

trait Services { self: Mongo with Setup =>
  lazy val electionStore = new ElectionStoreImpl(db)
  lazy val voteStore = new VoteStoreImpl(db)
  lazy val voterStore = new VoterStoreImpl(db)

  lazy val electionService = new ElectionServiceImpl(electionStore)
  lazy val voterService = new VoterServiceImpl(electionStore, voterStore)
  lazy val voteService = new VoteServiceImpl(electionStore, voteStore, voterStore, electionProcessorProvider)

  lazy val electionProcessorStore = system.actorOf(ActiveElectionStore.props)
  lazy val processorProps: String => Props = AvlTreeVoteProcessor.props

  lazy val electionProcessorProvider = new ElectionProcessorProviderImpl(processorProps, electionProcessorStore)
}
