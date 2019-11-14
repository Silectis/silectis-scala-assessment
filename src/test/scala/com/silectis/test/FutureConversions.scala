/*
 * Copyright (C) Silectis, Inc - All Rights Reserved.
 *
 *  All information contained herein is, and remains the property of Silectis, Inc. The intellectual
 *  and technical concepts contained herein are proprietary to Silectis, Inc and are protected by
 *  trade secret or copyright law. Dissemination of this information or reproduction of this
 *  material is strictly forbidden unless prior written permission is obtained from Silectis, Inc.
 */

package com.silectis.test

import com.twitter.util.{Future, Return, Throw}

import scala.language.implicitConversions

/**
  * @author jlouns
  */
trait FutureConversions extends FirstCompleteAndLastly {
  implicit def twitterFutureToScalaFuture[A](f: Future[A]): scala.concurrent.Future[A] = {
    val promise: scala.concurrent.Promise[A] = scala.concurrent.Promise()
    f.respond {
      case Return(value) => promise.success(value)
      case Throw(exception) => promise.failure(exception)
    }
    promise.future
  }
}
