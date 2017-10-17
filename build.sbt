import com.typesafe.sbt.osgi.SbtOsgi
import com.typesafe.sbt.osgi.SbtOsgi.autoImport._
import sbtrelease.ReleasePlugin
import com.typesafe.sbt.pgp.PgpKeys.publishSigned

val commonSettings = Seq(
  scalaVersion := Version.scala212,
  crossScalaVersions := Seq(Version.scala213, Version.scala212, Version.scala211, Version.scala210),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

val dontPublishSettings = Seq(
   publishSigned := (),
   publish := ()
 )

lazy val sslConfigCore = project.in(file("ssl-config-core"))
  .settings(commonSettings: _*)
  .settings(osgiSettings: _*)
  .settings(
    name := "ssl-config-core",
    libraryDependencies ++= Dependencies.sslConfigCore,
    libraryDependencies ++= (
      scalaBinaryVersion.value match {
        case "2.10" => Seq.empty[ModuleID]
        case _      => Seq(Library.parserCombinators)
      }),
    libraryDependencies ++= (
      scalaBinaryVersion.value match {
        case "2.10" => Dependencies.testDependencies210
        case _ => Dependencies.testDependencies
      }
    ),
    OsgiKeys.bundleSymbolicName := s"${organization.value}.sslconfig",
    OsgiKeys.exportPackage := Seq(s"com.typesafe.sslconfig.*;version=${version.value}"),
    OsgiKeys.importPackage := Seq("!sun.misc", "!sun.security.*", configImport(), "*")
  ).enablePlugins(ReleasePlugin, SbtOsgi)

lazy val documentation = project.in(file("documentation"))
  .settings(dontPublishSettings: _*)

lazy val sslConfigAkka = project.in(file("ssl-config-akka"))
  .dependsOn(sslConfigCore)
  .settings(commonSettings: _*)
  .settings(osgiSettings: _*)
  .settings(
    name := "ssl-config-akka",
    // Not building to Scala 2.13 to avoid circular dependency with Akka
    crossScalaVersions := Seq(Version.scala212, Version.scala211, Version.scala210),
    libraryDependencies ++= (
      scalaBinaryVersion.value match {
        case "2.10" => Dependencies.sslConfigAkka210
        case _      => Dependencies.sslConfigAkka
      }),
    OsgiKeys.bundleSymbolicName := s"${organization.value}.sslconfig.akka",
    OsgiKeys.exportPackage := Seq(s"com.typesafe.sslconfig.akka.*;version=${version.value}"),
    OsgiKeys.requireBundle := Seq(s"""com.typesafe.sslconfig;bundle-version="${version.value}"""")
  ).enablePlugins(ReleasePlugin, SbtOsgi)

lazy val root = project.in(file("."))
  .aggregate(
    sslConfigCore,
    sslConfigAkka,
    documentation
  )
  .settings(dontPublishSettings: _*)
  .enablePlugins(CrossPerProjectPlugin)


// JDK6: 1.2.0, Akka 2.4: 1.3.0
def configImport(packageName: String = "com.typesafe.config.*") = versionedImport(packageName, "1.2.0", "1.4.0")
def versionedImport(packageName: String, lower: String, upper: String) = s"""$packageName;version="[$lower,$upper)""""