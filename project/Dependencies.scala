import sbt._

object Version {
  val typesafeConfig = "1.2.0" // last Java 6 version of it
  val scalaTest      = "2.2.4"
}

object Library {
  val typesafeConfig  = "com.typesafe"           % "config"                         % Version.typesafeConfig
  val scalaTest       = "org.scalatest"         %% "scalatest"                      % Version.scalaTest
}

object Dependencies {
  import Library._

  val testing = Seq(scalaTest)
  val sslConfig = Seq(typesafeConfig) ++ testing
}