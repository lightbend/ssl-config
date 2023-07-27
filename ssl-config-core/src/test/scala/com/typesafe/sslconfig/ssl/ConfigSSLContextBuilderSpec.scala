/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import com.typesafe.sslconfig.util.NoopLogger
import org.specs2.mock._
import org.specs2.mutable._

import java.net.Socket
import java.security._
import java.security.cert.X509Certificate
import javax.net.ssl._

class ConfigSSLContextBuilderSpec extends Specification with Mockito {

  val CACERTS = s"${System.getProperty("java.home")}/lib/security/cacerts"

  val mkLogger = NoopLogger.factory()

  "ConfigSSLContentBuilder" should {

    "should have the right protocol by default" in {
      val info = SSLConfigSettings()

      val keyManagerFactory = mockKeyManagerFactory
      val trustManagerFactory = mockTrustManagerFactory

      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val actual: SSLContext = builder.build()
      actual.getProtocol must_== Protocols.recommendedProtocol
    }

    "with protocol" should {

      "should default to Protocols.recommendedProtocols" in {
        val info = SSLConfigSettings()

        val keyManagerFactory = mockKeyManagerFactory
        val trustManagerFactory = mockTrustManagerFactory

        val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

        val actual: SSLContext = builder.build()
        actual.getProtocol must_== Protocols.recommendedProtocol
      }

      "should have an explicit protocol if defined" in {
        val info = SSLConfigSettings().withProtocol("TLS")

        val keyManagerFactory = mockKeyManagerFactory
        val trustManagerFactory = mockTrustManagerFactory

        val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

        val actual: SSLContext = builder.build()
        actual.getProtocol must_== "TLS"
      }
    }

    "build a key manager" in {
      val info = SSLConfigSettings()
      val keyManagerFactory = mockKeyManagerFactory
      val trustManagerFactory = mockTrustManagerFactory

      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val keyStore = KeyStore.getInstance("PKCS12")
      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048) // 2048 is the NIST acceptable key length until 2030
      val keyPair = keyPairGenerator.generateKeyPair()
      val cert = FakeKeyStore.createSelfSignedCertificate(keyPair)
      val password = "changeit" // cannot have a null password for PKCS12 in 1.6
      keyStore.load(null, password.toCharArray)
      keyStore.setKeyEntry("playgenerated", keyPair.getPrivate, password.toCharArray, Array(cert))

      val tempFile = java.io.File.createTempFile("privatekeystore", ".p12")
      val out = java.nio.file.Files.newOutputStream(tempFile.toPath)
      try {
        keyStore.store(out, password.toCharArray)
      } finally {
        out.close()
      }
      val filePath = tempFile.getAbsolutePath
      val keyStoreConfig = KeyStoreConfig(None, Some(filePath)).withStoreType("PKCS12").withPassword(Some(password))

      val actual = builder.buildKeyManager(keyStoreConfig, SSLDebugConfig())
      actual must beAnInstanceOf[X509KeyManager]
    }

    "build a trust manager" in {
      val info = SSLConfigSettings()
      val keyManagerFactory = mockKeyManagerFactory
      val trustManagerFactory = mockTrustManagerFactory
      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val trustManagerConfig = TrustManagerConfig()
      val checkRevocation = false
      val revocationLists = None

      val actual = builder.buildCompositeTrustManager(trustManagerConfig, checkRevocation, revocationLists, SSLDebugConfig())
      actual must beAnInstanceOf[javax.net.ssl.X509TrustManager]
    }

    "build a composite key manager" in {
      val info = SSLConfigSettings()
      val keyManagerFactory = mockKeyManagerFactory
      val trustManagerFactory = mockTrustManagerFactory
      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val keyManagerConfig = new KeyManagerConfig()

      val actual = builder.buildCompositeKeyManager(keyManagerConfig, SSLDebugConfig())
      actual must beAnInstanceOf[CompositeX509KeyManager]
    }

    "build a composite trust manager" in {
      val info = SSLConfigSettings()
      val keyManagerFactory = mockKeyManagerFactory
      val trustManagerFactory = mockTrustManagerFactory
      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val trustManagerConfig = TrustManagerConfig()
      val checkRevocation = false
      val revocationLists = None

      val actual = builder.buildCompositeTrustManager(
        trustManagerConfig,
        checkRevocation,
        revocationLists, SSLDebugConfig())
      actual must beAnInstanceOf[CompositeX509TrustManager]
    }

    "build a composite trust manager with data" in {
      val info = SSLConfigSettings()
      val keyManagerFactory = new DefaultKeyManagerFactoryWrapper(KeyManagerFactory.getDefaultAlgorithm)
      val trustManagerFactory = new DefaultTrustManagerFactoryWrapper(TrustManagerFactory.getDefaultAlgorithm)
      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val certificate = CertificateGenerator.generateRSAWithSHA256()
      val certificateData = CertificateGenerator.toPEM(certificate)

      val trustStoreConfig = TrustStoreConfig(Some(certificateData), None).withStoreType("PEM")
      val trustManagerConfig = TrustManagerConfig().withTrustStoreConfigs(List(trustStoreConfig))

      val checkRevocation = false
      val revocationLists = None

      val actual = builder.buildCompositeTrustManager(trustManagerConfig, checkRevocation, revocationLists, SSLDebugConfig())

      actual must beAnInstanceOf[CompositeX509TrustManager]
      val issuers = actual.getAcceptedIssuers
      issuers.size must beEqualTo(1)
    }

