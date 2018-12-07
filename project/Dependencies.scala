import sbt._

object Version {
  val typesafeConfig = "1.3.3"

  val jodaTime       = "2.10.1"
  val jodaTimeConvert = "1.9.2"

  val parserCombinators = "1.1.1"
  val parserCombinators213M3 = "1.1.0"

  val specs2Scala210  = "3.8.9"
  val specs2Scala213M3 = "4.2.0"
  val specs2          = "4.3.5"

  val scala211       = "2.11.12"
  val scala212       = "2.12.6"
  val scala213M3     = "2.13.0-M3"
  val scala213       = "2.13.0-M5"
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

  val specs2Scala213M3 = Seq(
    "org.specs2" %% "specs2-core"          % Version.specs2Scala213M3 % "test",
    "org.specs2" %% "specs2-junit"         % Version.specs2Scala213M3 % "test",
    "org.specs2" %% "specs2-mock"          % Version.specs2Scala213M3 % "test",
    "org.specs2" %% "specs2-matcher-extra" % Version.specs2Scala213M3 % "test"
  )

  val specs2Scala210 = Seq(
    "org.specs2" %% "specs2-core"          % Version.specs2Scala210 % "test",
    "org.specs2" %% "specs2-junit"         % Version.specs2Scala210 % "test",
    "org.specs2" %% "specs2-mock"          % Version.specs2Scala210 % "test",
    "org.specs2" %% "specs2-matcher-extra" % Version.specs2Scala210 % "test"
  )

  val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.parserCombinators
  val parserCombinators213M3 = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.parserCombinators213M3

  val jodaTime          = "joda-time"              % "joda-time"                % Version.jodaTime  % "test" // ONLY FOR TESTS!
  val jodaTimeConvert   = "org.joda"               % "joda-convert"             % Version.jodaTimeConvert  % "test" // ONLY FOR TESTS!
}

object Dependencies {
  import Library._

  val sslConfigCore = Seq(typesafeConfig)
  val testDependencies210 = Library.specs2Scala210 ++ Seq(jodaTime, jodaTimeConvert)
  val testDependencies213M3 = Library.specs2Scala213M3 ++ Seq(jodaTime, jodaTimeConvert)

  val testDependencies = Library.specs2 ++ Seq(jodaTime, jodaTimeConvert)
}
