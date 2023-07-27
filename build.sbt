import com.typesafe.sbt.osgi.SbtOsgi
import com.typesafe.sbt.osgi.SbtOsgi.autoImport._
import com.typesafe.tools.mima.core._

ThisBuild / scalaVersion := Version.scala212

val disablePublishingSettings = Seq(
  // https://github.com/sbt/sbt/pull/3380
  publish / skip := true,
  publishArtifact := false,
  mimaReportBinaryIssues := false
 )

lazy val sslConfigCore = project.in(file("ssl-config-core"))
  .settings(AutomaticModuleName.settings("ssl.config.core"))
  .settings(osgiSettings: _*)
  .settings(
    crossScalaVersions := Seq(Version.scala213, Version.scala212, Version.scala3),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    name := "ssl-config-core",
    mimaReportSignatureProblems := true,
    mimaPreviousArtifacts := (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Set("0.5.0")
      case _ =>
        Set()
    }).map("com.typesafe" %% "ssl-config-core" % _),
    mimaBinaryIssueFilters ++= Seq(
      // private[sslconfig]
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLLooseConfig.this"),
      // private[sslconfig]
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.this"),
      // synthetic on private[sslconfig]
      ProblemFilters.exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default*"),
      // v0.7: removal of deprecations
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.DefaultHostnameVerifier"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.JavaSecurityDebugBuilder"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.JavaxNetDebugBuilder"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.MonkeyPatcher"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.certpath"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.defaultctx"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.handshake"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.keygen"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.ocsp"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.pluggability"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.record"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.session"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.sessioncache"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.withCertPath"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.withDefaultContext"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.withHandshake"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.withKeygen"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.withOcsp"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.withPluggability"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.withRecord"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.withSession"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.withSessioncache"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLDebugConfig.this"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.SSLDebugHandshakeOptions"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.SSLDebugHandshakeOptions$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.SSLDebugRecordOptions"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.SSLDebugRecordOptions$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.debug.ClassFinder"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.debug.DebugConfiguration"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.debug.FixCertpathDebugLogging"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.debug.FixCertpathDebugLogging$MonkeyPatchSunSecurityUtilDebugAction"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.debug.FixCertpathDebugLogging$SunSecurityUtilDebugLogger"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.debug.FixInternalDebugLogging"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.debug.FixInternalDebugLogging$MonkeyPatchInternalSslDebugAction"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.debug.FixLoggingAction"),
    ),
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

def configImport(packageName: String = "com.typesafe.config.*") = versionedImport(packageName, "1.4.2", "1.5.0")
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
