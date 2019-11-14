name := "silectis-scala-assessment"
organization := "com.silectis"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.7"

import Dependencies._

libraryDependencies ++= Seq(
  scalaParserCombinators,

  scalaLogging,
  slf4j.api,
  slf4j.julBridge,

  playJson,

  finagle.http,

  slf4j.log4j % "runtime",

  scalatest % "test",
  scalamock % "test"
)

// fork both the tests and run task
fork := true
