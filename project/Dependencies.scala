import sbt._

object Version {
  val typesafeConfig = "1.2.0" // last Java 6 version of it

  val jodaTime       = "2.9.9"
  val jodaTimeConvert = "1.9.2"

  val akkaActor      = "2.4.20"
  val akkaActor210   = "2.3.16"

  val parserCombinators = "1.0.6"

  val specs          = "3.8.9"

  val scala210       = "2.10.6"
  val scala211       = "2.11.11"
  val scala212       = "2.12.3"
}

object Library {
  val typesafeConfig  = "com.typesafe"           % "config"                     % Version.typesafeConfig // Apache2

  val akkaActor       = "com.typesafe.akka"     %% "akka-actor"                 % Version.akkaActor % "provided" // Apache2
  val akkaActor210    = "com.typesafe.akka"     %% "akka-actor"                 % Version.akkaActor210 % "provided" // Apache2

  // TESTS
  val specsCore         = "org.specs2"            %% "specs2-core"              % Version.specs % "test"
  val specsJunit        = "org.specs2"            %% "specs2-junit"             % Version.specs % "test"
  val specsMock         = "org.specs2"            %% "specs2-mock"              % Version.specs % "test"
  val specsMatcherExtra = "org.specs2"            %% "specs2-matcher-extra"     % Version.specs % "test"

  val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.parserCombinators

  val jodaTime          = "joda-time"              % "joda-time"                % Version.jodaTime  % "test" // ONLY FOR TESTS!
  val jodaTimeConvert   = "org.joda"               % "joda-convert"             % Version.jodaTimeConvert  % "test" // ONLY FOR TESTS!
}

object Dependencies {
  import Library._

  val sslConfigCore = Seq(typesafeConfig) 
  val sslConfigAkka = Seq(akkaActor)
  val sslConfigAkka210 = Seq(akkaActor210)
  val testDependencies = Seq(specsCore, specsJunit, specsMock, specsMatcherExtra, jodaTime, jodaTimeConvert)
}
