import com.typesafe.sbt.osgi.SbtOsgi
import com.typesafe.sbt.osgi.SbtOsgi.autoImport._
import com.typesafe.tools.mima.core.{DirectMissingMethodProblem, MissingClassProblem, ProblemFilters}

val commonSettings = Seq(
  scalaVersion := Version.scala212,
  crossScalaVersions := Seq(Version.scala213, Version.scala212, Version.scala211, Version.scala210),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

val disablePublishingSettings = Seq(
  // https://github.com/sbt/sbt/pull/3380
  skip in publish := true,
  publishArtifact := false
 )

lazy val sslConfigCore = project.in(file("ssl-config-core"))
  .settings(commonSettings: _*)
  .settings(AutomaticModuleName.settings("ssl.config.core"))
  .settings(osgiSettings: _*)
  .settings(
    name := "ssl-config-core",
    mimaPreviousArtifacts ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        Set("com.typesafe" %% "ssl-config-core" % "0.2.3")
      case _ => // 2.13 we don't have a library capable of this
        Set.empty[ModuleID]
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
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.Ciphers.java17RecommendedCiphers"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.Base64")
    )
).enablePlugins(SbtOsgi)

lazy val documentation = project.in(file("documentation"))
  .settings(disablePublishingSettings: _*)

lazy val root = project.in(file("."))
  .aggregate(
    sslConfigCore,
    documentation
  )
  .settings(disablePublishingSettings: _*)

// Release settings
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

// This automatically selects the snapshots or staging repository
// according to the version value.
publishTo in ThisBuild := Some(sonatypeDefaultResolver.value)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  // sbt-gpg works different from sbt-pgp. According to its docs it "hooks into the publish and publishLocal
  // tasks. All artifacts will be signed; there is no need to run a separate publishSigned task."
  // Keep in mind it requires proper `credentials` configuration:
  // https://github.com/jodersky/sbt-gpg#signing-key
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  // Automatically promote artifacts in Sonatype
  releaseStepCommand("sonatypeRelease"),
  pushChanges
)

def configImport(packageName: String = "com.typesafe.config.*") = versionedImport(packageName, "1.3.0", "1.4.0")
def versionedImport(packageName: String, lower: String, upper: String) = s"""$packageName;version="[$lower,$upper)""""

lazy val checkCodeFormat = taskKey[Unit]("Check that code format is following Scalariform rules")

checkCodeFormat := {
  import scala.sys.process._
  val exitCode = ("git diff --exit-code" !)
  if (exitCode != 0) {
    sys.error(
      """
        |ERROR: Scalariform check failed, see differences above.
        |To fix, format your sources using sbt scalariformFormat test:scalariformFormat before submitting a pull request.
        |Additionally, please squash your commits (eg, use git commit --amend) if you're going to update this pull request.
      """.stripMargin)
  }
}

addCommandAlias("validateCode", ";scalariformFormat;test:scalariformFormat;headerCheck;test:headerCheck;checkCodeFormat")
