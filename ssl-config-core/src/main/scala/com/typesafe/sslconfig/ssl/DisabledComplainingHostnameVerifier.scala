/*
 * Copyright (C) 2015 - 2023 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import com.typesafe.sslconfig.util.LoggerFactory

import javax.net.ssl.{ HostnameVerifier, SSLSession }

/**
 * Add a disabled but complaining hostname verifier.
 */
class DisabledComplainingHostnameVerifier(mkLogger: LoggerFactory) extends HostnameVerifier {

  private val logger = mkLogger(getClass)

  private val defaultHostnameVerifier = new NoopHostnameVerifier

  override def verify(hostname: String, sslSession: SSLSession): Boolean = {
    val hostNameMatches = defaultHostnameVerifier.verify(hostname, sslSession)
    if (!hostNameMatches) {
      // TODO fix config paths
      val msg =
        s"Hostname verification failed on hostname $hostname, " +
          "but the connection was accepted because ssl-config.loose.disableHostnameVerification is enabled. " +
          "Please fix the X.509 certificate on the host to remove this warning."
      logger.warn(msg)
    }
    true
  }
}
