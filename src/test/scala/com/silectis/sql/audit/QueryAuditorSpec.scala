package com.silectis.sql.audit

import com.silectis.sql.SqlException
import com.silectis.test.UnitSpec

/**
  * @author jlouns
  */
class QueryAuditorSpec extends UnitSpec {
  val auditor = new QueryAuditor

  it should "return failure for an invalid SQL query" in {
    a[SqlException] should be thrownBy
      auditor.audit("not valid sql").get
  }
}
