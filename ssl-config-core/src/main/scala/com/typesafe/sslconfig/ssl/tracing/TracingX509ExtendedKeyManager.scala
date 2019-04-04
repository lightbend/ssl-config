/*
 * Copyright (C) 2015 - 2019 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.tracing

import java.net.Socket
import java.security.cert.X509Certificate
import java.security.{ Principal, PrivateKey }

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.LoggerFactory
import javax.net.ssl.{ KeyManager, SSLEngine, X509ExtendedKeyManager }

private[sslconfig] class TracingX509ExtendedKeyManager(supplier: => X509ExtendedKeyManager, debug: SSLDebugConfig)(implicit loggerFactory: LoggerFactory) extends X509ExtendedKeyManager with TraceLogger {

  override def chooseEngineClientAlias(keyTypes: Array[String], issuers: Array[Principal], engine: SSLEngine): String = {
    tracer("chooseEngineClientAlias", Map("keyTypes" -> keyTypes, "issuers" -> issuers, "engine" -> engine),
      () => supplier.chooseEngineClientAlias(keyTypes, issuers, engine))
  }

  override def chooseEngineServerAlias(keyTypes: String, issuers: Array[Principal], engine: SSLEngine): String = {
    tracer("chooseEngineServerAlias", Map("keyTypes" -> keyTypes, "issuers" -> issuers, "engine" -> engine), () => supplier.chooseEngineServerAlias(keyTypes, issuers, engine))
  }

  override def getClientAliases(keyType: String, issuers: Array[Principal]): Array[String] = {
    tracer("getClientAliases", Map("keyType" -> keyType, "issuers" -> issuers), () => supplier.getClientAliases(keyType, issuers))
  }

  override def chooseClientAlias(keyTypes: Array[String], issuers: Array[Principal], socket: Socket): String = {
    tracer("chooseClientAlias", Map("keyTypes" -> keyTypes, "issuers" -> issuers, "socket" -> socket), () => supplier.chooseClientAlias(keyTypes, issuers, socket))
  }

  override def getServerAliases(keyType: String, issuers: Array[Principal]): Array[String] = {
    tracer("getServerAliases", Map("keyType" -> keyType, "issuers" -> issuers), () => supplier.getServerAliases(keyType, issuers))
  }

  override def chooseServerAlias(keyType: String, issuers: Array[Principal], socket: Socket): String = {
    tracer("chooseServerAlias", Map("keyType" -> keyType, "issuers" -> issuers, "socket" -> socket), () => supplier.chooseServerAlias(keyType, issuers, socket))
  }

  override def getCertificateChain(alias: String): Array[X509Certificate] = {
    tracer("getCertificateChain", Map("alias" -> alias), () => supplier.getCertificateChain(alias))
  }

  override def getPrivateKey(alias: String): PrivateKey = {
    tracer("getPrivateKey", Map("alias" -> alias), () => supplier.getPrivateKey(alias))
  }

  override def isLogEnabled(methodName: String, parameters: Map[String, Any]): Boolean = debug.all || debug.keymanager
}