/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.debug

import com.typesafe.sslconfig.util.LoggerFactory

@deprecated("FixInternalDebugLogging has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
class FixInternalDebugLogging(mkLogger: LoggerFactory) {
  private val logger = mkLogger("com.typesafe.sslconfig.ssl.debug.FixInternalDebugLogging")

  @deprecated("MonkeyPatchInternalSslDebugAction has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
  class MonkeyPatchInternalSslDebugAction(val newOptions: String) extends FixLoggingAction {
    override val logger = mkLogger("com.typesafe.sslconfig.ssl.debug.FixInternalDebugLogging.MonkeyPatchInternalSslDebugAction")

    val initialResource = "/sun/security/ssl/Debug.class"
    val debugClassName = "sun.security.ssl.Debug"

    def isValidClass(className: String): Boolean = {
      logger.warn("MonkeyPatchInternalSslDebugAction has been deprecated and does nothing.  Please use the javax.net.debug system property.")
      if (className.startsWith("com.sun.net.ssl.internal.ssl")) return true
      if (className.startsWith("sun.security.ssl")) return true
      false
    }

    def isUsingDebug: Boolean = {
      logger.warn("MonkeyPatchInternalSslDebugAction has been deprecated and does nothing.  Please use the javax.net.debug system property.")
      (newOptions != null) && (!newOptions.isEmpty)
    }

    def run(): Unit = {
      logger.warn("MonkeyPatchInternalSslDebugAction has been deprecated and does nothing.  Please use the javax.net.debug system property.")
    }

    def apply(newOptions: String): Unit = {
      logger.warn("MonkeyPatchInternalSslDebugAction has been deprecated and does nothing.  Please use the javax.net.debug system property.")
    }
  }

  def apply(newOptions: String): Unit = {
    logger.warn("FixInternalDebugLogging has been deprecated and does nothing.  Please use the javax.net.debug system property.")
  }
}
