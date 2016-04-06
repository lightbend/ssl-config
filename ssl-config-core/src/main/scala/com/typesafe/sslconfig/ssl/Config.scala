/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.ssl

import java.net.URL
import java.security.{ KeyStore, SecureRandom }
import javax.net.ssl.{ SSLParameters, HostnameVerifier, KeyManagerFactory, TrustManagerFactory }
import scala.collection.immutable

import com.typesafe.config.Config
import com.typesafe.sslconfig.util.EnrichedConfig

import scala.language.existentials

/**
 * Configuration for a keystore.
 *
 * A key store must either provide a file path, or a data String.
 *
 * @param storeType The store type. Defaults to the platform default store type (ie, JKS).
 * @param filePath The path of the key store file.
 * @param data The data to load the key store file from.
 * @param password The password to use to load the key store file, if the file is password protected.
 */
final case class KeyStoreConfig(storeType: String = KeyStore.getDefaultType,
                          filePath: Option[String] = None,
                          data: Option[String] = None,
                          password: Option[String] = None) {

  assert(filePath.isDefined ^ data.isDefined, "Either key store path or data must be defined, but not both.")
}

/**
 * Configuration for a trust store.
 *
 * A trust store must either provide a file path, or a data String.
 *
 * @param storeType The store type. Defaults to the platform default store type (ie, JKS).
 * @param filePath The path of the key store file.
 * @param data The data to load the key store file from.
 */
final case class TrustStoreConfig(storeType: String = KeyStore.getDefaultType,
                            filePath: Option[String],
                            data: Option[String]) {

  assert(filePath.isDefined ^ data.isDefined, "Either trust store path or data must be defined, but not both.")
}

/**
 * The key manager config.
 *
 * @param algorithm The algoritm to use.
 * @param keyStoreConfigs The key stores to use.
 */
final case class KeyManagerConfig(
  algorithm: String = KeyManagerFactory.getDefaultAlgorithm,
  keyStoreConfigs: immutable.Seq[KeyStoreConfig] = Nil)

/**
 * The trust manager config.
 *
 * @param algorithm The algorithm to use.
 * @param trustStoreConfigs The trust stores to use.
 */
final case class TrustManagerConfig(
  algorithm: String = TrustManagerFactory.getDefaultAlgorithm,
  trustStoreConfigs: immutable.Seq[TrustStoreConfig] = Nil)

/**
 * SSL debug configuration.
 */
final case class SSLDebugConfig(
    all: Boolean = false,
    ssl: Boolean = false,
    certpath: Boolean = false,
    ocsp: Boolean = false,
    record: Option[SSLDebugRecordOptions] = None,
    handshake: Option[SSLDebugHandshakeOptions] = None,
    keygen: Boolean = false,
    session: Boolean = false,
    defaultctx: Boolean = false,
    sslctx: Boolean = false,
    sessioncache: Boolean = false,
    keymanager: Boolean = false,
    trustmanager: Boolean = false,
    pluggability: Boolean = false) {

  /**
   * Whether any debug options are enabled.
   */
  def enabled = all || ssl || certpath || ocsp || record.isDefined || handshake.isDefined ||
    keygen || session || defaultctx || sslctx || sessioncache || keymanager || trustmanager ||
    pluggability

  def withAll = this.copy(all = true)

  def withCertPath = this.copy(certpath = true)

  def withOcsp = this.withCertPath.copy(ocsp = true) // technically a part of certpath, only available in 1.7+

  def withRecord(plaintext: Boolean = false, packet: Boolean = false) = {
    this.copy(record = Some(SSLDebugRecordOptions(plaintext, packet)))
  }

  def withHandshake(data: Boolean = false, verbose: Boolean = false) = {
    this.copy(handshake = Some(SSLDebugHandshakeOptions(data, verbose)))
  }

  def withSSL = this.copy(ssl = true)

  def withKeygen = this.copy(keygen = true)

  def withSession = this.copy(session = true)

  def withDefaultContext = this.copy(defaultctx = true)

  def withSSLContext = this.copy(sslctx = true)

  def withSessionCache = this.copy(sessioncache = true)

  def withKeyManager = this.copy(keymanager = true)

  def withTrustManager = this.copy(trustmanager = true)

  def withPluggability = this.copy(pluggability = true)

}

/**
 * SSL handshake debugging options.
 */
