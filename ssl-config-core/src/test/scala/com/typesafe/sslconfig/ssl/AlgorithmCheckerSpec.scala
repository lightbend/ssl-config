/*
 * Copyright (C) 2015 - 2019 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.security.cert.{ CertPathValidatorException, Certificate, X509Certificate }
import java.util.Collections._
import java.util.GregorianCalendar

import com.typesafe.sslconfig.ssl.AlgorithmConstraintsParser._
import com.typesafe.sslconfig.util.NoopLogger
import org.joda.time.{ DateTime, Instant }
import scalaprops._

import scala.concurrent.duration._
import scala.reflect.ClassTag

object AlgorithmCheckerProps extends Scalaprops {

  val mkLogger = NoopLogger.factory()

  def checker(sigs: Seq[String], keys: Seq[String]) = {
    new AlgorithmChecker(mkLogger, sigs.map(s => parseAll(expression, s).get).toSet,
      keys.map(s => parseAll(expression, s).get).toSet)
  }

  val `pass a good key algorithm (RSA > 1024)` = {
    pass(checker(Nil, Seq("RSA keySize < 1024")), CertificateGenerator.generateRSAWithSHA256(2048))
  }

  val `fail a weak key algorithm (RSA < 512)` = {
    fail(checker(Nil, Seq("RSA keySize < 1024")), CertificateGenerator.generateRSAWithSHA256(512))
  }

  val `pass a good signature algorithm (SHA256)` = {
    pass(checker(Seq("MD5"), Nil), CertificateGenerator.generateRSAWithSHA256(512))
  }

  val `fail a bad signature algorithm (MD5)` = {
    fail(checker(Seq("MD5"), Nil), CertificateGenerator.generateRSAWithMD5(2048))
  }

  val `neither info nor warning on a signature containing sha-1 that expires before 1 June 2016` = {
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

    pass(checker, certificate)
      .and(Property.prop(!infoCalled))
      .and(Property.prop(!warningCalled))
  }

  val `info on a signature containing sha-1 that expires between 1 June 2016 to 31 December 2016` = {
    val thirtyDays = 30.days
    val certificate = CertificateGenerator.generateRSAWithSHA1(2048, from = Instant.parse("2016-06-01T12:00:00Z"), duration = thirtyDays)

    var infoCalled = false
    val checker = new AlgorithmChecker(mkLogger, Set.empty, Set.empty) {
      override def infoOnSunset(x509Cert: X509Certificate, expirationDate: GregorianCalendar): Unit = {
        infoCalled = true
      }
    }
    pass(checker, certificate)
      .and(Property.prop(infoCalled))
  }

  val `warn on a signature containing sha-1 that expires after 2017` = {
    val tenYears = (365 * 10).days
    val certificate = CertificateGenerator.generateRSAWithSHA1(2048, from = Instant.parse("2016-06-01T12:00:00Z"), duration = tenYears)

    var warningCalled = false
    val checker = new AlgorithmChecker(mkLogger, Set.empty, Set.empty) {
      override def warnOnSunset(x509Cert: X509Certificate, expirationDate: GregorianCalendar): Unit = {
        warningCalled = true
      }
    }

    pass(checker, certificate)
      .and(Property.prop(warningCalled))
  }

  private def pass(chk: AlgorithmChecker, cert: Certificate) =
    noException(chk.check(cert, emptySet()))

  private def fail(chk: AlgorithmChecker, cert: Certificate) =
    throws[CertPathValidatorException](chk.check(cert, emptySet()))

  private def noException(thunk: => Any): Property =
    Property.exception(Property.prop(sideEffect(true, thunk)))

  private def throws[T <: Throwable](thunk: => Any)(implicit ct: ClassTag[T]): Property = {
    Property.prop {
      try sideEffect(false, thunk) catch {
        case t: Throwable if !t.isInstanceOf[VirtualMachineError] =>
          val cl: Class[_] = if (ct eq ClassTag.Nothing) classOf[Throwable] else ct.runtimeClass
          if (cl.isInstance(t)) true else throw t
      }
    }
  }

  private def sideEffect[A](result: A, exprs: Any*): A = result

}
