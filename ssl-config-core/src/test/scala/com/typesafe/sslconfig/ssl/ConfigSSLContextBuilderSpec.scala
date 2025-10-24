/*
 * Copyright (C) 2015 - 2025 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.net.Socket
import java.security._
import java.security.cert.X509Certificate
import javax.net.ssl._

import com.typesafe.sslconfig.util.NoopLogger
import org.mockito.Mockito._
import org.specs2.mutable._

class ConfigSSLContextBuilderSpec extends Specification {

  val CACERTS = s"${System.getProperty("java.home")}/lib/security/cacerts"

  val mkLogger = NoopLogger.factory()

  "ConfigSSLContentBuilder" should {

    "should have the right protocol by default" in {
      val info = SSLConfigSettings()

      val keyManagerFactory   = mockKeyManagerFactory
      val trustManagerFactory = mockTrustManagerFactory

      val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val actual: SSLContext = builder.build()
      actual.getProtocol must_== Protocols.recommendedProtocol
    }

    "with protocol" should {

      "should default to Protocols.recommendedProtocols" in {
        val info = SSLConfigSettings()

        val keyManagerFactory   = mockKeyManagerFactory
        val trustManagerFactory = mockTrustManagerFactory

        val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

        val actual: SSLContext = builder.build()
        actual.getProtocol must_== Protocols.recommendedProtocol
      }

      "should have an explicit protocol if defined" in {
        val info = SSLConfigSettings().withProtocol("TLS")

        val keyManagerFactory   = mockKeyManagerFactory
        val trustManagerFactory = mockTrustManagerFactory

        val builder = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

        val actual: SSLContext = builder.build()
        actual.getProtocol must_== "TLS"
      }
    }

    "build a trust manager" in {
      val info                = SSLConfigSettings()
      val keyManagerFactory   = mockKeyManagerFactory
      val trustManagerFactory = mockTrustManagerFactory
      val builder             = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val trustManagerConfig = TrustManagerConfig()
      val checkRevocation    = false
      val revocationLists    = None

      val actual =
        builder.buildCompositeTrustManager(trustManagerConfig, checkRevocation, revocationLists, SSLDebugConfig())
      actual must beAnInstanceOf[javax.net.ssl.X509TrustManager]
    }

    "build a composite key manager" in {
      val info                = SSLConfigSettings()
      val keyManagerFactory   = mockKeyManagerFactory
      val trustManagerFactory = mockTrustManagerFactory
      val builder             = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val keyManagerConfig = new KeyManagerConfig()

      val actual = builder.buildCompositeKeyManager(keyManagerConfig, SSLDebugConfig())
      actual must beAnInstanceOf[CompositeX509KeyManager]
    }

    "build a composite trust manager" in {
      val info                = SSLConfigSettings()
      val keyManagerFactory   = mockKeyManagerFactory
      val trustManagerFactory = mockTrustManagerFactory
      val builder             = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val trustManagerConfig = TrustManagerConfig()
      val checkRevocation    = false
      val revocationLists    = None

      val actual =
        builder.buildCompositeTrustManager(trustManagerConfig, checkRevocation, revocationLists, SSLDebugConfig())
      actual must beAnInstanceOf[CompositeX509TrustManager]
    }

    "build a file based keystore builder" in {
      val info                = SSLConfigSettings()
      val keyManagerFactory   = mock(classOf[KeyManagerFactoryWrapper])
      val trustManagerFactory = mock(classOf[TrustManagerFactoryWrapper])
      val builder             = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val storeType = KeyStore.getDefaultType
      val filePath  = "derp"

      val actual = builder.fileBuilder(storeType, filePath, None)
      actual must beAnInstanceOf[FileBasedKeyStoreBuilder]
    }

    "build a file on classpath based keystore builder" in {
      val info                = SSLConfigSettings()
      val keyManagerFactory   = mock(classOf[KeyManagerFactoryWrapper])
      val trustManagerFactory = mock(classOf[TrustManagerFactoryWrapper])
      val builder             = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val storeType = KeyStore.getDefaultType
      val filePath  = "derp"

      val actual = builder.fileOnClasspathBuilder(storeType, filePath, None)
      actual must beAnInstanceOf[FileOnClasspathBasedKeyStoreBuilder]

    }

    "build a string based keystore builder" in {
      val info                = SSLConfigSettings()
      val keyManagerFactory   = mock(classOf[KeyManagerFactoryWrapper])
      val trustManagerFactory = mock(classOf[TrustManagerFactoryWrapper])
      val builder             = new ConfigSSLContextBuilder(mkLogger, info, keyManagerFactory, trustManagerFactory)

      val data = "derp"

      val actual = builder.stringBuilder(data)
      actual must beAnInstanceOf[StringBasedKeyStoreBuilder]
    }
  }

  private def mockTrustManagerFactory = {
    new TrustManagerFactoryWrapper {
      override def init(spec: ManagerFactoryParameters): Unit = {}

      override def getTrustManagers: Array[TrustManager] = Array(
        new X509ExtendedTrustManager {
          override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String, socket: Socket): Unit =
            ???

          override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String, socket: Socket): Unit =
            ???

          override def checkClientTrusted(
              x509Certificates: Array[X509Certificate],
              s: String,
              sslEngine: SSLEngine
          ): Unit = ???

          override def checkServerTrusted(
              x509Certificates: Array[X509Certificate],
              s: String,
              sslEngine: SSLEngine
          ): Unit = ???

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

          override def chooseClientAlias(strings: Array[String], principals: Array[Principal], socket: Socket): String =
            ???

          override def getServerAliases(s: String, principals: Array[Principal]): Array[String] = ???

          override def chooseServerAlias(s: String, principals: Array[Principal], socket: Socket): String = ???

          override def getCertificateChain(s: String): Array[X509Certificate] = ???

          override def getPrivateKey(s: String): PrivateKey = ???
        })
      }
    }
  }
}
