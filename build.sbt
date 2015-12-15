import sbtrelease.ReleasePlugin

val commonSettings = Seq(
  scalacOptions += "-target:jvm-1.6",
  javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
)

lazy val core = project.in(file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "ssl-config-core",
    libraryDependencies ++= Dependencies.sslConfigCore
  ).enablePlugins(ReleasePlugin)

lazy val wsAkka = project.in(file("ws-akka"))
  .dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "ssl-config-akka",
    libraryDependencies ++= Dependencies.sslConfigAkka
  ).enablePlugins(ReleasePlugin)

lazy val wsPlay = project.in(file("ws-play"))
  .dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "ssl-config-play",
    libraryDependencies ++= Seq()
  ).enablePlugins(ReleasePlugin)

lazy val root = project.in(file("."))
  .aggregate(core, wsAkka, wsPlay)
