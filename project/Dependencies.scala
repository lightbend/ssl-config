import sbt._

object Version {
  val typesafeConfig = "1.4.1"

  val jodaTime       = "2.10.10"
  val jodaTimeConvert = "2.2.1"

  // Because of https://github.com/sbt/sbt/issues/4609
  val parserCombinators211 = "1.1.2"
  val parserCombinators = "1.1.2"
  val specs2          = "4.8.3"

  val scala211       = "2.11.12"
  val scala212       = "2.12.10"
  val scala213       = "2.13.6"
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

  val jodaTime          = "joda-time"              % "joda-time"                % Version.jodaTime  % "test" // ONLY FOR TESTS!
  val jodaTimeConvert   = "org.joda"               % "joda-convert"             % Version.jodaTimeConvert  % "test" // ONLY FOR TESTS!

  def parserCombinators(scalaVersion: String) = if (scalaVersion.equals(Version.scala211)) {
    "org.scala-lang.modules" %% "scala-parser-combinators" % Version.parserCombinators211
  } else {
    "org.scala-lang.modules" %% "scala-parser-combinators" % Version.parserCombinators
  }
}

object Dependencies {
  import Library._

  val sslConfigCore = Seq(typesafeConfig)
  val testDependencies = Library.specs2 ++ Seq(jodaTime, jodaTimeConvert)
}