final case class SSLDebugHandshakeOptions(data: Boolean = false, verbose: Boolean = false)

/**
 * SSL record debugging options.
 */
final case class SSLDebugRecordOptions(plaintext: Boolean = false, packet: Boolean = false)

/**
 * Configuration for specifying loose (potentially dangerous) ssl config.
 *
 * @param allowWeakCiphers Whether weak ciphers should be allowed or not.
 * @param allowWeakProtocols Whether weak protocols should be allowed or not.
 * @param allowLegacyHelloMessages Whether legacy hello messages should be allowed or not. If None, uses the platform
 *                                 default.
 * @param allowUnsafeRenegotiation Whether unsafe renegotiation should be allowed or not. If None, uses the platform
 *                                 default.
 * @param disableHostnameVerification Whether hostname verification should be disabled.
 * @param disableSNI Whether SNI should be disabled (up to client library to respect this setting or not)
 * @param acceptAnyCertificate Whether any X.509 certificate should be accepted or not.
 */
final case class SSLLooseConfig private[sslconfig] (
  allowWeakCiphers: Boolean = false,
  allowWeakProtocols: Boolean = false,
  allowLegacyHelloMessages: Option[Boolean] = None,
  allowUnsafeRenegotiation: Option[Boolean] = None,
  disableHostnameVerification: Boolean = false,
  disableSNI: Boolean = false,
  acceptAnyCertificate: Boolean = false)

/**
 * Carries values which will be later set on an [[SSLParameters]] object.
 *
 * @param clientAuth see [[ClientAuth]] for detailed docs on ClientAuth modes
 */
final case class SSLParametersConfig(
  clientAuth: ClientAuth = ClientAuth.Default,
  protocols: immutable.Seq[String] = Nil)

/**
 * The SSL configuration.
 *
 * @param default Whether we should use the default JVM SSL configuration or not.
 * @param protocol The SSL protocol to use. Defaults to TLSv1.2.
 * @param checkRevocation Whether revocation lists should be checked, if None, defaults to platform default setting.
 * @param revocationLists The revocation lists to check.
 * @param enabledCipherSuites If defined, override the platform default cipher suites.
 * @param enabledProtocols If defined, override the platform default protocols.
 * @param disabledSignatureAlgorithms The disabled signature algorithms.
 * @param disabledKeyAlgorithms The disabled key algorithms.
 * @param keyManagerConfig The key manager configuration.
 * @param trustManagerConfig The trust manager configuration.
 * @param hostnameVerifierClass The hostname verifier class.
 * @param secureRandom The SecureRandom instance to use. Let the platform choose if None.
 * @param debug The debug config.
 * @param loose Loose configuratino parameters
 */
final case class SSLConfig(
  default: Boolean = false,
  protocol: String = "TLSv1.2",
  checkRevocation: Option[Boolean] = None,
  revocationLists: Option[immutable.Seq[URL]] = None,
  enabledCipherSuites: Option[immutable.Seq[String]] = None,
  enabledProtocols: Option[immutable.Seq[String]] = Some(List("TLSv1.2", "TLSv1.1", "TLSv1")),
  disabledSignatureAlgorithms: immutable.Seq[String] = List("MD2", "MD4", "MD5"),
  disabledKeyAlgorithms: immutable.Seq[String] = List("RSA keySize < 2048", "DSA keySize < 2048", "EC keySize < 224"),
  sslParametersConfig: SSLParametersConfig = SSLParametersConfig(),
  keyManagerConfig: KeyManagerConfig = KeyManagerConfig(),
  trustManagerConfig: TrustManagerConfig = TrustManagerConfig(),
  hostnameVerifierClass: Class[_ <: HostnameVerifier] = classOf[DefaultHostnameVerifier],
  secureRandom: Option[SecureRandom] = None,
  debug: SSLDebugConfig = SSLDebugConfig(),
  loose: SSLLooseConfig = SSLLooseConfig())

/**
 * Factory for creating SSL config (for use from Java).
 */
object SSLConfigFactory {

  /** Parses the given config into an SSLConfig object, as given (does not select sub-config sections). */
  def parse(config: Config): SSLConfig =
    new SSLConfigParser(com.typesafe.sslconfig.util.EnrichedConfig(config), getClass.getClassLoader).parse()

