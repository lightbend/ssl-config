/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.nio.file.Files

import com.typesafe.sslconfig.util.NoopLogger
import org.specs2.mutable.Specification

import scala.util.Try

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

    "build a keystore with a selfsigned certificate and trust on that certificate" in {
      // create and persist a key store
      val fakeKeyStore = new FakeKeyStore(mkLogger)
      val basePath = Files.createTempDirectory("fake-keystore-spec-").toFile
      val ksPath = fakeKeyStore.getKeyStoreFilePath(basePath)
      fakeKeyStore.createKeyStore(basePath)

      // load the persisted key store
      val fakeKeyStore2 = new FakeKeyStore(mkLogger)
      val keyStore = fakeKeyStore2.createKeyStore(basePath)
      try {
        import scala.collection.JavaConverters._
        val certificates = keyStore.aliases().asScala.flatMap {
          alias =>
            Try(keyStore.getCertificate(alias)).toOption
        }
        certificates.size must be_==(2) // the self-signed and the trusted
      } finally {
        ksPath.delete()
      }
    }

    "build a keystore that's compatible with sslconfig.KeyStore" in {
      // create and persist a key store
      val fakeKeyStore = new FakeKeyStore(mkLogger)
      val basePath = Files.createTempDirectory("fake-keystore-spec-").toFile
      val ksPath = fakeKeyStore.getKeyStoreFilePath(basePath)
      fakeKeyStore.createKeyStore(basePath)


      // load the persisted key store using sslconfig.KeyStore
      val keyStore = new FileBasedKeyStoreBuilder(
        FakeKeyStore.KeystoreSettings.KeystoreType,
        ksPath.getAbsolutePath,
        Some(FakeKeyStore.KeystoreSettings.keystorePassword)
      ).build()
      try {
        import scala.collection.JavaConverters._
        val certificates = keyStore.aliases().asScala.flatMap {
          alias =>
            Try(keyStore.getCertificate(alias)).toOption
        }
        certificates.size must be_==(2) // the self-signed and the trusted
      } finally {
        ksPath.delete()
      }
    }

  }

}
