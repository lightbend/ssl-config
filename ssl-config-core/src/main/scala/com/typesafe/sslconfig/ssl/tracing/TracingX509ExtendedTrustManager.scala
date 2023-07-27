/*
 * Copyright (C) 2015 - 2023 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.tracing

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.LoggerFactory

import java.net.Socket
import java.security.cert.{ CertificateException, X509Certificate }
import javax.net.ssl.{ SSLEngine, X509ExtendedTrustManager }

private[sslconfig] class TracingX509ExtendedTrustManager(supplier: => X509ExtendedTrustManager, debug: SSLDebugConfig)(implicit loggerFactory: LoggerFactory) extends X509ExtendedTrustManager with TraceLogger {
  @throws[CertificateException]
  override def checkClientTrusted(chain: Array[X509Certificate], authType: String, socket: Socket): Unit = {
    tracer("checkClientTrusted", Map("chain" -> chain, "authType" -> authType, "socket" -> socket), () => supplier.checkClientTrusted(chain, authType, socket))
  }

  @throws[CertificateException]
  override def checkServerTrusted(chain: Array[X509Certificate], authType: String, socket: Socket): Unit = {
    tracer("checkServerTrusted", Map("chain" -> chain, "authType" -> authType, "socket" -> socket), () => supplier.checkServerTrusted(chain, authType, socket))
  }

  @throws[CertificateException]
  override def checkClientTrusted(chain: Array[X509Certificate], authType: String, engine: SSLEngine): Unit = {
    tracer("checkClientTrusted", Map("chain" -> chain, "authType" -> authType, "engine" -> engine), () => supplier.checkClientTrusted(chain, authType, engine))
  }

  @throws[CertificateException]
  override def checkServerTrusted(chain: Array[X509Certificate], authType: String, engine: SSLEngine): Unit = {
    tracer("checkServerTrusted", Map("chain" -> chain, "authType" -> authType, "engine" -> engine), () => supplier.checkServerTrusted(chain, authType, engine))
  }

  @throws[CertificateException]
  override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    tracer("checkClientTrusted", Map("chain" -> chain, "authType" -> authType), () => supplier.checkClientTrusted(chain, authType))
  }

  @throws[CertificateException]
  override def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    tracer("checkServerTrusted", Map("chain" -> chain, "authType" -> authType), () => supplier.checkServerTrusted(chain, authType))
  }

  override def getAcceptedIssuers: Array[X509Certificate] = {
    tracer("getAcceptedIssuers", Map(), () => supplier.getAcceptedIssuers)
  }

  override def isLogEnabled(methodName: String, parameters: Map[String, Any]): Boolean = debug.all || debug.trustmanager
}