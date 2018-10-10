/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import com.typesafe.sslconfig.util.NoopLogger

import org.specs2.mutable.Specification

class FakeKeyStoreSpec extends Specification {
  val mkLogger = NoopLogger.factory()

  "FakeKeyStore construction" should {
    "return true on MD5 cert" in {
      val weakCert = CertificateGenerator.generateRSAWithMD5()
      val actual = new FakeKeyStore(mkLogger).certificateTooWeak(weakCert)
      actual must beTrue
    }
    "return false on SHA256withRSA" in {
      val strongCert = CertificateGenerator.generateRSAWithSHA256()
      val actual = new FakeKeyStore(mkLogger).certificateTooWeak(strongCert)
      actual must beFalse
    }
  }

}