  /**
   * Create an instance of the default config
   */
  def defaultConfig = SSLConfig()
}

class SSLConfigParser(c: EnrichedConfig, classLoader: ClassLoader) {

  def parse(): SSLConfig = {

    val default = c.get[Boolean]("default")
    val protocol = c.get[String]("protocol")
    val checkRevocation = c.getOptional[Boolean]("checkRevocation")
    val revocationLists: Option[immutable.Seq[URL]] = Some(
      c.getSeq[String]("revocationLists").map(new URL(_))
    ).filter(_.nonEmpty)

    val debug = parseDebug(c.get[EnrichedConfig]("debug"))
    val looseOptions = parseLooseOptions(c.get[EnrichedConfig]("loose"))

    val ciphers = Some(c.getSeq[String]("enabledCipherSuites")).filter(_.nonEmpty)
    val protocols = Some(c.getSeq[String]("enabledProtocols")).filter(_.nonEmpty)

    val hostnameVerifierClass = c.getOptional[String]("hostnameVerifierClass") match {
      case None       => classOf[DefaultHostnameVerifier]
      case Some(fqcn) => classLoader.loadClass(fqcn).asSubclass(classOf[HostnameVerifier])
    }

    val disabledSignatureAlgorithms = c.getSeq[String]("disabledSignatureAlgorithms")
    val disabledKeyAlgorithms = c.getSeq[String]("disabledKeyAlgorithms")

    val keyManagers = parseKeyManager(c.get[EnrichedConfig]("keyManager"))

    val trustManagers = parseTrustManager(c.get[EnrichedConfig]("trustManager"))

    val sslParametersConfig = parseSSLParameters(c.get[EnrichedConfig]("sslParameters"))

    SSLConfig(
      default = default,
      protocol = protocol,
      checkRevocation = checkRevocation,
      revocationLists = revocationLists,
      enabledCipherSuites = ciphers,
      enabledProtocols = protocols,
      keyManagerConfig = keyManagers,
      hostnameVerifierClass = hostnameVerifierClass,
      disabledSignatureAlgorithms = disabledSignatureAlgorithms,
      disabledKeyAlgorithms = disabledKeyAlgorithms,
      sslParametersConfig = sslParametersConfig,
      trustManagerConfig = trustManagers,
      secureRandom = None,
      debug = debug,
      loose = looseOptions)
  }

  /**
   * Parses "ws.ssl.loose" section.
   */
  def parseLooseOptions(config: EnrichedConfig): SSLLooseConfig = {

    val allowWeakProtocols = config.get[Boolean]("allowWeakProtocols")
    val allowWeakCiphers = config.get[Boolean]("allowWeakCiphers")
    val allowMessages = config.getOptional[Boolean]("allowLegacyHelloMessages")
    val allowUnsafeRenegotiation = config.getOptional[Boolean]("allowUnsafeRenegotiation")
    val disableHostnameVerification = config.get[Boolean]("disableHostnameVerification")
    val disableSNI = config.get[Boolean]("disableSNI")
    val acceptAnyCertificate = config.get[Boolean]("acceptAnyCertificate")

    SSLLooseConfig(
      allowWeakCiphers = allowWeakCiphers,
      allowWeakProtocols = allowWeakProtocols,
      allowLegacyHelloMessages = allowMessages,
      allowUnsafeRenegotiation = allowUnsafeRenegotiation,
      disableHostnameVerification = disableHostnameVerification,
      disableSNI = disableSNI,
      acceptAnyCertificate = acceptAnyCertificate
    )
  }

