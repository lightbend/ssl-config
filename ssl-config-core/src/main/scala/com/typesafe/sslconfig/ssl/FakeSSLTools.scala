/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.security.KeyStore

import javax.net.ssl._

/** Extracted reusable tools from `play.it.test.ServerEndpoint.SelfSigned`. */
object FakeSSLTools {
  /** NOT FOR PRODUCTION USE. */
  def buildContextAndTrust(keyStore: KeyStore): (SSLContext, X509TrustManager) = {
    val kmf: KeyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    kmf.init(keyStore, Array.emptyCharArray)
    val kms: Array[KeyManager] = kmf.getKeyManagers

    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    tmf.init(keyStore)
    val tms: Array[TrustManager] = tmf.getTrustManagers

    val x509TrustManager: X509TrustManager = tms(0).asInstanceOf[X509TrustManager]

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(kms, tms, null)

    (sslContext, x509TrustManager)
  }
}
