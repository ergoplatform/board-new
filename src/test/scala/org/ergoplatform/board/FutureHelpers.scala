package org.ergoplatform.board

import org.ergoplatform.board.models.MongoId

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

trait FutureHelpers {

  def notFound(id: MongoId) = new NoSuchElementException(s"Cannot find record with id = $id")

  implicit class TurnIntoFuture[A](a: A) {
    def asFut: Future[A] = Future.successful(a)
  }

  implicit class ThrowableToFut(e: Throwable) {
    def asFut[A]: Future[A] = Future.failed[A](e)
  }

  implicit class FutToSync[A](f: Future[A]) {
    def await: A = Await.result(f, 10 seconds)
  }

}
