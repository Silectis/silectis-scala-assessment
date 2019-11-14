package com.silectis.sql.audit

import com.twitter.finagle.http.{Message, Request, Response}
import com.twitter.finagle.{Service, http}
import com.twitter.util.Future
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{JsString, Json}

import scala.util.{Failure, Success}

/**
  * Simple HTTP service that parses the body of the request as SQL and audits column access
  *
  * @author jlouns
  */
class HttpQueryAuditService(auditor: QueryAuditor)
  extends Service[http.Request, http.Response]
    with LazyLogging {
  /**
    * Parse the request body as SQL and return a JSON response with the audit results
    * @param request http request with sql in the body
    * @return audit results
    */
  override def apply(request: Request): Future[Response] = {
    // attempt to audit column access, using the request content as the sql query text
    val sql = request.contentString
    logger.debug(s"Received request with content $sql")

    val response = auditor.audit(sql) match {
      case Success(value) =>
        // success => return the result as json
        logger.debug("Successfully parsed requested content")

        val response = http.Response(request.version, http.Status.Ok)

        response.contentString = Json.prettyPrint(Json.toJson(value))

        response
      case Failure(ex) =>
        // failure => return exception
        logger.debug("Exception auditing query", ex)

        val response = http.Response(request.version, http.Status.BadRequest)

        // define a simple json response that shows the error
        val errorObject = Json.obj("error" -> JsString(ex.toString))
        response.contentString = Json.prettyPrint(errorObject)

        response
    }

    response.contentType = Message.ContentTypeJson

    // return the response
    Future.value(response)
  }
}
