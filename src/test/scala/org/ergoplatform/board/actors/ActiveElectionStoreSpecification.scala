package org.ergoplatform.board.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.ergoplatform.board.{FutureHelpers, Generators}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class ActiveElectionStoreSpecification extends TestKit(ActorSystem("proc-store-spec"))
  with FlatSpecLike
  with Matchers
  with BeforeAndAfterAll
  with Generators
  with ImplicitSender with FutureHelpers {

  import ActiveElectionStore._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "add refs to store and find them" in {
    val store = system.actorOf(ActiveElectionStore.props)
    val ref = self

    store ! Find(uuid)
    expectMsg(Found(None))

    val uuid1 = uuid

    store ! Add(uuid1, ref)
    expectMsg(Ok)

    store ! Find(uuid1)
    expectMsg(Found(Some(ref)))
  }

  it should "remove refs from store" in {
    val store = system.actorOf(ActiveElectionStore.props)
    val ref = self
    val uuid1 = uuid
    store ! Add(uuid1, ref)
    expectMsg(Ok)

    store ! Find(uuid1)
    expectMsg(Found(Some(ref)))

    store ! Remove(uuid1)
    expectMsg(Ok)

    store ! Find(uuid1)
    expectMsg(Found(None))
  }

  it should "show me what it got" in {
    val store = system.actorOf(ActiveElectionStore.props, "hey")
    val ref = self
    val uuids = List.fill(10)(uuid).sorted

    uuids.foreach { uuid =>
      store ! Add(uuid, ref)
      expectMsg(Ok)
    }

    store ! ShowMeWhatYouGot
    val list = receiveOne(5 seconds).asInstanceOf[CurrentIds].list

    list should contain theSameElementsAs uuids
  }
}
