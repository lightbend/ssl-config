/*
 * Copyright (C) 2015 - 2023 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig

import java.security.cert.{ CertPathValidatorResult, Certificate, PKIXCertPathValidatorResult, X509Certificate }
import scala.util.Properties.javaVmName

package object ssl {

  import scala.language.implicitConversions

  implicit def certificate2X509Certificate(cert: java.security.cert.Certificate): X509Certificate = {
    cert.asInstanceOf[X509Certificate]
  }

  implicit def arrayCertsToListCerts(chain: Array[Certificate]): java.util.List[Certificate] = {
    import scala.collection.JavaConverters._
    chain.toList.asJava
  }

  implicit def certResult2PKIXResult(result: CertPathValidatorResult): PKIXCertPathValidatorResult = {
    result.asInstanceOf[PKIXCertPathValidatorResult]
  }

  def debugChain(chain: Array[X509Certificate]): Seq[String] = {
    chain.map(debugCert)
  }

  private[sslconfig] def debugCert(cert: X509Certificate): String = {
    s"X509Certificate(serialNumber = ${cert.getSerialNumber.toString(16)}, subject = ${cert.getSubjectDN.getName})"
  }

  def isOpenJdk: Boolean = javaVmName contains "OpenJDK"

}