    "build a file based keystore builder" in {
      val info = SSLConfigSettings()
      val keyManagerFactory = mock[KeyManagerFactoryWrapper]
      val trustManagerFactory = mock[TrustManagerFactoryWrapper]
      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val storeType = KeyStore.getDefaultType
      val filePath = "derp"

      val actual = builder.fileBuilder(storeType, filePath, None)
      actual must beAnInstanceOf[FileBasedKeyStoreBuilder]
    }

    "build a file on classpath based keystore builder" in {
      val info = SSLConfigSettings()
      val keyManagerFactory = mock[KeyManagerFactoryWrapper]
      val trustManagerFactory = mock[TrustManagerFactoryWrapper]
      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val storeType = KeyStore.getDefaultType
      val filePath = "derp"

      val actual = builder.fileOnClasspathBuilder(storeType, filePath, None)
      actual must beAnInstanceOf[FileOnClasspathBasedKeyStoreBuilder]

    }

    "build a string based keystore builder" in {
      val info = SSLConfigSettings()
      val keyManagerFactory = mock[KeyManagerFactoryWrapper]
      val trustManagerFactory = mock[TrustManagerFactoryWrapper]
      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val data = "derp"

      val actual = builder.stringBuilder(data)
      actual must beAnInstanceOf[StringBasedKeyStoreBuilder]
    }

    "validate success of the keystore with a private key" in {
      val keyStore = KeyStore.getInstance("PKCS12")

      // Generate the key pair
      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048) // 2048 is the NIST acceptable key length until 2030
      val keyPair = keyPairGenerator.generateKeyPair()

      // Generate a self signed certificate
      val cert = FakeKeyStore.createSelfSignedCertificate(keyPair)

      val password = "changeit" // null passwords throw exception in 1.6
      keyStore.load(null, password.toCharArray)
      keyStore.setKeyEntry("playgenerated", keyPair.getPrivate, password.toCharArray, Array(cert))

      val keyManagerFactory = mock[KeyManagerFactoryWrapper]
      val trustManagerFactory = mock[TrustManagerFactoryWrapper]

      val ksc = KeyStoreConfig(None, Some("path")).withPassword(Some(password))
      val keyManagerConfig = KeyManagerConfig().withKeyStoreConfigs(List(ksc))

      val info = SSLConfigSettings().withKeyManagerConfig(keyManagerConfig)
      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)
      builder.validateStoreContainsPrivateKeys(ksc, keyStore) must beTrue
    }

    "validate a failure of the keystore without a private key" in {
      // must be JKS, PKCS12 does not support trusted certificate entries in 1.6 at least
      // KeyStoreException: : TrustedCertEntry not supported  (PKCS12KeyStore.java:620)
      // val keyStore = KeyStore.getInstance("PKCS12")
      val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)

      // Generate the key pair
      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048) // 2048 is the NIST acceptable key length until 2030
      val keyPair = keyPairGenerator.generateKeyPair()

      // Generate a self signed certificate
      val cert = FakeKeyStore.createSelfSignedCertificate(keyPair)

      val password = "changeit" // null passwords throw exception in 1.6 in PKCS12
      keyStore.load(null, password.toCharArray)
      // Don't add the private key here, instead add a public cert only.
      keyStore.setCertificateEntry("playgeneratedtrusted", cert)

      val keyManagerFactory = mock[KeyManagerFactoryWrapper]
      val trustManagerFactory = mock[TrustManagerFactoryWrapper]

      val ksc = KeyStoreConfig(None, Some("path")).withPassword(Some(password))
      val keyManagerConfig = KeyManagerConfig().withKeyStoreConfigs(List(ksc))

      val info = SSLConfigSettings().withKeyManagerConfig(keyManagerConfig)
      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      builder.validateStoreContainsPrivateKeys(ksc, keyStore) must beFalse
    }
  }

  private def mockTrustManagerFactory = {
    new TrustManagerFactoryWrapper {
      override def init(spec: ManagerFactoryParameters): Unit = {}

      override def getTrustManagers: Array[TrustManager] = Array(
        new X509ExtendedTrustManager {
          override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String, socket: Socket): Unit = ???

          override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String, socket: Socket): Unit = ???

          override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String, sslEngine: SSLEngine): Unit = ???

          override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String, sslEngine: SSLEngine): Unit = ???

          override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = ???

          override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = ???

          override def getAcceptedIssuers: Array[X509Certificate] = ???
        }
      )
    }
  }

  private def mockKeyManagerFactory = {
    new KeyManagerFactoryWrapper {
      override def init(keystore: KeyStore, password: Array[Char]): Unit = {}

      override def getKeyManagers: Array[KeyManager] = {
        Array(new X509ExtendedKeyManager {
          override def getClientAliases(s: String, principals: Array[Principal]): Array[String] = ???

          override def chooseClientAlias(strings: Array[String], principals: Array[Principal], socket: Socket): String = ???

          override def getServerAliases(s: String, principals: Array[Principal]): Array[String] = ???

          override def chooseServerAlias(s: String, principals: Array[Principal], socket: Socket): String = ???

          override def getCertificateChain(s: String): Array[X509Certificate] = ???

          override def getPrivateKey(s: String): PrivateKey = ???
        })
      }
    }
  }
}
