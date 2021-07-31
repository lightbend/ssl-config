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
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.FakeChainedKeyStore#KeystoreSettings.SignatureAlgorithmOID"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.OpSym"),
      ProblemFilters.exclude[IncompatibleMethTypeProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.buildCompositeKeyManager"),
      ProblemFilters.exclude[IncompatibleMethTypeProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.buildCompositeTrustManager"),
      ProblemFilters.exclude[IncompatibleMethTypeProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.buildKeyManager"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.buildTrustManagerParameters"),
      ProblemFilters.exclude[IncompatibleMethTypeProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.buildTrustManager"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.validateStore"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.ExpressionSymbol"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.CompositeX509TrustManager.this"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.OpSym$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.OpSym$LTE$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.OpSym$GT$"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.disabledSignatureAlgorithms"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.disabledKeyAlgorithms"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.withDisabledKeyAlgorithms"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.withDisabledSignatureAlgorithms"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.this"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.Ciphers"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.LessThanOrEqual"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.Algorithms$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.LessThan$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.OpSym$GTE$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.LessThan"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.Algorithms"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLLooseConfig.allowWeakCiphers"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLLooseConfig.allowWeakProtocols"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLLooseConfig.withAllowWeakCiphers"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLLooseConfig.withAllowWeakProtocols"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLLooseConfig.this"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.AlgorithmConstraintsParser$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.MoreThan"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.AlgorithmConstraintsParser"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.AlgorithmConstraint"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.OpSym$EQ$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.LessThanOrEqual$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.MoreThanOrEqual$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.Equal"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.NotEqual$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.Equal$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.OpSym$NE$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.MoreThanOrEqual"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.NotEqual"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.Ciphers$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.AlgorithmConstraint$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.AlgorithmChecker"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.MoreThan$"),
      ProblemFilters.exclude[MissingClassProblem]("com.typesafe.sslconfig.ssl.OpSym$LT$"),
      ProblemFilters.exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$7"),
      ProblemFilters.exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$8"),
      ProblemFilters.exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$9"),
      ProblemFilters.exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$10"),
      ProblemFilters.exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$11"),
      ProblemFilters.exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$12"),
      ProblemFilters.exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$13"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$14"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$15"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLLooseConfig.<init>$default$6"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.SSLLooseConfig.<init>$default$7"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.buildCompositeKeyManager"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.buildCompositeTrustManager"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.buildKeyManager"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder.buildTrustManager")
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
