import com.typesafe.sbt.osgi.SbtOsgi
import com.typesafe.sbt.osgi.SbtOsgi.autoImport._
import sbtrelease.ReleasePlugin
import com.typesafe.sbt.pgp.PgpKeys.publishSigned

val commonSettings = Seq(
  scalacOptions += "-target:jvm-1.6",
  javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
)

val dontPublishSettings = Seq(
   publishSigned := (),
   publish := ()
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
      }),
    osgiSettings,
    OsgiKeys.bundleSymbolicName := s"${organization.value}.sslconfig",
    OsgiKeys.exportPackage := Seq(s"com.typesafe.sslconfig.*;version=${version.value}"),
    OsgiKeys.requireBundle := (
      scalaBinaryVersion.value match {
        case "2.10" => Nil
        case _ => Seq(s"""org.scala-lang.modules.scala-parser-combinators;bundle-version="${Version.parserCombinators}"""")
      }
    )
  ).enablePlugins(ReleasePlugin, SbtOsgi)

lazy val documentation = project.in(file("documentation"))
  .settings(dontPublishSettings: _*)

lazy val sslConfigAkka = project.in(file("ssl-config-akka"))
  .dependsOn(sslConfigCore)
  .settings(commonSettings: _*)
  .settings(
    name := "ssl-config-akka",
    libraryDependencies ++= Dependencies.sslConfigAkka,
    osgiSettings,
    OsgiKeys.bundleSymbolicName := s"${organization.value}.sslconfig.akka",
    OsgiKeys.requireBundle := Seq(s"""com.typesafe.sslconfig;bundle-version="${version.value}""""),
    OsgiKeys.exportPackage := Seq("com.typesafe.sslconfig.akka.*")
  ).enablePlugins(ReleasePlugin, SbtOsgi)

//lazy val sslConfigPlay = project.in(file("ssl-config-play"))
//  .dependsOn(sslConfigCore)
//  .settings(commonSettings: _*)
//  .settings(
//    name := "ssl-config-play",
//    libraryDependencies ++= Dependencies.sslConfigPlay
//  ).enablePlugins(ReleasePlugin)

lazy val root = project.in(file("."))
  .aggregate(
    sslConfigCore,
    sslConfigAkka,
//    sslConfigPlay,
    documentation)
  .settings(dontPublishSettings: _*)
