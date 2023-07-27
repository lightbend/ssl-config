/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.io._
import java.math.BigInteger
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.{ KeyPair, KeyPairGenerator, KeyStore, SecureRandom }
import java.util.Date

import com.typesafe.sslconfig.util.{ LoggerFactory, NoDepsLogger }
import javax.net.ssl.KeyManagerFactory
import sun.security.util.ObjectIdentifier
import sun.security.x509._

/**
 * A fake key store with a selfsigned CA and a certificate issued by that CA. Includes a `trustedCertEntry` for
 * each of the two certificates.
 *
 * {{{
 * Your keystore contains 4 entries
 *
 * sslconfig-user-trust, Oct 4, 2018, trustedCertEntry,
 * Certificate fingerprint (SHA1): 19:2D:20:F0:36:59:E3:AD:C1:AA:55:82:0D:D2:94:5D:B3:75:3F:F8
 * sslconfig-user, Oct 4, 2018, PrivateKeyEntry,
 * Certificate fingerprint (SHA1): 19:2D:20:F0:36:59:E3:AD:C1:AA:55:82:0D:D2:94:5D:B3:75:3F:F8
 * sslconfig-CA-trust, Oct 4, 2018, trustedCertEntry,
 * Certificate fingerprint (SHA1): 9B:78:6B:4F:E4:B6:4D:EF:3E:3E:06:32:7A:53:83:28:96:7F:12:C7
 * sslconfig-CA, Oct 4, 2018, PrivateKeyEntry,
 * Certificate fingerprint (SHA1): 9B:78:6B:4F:E4:B6:4D:EF:3E:3E:06:32:7A:53:83:28:96:7F:12:C7
 * }}}
 *
 * Was: play.core.server.ssl.FakeKeyStore
 */
object FakeChainedKeyStore {
  private val EMPTY_PASSWORD = Array.emptyCharArray

  object CA {

    object Alias {
      // These two constants use a weird capitalization but that's what keystore uses internally (see class scaladoc)
      val trustedCertEntry = "sslconfig-CA-trust"
      val PrivateKeyEntry = "sslconfig-CA"
    }

    val DistinguishedName = "CN=certification.authority, OU=Unit Testing, O=Mavericks, L=SSL Config Base 1, ST=Cyberspace, C=CY"
    val keyPassword: Array[Char] = EMPTY_PASSWORD
  }

  object User {

    object Alias {
      // These two constants use a weird capitalization but that's what keystore uses internally (see class scaladoc)
      val trustedCertEntry = "sslconfig-user-trust"
      val PrivateKeyEntry = "sslconfig-user"
    }

    val DistinguishedName = "CN=localhost, OU=Unit Testing, O=Mavericks, L=SSL Config Base 1, ST=Cyberspace, C=CY"
    val keyPassword: Array[Char] = EMPTY_PASSWORD
  }

  object KeystoreSettings {
    val GeneratedKeyStore: String = fileInDevModeDir("chained.keystore")
    val SignatureAlgorithmName = "SHA256withRSA"
    val KeyPairAlgorithmName = "RSA"
    val KeyPairKeyLength = 2048 // 2048 is the NIST acceptable key length until 2030
    val KeystoreType = "JKS"
    val keystorePassword: Array[Char] = EMPTY_PASSWORD
  }

  private def fileInDevModeDir(filename: String): String = {
    "target" + File.separatorChar + "dev-mode" + File.separatorChar + filename
  }

  /**
   * Generate a fresh KeyStore object in memory. This KeyStore
   * is not saved to disk. If you want that, then call `keyManagerFactory`.
   *
   * This method is public only for consumption by Play/Lagom.
   */
  def generateKeyStore: KeyStore = {
    // Create a new KeyStore
    val keyStore: KeyStore = KeyStore.getInstance(KeystoreSettings.KeystoreType)

    // Generate the key pair
    val keyPairGenerator = KeyPairGenerator.getInstance(KeystoreSettings.KeyPairAlgorithmName)
    keyPairGenerator.initialize(KeystoreSettings.KeyPairKeyLength)
    val keyPair = keyPairGenerator.generateKeyPair()
    val certificateAuthorityKeyPair = keyPairGenerator.generateKeyPair()

    val cacert: X509Certificate = createCertificateAuthority(certificateAuthorityKeyPair)
    // Generate a self signed certificate
    val cert = createUserCertificate(keyPair, certificateAuthorityKeyPair)

    // Create the key store, first set the store pass
    keyStore.load(null, KeystoreSettings.keystorePassword)
    keyStore.setKeyEntry(CA.Alias.PrivateKeyEntry, keyPair.getPrivate, CA.keyPassword, Array(cacert))
    keyStore.setCertificateEntry(CA.Alias.trustedCertEntry, cacert)
    keyStore.setKeyEntry(User.Alias.PrivateKeyEntry, keyPair.getPrivate, User.keyPassword, Array(cert))
    keyStore.setCertificateEntry(User.Alias.trustedCertEntry, cert)
    keyStore
  }

