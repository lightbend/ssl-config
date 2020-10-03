/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.debug

import java.security.AccessController
import com.typesafe.sslconfig.util.{ LoggerFactory, NoDepsLogger }

import scala.util.control.NonFatal
import sun.security.util.Debug

@deprecated("FixCertpathDebugLogging has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
class FixCertpathDebugLogging(mkLogger: LoggerFactory) {
  val logger = mkLogger("com.typesafe.sslconfig.ssl.debug.FixCertpathDebugLogging")

  @deprecated("MonkeyPatchSunSecurityUtilDebugAction has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
  class MonkeyPatchSunSecurityUtilDebugAction(val newDebug: Debug, val newOptions: String) extends FixLoggingAction {
    val logger = mkLogger("com.typesafe.sslconfig.ssl.debug.FixCertpathDebugLogging.MonkeyPatchSunSecurityUtilDebugAction")

    val initialResource = "/sun/security/provider/certpath/Builder.class"

    val debugType = classOf[Debug]

    def isValidClass(className: String): Boolean = {
      logger.warn("MonkeyPatchSunSecurityUtilDebugAction has been deprecated and does nothing.  Please use the javax.net.debug system property.")
      if (className.startsWith("java.security.cert")) return true
      if (className.startsWith("sun.security.provider.certpath")) return true
      if (className.equals("sun.security.x509.InhibitAnyPolicyExtension")) return true
      false
    }

    def isUsingDebug: Boolean = {
      logger.warn("MonkeyPatchSunSecurityUtilDebugAction has been deprecated and does nothing.  Please use the javax.net.debug system property.")
      (newOptions != null) && newOptions.contains("certpath")
    }

    def run(): Unit = {
      logger.warn("MonkeyPatchSunSecurityUtilDebugAction has been deprecated and does nothing.  Please use the javax.net.debug system property.")
    }
  }

  @deprecated("SunSecurityUtilDebugLogger has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
  class SunSecurityUtilDebugLogger(logger: NoDepsLogger) extends sun.security.util.Debug {
    override def println(message: String): Unit = ()
    override def println(): Unit = ()
  }

  def apply(newOptions: String, debugOption: Option[Debug] = None): Unit = {
    logger.warn("FixCertpathDebugLogging has been deprecated and does nothing.  Please use the javax.net.debug system property.")
  }
}