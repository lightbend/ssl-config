import sbt._

object Version {
  val typesafeConfig = "1.2.0" // last Java 6 version of it

  val jodaTime       = "2.9.9"
  val jodaTimeConvert = "1.9.2"

  val akkaActor      = "2.4.20"
  val akkaActor210   = "2.3.16"

  val parserCombinators = "1.1.1"

  val specs2Scala210  = "3.8.9"
  val specs2          = "4.3.0"

  val scala210       = "2.10.7"
  val scala211       = "2.11.12"
  val scala212       = "2.12.6"
  val scala213       = "2.13.0-M4"
}

object Library {
  val typesafeConfig  = "com.typesafe"           % "config"                     % Version.typesafeConfig // Apache2

  val akkaActor       = "com.typesafe.akka"     %% "akka-actor"                 % Version.akkaActor % "provided" // Apache2
  val akkaActor210    = "com.typesafe.akka"     %% "akka-actor"                 % Version.akkaActor210 % "provided" // Apache2

  // TESTS
  val specs2 = Seq(
  "org.specs2" %% "specs2-core"          % Version.specs2 % "test",
  "org.specs2" %% "specs2-junit"         % Version.specs2 % "test",
  "org.specs2" %% "specs2-mock"          % Version.specs2 % "test",
  "org.specs2" %% "specs2-matcher-extra" % Version.specs2 % "test"
  )

  val specs2Scala210 = Seq(
    "org.specs2" %% "specs2-core"          % Version.specs2Scala210 % "test",
    "org.specs2" %% "specs2-junit"         % Version.specs2Scala210 % "test",
    "org.specs2" %% "specs2-mock"          % Version.specs2Scala210 % "test",
    "org.specs2" %% "specs2-matcher-extra" % Version.specs2Scala210 % "test"
  )

  val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.parserCombinators

  val jodaTime          = "joda-time"              % "joda-time"                % Version.jodaTime  % "test" // ONLY FOR TESTS!
  val jodaTimeConvert   = "org.joda"               % "joda-convert"             % Version.jodaTimeConvert  % "test" // ONLY FOR TESTS!
}

object Dependencies {
  import Library._

  val sslConfigCore = Seq(typesafeConfig)
  val sslConfigAkka = Seq(akkaActor)
  val sslConfigAkka210 = Seq(akkaActor210)
  val testDependencies210 = Library.specs2Scala210 ++ Seq(jodaTime, jodaTimeConvert)

  val testDependencies = Library.specs2 ++ Seq(jodaTime, jodaTimeConvert)
}
