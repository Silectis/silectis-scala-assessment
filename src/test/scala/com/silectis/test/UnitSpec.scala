package com.silectis.test

import org.scalatest._

/**
  * Base class for all synchronous unit tests
  */
abstract class UnitSpec
  extends FlatSpec
    with Matchers
    with OptionValues
    with Inside
    with Inspectors
