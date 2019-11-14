package com.silectis.sql

/**
  * @author dkotsiko
  */
// scalastyle:off null
class SqlException(message: String = null, cause: Throwable = null)
  extends RuntimeException(message, cause)
