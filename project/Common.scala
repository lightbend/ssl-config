/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

import sbt._
import sbt.Keys._
import de.heikoseeberger.sbtheader.{ HeaderPattern, HeaderPlugin, AutomateHeaderPlugin }
import com.typesafe.sbt.SbtScalariform.{ scalariformSettings, ScalariformKeys }
import scalariform.formatter.preferences._

/**
 * Common sbt settings — automatically added to all projects.
 */
object Common extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = plugins.JvmPlugin && HeaderPlugin

  // sonatype
  object sonatype extends PublishToSonatype {
     def projectUrl    = "https://github.com/lightbend/ssl-config"
     def scmUrl        = "git://github.com/lightbend/ssl-config.git"
     def developers    = List(
       Developer("wsargent", "Will Sargent", "https://tersesystems.com"),
       Developer("ktoso", "Konrad Malawski", "https://project13.pl"))
  }

  // AutomateHeaderPlugin is not an allRequirements-AutoPlugin, so explicitly add settings here:
  override def projectSettings = scalariformSettings ++
    AutomateHeaderPlugin.projectSettings ++
    sonatype.settings ++
    Seq(
      organization := "com.typesafe",
      scalaVersion := Version.scala210,
      crossScalaVersions := Seq(Version.scala210, Version.scala211),
      updateOptions := updateOptions.value.withCachedResolution(true),
      scalacOptions ++= Seq("-encoding", "UTF-8", "-unchecked", "-deprecation", "-feature"),
      scalacOptions ++= {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, v)) if v <= 11 =>
            Seq("-target:jvm-1.6")
          case _ =>
            Nil
        }
      },
      javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6"),
      // Scalariform settings
      ScalariformKeys.preferences := ScalariformKeys.preferences.value
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
        .setPreference(DoubleIndentClassDeclaration, true)
        .setPreference(PreserveDanglingCloseParenthesis, true)
        .setPreference(AlignParameters, true),

      // Header settings
      HeaderPlugin.autoImport.headers := Map(
        "scala" ->(HeaderPattern.cStyleBlockComment, scalaOrJavaHeader),
        "java" ->(HeaderPattern.cStyleBlockComment, scalaOrJavaHeader),
        "conf" ->(HeaderPattern.hashLineComment, confHeader)
      )
    )

  // Header text generation

  val scalaOrJavaHeader = header(before = Some("/*"), prefix = " * ", after = Some(" */"))
  val confHeader = header(before = None, prefix = "# ", after = None)

  def header(before: Option[String], prefix: String, after: Option[String]): String = {
    val content = Seq("Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>")
    (before.toSeq ++ content.map(prefix.+) ++ after.toSeq).mkString("", "\n", "\n\n")
  }

}

// from https://raw.github.com/paulp/scala-improving/master/project/PublishToSonatype.scala
abstract class PublishToSonatype {
  val ossSnapshots = "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
  val ossStaging   = "Sonatype OSS Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
  case class Developer(id: String, name: String, url: String)
  def projectUrl: String

  def licenseName         = "Apache License, Version 2.0"
  def licenseUrl          = "http://www.apache.org/licenses/LICENSE-2.0"
  def developers: List[Developer]
  def licenseDistribution = "repo"
  def scmUrl: String
  def scmConnection       = "scm:git:" + scmUrl

  def generatePomExtra: xml.NodeSeq = {
    <url>{ projectUrl }</url>
    <licenses>
      <license>
        <name>{ licenseName }</name>
        <url>{ licenseUrl }</url>
        <distribution>{ licenseDistribution }</distribution>
      </license>
    </licenses>
    <scm>
      <url>{ scmUrl }</url>
      <connection>{ scmConnection }</connection>
    </scm>
    <developers>
      {
        developers.map { dev =>
          <developer>
            <id>{ dev.id }</id>
            <name>{ dev.name }</name>
            <url>{ dev.url }</url>
          </developer>
        }
      }
    </developers>
  }

  def settings: Seq[Setting[_]] = Seq(
    publishMavenStyle := true,
    publishTo <<= isSnapshot { (snapshot) => Some(if (snapshot) ossSnapshots else ossStaging) },
    publishArtifact in Test := false,
    pomIncludeRepository := (_ => false),
    pomExtra := generatePomExtra
  )
}
