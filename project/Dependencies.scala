import sbt._

// noinspection TypeAnnotation
// scalastyle:off public.methods.have.type
object Dependencies {
  // Versions

  val versions = new {
    val playJson = "2.7.4"
    val scalaLogging = "3.9.0"
    val scalaParserCombinators = "1.1.0"
    val scalamock = "3.6.0"
    val scalatest = "3.0.5"
    val slf4j = "1.7.25"
    val twitter = "19.10.0"
  }

  // Libraries

  val playJson = "com.typesafe.play" %% "play-json" % versions.playJson

  val slf4j = new {
    val api = "org.slf4j" % "slf4j-api" % versions.slf4j
    val log4j = "org.slf4j" % "slf4j-log4j12" % versions.slf4j
    val julBridge = "org.slf4j" % "jul-to-slf4j" % versions.slf4j
  }

  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % versions.scalaLogging

  val scalaParserCombinators =
    "org.scala-lang.modules" %% "scala-parser-combinators" % versions.scalaParserCombinators

  val scalamock = ("org.scalamock" %% "scalamock-scalatest-support" % versions.scalamock)
    .excludeAll("org.scalatest" %% "scalatest")

  val scalatest = "org.scalatest" %% "scalatest" % versions.scalatest

  val finagle = new {
    val http = "com.twitter" %% "finagle-http" % versions.twitter
  }

}
