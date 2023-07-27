/*
 * Copyright (C) 2015 - 2021 Lightbend Inc. <https://www.lightbend.com>
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
import com.typesafe.sbt.SbtScalariform.autoImport._

/**
 * Common sbt settings — automatically added to all projects.
 */
object Common extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = plugins.JvmPlugin && HeaderPlugin

  // AutomateHeaderPlugin is not an allRequirements-AutoPlugin, so explicitly add settings here:
  override def projectSettings =
    AutomateHeaderPlugin.projectSettings ++
    Seq(
      scalariformAutoformat := true,
      organization := "com.typesafe",
      homepage := Some(url("https://github.com/lightbend/ssl-config")),
      developers := List(
        Developer("wsargent", "Will Sargent", "", url("https://tersesystems.com")),
        Developer("ktoso", "Konrad Malawski", "", url("https://project13.pl")),
      ),
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
        .setPreference(AlignParameters, false),

      // Header settings

      HeaderPlugin.autoImport.headerMappings := Map(
        HeaderFileType.scala -> HeaderCommentStyle.cStyleBlockComment,
        HeaderFileType.java  -> HeaderCommentStyle.cStyleBlockComment,
        HeaderFileType.conf -> HeaderCommentStyle.hashLineComment
      ),

      organizationName := "Lightbend",
      startYear := Some(2015),
      licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),

      HeaderPlugin.autoImport.headerLicense := {
        // To be manually updated yearly, preventing unrelated PR's to suddenly fail
        // just because time passed
        val currentYear = 2023
        Some(HeaderLicense.Custom(
          s"""Copyright (C) 2015 - $currentYear Lightbend Inc. <https://www.lightbend.com>"""
        ))
      }
    )

}
