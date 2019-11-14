package com.silectis.sql.audit

import com.silectis.test.AsyncUnitSpec
import com.twitter.finagle.{Http, http}
import com.twitter.util.Time
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.BeforeAndAfterEach

import scala.util.Success

/**
  * @author jlouns
  */
class QueryAuditAppSpec
  extends AsyncUnitSpec
    with AsyncMockFactory
    with BeforeAndAfterEach {
  private val port = "8081"

  private var app: QueryAuditApp = _

  private var auditor: QueryAuditor = _

  override protected def beforeEach(): Unit = {
    auditor = mock[QueryAuditor]

    app = new QueryAuditApp
  }

  it should "start a server that responds to audit requests" in {
    val sql = "select 1"

    (auditor.audit _)
      .expects(sql)
      .returning(Success(QueryAuditResult.Empty))

    val server = app.run(auditor, port)

    val client = Http.client.newService(s"localhost:$port")

    firstComplete {
      val req = http.Request()
      req.contentString = sql
      client(req)
        .map { response =>
          response.status shouldBe http.Status.Ok
        }
    } andLastly {
      client.close(Time.Top)
        .before(server.close(Time.Top))
    }
  }
}
