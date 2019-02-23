/*
 * Copyright (C) 2015 - 2019 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.tracing

import java.io.InputStream
import java.net.{ InetAddress, Socket }

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.LoggerFactory
import javax.net.ssl.SSLSocketFactory

private[sslconfig] class TracingSSLSocketFactory(factory: SSLSocketFactory, debug: SSLDebugConfig)(implicit loggerFactory: LoggerFactory) extends SSLSocketFactory with TraceLogger {
  override def createSocket(host: String, port: Int): Socket = {
    tracer("createSocket", Map("host" -> host, "port" -> port), () => factory.createSocket(host, port))
  }

  override def createSocket(): Socket = {
    tracer("createSocket", Map(), () => factory.createSocket())
  }

  override def createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket = {
    tracer("createSocket", Map("s" -> s, "host" -> host, "port" -> port, "autoClose" -> autoClose), () => factory.createSocket(s, host, port, autoClose))
  }

  override def createSocket(s: Socket, consumed: InputStream, autoClose: Boolean): Socket = {
    tracer("createSocket", Map("s" -> s, "consumed" -> consumed, "autoClose" -> autoClose), () => factory.createSocket(s, consumed, autoClose))
  }

  override def createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket = {
    tracer("createSocket", Map("host" -> host, "port" -> port, "localHost" -> localHost, "localPort" -> localPort),
      () => factory.createSocket(host, port, localHost, localPort))
  }

  override def createSocket(host: InetAddress, port: Int): Socket = {
    tracer("createSocket", Map("host" -> host, "port" -> port), () => factory.createSocket(host, port))
  }

  override def createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket = {
    tracer("createSocket", Map("address" -> address, "port" -> port, "localAddress" -> localAddress, "localPort" -> localPort),
      () => factory.createSocket(address, port, localAddress, localPort))
  }

  override def getDefaultCipherSuites: Array[String] = {
    tracer("getDefaultCipherSuites", Map(), () => factory.getDefaultCipherSuites)
  }

  override def getSupportedCipherSuites: Array[String] = {
    tracer("getSupportedCipherSuites", Map(), () => factory.getSupportedCipherSuites)
  }

  override def isLogEnabled(methodName: String, parameters: Map[String, Any]): Boolean = debug.all || debug.ssl
}
