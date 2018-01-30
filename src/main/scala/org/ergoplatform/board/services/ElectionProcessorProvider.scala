package org.ergoplatform.board.services

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * This service will be responsible for storing active elections actor refs.
  * Cause restoring actor ref each time from events is a huge time waste.
  */
trait ElectionProcessorProvider {

  /**
    * Asking for actor ref for election processor by its id.
    * In case if actor already is up - it will be returned
    * In case if actor isn't active yet or died or been removed - it will create actor and store it
    */
  def getById(electionId: String): Future[ActorRef]

  /**
    * Check if we already has one for this id
    */
  def exists(electionId: String): Future[Boolean]

  /**
    * Get ids list of active election actor processors
    */
  def getAll: Future[List[String]]

  /**
    * Shutdown one by id
    */
  def remove(electionId: String): Future[Unit]

}

/**
  * props - Props from election processor actor
  * refStore - ActorRef to ActiveElectionStore actor
  */
class ElectionProcessorProviderImpl(props: String => Props, refStore: ActorRef)(implicit as: ActorSystem)
  extends ElectionProcessorProvider {

  import akka.pattern.ask
  import org.ergoplatform.board.actors.ActiveElectionStore._

  implicit val timeout = Timeout(10 seconds)

  implicit val ec: ExecutionContext = as.dispatcher

  override def exists(electionId: String): Future[Boolean] = (refStore ? Find(electionId))
    .mapTo[Found].map(_.result).flatMap {
      case Some(_) => Future.successful(true)
      case None => Future.successful(false)
    }

  override def getById(electionId: String): Future[ActorRef] = (refStore ? Find(electionId))
    .mapTo[Found].map(_.result).flatMap {
      case Some(ref) => Future.successful(ref)
      case None =>
        val createdRef = as.actorOf(props.apply(electionId), electionId)
        (refStore ? Add(electionId, createdRef)).mapTo[Ok.type].map { _ => createdRef }
    }

  override def getAll: Future[List[String]] = (refStore ? ShowMeWhatYouGot).mapTo[CurrentIds].map(_.list)

  override def remove(electionId: String): Future[Unit] = (refStore ? Remove(electionId)).mapTo[Ok.type].map(_ => ())

}
