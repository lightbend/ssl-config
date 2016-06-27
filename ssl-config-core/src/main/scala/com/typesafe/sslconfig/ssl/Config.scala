/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.ssl

import java.net.URL
import java.security.{KeyStore, SecureRandom}
import java.util.Optional
import javax.net.ssl.{HostnameVerifier, KeyManagerFactory, SSLParameters, TrustManagerFactory}

import scala.collection.immutable
import com.typesafe.config.Config
import com.typesafe.sslconfig.util.EnrichedConfig

import scala.language.existentials

/**
 * Configuration for a keystore.
 *
 * A key store must either provide a file path, or a data String.
 *
 * Either key store path or data must be defined, but not both.
 *
 * @param data The data to load the key store file from.
 * @param filePath The path of the key store file.
 * @param password The password to use to load the key store file, if the file is password protected.
 * @param storeType The store type. Defaults to the platform default store type (ie, JKS).
 */
final class KeyStoreConfig private[sslconfig](
  val data: Option[String],
  val filePath: Option[String],
  val password: Option[String] = None,
  val storeType: String = KeyStore.getDefaultType) {
  assert(filePath.isDefined ^ data.isDefined, "Either key store path or data must be defined, but not both.")

  /** Disables `filePath` – only one of those can be used at any given time. */
  def withData(data: Option[String]): KeyStoreConfig = copy(data = data, filePath = None)
  /** Disables `data` – only one of those can be used at any given time. */
  def withFilePath(filePath: Option[String]): KeyStoreConfig = copy(filePath = filePath, data = None)
  def withPassword(value: Option[String]): KeyStoreConfig = copy(password = value)
  def withStoreType(value: String): KeyStoreConfig = copy(storeType = value)

  private def copy(
    data: Option[String] = data,
    filePath: Option[String] = filePath,
    password: Option[String] = password,
    storeType: String = storeType): KeyStoreConfig = new KeyStoreConfig(
      data = data,
      filePath = filePath,
      password = password,
      storeType = storeType)

  override def toString =
    s"""KeyStoreConfig(${data},${filePath},,${storeType})"""
}
object KeyStoreConfig {
  def apply(data: Option[String], filePath: Option[String]) = new KeyStoreConfig(data, filePath)
  /** Java API */
  def getInstance(data: Optional[String], filePath: Optional[String]) =
    apply(Option(data.orElse(null)), Option(filePath.orElse(null)))
}


/**
 * The trust manager config.
 *
 * @param algorithm The algorithm to use.
 * @param trustStoreConfigs The trust stores to use.
 */
final class TrustManagerConfig private[sslconfig](
  val algorithm: String = TrustManagerFactory.getDefaultAlgorithm,
  val trustStoreConfigs: immutable.Seq[TrustStoreConfig] = Nil) {

  def withAlgorithm(value: String): TrustManagerConfig = copy(algorithm = value)
  def withTrustStoreConfigs(value: scala.collection.immutable.Seq[com.typesafe.sslconfig.ssl.TrustStoreConfig]): TrustManagerConfig = copy(trustStoreConfigs = value)

  private def copy(
    algorithm: String = algorithm,
    trustStoreConfigs: scala.collection.immutable.Seq[com.typesafe.sslconfig.ssl.TrustStoreConfig] = trustStoreConfigs): TrustManagerConfig = new TrustManagerConfig(
      algorithm = algorithm,
      trustStoreConfigs = trustStoreConfigs)

  override def toString =
    s"""TrustManagerConfig(${algorithm},${trustStoreConfigs})"""
}
object TrustManagerConfig {
  def apply() = new TrustManagerConfig()
  /** Java API */
  def getInstance() = apply()
}


/**
 * Configuration for a trust store.
 *
 * A trust store must either provide a file path, or a data String.
 *
 * @param data The data to load the key store file from.
 * @param filePath The path of the key store file.
 * @param password The password to use to load the key store file, if the file is password protected.
 * @param storeType The store type. Defaults to the platform default store type (ie, JKS).
 */
