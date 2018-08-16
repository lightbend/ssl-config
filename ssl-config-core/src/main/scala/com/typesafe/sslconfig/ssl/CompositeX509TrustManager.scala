/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.net.Socket

import javax.net.ssl.{ SSLEngine, X509ExtendedTrustManager, X509TrustManager }
import java.security.cert._

import com.typesafe.sslconfig.util.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal
import java.security.GeneralSecurityException

/**
 * A trust manager that is a composite of several smaller trust managers.   It is responsible for verifying the
 * credentials received from a peer.
 */
class CompositeX509TrustManager(mkLogger: LoggerFactory, trustManagers: Seq[X509TrustManager], algorithmChecker: AlgorithmChecker) extends X509ExtendedTrustManager {

  private val logger = mkLogger(getClass)

  def getAcceptedIssuers: Array[X509Certificate] = {
    logger.debug("getAcceptedIssuers: ")
    val certificates = ArrayBuffer[X509Certificate]()
    val exceptionList = withTrustManagers[X509TrustManager] {
      trustManager =>
        certificates ++= trustManager.getAcceptedIssuers
    }
    // getAcceptedIssuers should never throw an exception.
    if (exceptionList.nonEmpty) {
      val msg = exceptionList.head.getMessage
      throw new CompositeCertificateException(msg, exceptionList.toArray)
    }
    certificates.toArray
  }

  override def checkClientTrusted(chain: Array[X509Certificate], authType: String, socket: Socket): Unit = {
    logger.debug(s"checkClientTrusted: chain = ${debugChain(chain)}, authType = $authType, socket = $socket")

    val anchor: TrustAnchor = new TrustAnchor(chain(chain.length - 1), null)
    logger.debug(s"checkClientTrusted: checking key size only on root anchor $anchor")
    algorithmChecker.checkKeyAlgorithms(anchor.getTrustedCert)

    var trusted = false
    val exceptionList = withTrustManagers[X509ExtendedTrustManager] {
      trustManager =>
        trustManager.checkClientTrusted(chain, authType, socket)
        logger.debug(s"checkClientTrusted: trustManager $trustManager found a match for ${debugChain(chain)}")
        trusted = true
    }

    if (!trusted) {
      val msg = "No trust manager was able to validate this certificate chain."
      throw new CompositeCertificateException(msg, exceptionList.toArray)
    }
  }

  override def checkClientTrusted(chain: Array[X509Certificate], authType: String, sslEngine: SSLEngine): Unit = {
    logger.debug(s"checkClientTrusted: chain = ${debugChain(chain)}, authType = $authType, sslEngine = $sslEngine")

    val anchor: TrustAnchor = new TrustAnchor(chain(chain.length - 1), null)
    logger.debug(s"checkClientTrusted: checking key size only on root anchor $anchor")
    algorithmChecker.checkKeyAlgorithms(anchor.getTrustedCert)

    var trusted = false
    val exceptionList = withTrustManagers[X509ExtendedTrustManager] {
      trustManager =>
        trustManager.checkClientTrusted(chain, authType, sslEngine)
        logger.debug(s"checkClientTrusted: trustManager $trustManager found a match for ${debugChain(chain)}")
        trusted = true
    }

    if (!trusted) {
      val msg = "No trust manager was able to validate this certificate chain."
      throw new CompositeCertificateException(msg, exceptionList.toArray)
    }
  }

  def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    logger.debug(s"checkClientTrusted: chain = ${debugChain(chain)}, authType = $authType")

    val anchor: TrustAnchor = new TrustAnchor(chain(chain.length - 1), null)
    logger.debug(s"checkClientTrusted: checking key size only on root anchor $anchor")
    algorithmChecker.checkKeyAlgorithms(anchor.getTrustedCert)

    var trusted = false
    val exceptionList = withTrustManagers[X509TrustManager] {
      trustManager =>
        trustManager.checkClientTrusted(chain, authType)
        logger.debug(s"checkClientTrusted: trustManager $trustManager found a match for ${debugChain(chain)}")
        trusted = true
    }

