/*
 * Copyright (C) 2015 - 2019 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.tracing

import java.net.{ InetAddress, ServerSocket }

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.LoggerFactory
import javax.net.ssl.SSLServerSocketFactory

private[sslconfig] class TracingSSLServerSocketFactory(factory: => SSLServerSocketFactory, debug: SSLDebugConfig)(implicit lf: LoggerFactory) extends SSLServerSocketFactory with TraceLogger {

  override def createServerSocket = {
    tracer("createServerSocket", Map(), () => factory.createServerSocket())
  }

  override def createServerSocket(port: Int): ServerSocket = {
    tracer("createServerSocket", Map("port" -> port), () => factory.createServerSocket(port))
  }

  override def createServerSocket(port: Int, backlog: Int): ServerSocket = {
    tracer("createServerSocket", Map("port" -> port, "backlog" -> backlog), () => factory.createServerSocket(port, backlog))
  }

  override def createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress): ServerSocket = {
    tracer("createServerSocket", Map("port" -> port, "backlog" -> backlog, "ifAddress" -> ifAddress), () => factory.createServerSocket(port, backlog, ifAddress))
  }

  override def getDefaultCipherSuites: Array[String] = {
    tracer("getDefaultCipherSuites", Map(), () => factory.getDefaultCipherSuites)
  }

  override def getSupportedCipherSuites: Array[String] = {
    tracer("getSupportedCipherSuites", Map(), () => factory.getSupportedCipherSuites)
  }

  override def isLogEnabled(methodName: String, parameters: Map[String, Any]): Boolean = debug.all || debug.ssl
}
