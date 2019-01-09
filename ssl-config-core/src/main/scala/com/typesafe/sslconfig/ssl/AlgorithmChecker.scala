/*
 * Copyright (C) 2015 - 2019 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.security.cert._
import java.text.SimpleDateFormat
import java.util.{ Date, GregorianCalendar, Calendar }
import javax.naming.InvalidNameException
import javax.naming.ldap.{ LdapName, Rdn }

import com.typesafe.sslconfig.util.{ LoggerFactory, NoDepsLogger }

import scala.collection.JavaConverters._

/**
 * Looks for disabled algorithms in the certificate.  This is because some certificates are signed with
 * forgable hashes such as MD2 or MD5, so we can't be certain of their authenticity.
 *
 * This class is needed because the JDK 1.6 Algorithm checker doesn't give us any way to customize the list of
 * disabled algorithms, and we need to be able to support that.
 *
 * Also note that we need to check the trust anchor for disabled key sizes, and the CertPath explicitly removes
 * the trust anchor from the chain of certificates.  This means we need to check the trust anchor explicitly in the
 * through the CompositeTrustManager.
 */
class AlgorithmChecker(mkLogger: LoggerFactory, val signatureConstraints: Set[AlgorithmConstraint], val keyConstraints: Set[AlgorithmConstraint]) extends PKIXCertPathChecker {

  private val logger = mkLogger(getClass)

  private val signatureConstraintsMap: Map[String, AlgorithmConstraint] = {
    for (c <- signatureConstraints.iterator) yield {
      c.algorithm -> c
    }
  }.toMap

  private val keyConstraintsMap: Map[String, AlgorithmConstraint] = {
    for (c <- keyConstraints.iterator) yield {
      c.algorithm -> c
    }
  }.toMap

  def isForwardCheckingSupported: Boolean = false

  def getSupportedExtensions: java.util.Set[String] = java.util.Collections.emptySet()

  def init(forward: Boolean): Unit = {
    logger.debug(s"init: forward = $forward")
    // forward is from target to most-trusted CA
    // backwards is from most-trusted CA to target, which means we get the root CA first.
    if (forward) {
      throw new CertPathValidatorException("Forward checking not supported")
    }
  }

  def findSignatureConstraint(algorithm: String): Option[AlgorithmConstraint] = {
    signatureConstraintsMap.get(algorithm)
  }

  def findKeyConstraint(algorithm: String): Option[AlgorithmConstraint] = {
    keyConstraintsMap.get(algorithm)
  }

  /**
   * Checks for signature algorithms in the certificate and throws CertPathValidatorException if matched.
   *
   * @param x509Cert
   */
  def checkSignatureAlgorithms(x509Cert: X509Certificate): Unit = {
    val sigAlgName = x509Cert.getSigAlgName
    val sigAlgorithms = Algorithms.decomposes(sigAlgName)

    logger.debug(s"checkSignatureAlgorithms: sigAlgName = $sigAlgName, sigAlgName = $sigAlgName, sigAlgorithms = $sigAlgorithms")

    for (a <- sigAlgorithms) {
      findSignatureConstraint(a).foreach { constraint =>
        if (constraint.matches(a)) {
          logger.debug(s"checkSignatureAlgorithms: x509Cert = $x509Cert failed on constraint $constraint")
          val msg = s"Certificate failed: $a matched constraint $constraint"
          throw new CertPathValidatorException(msg)
        }
      }
    }
  }

  /**
   * Checks for key algorithms in the certificate and throws CertPathValidatorException if matched.
   *
   * @param x509Cert
   */
  def checkKeyAlgorithms(x509Cert: X509Certificate): Unit = {
    val key = x509Cert.getPublicKey
    val keyAlgorithmName = key.getAlgorithm
    val keySize = Algorithms.keySize(key).getOrElse(throw new IllegalStateException(s"No keySize found for $key"))

    val keyAlgorithms = Algorithms.decomposes(keyAlgorithmName)
    logger.debug(s"checkKeyAlgorithms: keyAlgorithmName = $keyAlgorithmName, keySize = $keySize, keyAlgorithms = $keyAlgorithms")

    for (a <- keyAlgorithms) {
      findKeyConstraint(a).foreach {
        constraint =>
          if (constraint.matches(a, keySize)) {
            val certName = x509Cert.getSubjectX500Principal.getName
            logger.debug(s"""checkKeyAlgorithms: cert = "$certName" failed on constraint $constraint, algorithm = $a, keySize = $keySize""")

            val msg = s"""Certificate failed: cert = "$certName" failed on constraint $constraint, algorithm = $a, keySize = $keySize"""
            throw new CertPathValidatorException(msg)
          }
      }
    }
  }

