import sbt._

object Version {
  val typesafeConfig = "1.2.0" // last Java 6 version of it
  val jodaTime       = "2.9.1"

  val parserCombinators = "1.0.4"

  val scalaTest      = "3.0.0"
  val specs          = "3.8.6"
}

object Library {
  val typesafeConfig  = "com.typesafe"           % "config"                     % Version.typesafeConfig // Apache2


  // TESTS

  val specsCore         = "org.specs2"            %% "specs2-core"              % Version.specs % "test"
  val specsJunit        = "org.specs2"            %% "specs2-junit"             % Version.specs % "test"
  val specsMock         = "org.specs2"            %% "specs2-mock"              % Version.specs % "test"
  val specsMatcherExtra = "org.specs2"            %% "specs2-matcher-extra"     % Version.specs % "test"

  val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.parserCombinators

  val jodaTime          = "joda-time"              % "joda-time"                % Version.jodaTime  % "test" // ONLY FOR TESTS!
}

object Dependencies {
  import Library._

  val testing = Seq(specsCore, specsJunit, specsMock, specsMatcherExtra, jodaTime)
  val sslConfigCore = Seq(typesafeConfig) ++ testing
  val sslConfigPlay = Seq.empty[ModuleID]
}
