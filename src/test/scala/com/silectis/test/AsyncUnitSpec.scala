package com.silectis.test

import org.scalatest._

/**
  * Base class for all async unit tests
  */
abstract class AsyncUnitSpec
  extends AsyncFlatSpec
    with Matchers
    with OptionValues
    with Inside
    with Inspectors
    with FutureConversions
