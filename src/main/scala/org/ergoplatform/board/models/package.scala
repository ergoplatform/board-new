package org.ergoplatform.board

package object models {

  //TODO move all to a separate files

  case class ElectionId(id: Long) extends AnyVal

  case class SectionId(id: Long) extends AnyVal

  case class GroupId(id: Long) extends AnyVal

  //case class Election(id: ElectionId, start: Long, end: Long, keys: Keys, description: Option[String])

  case class VoteId(id: Long) extends AnyVal

  case class VoteRequest(electionId: ElectionId, sectionId: SectionId, groupId: GroupId, message: String, voterSign: SignedData)

  case class Vote(index: VoteId, electionId: ElectionId, sectionId: SectionId, groupId: GroupId, message: String, hash: String, signatures: Signatures)

  case class Signatures(serviceSign: SignedData, voterSign: SignedData)

  case class SignedData(pk: String, sign: String)



}
