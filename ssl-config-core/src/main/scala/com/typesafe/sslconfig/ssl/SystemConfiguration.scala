/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.ssl

import java.security.Security

import com.typesafe.sslconfig.util.{ LoggerFactory, NoDepsLogger }

/**
 * Configures global system properties on the JSSE implementation, if defined.
 *
 * WARNING: This class sets system properties to configure JSSE code which typically uses static initialization on
 * load.  Because of this, if classes are loaded in BEFORE this code has a chance to operate, you may find that this
 * code works inconsistently.  The solution is to set the system properties on the command line explicitly (or in the
 * case of "ocsp.enable", in the security property file).
 */
class SystemConfiguration(mkLogger: LoggerFactory) {

  val logger = mkLogger(getClass)

  def configure(config: SSLConfigSettings) {
    config.loose.allowUnsafeRenegotiation.foreach(configureUnsafeRenegotiation)
    config.loose.allowLegacyHelloMessages.foreach(configureAllowLegacyHelloMessages)
    config.checkRevocation.foreach(configureCheckRevocation)
  }

  def configureUnsafeRenegotiation(allowUnsafeRenegotiation: Boolean) {
    System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", allowUnsafeRenegotiation.toString)
    logger.debug(s"configureUnsafeRenegotiation: sun.security.ssl.allowUnsafeRenegotiation = ${allowUnsafeRenegotiation.toString}")
  }

  def configureAllowLegacyHelloMessages(allowLegacyHelloMessages: Boolean) {
    System.setProperty("sun.security.ssl.allowLegacyHelloMessages", allowLegacyHelloMessages.toString)
    logger.debug(s"configureAllowLegacyHelloMessages: sun.security.ssl.allowLegacyHelloMessages = ${allowLegacyHelloMessages.toString}")
  }

  def configureCheckRevocation(checkRevocation: Boolean) {
    // http://docs.oracle.com/javase/8/docs/technotes/guides/security/certpath/CertPathProgGuide.html#AppC
    // https://blogs.oracle.com/xuelei/entry/enable_ocsp_checking

    // 1.7: PXIXCertPathValidator.populateVariables, it is dynamic so no override needed.
    Security.setProperty("ocsp.enable", checkRevocation.toString)
    logger.debug(s"configureCheckRevocation: ocsp.enable = ${checkRevocation.toString}")
    System.setProperty("com.sun.security.enableCRLDP", checkRevocation.toString)
    logger.debug(s"configureCheckRevocation: com.sun.security.enableCRLDP = ${checkRevocation.toString}")
    System.setProperty("com.sun.net.ssl.checkRevocation", checkRevocation.toString)
  }

  /**
   * For use in testing.
   */
  def clearProperties() {
    Security.setProperty("ocsp.enable", "false")
    System.clearProperty("com.sun.security.enableCRLDP")
    System.clearProperty("com.sun.net.ssl.checkRevocation")

    System.clearProperty("sun.security.ssl.allowLegacyHelloMessages")
    System.clearProperty("sun.security.ssl.allowUnsafeRenegotiation")
  }
}

