import com.github.sbt.osgi.SbtOsgi
import com.github.sbt.osgi.SbtOsgi.autoImport._
import com.typesafe.tools.mima.core._
import Common.autoImport._

ThisBuild / scalaVersion       := Version.scala212
ThisBuild / crossScalaVersions := Seq(Version.scala213, Version.scala212, Version.scala3)

val disablePublishingSettings = Seq(
  publish / skip         := true,
  mimaReportBinaryIssues := false
)

lazy val sslConfigCore = project
  .in(file("ssl-config-core"))
  .settings(AutomaticModuleName.settings("ssl.config.core"))
  .settings(osgiSettings: _*)
  .settings(
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    name                        := "ssl-config-core",
    mimaReportSignatureProblems := true,
    mimaPreviousArtifacts       := Set("com.typesafe" %% "ssl-config-core" % "0.7.0"),
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters
        .exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$10"),
      ProblemFilters
        .exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$11"),
      ProblemFilters
        .exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$9"),
      ProblemFilters
        .exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$7"),
      ProblemFilters
        .exclude[IncompatibleResultTypeProblem]("com.typesafe.sslconfig.ssl.SSLConfigSettings.<init>$default$8"),
    ),
    libraryDependencies ++= Dependencies.sslConfigCore,
    libraryDependencies ++= Dependencies.testDependencies,
    OsgiKeys.bundleSymbolicName := s"${organization.value}.sslconfig",
    OsgiKeys.exportPackage      := Seq(s"com.typesafe.sslconfig.*;version=${version.value}"),
    OsgiKeys.importPackage      := Seq("!sun.misc", "!sun.security.*", configImport(), "*"),
    OsgiKeys.requireCapability  := {
      val javaVersion = if (scalaVersion.value.startsWith("3.3.")) "11" else "1.8"
      s"""osgi.ee;filter:="(&(osgi.ee=JavaSE)(version>=$javaVersion))"""
    },
  )
  .enablePlugins(SbtOsgi)

val documentation = project
  .enablePlugins(ParadoxPlugin, ParadoxSitePlugin)
  .settings(
    name                     := "SSL Config",
    Paradox / siteSubdirName := "",
    paradoxTheme             := Some(builtinParadoxTheme("generic")),
    disablePublishingSettings,
  )

lazy val root = project
  .in(file("."))
  .aggregate(
    sslConfigCore,
    documentation
  )
  .settings(disablePublishingSettings: _*)

def configImport(packageName: String = "com.typesafe.config.*")        = versionedImport(packageName, "1.4.2", "1.5.0")
def versionedImport(packageName: String, lower: String, upper: String) = s"""$packageName;version="[$lower,$upper)""""

addCommandAlias("validateCode", "headerCheckAll ; scalafmtSbtCheck ; scalafmtCheckAll")

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(
    List("validateCode", "test", "doc", "mimaReportBinaryIssues"),
    cond = Some(s"github.repository == '${githubOrg}/${githubRepo}'")
  ),
  WorkflowStep.Run(
    List("./scripts/validate-docs.sh"),
    cond = Some(s"matrix.java != 'temurin@8' && github.repository == '${githubOrg}/${githubRepo}'")
  ),
)

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishCond           := Some(s"github.repository == '${githubOrg}/${githubRepo}'")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(
    RefPredicate.StartsWith(Ref.Tag("v")),
    RefPredicate.Equals(Ref.Branch("main"))
  )
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    commands = List("ci-release"),
    name = Some("Publish project"),
    env = Map(
      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}",
    )
  )
)

ThisBuild / githubWorkflowOSes := Seq("ubuntu-latest", "macos-latest", "windows-latest")

ThisBuild / githubWorkflowJavaVersions := Seq(
  JavaSpec.temurin("11"), // The first JDK is also used for publishing.
  JavaSpec.temurin("8"),
  // JavaSpec.temurin("17"), // can't test currently because until we drop usage of sun.security.x509.*
  // JavaSpec.temurin("21"),
  // JavaSpec.temurin("25"),
)

ThisBuild / githubWorkflowBuildMatrixExclusions ++= Seq(
  MatrixExclude(Map("java" -> "temurin@8", "os" -> "macos-latest")),
  MatrixExclude(Map("java" -> "temurin@8", "scala" -> Version.scala3)),
)
