/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.ssl.debug

import com.typesafe.sslconfig.ssl.{ JavaSecurityDebugBuilder, JavaxNetDebugBuilder, SSLDebugConfig }
import com.typesafe.sslconfig.util.LoggerFactory

class DebugConfiguration(mkLogger: LoggerFactory) {

  private val logger = mkLogger(getClass)

  def configure(d: SSLDebugConfig): Unit = {
    configureJavaxNetDebug(d)
    configureJavaSecurityDebug(d)
  }

  def configureJavaxNetDebug(d: SSLDebugConfig): Unit = {
    val netDebugOptions = new JavaxNetDebugBuilder(d).build()
    logger.debug(s"configureJavaxNetDebug: d = $d, netDebugOptions = $netDebugOptions")
    new FixInternalDebugLogging(mkLogger).apply(netDebugOptions)
  }

  def configureJavaSecurityDebug(d: SSLDebugConfig): Unit = {
    val securityOptions = new JavaSecurityDebugBuilder(d).build()
    logger.debug(s"configureJavaSecurityDebug: d = $d, securityOptions = $securityOptions")
    System.setProperty("java.security.debug", securityOptions)
    new FixCertpathDebugLogging(mkLogger).apply(securityOptions)
  }

}