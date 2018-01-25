package org.ergoplatform.board.services

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.ergoplatform.board.actors.ActiveElectionStore
import org.ergoplatform.board.actors.ActiveElectionStore.{Find, Found}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ElectionActorLookupServiceSpec extends TestKit(ActorSystem("proc-lookup-spec"))
  with FlatSpecLike
  with Matchers
  with BeforeAndAfterAll
  with Generators
  with FutureHelpers {

  implicit val timeout = Timeout(10 seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  /**
    * Just a mock for a processor actor. We don't need the real actor here. Just refs.
    */
  class DummyActorTheMeansNothing extends Actor {
    override def receive: Receive = {
      case "ping" => sender() ! "pong"
      case "hello" => sender() ! "world"
      case _ => "I DO NOT UNDERSTAND"
    }
  }

  def props: Props = Props(new DummyActorTheMeansNothing())


  it should "check existence correclty and get actors properly" in {
    val store = system.actorOf(ActiveElectionStore.props)
    val service = new ElectionActorLookupServiceImpl(props, store)

    val uuid1 = uuid

    service.exists(uuid1).await shouldBe false

    val ref = service.getById(uuid1).await

    service.exists(uuid1).await shouldBe true

    service.getById(uuid1).await shouldBe ref

    (store ? Find(uuid1)).mapTo[Found].map(_.result).await shouldBe Some(ref)
  }

  it should "correctly show getAll and remove ref from store" in {
    val store = system.actorOf(ActiveElectionStore.props)
    val service = new ElectionActorLookupServiceImpl(props, store)

    val uuid1 = uuid
    val uuid2 = uuid
    val uuid3 = uuid
    val uuid4 = uuid

    val all = List(uuid1, uuid2, uuid3, uuid4)

    service.getAll.await shouldBe empty

    service.getById(uuid1).await
    service.getById(uuid2).await
    service.getById(uuid3).await
    service.getById(uuid4).await

    service.getAll.await should contain theSameElementsAs all

    service.remove(uuid1).await
    service.getAll.await should contain allOf(uuid2, uuid3, uuid4)
  }
}