final class TrustStoreConfig private[sslconfig](
  val data: Option[String],
  val filePath: Option[String],
  val password: Option[String] = None,
  val storeType: String = KeyStore.getDefaultType) {

  assert(filePath.isDefined ^ data.isDefined, "Either trust store path or data must be defined, but not both.")

  /** Disables `filePath` – only one of those can be used at any given time. */
  def withData(data: Option[String]): TrustStoreConfig = copy(data = data, filePath = None)
  /** Disables `data` – only one of those can be used at any given time. */
  def withFilePath(filePath: Option[String]): TrustStoreConfig = copy(filePath = filePath, data = None)
  def withPassword(password: Option[String]): TrustStoreConfig = copy(password = password)
  def withStoreType(value: String): TrustStoreConfig = copy(storeType = value)

  private def copy(
    data: Option[String] = data,
    filePath: Option[String] = filePath,
    password: Option[String] = password,
    storeType: String = storeType): TrustStoreConfig = new TrustStoreConfig(
      data = data,
      filePath = filePath,
      password = password,
      storeType = storeType)

  override def toString =
    s"""TrustStoreConfig(${data},${filePath},,${storeType})"""
}
object TrustStoreConfig {
  def apply(data: Option[String], filePath: Option[String]) = new TrustStoreConfig(data, filePath)
  /** Java API */
  def getInstance(data: Optional[String], filePath: Optional[String]) =
    apply(Option(data.orElse(null)), Option(filePath.orElse(null)))
}


/**
 * The key manager config.
 *
 * @param algorithm The algoritm to use.
 * @param keyStoreConfigs The key stores to use.
 */
final class KeyManagerConfig private[sslconfig](
  val algorithm: String = KeyManagerFactory.getDefaultAlgorithm,
  val keyStoreConfigs: immutable.Seq[KeyStoreConfig] = Nil) {

  def withAlgorithm(value: String): KeyManagerConfig = copy(algorithm = value)
  def withKeyStoreConfigs(value: scala.collection.immutable.Seq[com.typesafe.sslconfig.ssl.KeyStoreConfig]): KeyManagerConfig = copy(keyStoreConfigs = value)

  private def copy(
    algorithm: String = algorithm,
    keyStoreConfigs: scala.collection.immutable.Seq[com.typesafe.sslconfig.ssl.KeyStoreConfig] = keyStoreConfigs): KeyManagerConfig = new KeyManagerConfig(
      algorithm = algorithm,
      keyStoreConfigs = keyStoreConfigs)

  override def toString =
    s"""KeyManagerConfig(${algorithm},${keyStoreConfigs})"""
}
object KeyManagerConfig {
  def apply() = new KeyManagerConfig()
  /** Java API */
  def getInstance() = apply()
}


/**
 * SSL debug configuration.
 */
final class SSLDebugConfig private[sslconfig](
  val all: Boolean = false,
  val certpath: Boolean = false,
  val defaultctx: Boolean = false,
  val handshake: Option[com.typesafe.sslconfig.ssl.SSLDebugHandshakeOptions] = None,
  val keygen: Boolean = false,
  val keymanager: Boolean = false,
  val ocsp: Boolean = false,
  val pluggability: Boolean = false,
  val record: Option[com.typesafe.sslconfig.ssl.SSLDebugRecordOptions] = None,
  val session: Boolean = false,
  val sessioncache: Boolean = false,
  val ssl: Boolean = false,
  val sslctx: Boolean = false,
  val trustmanager: Boolean = false) {

  /**
   * Whether any debug options are enabled.
   */
  def enabled = all || ssl || certpath || ocsp || record.isDefined || handshake.isDefined ||
    keygen || session || defaultctx || sslctx || sessioncache || keymanager || trustmanager ||
    pluggability

  def withAll(value: Boolean): SSLDebugConfig = copy(all = value)
  def withCertPath(value: Boolean): SSLDebugConfig = copy(certpath = value)
  def withDefaultContext(value: Boolean): SSLDebugConfig = copy(defaultctx = value)
  def withHandshake(value: Option[com.typesafe.sslconfig.ssl.SSLDebugHandshakeOptions]): SSLDebugConfig = copy(handshake = value)
  def withKeygen(value: Boolean): SSLDebugConfig = copy(keygen = value)
  def withKeymanager(value: Boolean): SSLDebugConfig = copy(keymanager = value)
  def withOcsp(value: Boolean): SSLDebugConfig = copy(ocsp = value)
  def withPluggability(value: Boolean): SSLDebugConfig = copy(pluggability = value)
  def withRecord(value: Option[com.typesafe.sslconfig.ssl.SSLDebugRecordOptions]): SSLDebugConfig = copy(record = value)
  def withSession(value: Boolean): SSLDebugConfig = copy(session = value)
  def withSessioncache(value: Boolean): SSLDebugConfig = copy(sessioncache = value)
  def withSsl(value: Boolean): SSLDebugConfig = copy(ssl = value)
  def withSslctx(value: Boolean): SSLDebugConfig = copy(sslctx = value)
  def withTrustmanager(value: Boolean): SSLDebugConfig = copy(trustmanager = value)

  private def copy(
    all: Boolean = all,
    certpath: Boolean = certpath,
    defaultctx: Boolean = defaultctx,
    handshake: Option[com.typesafe.sslconfig.ssl.SSLDebugHandshakeOptions] = handshake,
    keygen: Boolean = keygen,
    keymanager: Boolean = keymanager,
    ocsp: Boolean = ocsp,
    pluggability: Boolean = pluggability,
    record: Option[com.typesafe.sslconfig.ssl.SSLDebugRecordOptions] = record,
    session: Boolean = session,
    sessioncache: Boolean = sessioncache,
    ssl: Boolean = ssl,
    sslctx: Boolean = sslctx,
    trustmanager: Boolean = trustmanager): SSLDebugConfig = new SSLDebugConfig(
      all = all,
      certpath = certpath,
      defaultctx = defaultctx,
      handshake = handshake,
      keygen = keygen,
      keymanager = keymanager,
      ocsp = ocsp,
      pluggability = pluggability,
      record = record,
      session = session,
      sessioncache = sessioncache,
      ssl = ssl,
      sslctx = sslctx,
      trustmanager = trustmanager)

  override def toString =
    s"""SSLDebugConfig(${all},${certpath},${defaultctx},${handshake},${keygen},${keymanager},${ocsp},${pluggability},${record},${session},${sessioncache},${ssl},${sslctx},${trustmanager})"""
}
object SSLDebugConfig {
  def apply() = new SSLDebugConfig()
  /** Java API */
  def getInstance() = apply()
}


