/*
 * Copyright (C) 2015 - 2025 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.tracing

import javax.net.ssl.SSLContext

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.LoggerFactory
import com.typesafe.sslconfig.util.NoopLogger
import org.specs2.mutable.Specification

class TracingSSLEngineSpec extends Specification {

  private implicit val loggerFactory: LoggerFactory = new LoggerFactory {
    override def apply(clazz: Class[?]) = new NoopLogger
    override def apply(name: String)    = new NoopLogger
  }

  "TracingSSLEngine" should {
    "forward SSLParameters to the engine performing the handshake" in {
      val delegate = SSLContext.getDefault.createSSLEngine("localhost", 443)
      val traced   = new TracingSSLEngine(delegate, SSLDebugConfig().withSsl(true))

      val parameters = traced.getSSLParameters
      parameters.setEndpointIdentificationAlgorithm("HTTPS")
      traced.setSSLParameters(parameters)

      delegate.getSSLParameters.getEndpointIdentificationAlgorithm must_== "HTTPS"
    }

    "read SSLParameters from the delegate" in {
      val delegate = SSLContext.getDefault.createSSLEngine("localhost", 443)
      val traced   = new TracingSSLEngine(delegate, SSLDebugConfig().withSsl(true))

      val parameters = delegate.getSSLParameters
      parameters.setEndpointIdentificationAlgorithm("HTTPS")
      delegate.setSSLParameters(parameters)

      traced.getSSLParameters.getEndpointIdentificationAlgorithm must_== "HTTPS"
    }
  }
}