  /**
   * Checks the algorithms in the given certificate.  Note that this implementation skips signature checking in a
   * root certificate, as a trusted root cert by definition is in the trust store and doesn't need to be signed.
   */
  def check(cert: Certificate, unresolvedCritExts: java.util.Collection[String]): Unit = {
    cert match {
      case x509Cert: X509Certificate =>

        val commonName = getCommonName(x509Cert)
        val subAltNames = x509Cert.getSubjectAlternativeNames
        val certName = x509Cert.getSubjectX500Principal.getName
        if (logger.isDebugEnabled) {
          val expirationDate = x509Cert.getNotAfter.getTime
          val isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // reminder for scala people, that one's not thread-safe...
          logger.debug(s"check: checking certificate commonName = $commonName, subjAltName = $subAltNames, certName = $certName, expirationDate = ${isoFormat.format(expirationDate)}")
        }

        sunsetSHA1SignatureAlgorithm(x509Cert)
        checkSignatureAlgorithms(x509Cert)
        checkKeyAlgorithms(x509Cert)
      case _ =>
        throw new UnsupportedOperationException("check only works with x509 certificates!")
    }
  }

  /**
   * Logs an info or warning message to sunset a SHA-1 certificate, based on
   * <a href="http://googleonlinesecurity.blogspot.com/2014/09/gradually-sunsetting-sha-1.html">Google's recommendations</a>.
   *
   * @param x509Cert
   */
  def sunsetSHA1SignatureAlgorithm(x509Cert: X509Certificate): Unit = {

    val sigAlgName = x509Cert.getSigAlgName
    val sigAlgorithms = Algorithms.decomposes(sigAlgName)
    if (sigAlgorithms.contains("SHA1") || sigAlgorithms.contains("SHA-1")) {
      // https://github.com/playframework/playframework/issues/4241
      //
      // Sites with end-entity certificates that expire between 1 June 2016 to 31 December 2016 (inclusive),
      // and which include a SHA-1-based signature as part of the certificate chain, will be treated as “secure, but with minor errors”.

      // TODO use Java 8 time once we can
      // val june2016 = new DateTime(2016, 6, 1, 0, 0, 0, 0)
      // val december2016 = new DateTime(2016, 12, 31, 0, 0, 0, 0)
      // val secureInterval = new Interval(june2016, december2016)
      // val expirationDate = new DateTime(x509Cert.getNotAfter.getTime)
      // log warning if expiration between those two

      val june2016 = new GregorianCalendar(2016, 6, 1, 0, 0, 0)
      val december2016 = new GregorianCalendar(2016, 12, 31, 0, 0, 0)

      val expirationDate = new GregorianCalendar()
      expirationDate.setTime(new Date(x509Cert.getNotAfter.getTime))

      // expiration date between those two above
      if (expirationDate.after(june2016) && expirationDate.before(december2016)) {
        infoOnSunset(x509Cert, expirationDate)
      }

      // Sites with end-entity certificates that expire on or after 1 January 2017, and which include
      // a SHA-1-based signature as part of the certificate chain, will be treated as
      // “neutral, lacking security”.
      val january2017 = new GregorianCalendar(2017, 1, 1, 0, 0, 0)
      if (january2017.equals(expirationDate) || january2017.before(expirationDate)) {
        warnOnSunset(x509Cert, expirationDate)
      }
    }
  }

  def infoOnSunset(x509Cert: X509Certificate, expirationDate: GregorianCalendar): Unit = {
    val certName = x509Cert.getSubjectX500Principal.getName
    logger.info(s"Certificate $certName uses SHA-1 and expires $expirationDate: this certificate expires soon, but SHA-1 is being sunsetted.")
  }

  def warnOnSunset(x509Cert: X509Certificate, expirationDate: GregorianCalendar): Unit = {
    val certName = x509Cert.getSubjectX500Principal.getName
    logger.warn(s"Certificate $certName uses SHA-1 and expires $expirationDate: SHA-1 cannot be considered secure and this certificate should be replaced.")
  }

  /**
   * Useful way to get certificate info without getting spammed with data.
   */
  def getCommonName(cert: X509Certificate) = {
    // http://stackoverflow.com/a/18174689/5266
    try {
      val ldapName = new LdapName(cert.getSubjectX500Principal.getName)
      /*
       * Looking for the "most specific CN" (i.e. the last).
       */
      var cn: String = null
      for (rdn: Rdn <- ldapName.getRdns.asScala) {
        if ("CN".equalsIgnoreCase(rdn.getType)) {
          cn = rdn.getValue.toString
        }
      }
      cn
    } catch {
      case e: InvalidNameException =>
        null
    }
  }

}
