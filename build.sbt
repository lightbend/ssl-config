lazy val root = project.in(file("."))
  .settings(
    libraryDependencies ++= Dependencies.sslConfig,
    scalacOptions += "-target:jvm-1.6",
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
  ).enablePlugins(ReleasePlugin)