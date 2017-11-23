package org.ergoplatform.board.protocol

import scala.util.control.NoStackTrace

class ApiError(val msg: String, val statusCode: Int = 500)
  extends RuntimeException(msg)
  with NoStackTrace
