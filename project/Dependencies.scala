import sbt._

object Version {
  val typesafeConfig = "1.4.5"

  val jodaTime        = "2.14.0"
  val jodaTimeConvert = "3.0.1"

  val specs2 = "4.23.0"

  val scala212 = "2.12.20"
  val scala213 = "2.13.17"
  val scala3   = "3.7.3"
}

object Library {
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig // Apache2

  // TESTS
  val specs2 = Seq(
    "org.specs2" %% "specs2-core"          % Version.specs2 % Test,
    "org.specs2" %% "specs2-junit"         % Version.specs2 % Test,
    "org.specs2" %% "specs2-matcher-extra" % Version.specs2 % Test,
  )

  val mockito = "org.mockito" % "mockito-core" % "4.11.0" % Test // ONLY FOR TESTS!

  val jodaTime        = "joda-time" % "joda-time"    % Version.jodaTime        % Test // ONLY FOR TESTS!
  val jodaTimeConvert = "org.joda"  % "joda-convert" % Version.jodaTimeConvert % Test // ONLY FOR TESTS!

}

object Dependencies {
  import Library._

  val sslConfigCore    = Seq(typesafeConfig)
  val testDependencies = Library.specs2 ++ Seq(mockito, jodaTime, jodaTimeConvert)
}
