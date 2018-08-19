/*
 * Copyright (C) 2018 Lightbend Inc. <https://www.lightbend.com>
 */

import com.typesafe.sbt.SbtScalariform
import de.heikoseeberger.sbtheader._
import sbt.Keys._
import sbt._

// Docs have it as HeaderFileType but that is actually a TYPE ALIAS >:-(
// https://github.com/sbt/sbt-header/blob/master/src/main/scala/de/heikoseeberger/sbtheader/HeaderPlugin.scala#L58
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import de.heikoseeberger.sbtheader.{CommentStyle => HeaderCommentStyle, FileType => HeaderFileType, License => HeaderLicense}
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
  override def projectSettings = SbtScalariform.autoImport.scalariformSettings(autoformat = true) ++
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
            Seq("-target:jvm-1.8")
          case _ =>
            Nil
        }
      },
      javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8"),
      // Scalariform settings
      ScalariformKeys.preferences := ScalariformKeys.preferences.value
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
        .setPreference(DoubleIndentConstructorArguments, true)
        .setPreference(DanglingCloseParenthesis, Preserve)
        .setPreference(AlignParameters, true),

      // Header settings

      HeaderPlugin.autoImport.headerMappings := Map(
        HeaderFileType.scala -> HeaderCommentStyle.cStyleBlockComment,
        HeaderFileType.java  -> HeaderCommentStyle.cStyleBlockComment,
        HeaderFileType.conf -> HeaderCommentStyle.hashLineComment
      ),

      organizationName := "Lightbend",
      startYear := Some(2015),
      licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),

      HeaderPlugin.autoImport.headerLicense := Some(HeaderLicense.Custom(
        """Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>"""
      ))
    )

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

  def settings = Seq(
    publishMavenStyle := true,
    publishTo := { Some(if (isSnapshot.value) ossSnapshots else ossStaging) },
    publishArtifact in Test := false,
    pomIncludeRepository := (_ => false),
    pomExtra := generatePomExtra
  )
}
