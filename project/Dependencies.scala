import sbt._

object Version {
  val typesafeConfig = "1.2.0" // last Java 6 version of it
  val scalaTest      = "2.2.4"
  val slf4j          = "1.7.13"
  val jodaTime       = "2.9.1"
}

object Library {
  val typesafeConfig  = "com.typesafe"           % "config"                         % Version.typesafeConfig
  val scalaTest       = "org.scalatest"         %% "scalatest"                      % Version.scalaTest
  val slf4j           = "org.slf4j"              % "slf4j-api"                      % Version.slf4j
  val slf4jSimple     = "org.slf4j"              % "slf4j-simple"                   % Version.slf4j
  val jodaTime        = "joda-time"              % "joda-time"                      % Version.jodaTime
}

object Dependencies {
  import Library._

  val testing = Seq(scalaTest)
  val sslConfig = Seq(typesafeConfig, slf4j, jodaTime) ++ testing
}