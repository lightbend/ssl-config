/*
 * Copyright (C) 2015 - 2025 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.tracing

import java.nio.ByteBuffer
import java.security.KeyStore
import java.util.function.BiFunction
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult.HandshakeStatus
import javax.net.ssl.TrustManagerFactory

import scala.util.Try

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.LoggerFactory
import com.typesafe.sslconfig.util.NoopLogger
import org.specs2.mutable.Specification

class TracingSSLEngineSpec extends Specification {

  private implicit val loggerFactory: LoggerFactory = new LoggerFactory {
    override def apply(clazz: Class[?]) = new NoopLogger
    override def apply(name: String)    = new NoopLogger
  }

  private val debug    = SSLDebugConfig().withSsl(true)
  private val password = "changeit".toCharArray

  /** A server context with a certificate for `localhost` and a client context that trusts only that certificate. */
  private def contexts(): (SSLContext, SSLContext) = {
    val keyStore = KeyStore.getInstance("PKCS12")
    val input    = getClass.getResourceAsStream("/tracing-localhost.p12")
    try keyStore.load(input, password)
    finally input.close()

    val keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    keyManagers.init(keyStore, password)
    val server = SSLContext.getInstance("TLS")
    server.init(keyManagers.getKeyManagers, null, null)

    val trusted = KeyStore.getInstance("PKCS12")
    trusted.load(null, null)
    trusted.setCertificateEntry("localhost", keyStore.getCertificate("localhost"))
    val trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    trustManagers.init(trusted)
    val client = SSLContext.getInstance("TLS")
    client.init(null, trustManagers.getTrustManagers, null)

    (client, server)
  }

  /** A traced client engine for `host` that verifies the server's hostname, configured as an HTTPS client would. */
  private def tracedClient(client: SSLContext, host: String): SSLEngine = {
    val engine = new TracingSSLContext(client, debug).createSSLEngine(host, 443)
    engine.setUseClientMode(true)
    val parameters = engine.getSSLParameters
    parameters.setEndpointIdentificationAlgorithm("HTTPS")
    engine.setSSLParameters(parameters)
    engine
  }

  private def serverEngine(server: SSLContext): SSLEngine = {
    val engine = server.createSSLEngine()
    engine.setUseClientMode(false)
    engine
  }

  /** Runs a complete handshake between two engines in memory. */
  private def handshake(client: SSLEngine, server: SSLEngine): Unit = {
    val empty                             = ByteBuffer.allocate(0)
    val toServer                          = ByteBuffer.allocate(client.getSession.getPacketBufferSize)
    val toClient                          = ByteBuffer.allocate(server.getSession.getPacketBufferSize)
    val clientInput                       = ByteBuffer.allocate(client.getSession.getApplicationBufferSize)
    val serverInput                       = ByteBuffer.allocate(server.getSession.getApplicationBufferSize)
    def runTasks(engine: SSLEngine): Unit = {
      var task = engine.getDelegatedTask
      while (task != null) {
        task.run()
        task = engine.getDelegatedTask
      }
    }
    def handshaking(engine: SSLEngine) = engine.getHandshakeStatus != HandshakeStatus.NOT_HANDSHAKING

    client.beginHandshake()
    server.beginHandshake()
    var rounds = 0
    while ((handshaking(client) || handshaking(server)) && rounds < 100) {
      rounds += 1
      client.wrap(empty, toServer)
      runTasks(client)
      server.wrap(empty, toClient)
      runTasks(server)
      toServer.flip()
      server.unwrap(toServer, serverInput)
      toServer.compact()
      runTasks(server)
      toClient.flip()
      client.unwrap(toClient, clientInput)
      toClient.compact()
      runTasks(client)
    }
    if (handshaking(client) || handshaking(server)) throw new IllegalStateException("Handshake did not complete")
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

    "verify the server's hostname during a handshake" in {
      val (client, server) = contexts()

      val matching = Try(handshake(tracedClient(client, "localhost"), serverEngine(server)))
      val wrong    = Try(handshake(tracedClient(client, "wronghost.example"), serverEngine(server)))

      (matching must beSuccessfulTry)
        .and(wrong must beFailedTry)
        .and(wrong.failed.get.getMessage must contain("wronghost.example"))
    }

    "negotiate ALPN through traced engines" in {
      val (client, server)   = contexts()
      val tracedClientEngine = tracedClient(client, "localhost")
      val parameters         = tracedClientEngine.getSSLParameters
      parameters.setApplicationProtocols(Array("h2", "http/1.1"))
      tracedClientEngine.setSSLParameters(parameters)

      val serverDelegate     = serverEngine(server) // the delegate is a by-name parameter: evaluate it once
      val tracedServerEngine = new TracingSSLEngine(serverDelegate, debug)
      val selector           = new BiFunction[SSLEngine, java.util.List[String], String] {
        override def apply(engine: SSLEngine, protocols: java.util.List[String]): String =
          if (protocols.contains("h2")) "h2" else null
      }
      tracedServerEngine.setHandshakeApplicationProtocolSelector(selector)

      handshake(tracedClientEngine, tracedServerEngine)

      (tracedServerEngine.getHandshakeApplicationProtocolSelector must be(selector))
        .and(tracedClientEngine.getApplicationProtocol must_== "h2")
        .and(tracedServerEngine.getApplicationProtocol must_== "h2")
    }

    "expose the delegate's peer and handshake session" in {
      val delegate = SSLContext.getDefault.createSSLEngine("localhost", 8443)
      val traced   = new TracingSSLEngine(delegate, debug)

      (traced.getPeerHost must_== "localhost")
        .and(traced.getPeerPort must_== 8443)
        .and(traced.getHandshakeSession must beNull)
        .and(traced.getHandshakeApplicationProtocol must beNull)
    }
  }
}
