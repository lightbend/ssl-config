/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.security.{ KeyStore, SecureRandom, KeyPairGenerator, KeyPair }
import com.typesafe.sslconfig.util.{ LoggerFactory, NoDepsLogger }
import sun.security.x509._
import sun.security.util.ObjectIdentifier
import java.util.Date
import java.math.BigInteger
import java.security.cert.X509Certificate
import java.io._
import javax.net.ssl.KeyManagerFactory
import java.security.interfaces.RSAPublicKey

/**
 * A fake key store
 *
 * Was: play.core.server.ssl.FakeKeyStore
 */
class FakeKeyStore(mkLogger: LoggerFactory) {
  private val logger = mkLogger(getClass)

  val GeneratedKeyStore: String = fileInDevModeDir("generated.keystore")
  val ExportedCert: String = fileInDevModeDir("service.crt")
  val TrustedAlias = "playgeneratedtrusted"
  val DistinguishedName = "CN=localhost, OU=Unit Testing, O=Mavericks, L=Play Base 1, ST=Cyberspace, C=CY"
  val SignatureAlgorithmName = "SHA256withRSA"
  val SignatureAlgorithmOID: ObjectIdentifier = AlgorithmId.sha256WithRSAEncryption_oid

  object CertificateAuthority {
    val ExportedCertificate = "target/dev-mode/ca.crt"
    val TrustedAlias = "playgeneratedCAtrusted"
    val DistinguishedName = "CN=localhost-CA, OU=Unit Testing, O=Mavericks, L=Play Base 1, ST=Cyberspace, C=CY"
  }

  private def fileInDevModeDir(filename: String): String = {
    "target" + File.separatorChar + "dev-mode" + File.separatorChar + filename
  }

