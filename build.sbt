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
    crossScalaVersions := Seq(Version.scala213, Version.scala212, Version.scala211, Version.scala3),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    // work around for https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8266261
    // also https://bugs.openjdk.java.net/browse/JDK-8266279
    Test / fork := true,
    Test / javaOptions += "-Dkeystore.pkcs12.keyProtectionAlgorithm=PBEWithHmacSHA256AndAES_256",
    name := "ssl-config-core",
    mimaReportSignatureProblems := true,
    mimaPreviousArtifacts := (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Set("0.5.0")
      case _ =>
        Set()
    }).map("com.typesafe" %% "ssl-config-core" % _),
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

def configImport(packageName: String = "com.typesafe.config.*") = versionedImport(packageName, "1.4.1", "1.5.0")
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