/**
 * SSL handshake debugging options.
 */
final class SSLDebugHandshakeOptions private[sslconfig](
  val data: Boolean = false,
  val verbose: Boolean = false) {

  def withData(value: Boolean): SSLDebugHandshakeOptions = copy(data = value)
  def withVerbose(value: Boolean): SSLDebugHandshakeOptions = copy(verbose = value)

  private def copy(
    data: Boolean = data,
    verbose: Boolean = verbose): SSLDebugHandshakeOptions = new SSLDebugHandshakeOptions(
      data = data,
      verbose = verbose)

  override def toString =
    s"""SSLDebugHandshakeOptions(${data},${verbose})"""
}
object SSLDebugHandshakeOptions {
  def apply() = new SSLDebugHandshakeOptions()
  /** Java API */
  def getInstance() = apply()
}



/**
 * SSL record debugging options.
 */
final class SSLDebugRecordOptions private[sslconfig](
  val packet: Boolean = false,
  val plaintext: Boolean = false) {

  def withPacket(value: Boolean): SSLDebugRecordOptions = copy(packet = value)
  def withPlaintext(value: Boolean): SSLDebugRecordOptions = copy(plaintext = value)

  private def copy(
    packet: Boolean = packet,
    plaintext: Boolean = plaintext): SSLDebugRecordOptions = new SSLDebugRecordOptions(
      packet = packet,
      plaintext = plaintext)

  override def toString =
    s"""SSLDebugRecordOptions(${packet},${plaintext})"""
}
object SSLDebugRecordOptions {
  def apply() = new SSLDebugRecordOptions()
  /** Java API */
  def getInstance() = apply()
}



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
final class SSLLooseConfig private[sslconfig](
  val acceptAnyCertificate: Boolean = false,
  val allowLegacyHelloMessages: Option[Boolean] = None,
  val allowUnsafeRenegotiation: Option[Boolean]= None,
  val allowWeakCiphers: Boolean = false,
  val allowWeakProtocols: Boolean = false,
  val disableHostnameVerification: Boolean = false,
  val disableSNI: Boolean = false) {

  def withAcceptAnyCertificate(value: Boolean): SSLLooseConfig = copy(acceptAnyCertificate = value)
  def withAllowLegacyHelloMessages(value: Option[Boolean]): SSLLooseConfig = copy(allowLegacyHelloMessages = value)
  def withAllowUnsafeRenegotiation(value: Option[Boolean]): SSLLooseConfig = copy(allowUnsafeRenegotiation = value)
  def withAllowWeakCiphers(value: Boolean): SSLLooseConfig = copy(allowWeakCiphers = value)
  def withAllowWeakProtocols(value: Boolean): SSLLooseConfig = copy(allowWeakProtocols = value)
  def withDisableHostnameVerification(value: Boolean): SSLLooseConfig = copy(disableHostnameVerification = value)
  def withDisableSNI(value: Boolean): SSLLooseConfig = copy(disableSNI = value)

  private def copy(
    acceptAnyCertificate: Boolean = acceptAnyCertificate,
    allowLegacyHelloMessages: Option[Boolean] = allowLegacyHelloMessages,
    allowUnsafeRenegotiation: Option[Boolean] = allowUnsafeRenegotiation,
    allowWeakCiphers: Boolean = allowWeakCiphers,
    allowWeakProtocols: Boolean = allowWeakProtocols,
    disableHostnameVerification: Boolean = disableHostnameVerification,
    disableSNI: Boolean = disableSNI): SSLLooseConfig = new SSLLooseConfig(
      acceptAnyCertificate = acceptAnyCertificate,
      allowLegacyHelloMessages = allowLegacyHelloMessages,
      allowUnsafeRenegotiation = allowUnsafeRenegotiation,
      allowWeakCiphers = allowWeakCiphers,
      allowWeakProtocols = allowWeakProtocols,
      disableHostnameVerification = disableHostnameVerification,
      disableSNI = disableSNI)

  override def toString =
    s"""SSLLooseConfig(${acceptAnyCertificate},${allowLegacyHelloMessages},${allowUnsafeRenegotiation},${allowWeakCiphers},${allowWeakProtocols},${disableHostnameVerification},${disableSNI})"""
}
object SSLLooseConfig {
  def apply() = new SSLLooseConfig()
  /** Java API */
  def getInstance() = apply()
}



