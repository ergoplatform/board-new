package org.ergoplatform.board


/**
  * Only dummy classes for swagger modeling.
  */
package object protocol {

  case class BooleanResultResponse(result: Boolean)

  case class ApiErrorResponse(msg: String, statusCode: Int = 500)

}
