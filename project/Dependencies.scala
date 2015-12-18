import sbt._

object Version {
  val typesafeConfig = "1.2.0" // last Java 6 version of it
  //  val slf4j          = "1.7.13"
  val jodaTime       = "2.9.1"
  val akkaActor      = "2.3.12"

  val scalaTest      = "2.2.4"
  val specs          = "3.6.6"
}

object Library {
  val typesafeConfig  = "com.typesafe"           % "config"                         % Version.typesafeConfig
  //  val slf4j           = "org.slf4j"              % "slf4j-api"                      % Version.slf4j
  //  val slf4jSimple     = "org.slf4j"              % "slf4j-simple"                   % Version.slf4j
  val jodaTime        = "joda-time"              % "joda-time"                      % Version.jodaTime // TODO remove from core!

  val akkaActor       = "com.typesafe.akka"     %% "akka-actor"                     % Version.akkaActor % "provided"

  val specsCore         = "org.specs2"            %% "specs2-core"                    % Version.specs
  val specsJunit        = "org.specs2"            %% "specs2-junit"                   % Version.specs
  val specsMock         = "org.specs2"            %% "specs2-mock"                    % Version.specs
  val specsMatcherExtra = "org.specs2"            %% "specs2-matcher-extra"           % Version.specs

  val scalaTest       = "org.scalatest"         %% "scalatest"                      % Version.scalaTest // not used at this moment
}

object Dependencies {
  import Library._

  val testing = Seq(specsCore, specsJunit, specsMock, specsMatcherExtra)
  val sslConfigCore = Seq(typesafeConfig, jodaTime) ++ testing
  val sslConfigAkka = Seq(akkaActor)
  val sslConfigPlay = Seq.empty[ModuleID]
}