/**
 * Carries values which will be later set on an [[SSLParameters]] object.
 *
 * @param clientAuth see [[ClientAuth]] for detailed docs on ClientAuth modes
 */
final class SSLParametersConfig private[sslconfig](
  val clientAuth: ClientAuth = ClientAuth.Default,
  val protocols: scala.collection.immutable.Seq[String] = Nil) {

  def withClientAuth(value: com.typesafe.sslconfig.ssl.ClientAuth): SSLParametersConfig = copy(clientAuth = value)
  def withProtocols(value: scala.collection.immutable.Seq[String]): SSLParametersConfig = copy(protocols = value)

  private def copy(
    clientAuth: com.typesafe.sslconfig.ssl.ClientAuth = clientAuth,
    protocols: scala.collection.immutable.Seq[String] = protocols): SSLParametersConfig = new SSLParametersConfig(
      clientAuth = clientAuth,
      protocols = protocols)

  override def toString =
    s"""SSLParametersConfig(${clientAuth},${protocols})"""
}
object SSLParametersConfig {
  def apply() = new SSLParametersConfig()
  /** Java API */
  def getInstance() = apply()
}


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
final class SSLConfigSettings private[sslconfig](
  val default: Boolean = false,
  val protocol: String = "TLSv1.2",
  val checkRevocation: Option[Boolean] = None,
  val revocationLists: Option[immutable.Seq[URL]] = None,
  val enabledCipherSuites: Option[immutable.Seq[String]] = None,
  val enabledProtocols: Option[immutable.Seq[String]] = Some(List("TLSv1.2", "TLSv1.1", "TLSv1")),
  val disabledSignatureAlgorithms: immutable.Seq[String] = List("MD2", "MD4", "MD5"),
  val disabledKeyAlgorithms: immutable.Seq[String] = List("RSA keySize < 2048", "DSA keySize < 2048", "EC keySize < 224"),
  val sslParametersConfig: SSLParametersConfig = SSLParametersConfig(),
  val keyManagerConfig: KeyManagerConfig = KeyManagerConfig(),
  val trustManagerConfig: TrustManagerConfig = TrustManagerConfig(),
  val hostnameVerifierClass: Class[_ <: HostnameVerifier] = classOf[DefaultHostnameVerifier],
  val secureRandom: Option[SecureRandom] = None,
  val debug: SSLDebugConfig = SSLDebugConfig(),
  val loose: SSLLooseConfig = SSLLooseConfig()) {

  def withCheckRevocation(value: Option[Boolean]): SSLConfigSettings = copy(checkRevocation = value)
  def withDebug(value: com.typesafe.sslconfig.ssl.SSLDebugConfig): SSLConfigSettings = copy(debug = value)
  def withDefault(value: Boolean): SSLConfigSettings = copy(default = value)
  def withDisabledKeyAlgorithms(value: scala.collection.immutable.Seq[String]): SSLConfigSettings = copy(disabledKeyAlgorithms = value)
  def withDisabledSignatureAlgorithms(value: scala.collection.immutable.Seq[String]): SSLConfigSettings = copy(disabledSignatureAlgorithms = value)
  def withEnabledCipherSuites(value: Option[scala.collection.immutable.Seq[String]]): SSLConfigSettings = copy(enabledCipherSuites = value)
  def withEnabledProtocols(value: Option[scala.collection.immutable.Seq[String]]): SSLConfigSettings = copy(enabledProtocols = value)
  def withHostnameVerifierClass(value: Class[javax.net.ssl.HostnameVerifier]): SSLConfigSettings = copy(hostnameVerifierClass = value)
  def withKeyManagerConfig(value: com.typesafe.sslconfig.ssl.KeyManagerConfig): SSLConfigSettings = copy(keyManagerConfig = value)
  def withLoose(value: com.typesafe.sslconfig.ssl.SSLLooseConfig): SSLConfigSettings = copy(loose = value)
  def withProtocol(value: String): SSLConfigSettings = copy(protocol = value)
  def withRevocationLists(value: Option[scala.collection.immutable.Seq[java.net.URL]]): SSLConfigSettings = copy(revocationLists = value)
  def withSecureRandom(value: Option[java.security.SecureRandom]): SSLConfigSettings = copy(secureRandom = value)
  def withSslParametersConfig(value: com.typesafe.sslconfig.ssl.SSLParametersConfig): SSLConfigSettings = copy(sslParametersConfig = value)
  def withTrustManagerConfig(value: com.typesafe.sslconfig.ssl.TrustManagerConfig): SSLConfigSettings = copy(trustManagerConfig = value)

  private def copy(
    checkRevocation: Option[Boolean] = checkRevocation,
    debug: com.typesafe.sslconfig.ssl.SSLDebugConfig = debug,
    default: Boolean = default,
    disabledKeyAlgorithms: scala.collection.immutable.Seq[String] = disabledKeyAlgorithms,
    disabledSignatureAlgorithms: scala.collection.immutable.Seq[String] = disabledSignatureAlgorithms,
    enabledCipherSuites: Option[scala.collection.immutable.Seq[String]] = enabledCipherSuites,
    enabledProtocols: Option[scala.collection.immutable.Seq[String]] = enabledProtocols,
    hostnameVerifierClass: Class[_ <: javax.net.ssl.HostnameVerifier] = hostnameVerifierClass,
    keyManagerConfig: com.typesafe.sslconfig.ssl.KeyManagerConfig = keyManagerConfig,
    loose: com.typesafe.sslconfig.ssl.SSLLooseConfig = loose,
    protocol: String = protocol,
    revocationLists: Option[scala.collection.immutable.Seq[java.net.URL]] = revocationLists,
    secureRandom: Option[java.security.SecureRandom] = secureRandom,
    sslParametersConfig: com.typesafe.sslconfig.ssl.SSLParametersConfig = sslParametersConfig,
    trustManagerConfig: com.typesafe.sslconfig.ssl.TrustManagerConfig = trustManagerConfig): SSLConfigSettings = new SSLConfigSettings(
      checkRevocation = checkRevocation,
      debug = debug,
      default = default,
      disabledKeyAlgorithms = disabledKeyAlgorithms,
      disabledSignatureAlgorithms = disabledSignatureAlgorithms,
      enabledCipherSuites = enabledCipherSuites,
      enabledProtocols = enabledProtocols,
      hostnameVerifierClass = hostnameVerifierClass,
      keyManagerConfig = keyManagerConfig,
      loose = loose,
      protocol = protocol,
      revocationLists = revocationLists,
      secureRandom = secureRandom,
      sslParametersConfig = sslParametersConfig,
      trustManagerConfig = trustManagerConfig)

  override def toString =
    s"""SSLConfig(${checkRevocation},${debug},${default},${disabledKeyAlgorithms},${disabledSignatureAlgorithms},${enabledCipherSuites},${enabledProtocols},${hostnameVerifierClass},${keyManagerConfig},${loose},${protocol},${revocationLists},${secureRandom},${sslParametersConfig},${trustManagerConfig})"""
}
object SSLConfigSettings {
  def apply() = new SSLConfigSettings()
  /** Java API */
  def getInstance() = apply()
}

