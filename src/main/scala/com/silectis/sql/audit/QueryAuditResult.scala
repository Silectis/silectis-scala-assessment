package com.silectis.sql.audit

import com.silectis.sql.TableReference
import play.api.libs.json._

/**
  * A container for query audit results consisting of a table referenced by the query (if present)
  * and a list of columns in that table that are referenced by the query.
  * @author jlouns
  */
case class QueryAuditResult(table: Option[TableReference],
                            columns: Seq[String])

object QueryAuditResult {
  val Empty = QueryAuditResult(None, Nil)

  private implicit val writeTableReference: Format[TableReference] = new Format[TableReference] {
    override def writes(ref: TableReference): JsValue = {
      JsString(ref.schemaName.map(_ + ".").getOrElse("") + ref.tableName)
    }

    override def reads(json: JsValue): JsResult[TableReference] = {
      json.validate[String].flatMap { ref =>
        ref.split('.') match {
          case Array(schema, table) =>
            // qualified
            JsSuccess(TableReference(Some(schema), table))
          case Array(table) =>
            // unqualified
            JsSuccess(TableReference(None, table))
          case _ =>
            // empty or too many components
            JsError(s"""Invalid table reference "$ref"""")
        }
      }
    }
  }

  implicit val writes: Format[QueryAuditResult] = Json.format[QueryAuditResult]
}
