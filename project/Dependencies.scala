import sbt._

object Version {
  val typesafeConfig = "1.3.3"

  val jodaTime       = "2.10.1"
  val jodaTimeConvert = "2.2.0"

  val parserCombinators = "1.1.2"
  val specs2          = "4.5.1"

  val scala211       = "2.11.12"
  val scala212       = "2.12.8"
  val scala213       = "2.13.0-RC1"
}

object Library {
  val typesafeConfig  = "com.typesafe"           % "config"                     % Version.typesafeConfig // Apache2

  // TESTS
  val specs2 = Seq(
  "org.specs2" %% "specs2-core"          % Version.specs2 % "test",
  "org.specs2" %% "specs2-junit"         % Version.specs2 % "test",
  "org.specs2" %% "specs2-mock"          % Version.specs2 % "test",
  "org.specs2" %% "specs2-matcher-extra" % Version.specs2 % "test"
  )

  val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.parserCombinators

  val jodaTime          = "joda-time"              % "joda-time"                % Version.jodaTime  % "test" // ONLY FOR TESTS!
  val jodaTimeConvert   = "org.joda"               % "joda-convert"             % Version.jodaTimeConvert  % "test" // ONLY FOR TESTS!
}

object Dependencies {
  import Library._

  val sslConfigCore = Seq(typesafeConfig)
  val testDependencies = Library.specs2 ++ Seq(jodaTime, jodaTimeConvert)
}
