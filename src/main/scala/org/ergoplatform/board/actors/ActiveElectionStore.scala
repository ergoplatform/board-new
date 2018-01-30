package org.ergoplatform.board.actors

import akka.actor.{Actor, ActorRef, Props}

/**
  * Little actor that helps to store active processors refs online.
  * Cause restoring processors from events is a long operation.
  * Could be considered as a ref's inmemory db that can operate within multithread environment
  */
class ActiveElectionStore extends Actor {
  import ActiveElectionStore._

  var activeProcessors: Map[String, ActorRef] = Map.empty

  override def receive: Receive = {
    case Add(electionId, ref ) =>
      activeProcessors = activeProcessors.updated(electionId, ref)
      sender() ! Ok
    case Find(electionId) =>
      val result = activeProcessors.get(electionId)
      sender() ! Found(result)
    case Remove(electionId) =>
      activeProcessors = activeProcessors.filterKeys(_ != electionId)
      sender() ! Ok
    case ShowMeWhatYouGot =>
      val list = activeProcessors.keys.toList
      sender() ! CurrentIds(list)
  }
}

object ActiveElectionStore {

  case class Add(electionId: String, ref: ActorRef)

  case class Find(electionId: String)

  case class Found(result: Option[ActorRef])

  case class Remove(electionId: String)

  case class CurrentIds(list: List[String])

  case object ShowMeWhatYouGot

  case object Ok

  def props: Props = Props(new ActiveElectionStore())

}
