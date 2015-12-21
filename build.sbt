import sbtrelease.ReleasePlugin

val commonSettings = Seq(
  scalacOptions += "-target:jvm-1.6",
  javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
)

lazy val sslConfigCore = project.in(file("ssl-config-core"))
  .settings(commonSettings: _*)
  .settings(
    name := "ssl-config-core",
    libraryDependencies ++= Dependencies.sslConfigCore,
    libraryDependencies ++= (
      scalaBinaryVersion.value match {
        case "2.10" => Seq.empty[ModuleID]
        case _      => Seq(Library.parserCombinators)
      })
  ).enablePlugins(ReleasePlugin)

lazy val documentation = project.in(file("documentation"))

lazy val sslConfigAkka = project.in(file("ssl-config-akka"))
  .dependsOn(sslConfigCore)
  .settings(commonSettings: _*)
  .settings(
    name := "ssl-config-akka",
    libraryDependencies ++= Dependencies.sslConfigAkka
  ).enablePlugins(ReleasePlugin)

lazy val sslConfigPlay = project.in(file("ssl-config-play"))
  .dependsOn(sslConfigCore)
  .settings(commonSettings: _*)
  .settings(
    name := "ssl-config-play",
    libraryDependencies ++= Dependencies.sslConfigPlay
  ).enablePlugins(ReleasePlugin)

lazy val root = project.in(file("."))
  .aggregate(sslConfigCore, sslConfigAkka, sslConfigPlay, documentation)