  private[ssl] def createUserCertificate(userKeyPair: KeyPair, certificateAuthorityKeyPair: KeyPair): X509Certificate = {
    val certInfo = new X509CertInfo()

    // Serial number and version
    certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())))
    certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3))

    // Validity
    val validFrom = new Date()
    val validTo = new Date(validFrom.getTime + 50L * 365L * 24L * 60L * 60L * 1000L)
    val validity = new CertificateValidity(validFrom, validTo)
    certInfo.set(X509CertInfo.VALIDITY, validity)

    // Subject and issuer
    val certificateAuthorityName = new X500Name(CA.DistinguishedName)
    certInfo.set(X509CertInfo.ISSUER, certificateAuthorityName)
    val owner = new X500Name(User.DistinguishedName)
    certInfo.set(X509CertInfo.SUBJECT, owner)

    // Key and algorithm
    certInfo.set(X509CertInfo.KEY, new CertificateX509Key(userKeyPair.getPublic))
    val algorithm = AlgorithmId.get("SHA256WithRSA")
    certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithm))

    // Create a new certificate and sign it
    val cert = new X509CertImpl(certInfo)
    cert.sign(userKeyPair.getPrivate, KeystoreSettings.SignatureAlgorithmName)

    // Since the signature provider may have a different algorithm ID to what we think it should be,
    // we need to reset the algorithm ID, and resign the certificate
    val actualAlgorithm = cert.get(X509CertImpl.SIG_ALG).asInstanceOf[AlgorithmId]
    certInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, actualAlgorithm)
    val newCert = new X509CertImpl(certInfo)
    newCert.sign(certificateAuthorityKeyPair.getPrivate, KeystoreSettings.SignatureAlgorithmName)
    newCert
  }

  private def createCertificateAuthority(keyPair: KeyPair): X509Certificate = {
    val certInfo = new X509CertInfo()
    // Serial number and version
    certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())))
    certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3))

    // Validity
    val validFrom = new Date()
    val validTo = new Date(validFrom.getTime + 50L * 365L * 24L * 60L * 60L * 1000L) // 50 years
    val validity = new CertificateValidity(validFrom, validTo)
    certInfo.set(X509CertInfo.VALIDITY, validity)

    // Subject and issuer
    val owner = new X500Name(CA.DistinguishedName)
    certInfo.set(X509CertInfo.SUBJECT, owner)
    certInfo.set(X509CertInfo.ISSUER, owner)

    // Key and algorithm
    certInfo.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic))
    val algorithm = AlgorithmId.get("SHA256WithRSA")
    certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithm))

    val caExtension = new CertificateExtensions
    caExtension.set(BasicConstraintsExtension.NAME, new BasicConstraintsExtension( /* isCritical */ true, /* isCA */ true, 0))
    certInfo.set(X509CertInfo.EXTENSIONS, caExtension)

    // Create a new certificate and sign it
    val cert = new X509CertImpl(certInfo)
    cert.sign(keyPair.getPrivate, KeystoreSettings.SignatureAlgorithmName)

    // Since the signature provider may have a different algorithm ID to what we think it should be,
    // we need to reset the algorithm ID, and resign the certificate
    val actualAlgorithm = cert.get(X509CertImpl.SIG_ALG).asInstanceOf[AlgorithmId]
    certInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, actualAlgorithm)
    val newCert = new X509CertImpl(certInfo)
    newCert.sign(keyPair.getPrivate, KeystoreSettings.SignatureAlgorithmName)
    newCert
  }

}

/**
 * A fake key store
 *
 * Was: play.core.server.ssl.FakeKeyStore
 */
final class FakeChainedKeyStore(mkLogger: LoggerFactory) {

  import FakeChainedKeyStore._

