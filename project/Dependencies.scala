import sbt._

object Version {
  val typesafeConfig = "1.4.2"

  val jodaTime       = "2.10.10"
  val jodaTimeConvert = "2.2.1"

  val specs2          = "4.8.3"

  val scala211       = "2.11.12"
  val scala212       = "2.12.14"
  val scala213       = "2.13.6"
  val scala3         = "3.0.1"
}

object Library {
  val typesafeConfig  = "com.typesafe"           % "config"                     % Version.typesafeConfig // Apache2

  // TESTS
  val specs2 = Seq(
  "org.specs2" %% "specs2-core"          % Version.specs2 % Test cross CrossVersion.for3Use2_13,
  "org.specs2" %% "specs2-junit"         % Version.specs2 % Test cross CrossVersion.for3Use2_13,
  "org.specs2" %% "specs2-mock"          % Version.specs2 % Test cross CrossVersion.for3Use2_13,
  "org.specs2" %% "specs2-matcher-extra" % Version.specs2 % Test cross CrossVersion.for3Use2_13,
  )

  val jodaTime          = "joda-time"              % "joda-time"                % Version.jodaTime  % Test // ONLY FOR TESTS!
  val jodaTimeConvert   = "org.joda"               % "joda-convert"             % Version.jodaTimeConvert  % Test // ONLY FOR TESTS!

}

object Dependencies {
  import Library._

  val sslConfigCore = Seq(typesafeConfig)
  val testDependencies = Library.specs2 ++ Seq(jodaTime, jodaTimeConvert)
}
