lazy val root = project.in(file("."))
  .settings(
    libraryDependencies ++= Dependencies.sslConfig
  ).enablePlugins(ReleasePlugin)