  private val logger: NoDepsLogger = mkLogger(getClass)

  /**
   * @param appPath a file descriptor to the root folder of the project (the root, not a particular module).
   */
  def getKeyStoreFilePath(appPath: File) = new File(appPath, KeystoreSettings.GeneratedKeyStore)

  private[ssl] def shouldGenerate(keyStoreFile: File): Boolean = {
    import scala.collection.JavaConverters._

    if (!keyStoreFile.exists()) {
      return true
    }

    // Should regenerate if we find an unacceptably weak key in there.
    val store = loadKeyStore(keyStoreFile)
    store.aliases().asScala.exists { alias =>
      Option(store.getCertificate(alias)).exists(c => certificateTooWeak(c))
    }
  }

  private def loadKeyStore(file: File): KeyStore = {
    val keyStore: KeyStore = KeyStore.getInstance(KeystoreSettings.KeystoreType)
    val in = java.nio.file.Files.newInputStream(file.toPath)
    try {
      keyStore.load(in, KeystoreSettings.keystorePassword)
    } finally {
      closeQuietly(in)
    }
    keyStore
  }

  private[ssl] def certificateTooWeak(c: java.security.cert.Certificate): Boolean = {
    val key: RSAPublicKey = c.getPublicKey.asInstanceOf[RSAPublicKey]
    key.getModulus.bitLength < 2048 || c.asInstanceOf[X509CertImpl].getSigAlgName != KeystoreSettings.SignatureAlgorithmName
  }

  /** Public only for consumption by Play/Lagom. */
  def createKeyStore(appPath: File): KeyStore = {
    val keyStoreFile = getKeyStoreFilePath(appPath)
    val keyStoreDir = keyStoreFile.getParentFile

    createKeystoreParentDirectory(keyStoreDir)

    val keyStore: KeyStore = synchronized(if (shouldGenerate(keyStoreFile)) {
      logger.info(s"Generating HTTPS key pair in ${keyStoreFile.getAbsolutePath} - this may take some time. If nothing happens, try moving the mouse/typing on the keyboard to generate some entropy.")

      val freshKeyStore: KeyStore = generateKeyStore
      val out = java.nio.file.Files.newOutputStream(keyStoreFile.toPath)
      try {
        freshKeyStore.store(out, Array.emptyCharArray)
      } finally {
        closeQuietly(out)
      }
      freshKeyStore
    } else {
      // Load a KeyStore from a file
      val loadedKeyStore = loadKeyStore(keyStoreFile)
      logger.info(s"HTTPS key pair generated in ${keyStoreFile.getAbsolutePath}.")
      loadedKeyStore
    })
    keyStore
  }

  private def createKeystoreParentDirectory(keyStoreDir: File) = {
    if (keyStoreDir.mkdirs()) {
      logger.debug(s"Parent directory for keystore successfully created at ${keyStoreDir.getAbsolutePath}")
    } else if (keyStoreDir.exists() && keyStoreDir.isDirectory) {
      // File.mkdirs returns false when the directory already exists.
      logger.debug(s"No need to create $keyStoreDir since it already exists.")
    } else if (keyStoreDir.exists() && keyStoreDir.isFile) {
      // File.mkdirs also returns false when there is a file for that path.
      // A consumer will then fail to write the keystore file later, so we fail fast here.
      throw new IllegalStateException(s"$keyStoreDir exists, but it is NOT a directory, making it not possible to generate a key store file.")
    } else {
      // Not being able to create a directory inside target folder is weird, but if it happens
      // a consumer will then fail to write the keystore file later, so we fail fast here.
      throw new IllegalStateException(s"Failed to create $keyStoreDir. Check if there is permission to create such folder.")
    }
  }

  /** Public only for consumption by Play/Lagom. */
  def keyManagerFactory(appPath: File): KeyManagerFactory = {
    val keyStore = createKeyStore(appPath)

    // Load the key and certificate into a key manager factory
    val kmf = KeyManagerFactory.getInstance("SunX509")
    kmf.init(keyStore, Array.emptyCharArray)
    kmf
  }

  /**
   * Close the given closeable quietly.
   *
   * Logs any IOExceptions encountered.
   */
  def closeQuietly(closeable: Closeable) = {
    try {
      if (closeable != null) {
        closeable.close()
      }
    } catch {
      case e: IOException => logger.warn(s"Error closing stream. Cause: $e")
    }
  }

}
