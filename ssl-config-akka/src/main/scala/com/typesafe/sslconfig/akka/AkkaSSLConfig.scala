/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.akka

import java.security.KeyStore
import java.security.cert.CertPathValidatorException
import javax.net.ssl.{ X509TrustManager, TrustManagerFactory, SSLContext, HostnameVerifier }

import akka.actor._
import akka.event.Logging
import com.typesafe.config.Config
import com.typesafe.sslconfig.ssl._

object AkkaSSLConfig extends ExtensionId[AkkaSSLConfig] with ExtensionIdProvider {

  //////////////////// EXTENSION SETUP ///////////////////

  def apply()(implicit system: ActorSystem): AkkaSSLConfig = super.apply(system)

  override def lookup() = AkkaSSLConfig

  override def createExtension(system: ExtendedActorSystem): AkkaSSLConfig =
    new AkkaSSLConfig(system)
}

final class AkkaSSLConfig(system: ExtendedActorSystem) extends Extension {
  private val log = Logging(system, getClass)
  log.debug("Initializing AkkaSSLConfig extension...")

  val config = {
    val akkaOverrides = system.settings.config.getConfig("akka.ssl-config")
    val defaults = system.settings.config.getConfig("ssl-config")
    SSLConfigFactory.parse(akkaOverrides withFallback defaults)
  }

  val hostnameVerifier = {
    val v = system.dynamicAccess.createInstanceFor[HostnameVerifier](config.hostnameVerifierClass, Nil)
      .getOrElse(throw new Exception("Unable to obtain hostname verifier for class: " + config.hostnameVerifierClass))

    log.debug("hostnameVerifier: " + v)
    v
  }

  val sslEngineConfigurator = {
    val sslContext = if (config.default) {
      log.info("buildSSLContext: ssl-config.default is true, using default SSLContext")
      validateDefaultTrustManager(config)
      SSLContext.getDefault
    } else {
      // break out the static methods as much as we can...
      val keyManagerFactory = buildKeyManagerFactory(config)
      val trustManagerFactory = buildTrustManagerFactory(config)
      new ConfigSSLContextBuilder(config, keyManagerFactory, trustManagerFactory).build()
    }

    // protocols!
    val defaultParams = sslContext.getDefaultSSLParameters
    val defaultProtocols = defaultParams.getProtocols
    val protocols = configureProtocols(defaultProtocols, config)

    // ciphers!
    val defaultCiphers = defaultParams.getCipherSuites
    val cipherSuites = configureCipherSuites(defaultCiphers, config)

    new DefaultSSLEngineConfigurator(config, protocols, cipherSuites)
  }

  runChecks()

  def runChecks(): Unit = {
    // TODO, check: -Djdk.tls.ephemeralDHKeySize=2048
    // TODO ...
  }

  ////////////////// CONFIGURING //////////////////////

  def buildKeyManagerFactory(ssl: SSLConfig): KeyManagerFactoryWrapper = {
    val keyManagerAlgorithm = ssl.keyManagerConfig.algorithm
    new DefaultKeyManagerFactoryWrapper(keyManagerAlgorithm)
  }

  def buildTrustManagerFactory(ssl: SSLConfig): TrustManagerFactoryWrapper = {
    val trustManagerAlgorithm = ssl.trustManagerConfig.algorithm
    new DefaultTrustManagerFactoryWrapper(trustManagerAlgorithm)
  }

  def buildHostnameVerifier(conf: SSLConfig): HostnameVerifier = {
    val hostnameVerifierClass = conf.hostnameVerifierClass
    log.debug("buildHostnameVerifier: enabling hostname verification using {}", hostnameVerifierClass)

    try {
      hostnameVerifierClass.newInstance()
    } catch {
      case e: Exception ⇒
        throw new IllegalStateException("Cannot configure hostname verifier!", e)
    }
  }

  def validateDefaultTrustManager(sslConfig: SSLConfig) {
    // If we are using a default SSL context, we can't filter out certificates with weak algorithms
    // We ALSO don't have access to the trust manager from the SSLContext without doing horrible things
    // with reflection.
    //
    // However, given that the default SSLContextImpl will call out to the TrustManagerFactory and any
    // configuration with system properties will also apply with the factory, we can use the factory
    // method to recreate the trust manager and validate the trust certificates that way.
    //
    // This is really a last ditch attempt to satisfy https://wiki.mozilla.org/CA:MD5and1024 on root certificates.
    //
    // http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/sun/security/ssl/SSLContextImpl.java#79

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    tmf.init(null.asInstanceOf[KeyStore])
    val trustManager: X509TrustManager = tmf.getTrustManagers()(0).asInstanceOf[X509TrustManager]

    //    val disabledKeyAlgorithms = sslConfig.disabledKeyAlgorithms.getOrElse(Algorithms.disabledKeyAlgorithms) // was Option
    val disabledKeyAlgorithms = sslConfig.disabledKeyAlgorithms.mkString(",") // TODO Sub optimal, we got a Seq...
    val constraints = AlgorithmConstraintsParser.parseAll(AlgorithmConstraintsParser.line, disabledKeyAlgorithms).get.toSet
    val algorithmChecker = new AlgorithmChecker(keyConstraints = constraints, signatureConstraints = Set())
    for (cert ← trustManager.getAcceptedIssuers) {
      try {
        algorithmChecker.checkKeyAlgorithms(cert)
      } catch {
        case e: CertPathValidatorException ⇒
          log.warning("You are using ws.ssl.default=true and have a weak certificate in your default trust store!  (You can modify akka.ssl-config.disabledKeyAlgorithms to remove this message.)", e)
      }
    }
  }

  def configureProtocols(existingProtocols: Array[String], sslConfig: SSLConfig): Array[String] = {
    val definedProtocols = sslConfig.enabledProtocols match {
      case Some(configuredProtocols) ⇒
        // If we are given a specific list of protocols, then return it in exactly that order,
        // assuming that it's actually possible in the SSL context.
        configuredProtocols.filter(existingProtocols.contains).toArray

      case None ⇒
        // Otherwise, we return the default protocols in the given list.
        Protocols.recommendedProtocols.filter(existingProtocols.contains).toArray
    }

    val allowWeakProtocols = sslConfig.loose.allowWeakProtocols
    if (!allowWeakProtocols) {
      val deprecatedProtocols = Protocols.deprecatedProtocols
      for (deprecatedProtocol <- deprecatedProtocols) {
        if (definedProtocols.contains(deprecatedProtocol)) {
          throw new IllegalStateException(s"Weak protocol $deprecatedProtocol found in ws.ssl.protocols!")
        }
      }
    }
    definedProtocols
  }

  def configureCipherSuites(existingCiphers: Array[String], sslConfig: SSLConfig): Array[String] = {
    val definedCiphers = sslConfig.enabledCipherSuites match {
      case Some(configuredCiphers) ⇒
        // If we are given a specific list of ciphers, return it in that order.
        configuredCiphers.filter(existingCiphers.contains(_)).toArray

      case None ⇒
        Ciphers.recommendedCiphers.filter(existingCiphers.contains(_)).toArray
    }

    val allowWeakCiphers = sslConfig.loose.allowWeakCiphers
    if (!allowWeakCiphers) {
      val deprecatedCiphers = Ciphers.deprecatedCiphers
      for (deprecatedCipher <- deprecatedCiphers) {
        if (definedCiphers.contains(deprecatedCipher)) {
          throw new IllegalStateException(s"Weak cipher $deprecatedCipher found in ws.ssl.ciphers!")
        }
      }
    }
    definedCiphers
  }
}