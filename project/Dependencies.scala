import sbt._

object Version {
  val typesafeConfig = "1.4.2"

  val jodaTime = "2.12.5"
  val jodaTimeConvert = "2.2.3"

  val specs2 = "4.20.0"

  val scala212 = "2.12.18"
  val scala213 = "2.13.11"
  val scala3 = "3.3.0"
}

object Library {
  val typesafeConfig = "com.typesafe" % "config" % Version.typesafeConfig // Apache2

  // TESTS
  val specs2: Seq[ModuleID] = Seq(
    "org.specs2" %% "specs2-core" % Version.specs2 % Test cross CrossVersion.for3Use2_13,
    "org.specs2" %% "specs2-junit" % Version.specs2 % Test cross CrossVersion.for3Use2_13,
    "org.specs2" %% "specs2-mock" % Version.specs2 % Test cross CrossVersion.for3Use2_13,
    "org.specs2" %% "specs2-matcher-extra" % Version.specs2 % Test cross CrossVersion.for3Use2_13,
  )

  val jodaTime = "joda-time" % "joda-time" % Version.jodaTime % Test // ONLY FOR TESTS!
  val jodaTimeConvert = "org.joda" % "joda-convert" % Version.jodaTimeConvert % Test // ONLY FOR TESTS!

}

object Dependencies {

  import Library._

  val sslConfigCore: Seq[ModuleID] = Seq(typesafeConfig)
  val testDependencies: Seq[sbt.ModuleID] = Library.specs2 ++ Seq(jodaTime, jodaTimeConvert)
}
