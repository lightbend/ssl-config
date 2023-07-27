/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder
import org.bouncycastle.operator.{ DefaultDigestAlgorithmIdentifierFinder, DefaultSignatureAlgorithmIdentifierFinder }
import org.joda.time.Instant

import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security._
import java.security.cert.X509Certificate
import java.util.Date
import scala.concurrent.duration.{ FiniteDuration, _ }

/**
 * Used for testing only.  This relies on internal sun.security packages, so cannot be used in OpenJDK.
 */
object CertificateGenerator {

  // http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
  // http://www.keylength.com/en/4/

  /**
   * Generates a certificate using RSA (which is available in 1.6).
   */
  def generateRSAWithSHA256(keySize: Int = 2048, from: Instant = Instant.now, duration: FiniteDuration = 365.days): X509Certificate = {
    val dn = "CN=localhost, OU=Unit Testing, O=Mavericks, L=Moon Base 1, ST=Cyberspace, C=CY"
    val to = from plus duration.toMillis

    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(keySize, new SecureRandom())
    val pair = keyGen.generateKeyPair()
    generateCertificate(dn, pair, from.toDate, to.toDate, "SHA256withRSA")
  }

  def generateRSAWithSHA1(keySize: Int = 2048, from: Instant = Instant.now, duration: FiniteDuration = 365.days): X509Certificate = {
    val dn = "CN=localhost, OU=Unit Testing, O=Mavericks, L=Moon Base 1, ST=Cyberspace, C=CY"
    val to = from plus duration.toMillis

    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(keySize, new SecureRandom())
    val pair = keyGen.generateKeyPair()
    generateCertificate(dn, pair, from.toDate, to.toDate, "SHA1withRSA")
  }

  def toPEM(certificate: X509Certificate) = {
    val encoder = java.util.Base64.getMimeEncoder()
    val certBegin = "-----BEGIN CERTIFICATE-----\n"
    val certEnd = "-----END CERTIFICATE-----"

    val derCert = certificate.getEncoded()
    val pemCertPre = encoder.encodeToString(derCert)
    val pemCert = certBegin + pemCertPre + certEnd
    pemCert
  }

  def generateRSAWithMD5(keySize: Int = 2048, from: Instant = Instant.now, duration: FiniteDuration = 365.days): X509Certificate = {
    val dn = "CN=localhost, OU=Unit Testing, O=Mavericks, L=Moon Base 1, ST=Cyberspace, C=CY"
    val to = from plus duration.toMillis

    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(keySize, new SecureRandom())
    val pair = keyGen.generateKeyPair()
    generateCertificate(dn, pair, from.toDate, to.toDate, "MD5WithRSA")
  }

  private[sslconfig] def generateCertificate(dn: String, pair: KeyPair, from: Date, to: Date, algorithm: String): X509Certificate = {
    val serialNumber = new BigInteger(64, new SecureRandom())
    val owner = new X500Name(dn)

    val sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(algorithm);
    val digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

    val privateKeyParameter = PrivateKeyFactory.createKey(pair.getPrivate.getEncoded)
    val contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKeyParameter);

    val publicKeyInfo = SubjectPublicKeyInfo.getInstance(pair.getPublic.getEncoded)

    val x509Certificate = new X509v3CertificateBuilder(owner, serialNumber, from, to, owner, publicKeyInfo);
    val x509CertificateHolder = x509Certificate.build(contentSigner)

    val certificateFactory = new CertificateFactory()
    certificateFactory.engineGenerateCertificate(new ByteArrayInputStream(x509CertificateHolder.getEncoded))
  }
}