    if (!trusted) {
      val msg = "No trust manager was able to validate this certificate chain."
      throw new CompositeCertificateException(msg, exceptionList.toArray)
    }
  }

  def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    logger.debug(s"checkServerTrusted: chain = ${debugChain(chain)}, authType = $authType")

    // Trust anchor is at the end of the chain... there is no way to pass a trust anchor
    // through to a checker in PKIXCertPathValidator.doValidate(), so the trust manager is the
    // last place we have access to it.
    val anchor: TrustAnchor = new TrustAnchor(chain(chain.length - 1), null)
    logger.debug(s"checkServerTrusted: checking key size only on root anchor $anchor")
    algorithmChecker.checkKeyAlgorithms(anchor.getTrustedCert)

    var trusted = false
    val exceptionList = withTrustManagers[X509TrustManager] {
      trustManager =>
        // always run through the trust manager before making any decisions
        trustManager.checkServerTrusted(chain, authType)
        logger.debug(s"checkServerTrusted: trustManager $trustManager using authType $authType found a match for ${debugChain(chain).toSeq}")
        trusted = true
    }

    if (!trusted) {
      val msg = s"No trust manager was able to validate this certificate chain: # of exceptions = ${exceptionList.size}"
      throw new CompositeCertificateException(msg, exceptionList.toArray)
    }
  }

  override def checkServerTrusted(chain: Array[X509Certificate], authType: String, socket: Socket): Unit = {
    logger.debug(s"checkServerTrusted: chain = ${debugChain(chain)}, authType = $authType, socket = $socket")

    // Trust anchor is at the end of the chain... there is no way to pass a trust anchor
    // through to a checker in PKIXCertPathValidator.doValidate(), so the trust manager is the
    // last place we have access to it.
    val anchor: TrustAnchor = new TrustAnchor(chain(chain.length - 1), null)
    logger.debug(s"checkServerTrusted: checking key size only on root anchor $anchor")
    algorithmChecker.checkKeyAlgorithms(anchor.getTrustedCert)

    var trusted = false
    val exceptionList = withTrustManagers[X509ExtendedTrustManager] {
      trustManager =>
        // always run through the trust manager before making any decisions
        trustManager.checkServerTrusted(chain, authType, socket)
        logger.debug(s"checkServerTrusted: trustManager $trustManager using authType $authType found a match for ${debugChain(chain).toSeq}")
        trusted = true
    }

    if (!trusted) {
      val msg = s"No trust manager was able to validate this certificate chain: # of exceptions = ${exceptionList.size}"
      throw new CompositeCertificateException(msg, exceptionList.toArray)
    }
  }

  override def checkServerTrusted(chain: Array[X509Certificate], authType: String, sslEngine: SSLEngine): Unit = {
    logger.debug(s"checkServerTrusted: chain = ${debugChain(chain)}, authType = $authType, sslEngine = $sslEngine")

    // Trust anchor is at the end of the chain... there is no way to pass a trust anchor
    // through to a checker in PKIXCertPathValidator.doValidate(), so the trust manager is the
    // last place we have access to it.
    val anchor: TrustAnchor = new TrustAnchor(chain(chain.length - 1), null)
    logger.debug(s"checkServerTrusted: checking key size only on root anchor $anchor")
    algorithmChecker.checkKeyAlgorithms(anchor.getTrustedCert)

    var trusted = false
    val exceptionList = withTrustManagers[X509ExtendedTrustManager] {
      trustManager =>
        // always run through the trust manager before making any decisions
        trustManager.checkServerTrusted(chain, authType, sslEngine)
        logger.debug(s"checkServerTrusted: trustManager $trustManager using authType $authType found a match for ${debugChain(chain).toSeq}")
        trusted = true
    }

    if (!trusted) {
      val msg = s"No trust manager was able to validate this certificate chain: # of exceptions = ${exceptionList.size}"
      throw new CompositeCertificateException(msg, exceptionList.toArray)
    }
  }

  private def withTrustManagers[T](block: T => Unit): Seq[Throwable] = {
    val exceptionList = ArrayBuffer[Throwable]()
    trustManagers.foreach {
      trustManager =>
        try {
          block(trustManager.asInstanceOf[T])
        } catch {
          case e: CertPathBuilderException =>
            logger.debug(s"No path found to certificate: this usually means the CA is not in the trust store. Cause: $e")
            exceptionList.append(e)
          case e: GeneralSecurityException =>
            logger.debug(s"General security exception. Cause: $e")
            exceptionList.append(e)
          case NonFatal(e) =>
            logger.debug(s"Unexpected exception! Cause: $e")
            exceptionList.append(e)
        }
    }
    exceptionList.toSeq
  }

  override def toString = {
    s"CompositeX509TrustManager(trustManagers = [$trustManagers], algorithmChecker = $algorithmChecker)"
  }

}
