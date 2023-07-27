/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.tracing

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.LoggerFactory

import java.nio.ByteBuffer
import javax.net.ssl.{ SSLEngine, SSLEngineResult, SSLSession }

private[sslconfig] class TracingSSLEngine(engine: => SSLEngine, debug: SSLDebugConfig)(implicit loggerFactory: LoggerFactory) extends SSLEngine with TraceLogger {

  override def wrap(srcs: Array[ByteBuffer], offset: Int, length: Int, dst: ByteBuffer): SSLEngineResult = {
    tracer("wrap", Map("srcs" -> srcs, "offset" -> offset, "length" -> length, "dst" -> dst), () => engine.wrap(srcs, offset, length, dst))
  }

  override def unwrap(src: ByteBuffer, dsts: Array[ByteBuffer], offset: Int, length: Int): SSLEngineResult = {
    tracer("wrap", Map("src" -> src, "dsts" -> dsts, "offset" -> offset, "length" -> length), () => engine.unwrap(src, dsts, offset, length))
  }

  override def getDelegatedTask: Runnable = {
    tracer("getDelegatedTask", Map(), () => engine.getDelegatedTask)
  }

  override def closeInbound(): Unit = {
    tracer("closeInbound", Map(), () => engine.closeInbound())
  }

  override def isInboundDone: Boolean = {
    tracer("isInboundDone", Map(), () => engine.isInboundDone)
  }

  override def closeOutbound(): Unit = {
    tracer("isInboundDone", Map(), () => engine.closeOutbound())
  }

  override def isOutboundDone: Boolean = {
    tracer("isOutboundDone", Map(), () => engine.isOutboundDone)
  }

  override def getSupportedCipherSuites: Array[String] = {
    tracer("getSupportedCipherSuites", Map(), () => engine.getSupportedCipherSuites)
  }

  override def getEnabledCipherSuites: Array[String] = {
    tracer("getEnabledCipherSuites", Map(), () => engine.getEnabledCipherSuites)
  }

  override def setEnabledCipherSuites(suites: Array[String]): Unit = {
    tracer("setEnabledCipherSuites", Map("suites" -> suites), () => engine.setEnabledCipherSuites(suites))
  }

  override def getSupportedProtocols: Array[String] = {
    tracer("getSupportedProtocols", Map(), () => engine.getSupportedProtocols)
  }

  override def getEnabledProtocols: Array[String] = {
    tracer("getEnabledProtocols", Map(), () => engine.getEnabledProtocols)
  }

  override def setEnabledProtocols(protocols: Array[String]): Unit = {
    tracer("setEnabledProtocols", Map("protocols" -> protocols), () => engine.setEnabledProtocols(protocols))
  }

  override def getSession: SSLSession = {
    tracer("getSession", Map(), () => engine.getSession)
  }

  override def beginHandshake(): Unit = {
    tracer("beginHandshake", Map(), () => engine.beginHandshake())
  }

  override def getHandshakeStatus: SSLEngineResult.HandshakeStatus = {
    tracer("getHandshakeStatus", Map(), () => engine.getHandshakeStatus)
  }

  override def setUseClientMode(clientMode: Boolean): Unit = {
    tracer("setUseClientMode", Map("clientMode" -> clientMode), () => engine.setUseClientMode(clientMode))
  }

  override def getUseClientMode: Boolean = {
    tracer("getUseClientMode", Map(), () => engine.getUseClientMode)
  }

  override def setNeedClientAuth(needClientAuth: Boolean): Unit = {
    tracer("setNeedClientAuth", Map("needClientAuth" -> needClientAuth), () => engine.setNeedClientAuth(needClientAuth))
  }

  override def getNeedClientAuth: Boolean = {
    tracer("getNeedClientAuth", Map(), () => engine.getNeedClientAuth)
  }

  override def setWantClientAuth(wantClientAuth: Boolean): Unit = {
    tracer("setWantClientAuth", Map("wantClientAuth" -> wantClientAuth), () => engine.setWantClientAuth(wantClientAuth))
  }

  override def getWantClientAuth: Boolean = {
    tracer("getWantClientAuth", Map(), () => engine.getWantClientAuth)
  }

  override def setEnableSessionCreation(enableSessionCreation: Boolean): Unit = {
    tracer("setEnableSessionCreation", Map("enableSessionCreation" -> enableSessionCreation), () => engine.setEnableSessionCreation(enableSessionCreation))
  }

  override def getEnableSessionCreation: Boolean = {
    tracer("getEnableSessionCreation", Map(), () => engine.getEnableSessionCreation())
  }

  override def isLogEnabled(methodName: String, parameters: Map[String, Any]): Boolean = debug.all || debug.ssl
}
