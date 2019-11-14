package com.silectis.sql.audit

import com.silectis.sql.TableReference
import com.silectis.test.AsyncUnitSpec
import com.twitter.finagle.http
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json

import scala.util.{Failure, Success}

/**
  * @author jlouns
  */
class HttpQueryAuditServiceSpec
  extends AsyncUnitSpec
    with AsyncMockFactory
    with BeforeAndAfterEach {

  private var service: HttpQueryAuditService = _

  private var auditor: QueryAuditor = _

  override protected def beforeEach(): Unit = {
    auditor = mock[QueryAuditor]

    service = new HttpQueryAuditService(auditor)
  }

  private def request(content: String) = {
    val req = http.Request()
    req.contentString = content
    service(req)
  }

  it should "respond to successful audits" in {
    val sql = "select test2 from test1"

    (auditor.audit _)
      .expects(sql)
      .returning(Success(QueryAuditResult(Some(TableReference(None, "test1")), Seq("test2"))))

    request(sql)
      .map { response =>
        response.status shouldBe http.Status.Ok
        val result = Json.parse(response.contentString).as[QueryAuditResult]
        result.columns should contain only "test2"
      }
  }

  it should "respond to failed audits" in {
    val sql = "select 1"

    (auditor.audit _)
      .expects(sql)
      .returning(Failure(new RuntimeException("some exception")))

    request(sql)
      .map { response =>
        response.status shouldBe http.Status.BadRequest
        val error = (Json.parse(response.contentString) \ "error").as[String]
        error should include ("some exception")
      }
  }
}
