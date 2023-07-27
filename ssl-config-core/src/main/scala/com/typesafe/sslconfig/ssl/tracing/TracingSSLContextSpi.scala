/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.tracing

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.LoggerFactory

import java.security._
import javax.net.ssl._

private[sslconfig] class TracingSSLContext(context: SSLContext, debugConfig: SSLDebugConfig)(implicit loggerFactory: LoggerFactory) extends SSLContext(
  new TracingSSLContextSpi(context, debugConfig), context.getProvider, context.getProtocol
)

private[tracing] class TracingSSLContextSpi(ctx: => SSLContext, debug: SSLDebugConfig)(implicit loggerFactory: LoggerFactory) extends SSLContextSpi with TraceLogger {

  def isLogEnabled(methodName: String, parameters: Map[String, Any]): Boolean = debug.all || debug.sslctx

  override def engineInit(keyManagers: Array[KeyManager], trustManagers: Array[TrustManager], secureRandom: SecureRandom): Unit = {
    tracer("init", Map("keyManagers" -> keyManagers, "trustManagers" -> trustManagers, "secureRandom" -> secureRandom),
      () => ctx.init(keyManagers, trustManagers, secureRandom))
  }

  override def engineGetSocketFactory(): SSLSocketFactory = {
    tracer("getSocketFactory", Map(), () => new TracingSSLSocketFactory(ctx.getSocketFactory, debug))
  }

  override def engineGetServerSocketFactory(): SSLServerSocketFactory = {
    tracer("getServerSocketFactory", Map(), () => new TracingSSLServerSocketFactory(ctx.getServerSocketFactory, debug))
  }

  override def engineCreateSSLEngine(): SSLEngine = {
    tracer("createSSLEngine", Map(), () => {
      val engine = ctx.createSSLEngine()
      new TracingSSLEngine(engine, debug)
    })
  }

  override def engineCreateSSLEngine(host: String, port: Int): SSLEngine = {
    tracer("createSSLEngine", Map("host" -> host, "port" -> port), () => {
      val engine = ctx.createSSLEngine(host, port)
      new TracingSSLEngine(engine, debug)
    })
  }

  override def engineGetServerSessionContext(): SSLSessionContext = {
    tracer("getServerSessionContext", Map(), () => ctx.getServerSessionContext)
  }

  override def engineGetClientSessionContext(): SSLSessionContext = {
    tracer("getClientSessionContext", Map(), () => ctx.getClientSessionContext)
  }
}
