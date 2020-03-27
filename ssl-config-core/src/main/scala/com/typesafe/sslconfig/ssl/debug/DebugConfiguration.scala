/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.debug

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.LoggerFactory

@deprecated("DebugConfiguration has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
class DebugConfiguration(mkLogger: LoggerFactory) {

  private val logger = mkLogger(getClass)

  def configure(d: SSLDebugConfig): Unit = {
    logger.warn("DebugConfiguration has been deprecated and does nothing.  Please use the javax.net.debug system property.")
  }

  def configureJavaxNetDebug(d: SSLDebugConfig): Unit = {
    logger.warn("DebugConfiguration has been deprecated and does nothing.  Please use the javax.net.debug system property.")
  }

  def configureJavaSecurityDebug(d: SSLDebugConfig): Unit = {
    logger.warn("DebugConfiguration has been deprecated and does nothing.  Please use the javax.net.debug system property.")
  }

}