/**
 * Factory for creating SSL config (for use from Java).
 */
object SSLConfigFactory {

  /** Parses the given config into an SSLConfig object, as given (does not select sub-config sections). */
  def parse(config: Config): SSLConfigSettings =
    new SSLConfigParser(com.typesafe.sslconfig.util.EnrichedConfig(config), getClass.getClassLoader).parse()

  /**
   * Create an instance of the default config
   */
  def defaultConfig = SSLConfigSettings()
}

class SSLConfigParser(c: EnrichedConfig, classLoader: ClassLoader) {

  def parse(): SSLConfigSettings = {

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

    new SSLConfigSettings(
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
   * Parses "ssl-config.loose" section.
   */
  def parseLooseOptions(config: EnrichedConfig): SSLLooseConfig = {

    val allowWeakProtocols = config.get[Boolean]("allowWeakProtocols")
    val allowWeakCiphers = config.get[Boolean]("allowWeakCiphers")
    val allowMessages = config.getOptional[Boolean]("allowLegacyHelloMessages")
    val allowUnsafeRenegotiation = config.getOptional[Boolean]("allowUnsafeRenegotiation")
    val disableHostnameVerification = config.get[Boolean]("disableHostnameVerification")
    val disableSNI = config.get[Boolean]("disableSNI")
    val acceptAnyCertificate = config.get[Boolean]("acceptAnyCertificate")

    new SSLLooseConfig(
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
   * Parses the "ssl-config.debug" section.
   */
  def parseDebug(config: EnrichedConfig): SSLDebugConfig = {
    val certpath = config.get[Boolean]("certpath")

    if (config.get[Boolean]("all")) {
      new SSLDebugConfig(all = true, certpath = certpath)
    } else {

      val record: Option[SSLDebugRecordOptions] = if (config.get[Boolean]("record")) {
        val plaintext = config.get[Boolean]("plaintext")
        val packet = config.get[Boolean]("packet")
        Some(new SSLDebugRecordOptions(plaintext = plaintext, packet = packet))
      } else None

      val handshake = if (config.get[Boolean]("handshake")) {
        val data = config.get[Boolean]("data")
        val verbose = config.get[Boolean]("verbose")
        Some(new SSLDebugHandshakeOptions(data = data, verbose = verbose))
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

      new SSLDebugConfig(
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
   * Parses the "ssl-config.keyManager { stores = [ ... ]" section of configuration.
   */
  def parseKeyStoreInfo(config: EnrichedConfig): KeyStoreConfig = {
    val storeType = config.getOptional[String]("type").getOrElse(KeyStore.getDefaultType)
    val path = config.getOptional[String]("path")
    val data = config.getOptional[String]("data")
    val password = config.getOptional[String]("password")

    new KeyStoreConfig(filePath = path, storeType = storeType, data = data, password = password)
  }

  /**
   * Parses the "ssl-config.trustManager { stores = [ ... ]" section of configuration.
   */
  def parseTrustStoreInfo(config: EnrichedConfig): TrustStoreConfig = {
    val storeType = config.getOptional[String]("type").getOrElse(KeyStore.getDefaultType)
    val path = config.getOptional[String]("path")
    val data = config.getOptional[String]("data")
    val password = config.getOptional[String]("password")

    new TrustStoreConfig(filePath = path, storeType = storeType, data = data, password = password)
  }

  /**
   * Parses the "ssl-config.keyManager" section of the configuration.
   */
  def parseKeyManager(config: EnrichedConfig): KeyManagerConfig = {

    val algorithm = config.getOptional[String]("algorithm") match {
      case None        => KeyManagerFactory.getDefaultAlgorithm
      case Some(other) => other
    }

    val keyStoreInfos = config.getPrototypedSeq("stores").map { store =>
      parseKeyStoreInfo(store)
    }

    new KeyManagerConfig(algorithm, keyStoreInfos)
  }

  /**
   * Parses the "ssl-config.trustManager" section of configuration.
   */
  def parseTrustManager(config: EnrichedConfig): TrustManagerConfig = {
    val algorithm = config.getOptional[String]("algorithm") match {
      case None        => TrustManagerFactory.getDefaultAlgorithm
      case Some(other) => other
    }

    val trustStoreInfos = config.getPrototypedSeq("stores").map { store =>
      parseTrustStoreInfo(store)
    }

    new TrustManagerConfig(algorithm, trustStoreInfos)
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

    new SSLParametersConfig(clientAuth, protocols)
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
