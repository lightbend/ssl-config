import com.typesafe.sbt.osgi.SbtOsgi
import com.typesafe.sbt.osgi.SbtOsgi.autoImport._
import com.typesafe.tools.mima.core._

ThisBuild / scalaVersion := Version.scala212

val disablePublishingSettings = Seq(
  // https://github.com/sbt/sbt/pull/3380
  skip in publish := true,
  publishArtifact := false,
  mimaReportBinaryIssues := false
 )

lazy val sslConfigCore = project.in(file("ssl-config-core"))
  .settings(AutomaticModuleName.settings("ssl.config.core"))
  .settings(osgiSettings: _*)
  .settings(
    crossScalaVersions := Seq(Version.scala213, Version.scala212, Version.scala211),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    // work around for https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8266261
    // also https://bugs.openjdk.java.net/browse/JDK-8266279
    Test / fork := true,
    Test / javaOptions += "-Dkeystore.pkcs12.keyProtectionAlgorithm=PBEWithHmacSHA256AndAES_256",
    name := "ssl-config-core",
    mimaReportSignatureProblems := true,
    mimaPreviousArtifacts ++= (((CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 =>
        Set("0.3.7")
      case _ => // 2.13 we don't have a library capable of this
        Set.empty[String]
    }) ++ Set(
      "0.3.8",
      "0.4.0",
      "0.4.1",
      "0.4.2",
    ))).map("com.typesafe" %% "ssl-config-core" % _), // "sbt mimaReportBinaryIssues"
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[IncompatibleSignatureProblem]("com.typesafe.sslconfig.ssl.AlgorithmConstraintsParser.*"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.FakeKeyStore#KeystoreSettings.SignatureAlgorithmOID"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.FakeChainedKeyStore#KeystoreSettings.SignatureAlgorithmOID")
    ),
    libraryDependencies += Library.parserCombinators(scalaVersion.value),
    libraryDependencies ++= Dependencies.sslConfigCore,
    libraryDependencies ++= Dependencies.testDependencies,
    OsgiKeys.bundleSymbolicName := s"${organization.value}.sslconfig",
    OsgiKeys.exportPackage := Seq(s"com.typesafe.sslconfig.*;version=${version.value}"),
    OsgiKeys.importPackage := Seq("!sun.misc", "!sun.security.*", configImport(), "*"),
    OsgiKeys.requireCapability := """osgi.ee;filter:="(&(osgi.ee=JavaSE)(version>=1.8))"""",
).enablePlugins(SbtOsgi)

val documentation = project.enablePlugins(ParadoxPlugin, ParadoxSitePlugin).settings(
  name := "SSL Config",
  Paradox / siteSubdirName := "",
  paradoxTheme := Some(builtinParadoxTheme("generic")),
  disablePublishingSettings,
)

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
publishTo in ThisBuild := sonatypePublishToBundle.value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  // Automatically promote artifacts in Sonatype
  releaseStepCommand("sonatypeBundleRelease"),
  pushChanges
)

def configImport(packageName: String = "com.typesafe.config.*") = versionedImport(packageName, "1.4.0", "1.5.0")
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