  /**
   * Parses the "ws.ssl.debug" section.
   */
  def parseDebug(config: EnrichedConfig): SSLDebugConfig = {
    val certpath = config.get[Boolean]("certpath")

    if (config.get[Boolean]("all")) {
      SSLDebugConfig(all = true, certpath = certpath)
    } else {

      val record: Option[SSLDebugRecordOptions] = if (config.get[Boolean]("record")) {
        val plaintext = config.get[Boolean]("plaintext")
        val packet = config.get[Boolean]("packet")
        Some(SSLDebugRecordOptions(plaintext = plaintext, packet = packet))
      } else None

      val handshake = if (config.get[Boolean]("handshake")) {
        val data = config.get[Boolean]("data")
        val verbose = config.get[Boolean]("verbose")
        Some(SSLDebugHandshakeOptions(data = data, verbose = verbose))
      } else {
        None
      }

      val keygen = config.get[Boolean]("keygen")
      val session = config.get[Boolean]("session")
      val defaultctx = config.get[Boolean]("defaultctx")
      val sslctx = config.get[Boolean]("sslctx")
      val sessioncache = config.get[Boolean]("sessioncache")
      val keymanager = config.get[Boolean]("keymanager")
      val trustmanager = config.get[Boolean]("trustmanager")
      val pluggability = config.get[Boolean]("pluggability")
      val ssl = config.get[Boolean]("ssl")

      SSLDebugConfig(
        ssl = ssl,
        record = record,
        handshake = handshake,
        keygen = keygen,
        session = session,
        defaultctx = defaultctx,
        sslctx = sslctx,
        sessioncache = sessioncache,
        keymanager = keymanager,
        trustmanager = trustmanager,
        pluggability = pluggability,
        certpath = certpath)
    }
  }

  /**
   * Parses the "ws.ssl.keyManager { stores = [ ... ]" section of configuration.
   */
  def parseKeyStoreInfo(config: EnrichedConfig): KeyStoreConfig = {
    val storeType = config.getOptional[String]("type").getOrElse(KeyStore.getDefaultType)
    val path = config.getOptional[String]("path")
    val data = config.getOptional[String]("data")
    val password = config.getOptional[String]("password")

    KeyStoreConfig(filePath = path, storeType = storeType, data = data, password = password)
  }

  /**
   * Parses the "ws.ssl.trustManager { stores = [ ... ]" section of configuration.
   */
  def parseTrustStoreInfo(config: EnrichedConfig): TrustStoreConfig = {
    val storeType = config.getOptional[String]("type").getOrElse(KeyStore.getDefaultType)
    val path = config.getOptional[String]("path")
    val data = config.getOptional[String]("data")

    TrustStoreConfig(filePath = path, storeType = storeType, data = data)
  }

  /**
   * Parses the "ws.ssl.keyManager" section of the configuration.
   */
  def parseKeyManager(config: EnrichedConfig): KeyManagerConfig = {

    val algorithm = config.getOptional[String]("algorithm") match {
      case None        => KeyManagerFactory.getDefaultAlgorithm
      case Some(other) => other
    }

    val keyStoreInfos = config.getPrototypedSeq("stores").map { store =>
      parseKeyStoreInfo(store)
    }

    KeyManagerConfig(algorithm, keyStoreInfos)
  }

  /**
   * Parses the "ws.ssl.trustManager" section of configuration.
   */
  def parseTrustManager(config: EnrichedConfig): TrustManagerConfig = {
    val algorithm = config.getOptional[String]("algorithm") match {
      case None        => TrustManagerFactory.getDefaultAlgorithm
      case Some(other) => other
    }

    val trustStoreInfos = config.getPrototypedSeq("stores").map { store =>
      parseTrustStoreInfo(store)
    }

    TrustManagerConfig(algorithm, trustStoreInfos)
  }

  def parseSSLParameters(config: EnrichedConfig): SSLParametersConfig = {
    // could instanciate SSLParameters directly, but seems less clean, here we only parse config

    val clientAuth = config.getOptional[String]("clientAuth") match {
      case Some("none")           => ClientAuth.None
      case Some("want")           => ClientAuth.Want
      case Some("need")           => ClientAuth.Need
      case None | Some("default") => ClientAuth.Default
    }

    val protocols = config.getSeq[String]("protocols")

    SSLParametersConfig(clientAuth, protocols)
  }
}

/**
 * An SSLEngine can either demand, allow or ignore its peer’s authentication
 * (via certificates), where `Need` will fail the handshake if the peer does
 * not provide valid credentials, `Want` allows the peer to send credentials
 * and verifies them if provided, and `None` disables peer certificate
 * verification.
 *
 * See the documentation for `SSLEngine::setWantClientAuth` for more information.
 */
sealed abstract class ClientAuth
object ClientAuth {
  case object Default extends ClientAuth
  case object None extends ClientAuth
  case object Want extends ClientAuth
  case object Need extends ClientAuth

  def none: ClientAuth = None
  def want: ClientAuth = Want
  def need: ClientAuth = Need
  def defaultAuth: ClientAuth = Default // since `default` is a Java keyword
}
