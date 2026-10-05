/*
 * Copyright (C) 2015 - 2021 Lightbend Inc. <https://www.lightbend.com>
 */

import sbt._
import sbt.Keys._

// Docs have it as HeaderFileType but that is actually a TYPE ALIAS >:-(
// https://github.com/sbt/sbt-header/blob/master/src/main/scala/de/heikoseeberger/sbtheader/HeaderPlugin.scala#L58
import sbtheader.{ CommentStyle => HeaderCommentStyle }
import sbtheader.{ FileType => HeaderFileType }
import sbtheader.{ License => HeaderLicense }
import sbtheader.AutomateHeaderPlugin
import sbtheader.HeaderPlugin

/**
 * Common sbt settings — automatically added to all projects.
 */
object Common extends AutoPlugin {

  object autoImport {
    val githubOrg  = "lightbend"
    val githubRepo = "ssl-config"
  }

  import autoImport._

  override def trigger = allRequirements

  override def requires = plugins.JvmPlugin && HeaderPlugin

  // AutomateHeaderPlugin is not an allRequirements-AutoPlugin, so explicitly add settings here:
  override def projectSettings = {
    AutomateHeaderPlugin.projectSettings ++
      Seq(
        organization := "com.typesafe",
        homepage     := Some(url(s"https://${githubOrg}.github.io/${githubRepo}/")),
        scmInfo      := Some(
          ScmInfo(
            url(s"https://github.com/${githubOrg}/${githubRepo}"),
            s"scm:git:https://github.com/${githubOrg}/${githubRepo}.git",
            Some(s"scm:git:git@github.com:${githubOrg}/${githubRepo}.git")
          )
        ),
        developers := List(
          Developer("wsargent", "Will Sargent", "", url("https://tersesystems.com")),
          Developer("ktoso", "Konrad Malawski", "", url("https://project13.pl")),
        ),
        updateOptions := updateOptions.value.withCachedResolution(true),
        scalacOptions ++= Seq("-encoding", "UTF-8", "-unchecked", "-deprecation", "-feature") ++ {
          if (scalaVersion.value.startsWith("3.3.")) Seq("-release:11", "-Yfuture-lazy-vals")
          else Seq("-release:8")
        },
        javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8"),

        // Header settings
        HeaderPlugin.autoImport.headerMappings := Map(
          HeaderFileType.scala -> HeaderCommentStyle.cStyleBlockComment,
          HeaderFileType.java  -> HeaderCommentStyle.cStyleBlockComment,
          HeaderFileType.conf  -> HeaderCommentStyle.hashLineComment
        ),
        organizationName                      := "Lightbend",
        startYear                             := Some(2015),
        licenses                              := Seq(License.Apache2),
        HeaderPlugin.autoImport.headerLicense := {
          // To be manually updated yearly, preventing unrelated PR's to suddenly fail
          // just because time passed
          val currentYear = 2025
          Some(
            HeaderLicense.Custom(
              s"""Copyright (C) 2015 - $currentYear Lightbend Inc. <https://www.lightbend.com>"""
            )
          )
        }
      )
  }

}
