/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.security.cert.{ CertPathValidatorException, Certificate, X509Certificate }
import java.util.Collections._
import java.util.GregorianCalendar

import com.typesafe.sslconfig.ssl.AlgorithmConstraintsParser._
import com.typesafe.sslconfig.util.NoopLogger
import org.joda.time.{ DateTime, Instant }
import org.specs2.mutable._

import scala.concurrent.duration._

object AlgorithmCheckerSpec extends Specification {

  val mkLogger = NoopLogger.factory()

  "AlgorithmChecker" should {

    def checker(sigs: Seq[String], keys: Seq[String]) = {
      new AlgorithmChecker(mkLogger, sigs.map(s => parseAll(expression, s).get).toSet,
        keys.map(s => parseAll(expression, s).get).toSet)
    }

    "pass a good key algorithm (RSA > 1024)" in {
      val certificate: Certificate = CertificateGenerator.generateRSAWithSHA256(2048)
      checker(Nil, Seq("RSA keySize < 1024")).check(certificate, emptySet())
      success
    }

    "fail a weak key algorithm (RSA < 512)" in {
      val certificate: Certificate = CertificateGenerator.generateRSAWithSHA256(512)
      checker(Nil, Seq("RSA keySize < 1024")).check(certificate, emptySet()).must(throwA[CertPathValidatorException])
    }

    "pass a good signature algorithm (SHA256)" in {
      val certificate: Certificate = CertificateGenerator.generateRSAWithSHA256(512)
      checker(Seq("MD5"), Nil).check(certificate, emptySet())
      success
    }

    "fail a bad signature algorithm (MD5)" in {
      val intermediateCert: Certificate = CertificateGenerator.generateRSAWithMD5(2048)
      checker(Seq("MD5"), Nil).check(intermediateCert, emptySet()).must(throwA[CertPathValidatorException])
    }

    "neither info nor warning on a signature containing sha-1 that expires before 1 June 2016" in {
      val oneHundredAndEightyDays = 180.days
      val certificate = CertificateGenerator.generateRSAWithSHA1(2048, from = Instant.parse("2015-06-01T12:00:00Z"), duration = oneHundredAndEightyDays)

      var infoCalled = false
      var warningCalled = false
      val checker = new AlgorithmChecker(mkLogger, Set.empty, Set.empty) {
        override def infoOnSunset(x509Cert: X509Certificate, expirationDate: GregorianCalendar): Unit = {
          infoCalled = true
        }
        override def warnOnSunset(x509Cert: X509Certificate, expirationDate: GregorianCalendar): Unit = {
          warningCalled = true
        }
      }

      checker.check(certificate, emptySet())
      infoCalled must beFalse
      warningCalled must beFalse
    }

    "info on a signature containing sha-1 that expires between 1 June 2016 to 31 December 2016" in {
      val thirtyDays = 30.days
      val certificate = CertificateGenerator.generateRSAWithSHA1(2048, from = Instant.parse("2016-06-01T12:00:00Z"), duration = thirtyDays)

      var infoCalled = false
      val checker = new AlgorithmChecker(mkLogger, Set.empty, Set.empty) {
        override def infoOnSunset(x509Cert: X509Certificate, expirationDate: GregorianCalendar): Unit = {
          infoCalled = true
        }
      }

      checker.check(certificate, emptySet())
      infoCalled must beTrue
    }

    "warn on a signature containing sha-1 that expires after 2017" in {
      val tenYears = (365 * 10).days
      val certificate = CertificateGenerator.generateRSAWithSHA1(2048, from = Instant.parse("2016-06-01T12:00:00Z"), duration = tenYears)

      var warningCalled = false
      val checker = new AlgorithmChecker(mkLogger, Set.empty, Set.empty) {
        override def warnOnSunset(x509Cert: X509Certificate, expirationDate: GregorianCalendar): Unit = {
          warningCalled = true
        }
      }

      checker.check(certificate, emptySet())
      warningCalled must beTrue
    }
  }
}
