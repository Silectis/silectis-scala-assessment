package com.silectis.sql.audit

import com.silectis.sql.parse.SqlParser
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

class QueryAuditor extends LazyLogging {
  private val parser = new SqlParser

  def audit(sql: String): Try[QueryAuditResult] = {
    Try(parser.parse(sql))
      .map { query =>
        logger.debug(s"""Parsed query "$sql" as $query""")

        // TODO determine which columns are accessed by the query

        throw new NotImplementedError
      }
  }
}
