import com.typesafe.sbt.osgi.SbtOsgi
import com.typesafe.sbt.osgi.SbtOsgi.autoImport._
import sbtrelease.ReleasePlugin
import com.typesafe.sbt.pgp.PgpKeys.publishSigned
import com.typesafe.tools.mima.core.{DirectMissingMethodProblem, ProblemFilters}

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
  .settings(AutomaticModuleName.settings("ssl.config.core"))
  .settings(osgiSettings: _*)
  .settings(
    name := "ssl-config-core",
    mimaPreviousArtifacts ++= (scalaBinaryVersion.value match {
      case "2.13" => Set.empty[ModuleID]
      case _ => Set("com.typesafe" %% "ssl-config-core" % "0.2.3")
    }), // "sbt mimaReportBinaryIssues"
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
    OsgiKeys.importPackage := Seq("!sun.misc", "!sun.security.*", configImport(), "*"),
    OsgiKeys.requireCapability := """osgi.ee;filter:="(&(osgi.ee=JavaSE)(version>=1.8))"""",

    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.package.foldVersion"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.package.foldRuntime"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.Ciphers.java16RecommendedCiphers"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.Ciphers.java17RecommendedCiphers")
    )
).enablePlugins(ReleasePlugin, SbtOsgi)

lazy val documentation = project.in(file("documentation"))
  .settings(dontPublishSettings: _*)

lazy val root = project.in(file("."))
  .aggregate(
    sslConfigCore,
    documentation
  )
  .settings(dontPublishSettings: _*)
  .enablePlugins(CrossPerProjectPlugin)


def configImport(packageName: String = "com.typesafe.config.*") = versionedImport(packageName, "1.3.0", "1.4.0")
def versionedImport(packageName: String, lower: String, upper: String) = s"""$packageName;version="[$lower,$upper)""""
