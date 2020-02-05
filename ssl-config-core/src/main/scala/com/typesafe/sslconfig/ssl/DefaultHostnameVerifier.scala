/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.security.Principal
import java.security.cert.{ Certificate, CertificateException, X509Certificate }

import com.typesafe.sslconfig.util.LoggerFactory
import javax.net.ssl.{ HostnameVerifier, SSLPeerUnverifiedException, SSLSession }
import javax.security.auth.kerberos.KerberosPrincipal
import sun.security.util.HostnameChecker

@deprecated("DefaultHostnameVerifier has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
class DefaultHostnameVerifier(mkLogger: LoggerFactory) extends HostnameVerifier {
  private val logger = mkLogger(getClass)

  def hostnameChecker: HostnameChecker = {
    logger.warn("DefaultHostnameVerifier has been deprecated and does nothing.  Please use the javax.net.debug system property.")
    HostnameChecker.getInstance(HostnameChecker.TYPE_TLS)
  }

  def matchKerberos(hostname: String, principal: Principal) = {
    logger.warn("DefaultHostnameVerifier has been deprecated and does nothing.  Please use the javax.net.debug system property.")
    true
  }

  def isKerberos(principal: Principal): Boolean = {
    logger.warn("DefaultHostnameVerifier has been deprecated and does nothing.  Please use the javax.net.debug system property.")
    true
  }

  def verify(hostname: String, session: SSLSession): Boolean = {
    logger.warn("DefaultHostnameVerifier has been deprecated and does nothing.  Please use the javax.net.debug system property.")
    true
  }

  /** INTERNAL API */
  def matchCertificates(hostname: String, peerCertificates: Array[Certificate]): Boolean = {
    logger.warn("DefaultHostnameVerifier has been deprecated and does nothing.  Please use the javax.net.debug system property.")
    true
  }
}