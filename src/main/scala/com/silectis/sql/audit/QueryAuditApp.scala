package com.silectis.sql.audit

import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Await
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.bridge.SLF4JBridgeHandler

/**
  * Simple application that runs an HTTP server to audit columns accessed by SQL queries
  */
class QueryAuditApp extends LazyLogging {

  /**
    * Exposed for testing. Starts up a server but does not wait for it to complete.
    */
  private[audit] def run(auditor: QueryAuditor, port: String): ListeningServer = {
    logger.debug(s"Serving auditor at port $port")
    Http.serve(s":$port", new HttpQueryAuditService(auditor))
  }

  /**
    * Main entry point. Starts the server and blocks waiting for it to complete.
    */
  def main(args: Array[String]): Unit = {
    // remove existing handlers attached to j.u.l root logger
    SLF4JBridgeHandler.removeHandlersForRootLogger()

    // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
    // the initialization phase of the application
    SLF4JBridgeHandler.install()

    val server = run(new QueryAuditor, "8080")

    logger.debug("Server started")

    Await.result(server)
  }
}
