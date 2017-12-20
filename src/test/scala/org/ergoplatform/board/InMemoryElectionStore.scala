package org.ergoplatform.board

import org.ergoplatform.board.models.ElectionRecord
import org.ergoplatform.board.stores.ElectionStore

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

class InMemoryElectionStore extends ElectionStore with FutureHelpers {

  val data = new TrieMap[String, ElectionRecord]()

  def create(rec: ElectionRecord): Future[ElectionRecord] = {
    data.put(rec._id, rec)
    rec.asFut
  }

  def find(id: String): Future[Option[ElectionRecord]] = data.get(id).asFut

  def get(id: String): Future[ElectionRecord] = data(id).asFut

  def exists(id: String): Future[Boolean] = data.exists(_._1 == id).asFut

  def extend(id: String, extendFor: Long): Future[ElectionRecord] = {
    data.get(id).map(e => e.copy(end = e.end + extendFor)).foreach { e =>
      data.update(id, e)
    }
    data(id).asFut
  }
}