  /**
   * @param appPath a file descriptor to the root folder of the project (the root, not a particular module).
   */
  def getKeyStoreFilePath(appPath: File) = new File(appPath, GeneratedKeyStore)

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
    val keyStore: KeyStore = KeyStore.getInstance("JKS")
    val in = java.nio.file.Files.newInputStream(file.toPath)
    try {
      keyStore.load(in, "".toCharArray)
    } finally {
      closeQuietly(in)
    }
    keyStore
  }

  private[ssl] def certificateTooWeak(c: java.security.cert.Certificate): Boolean = {
    val key: RSAPublicKey = c.getPublicKey.asInstanceOf[RSAPublicKey]
    key.getModulus.bitLength < 2048 || c.asInstanceOf[X509CertImpl].getSigAlgName != SignatureAlgorithmName
  }

  /** Public only for consumption by Play/Lagom. */
  def createKeyStore(appPath: File): KeyStore = {
    val keyStoreFile = getKeyStoreFilePath(appPath)
    val keyStoreDir = keyStoreFile.getParentFile

    createKeystoreParentDirectory(keyStoreDir)

    val keyStore: KeyStore = if (shouldGenerate(keyStoreFile)) {
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
    }
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
      // Play will then fail to write the keystore file later, so we fail fast here.
      throw new IllegalStateException(s"$keyStoreDir exists, but it is NOT a directory. Play won't be able to generate a key store file.")
    } else {
      // Not being able to create a directory inside target folder is weird, but if it happens
      // Play will then fail to write the keystore file later, so we fail fast here.
      throw new IllegalStateException(s"Play was not able to create $keyStoreDir. Check if there is permission to create such folder.")
    }
  }

  private[ssl] def keyManagerFactory(appPath: File): KeyManagerFactory = {
    val keyStore = createKeyStore(appPath)

    // Load the key and certificate into a key manager factory
    val kmf = KeyManagerFactory.getInstance("SunX509")
    kmf.init(keyStore, Array.emptyCharArray)
    kmf
  }

  /**
   * Generate a fresh KeyStore object in memory. This KeyStore
   * is not saved to disk. If you want that, then call `keyManagerFactory`.
   *
   * This method has has `private[ssl]` access so it can be used for
   * testing.
   */
  private[ssl] def generateKeyStore: KeyStore = {
    // Create a new KeyStore
    val keyStore: KeyStore = KeyStore.getInstance("JKS")

    // Generate the key pair
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(2048) // 2048 is the NIST acceptable key length until 2030
    val keyPair = keyPairGenerator.generateKeyPair()
    val certificateAuthorityKeyPair = keyPairGenerator.generateKeyPair()

    val cacert: X509Certificate = createCertificateAuthority(certificateAuthorityKeyPair)
    // Generate a self signed certificate
    val cert = createSelfSignedCertificate(keyPair, certificateAuthorityKeyPair)

    // Create the key store, first set the store pass
    keyStore.load(null, Array.emptyCharArray)
    keyStore.setKeyEntry("playgeneratedCA", keyPair.getPrivate, Array.emptyCharArray, Array(cacert))
    keyStore.setCertificateEntry(CertificateAuthority.TrustedAlias, cacert)
    keyStore.setKeyEntry("playgenerated", keyPair.getPrivate, Array.emptyCharArray, Array(cert))
    keyStore.setCertificateEntry(TrustedAlias, cert)
    keyStore
  }

  private[ssl] def createSelfSignedCertificate(keyPair: KeyPair, certificateAuthorityKeyPair: KeyPair): X509Certificate = {
    val certInfo = new X509CertInfo()

    // Serial number and version
    certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())))
    certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3))

    // Validity
    val validFrom = new Date()
    val validTo = new Date(validFrom.getTime + 50l * 365l * 24l * 60l * 60l * 1000l)
    val validity = new CertificateValidity(validFrom, validTo)
    certInfo.set(X509CertInfo.VALIDITY, validity)

    // Subject and issuer
    val certificateAuthorityName = new X500Name(CertificateAuthority.DistinguishedName)
    certInfo.set(X509CertInfo.ISSUER, certificateAuthorityName)
    val owner = new X500Name(DistinguishedName)
    certInfo.set(X509CertInfo.SUBJECT, owner)

    // Key and algorithm
    certInfo.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic))
    val algorithm = new AlgorithmId(SignatureAlgorithmOID)
    certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithm))

    // Create a new certificate and sign it
    val cert = new X509CertImpl(certInfo)
    cert.sign(keyPair.getPrivate, SignatureAlgorithmName)

    // Since the signature provider may have a different algorithm ID to what we think it should be,
    // we need to reset the algorithm ID, and resign the certificate
    val actualAlgorithm = cert.get(X509CertImpl.SIG_ALG).asInstanceOf[AlgorithmId]
    certInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, actualAlgorithm)
    val newCert = new X509CertImpl(certInfo)
    newCert.sign(certificateAuthorityKeyPair.getPrivate, SignatureAlgorithmName)
    newCert
  }

  private def createCertificateAuthority(keyPair: KeyPair): X509Certificate = {
    val certInfo = new X509CertInfo()
    // Serial number and version
    certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())))
    certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3))

    // Validity
    val validFrom = new Date()
    val validTo = new Date(validFrom.getTime + 50l * 365l * 24l * 60l * 60l * 1000l) // 50 years
    val validity = new CertificateValidity(validFrom, validTo)
    certInfo.set(X509CertInfo.VALIDITY, validity)

    // Subject and issuer
    val owner = new X500Name(CertificateAuthority.DistinguishedName)
    certInfo.set(X509CertInfo.SUBJECT, owner)
    certInfo.set(X509CertInfo.ISSUER, owner)

    // Key and algorithm
    certInfo.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic))
    val algorithm = new AlgorithmId(SignatureAlgorithmOID)
    certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithm))

    val caExtension = new CertificateExtensions
    caExtension.set(BasicConstraintsExtension.NAME, new BasicConstraintsExtension( /* isCritical */ true, /* isCA */ true, 0))
    certInfo.set(X509CertInfo.EXTENSIONS, caExtension)

    // Create a new certificate and sign it
    val cert = new X509CertImpl(certInfo)
    cert.sign(keyPair.getPrivate, SignatureAlgorithmName)

    // Since the signature provider may have a different algorithm ID to what we think it should be,
    // we need to reset the algorithm ID, and resign the certificate
    val actualAlgorithm = cert.get(X509CertImpl.SIG_ALG).asInstanceOf[AlgorithmId]
    certInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, actualAlgorithm)
    val newCert = new X509CertImpl(certInfo)
    newCert.sign(keyPair.getPrivate, SignatureAlgorithmName)
    newCert
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
