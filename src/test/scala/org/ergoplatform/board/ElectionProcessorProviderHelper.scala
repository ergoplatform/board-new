package org.ergoplatform.board

import akka.actor.{ActorSystem, Props}
import org.ergoplatform.board.actors.ActiveElectionStore
import org.ergoplatform.board.persistence.AvlTreeVoteProcessor
import org.ergoplatform.board.services.ElectionProcessorProviderImpl

trait ElectionProcessorProviderHelper {

  implicit val system: ActorSystem

  lazy val realOneProps: String => Props = AvlTreeVoteProcessor.props
  lazy val refStore = system.actorOf(ActiveElectionStore.props)
  lazy val electionProcessorProvider = new ElectionProcessorProviderImpl(realOneProps, refStore)

}
