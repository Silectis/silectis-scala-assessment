/*
 * Copyright (C) Silectis, Inc - All Rights Reserved.
 *
 *  All information contained herein is, and remains the property of Silectis, Inc. The intellectual
 *  and technical concepts contained herein are proprietary to Silectis, Inc and are protected by
 *  trade secret or copyright law. Dissemination of this information or reproduction of this
 *  material is strictly forbidden unless prior written permission is obtained from Silectis, Inc.
 */

package com.silectis.test

import com.twitter.util.{Future, Promise, Return, Throw}

import scala.util.{Failure, Success, Try}

/**
  * @author jlouns
  */
trait FirstCompleteAndLastly {

  /**
    * Registers a block of code that produces any <code>Future[T]</code>,
    * returning an object that offers an <code>andLastly</code> method to execute async cleanup code.
    *
    * <p>
    * Compare to [[org.scalatest.CompleteLastly#complete]] which does not wait for the
    * <code>lastly</code> block to finish execution if it produces a <code>Future</code>
    * </p>
    *
    * @param completeBlock code to execute, after which an andLastly block will
    *                      be executed regardless of the return status
    */
  def firstComplete[T](completeBlock: => Future[T]): ResultOfFirstCompleteInvocation[T] =
    new ResultOfFirstCompleteInvocation[T](completeBlock)
}

class ResultOfFirstCompleteInvocation[T](completeBlock: => Future[T]) {
  /**
    * Registers cleanup code to be executed immediately if the future-producing code passed
    * to <code>firstComplete</code> throws an exception, or otherwise asynchronously, when the future
    * returned by the code passed to <code>firstComplete</code> itself completes.
    *
    * <p>
    *   The returned <code>Future</code> waits until the <code>lastlyBlock</code> finishes, then
    *   returns the result of the <code>firstComplete</code> block, unless the lastlyBlock resolves
    *   with an error.
    * </p>
    *
    * @param lastlyBlock cleanup code to execute whether the code passed to
    *                    <code>firstComplete</code> throws an exception or succesfully returns a
    *                    futuristic value.
    */
  def andLastly(lastlyBlock: => Future[Any]): Future[T] = {
    val p = Promise[T]()

    def executeLastlyOnFailure(ex: Throwable): Unit = Try(lastlyBlock) match {
      // success creating the lastly future
      case Success(lastlyFuture) => lastlyFuture respond {
        _ => p.setException(ex)
      }
      // error creating the lastly future
      case Failure(_) => p.setException(ex)
    }

    // evaluate the by name parameter
    Try(completeBlock) match {
      // success creating the future
      case Success(f) => f respond {
        // success executing the future
        // execute the lastly block
        case Return(v) => Try(lastlyBlock) match {
          // success creating the lastly future
          case Success(lastlyFuture) => lastlyFuture respond {
            // success executing the lastly block, complete with the original return value
            case Return(_) => p.setValue(v)
            // error executing the lastly block, complete with the error
            case Throw(ex) => p.setException(ex)
          }
          // error creating the lastly future, complete with the error
          case Failure(ex) => p.setException(ex)
        }
        // error executing the future
        // execute the lastly block and complete with the original error
        case Throw(ex) => executeLastlyOnFailure(ex)
      }
      // failure creating the future, execute lastly and complete with error
      case Failure(ex) => executeLastlyOnFailure(ex)
    }

    p
  }
}

object FirstCompleteAndLastly extends FirstCompleteAndLastly
