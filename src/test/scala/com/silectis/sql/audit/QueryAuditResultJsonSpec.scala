package com.silectis.sql.audit

import com.silectis.sql.TableReference
import com.silectis.test.UnitSpec
import play.api.libs.json.Json

/**
  * @author jlouns
  */
class QueryAuditResultJsonSpec extends UnitSpec {
  private def writeAndCompare(result: QueryAuditResult) = {
    // convert to json then back to the query result to make sure the json writer is accurate
    val reparsed = Json.toJson(result).as[QueryAuditResult]

    // converting to json and back should not have changed the query result
    reparsed shouldBe result
  }

  it should "parse and write an empty query audit result" in {
    val json =
      """
        |{
        |  "columns": []
        |}
        |""".stripMargin
    val parsed = Json.parse(json).as[QueryAuditResult]

    parsed.table shouldBe empty
    parsed.columns shouldBe empty

    writeAndCompare(parsed)
  }

  it should "parse and write a query audit result with an unqualified table" in {
    val json =
      """
        |{
        |  "table": "some_table",
        |  "columns": ["column_a", "column_b"]
        |}
        |""".stripMargin
    val parsed = Json.parse(json).as[QueryAuditResult]

    parsed.table.value shouldBe TableReference(None, "some_table")
    parsed.columns should contain only ("column_a", "column_b")

    writeAndCompare(parsed)
  }

  it should "parse and write a query audit result with a qualified table" in {
    val json =
      """
        |{
        |  "table": "some_schema.some_table",
        |  "columns": []
        |}
        |""".stripMargin
    val parsed = Json.parse(json).as[QueryAuditResult]

    parsed.table.value shouldBe TableReference(Some("some_schema"), "some_table")

    writeAndCompare(parsed)
  }

  it should "fail to parse a query audit result with an invalid reference" in {
    val json =
      """
        |{
        |  "table": "some_database.some_schema.some_table",
        |  "columns": []
        |}
        |""".stripMargin

    Json.parse(json).validate[QueryAuditResult].isError shouldBe true
  }
}
