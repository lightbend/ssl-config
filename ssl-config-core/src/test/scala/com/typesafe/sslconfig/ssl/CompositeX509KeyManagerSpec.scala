/*
 * Copyright (C) 2015 - 2025 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.net.Socket
import java.security.cert.X509Certificate
import java.security.Principal
import java.security.PrivateKey
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.X509KeyManager

import com.typesafe.sslconfig.util.NoopLogger
import org.mockito.Mockito._
import org.specs2.mutable._

object CompositeX509KeyManagerSpec extends Specification {

  def mockExtendedX509KeyManager(clientResponse: String = null, serverResponse: String = null) =
    new X509ExtendedKeyManager() {

      override def chooseEngineClientAlias(
          keyType: Array[String],
          issuers: Array[Principal],
          engine: SSLEngine
      ): String = clientResponse

      override def chooseEngineServerAlias(keyType: String, issuers: Array[Principal], engine: SSLEngine): String =
        serverResponse

      def getClientAliases(keyType: String, issuers: Array[Principal]): Array[String] = ???

      def chooseClientAlias(keyType: Array[String], issuers: Array[Principal], socket: Socket): String = ???

      def getServerAliases(keyType: String, issuers: Array[Principal]): Array[String] = ???

      def chooseServerAlias(keyType: String, issuers: Array[Principal], socket: Socket): String = ???

      def getCertificateChain(alias: String): Array[X509Certificate] = ???

      def getPrivateKey(alias: String): PrivateKey = ???
    }
  val mkLogger = NoopLogger.factory()

  "CompositeX509KeyManager" should {

    "chooseEngineClientAlias" should {
      "not do anything with a X509KeyManager" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = Array("derp")
        val issuers        = Array[Principal]()
        val engine         = mock(classOf[SSLEngine])

        val clientAlias = keyManager.chooseEngineClientAlias(keyType = keyType, issuers = issuers, engine = engine)
        clientAlias must beNull
      }

      "return a result" in {
        val mockKeyManager = mockExtendedX509KeyManager(clientResponse = "clientAlias")
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = Array("derp")
        val issuers        = Array[Principal]()
        val engine         = mock(classOf[SSLEngine])

        val clientAlias = keyManager.chooseEngineClientAlias(keyType = keyType, issuers = issuers, engine = engine)
        clientAlias must be_==("clientAlias")
      }

      "return null" in {
        val mockKeyManager = mockExtendedX509KeyManager()
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = Array("derp")
        val issuers        = Array[Principal]()
        val engine         = mock(classOf[SSLEngine])

        val clientAlias = keyManager.chooseEngineClientAlias(keyType = keyType, issuers = issuers, engine = engine)
        clientAlias must beNull
      }

      "return null when issuers are null" in {
        val mockKeyManager = mockExtendedX509KeyManager()
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = Array("derp")
        val issuers        = null
        val engine         = mock(classOf[SSLEngine])
        val clientAlias    = keyManager.chooseEngineClientAlias(keyType = keyType, issuers = issuers, engine = engine)
        clientAlias must beNull
      }
    }

    "chooseEngineServerAlias" should {

      "not do anything with a X509KeyManager" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = Array[Principal]()
        val engine         = mock(classOf[SSLEngine])

        val serverAlias = keyManager.chooseEngineServerAlias(keyType = keyType, issuers = issuers, engine = engine)
        serverAlias must beNull
      }

      "return a result" in {
        val mockKeyManager = mockExtendedX509KeyManager(serverResponse = "serverAlias")
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = Array[Principal]()
        val engine         = mock(classOf[SSLEngine])

        val serverAlias = keyManager.chooseEngineServerAlias(keyType = keyType, issuers = issuers, engine = engine)
        serverAlias must be_==("serverAlias")
      }

      "return null" in {
        val mockKeyManager = mockExtendedX509KeyManager()
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = Array[Principal]()
        val engine         = mock(classOf[SSLEngine])

        val serverAlias = keyManager.chooseEngineServerAlias(keyType = keyType, issuers = issuers, engine = engine)
        serverAlias must beNull
      }

      "return null when issuers are null" in {
        val mockKeyManager = mockExtendedX509KeyManager()
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = null
        val engine         = mock(classOf[SSLEngine])
        val serverAlias    = keyManager.chooseEngineServerAlias(keyType = keyType, issuers = issuers, engine = engine)
        serverAlias must beNull
      }
    }

    "chooseClientAlias" should {
      "return a result" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = Array("derp")
        val issuers        = Array[Principal]()
        val socket         = mock(classOf[Socket])

        when(mockKeyManager.chooseClientAlias(keyType, issuers, socket)).thenReturn("clientAlias")

        val clientAlias = keyManager.chooseClientAlias(keyType = keyType, issuers = issuers, socket = socket)
        clientAlias must be_==("clientAlias")
      }

      "return null" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = Array("derp")
        val issuers        = Array[Principal]()
        val socket         = mock(classOf[Socket])

        when(mockKeyManager.chooseClientAlias(keyType, issuers, socket)).thenReturn(null)

        val clientAlias = keyManager.chooseClientAlias(keyType = keyType, issuers = issuers, socket = socket)
        clientAlias must beNull
      }

      "return null when issuers are null" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = Array("derp")
        val issuers        = null
        val socket         = mock(classOf[Socket])
        when(mockKeyManager.chooseClientAlias(keyType, issuers, socket)).thenReturn(null)
        val clientAlias = keyManager.chooseClientAlias(keyType = keyType, issuers = issuers, socket = socket)
        clientAlias must beNull
      }
    }

    "getClientAliases" should {

      "return a result" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = Array[Principal]()

        when(mockKeyManager.getClientAliases(keyType, issuers)).thenReturn(Array("clientAliases"))

        val clientAliases = keyManager.getClientAliases(keyType = keyType, issuers = issuers)
        clientAliases must be_==(Array("clientAliases"))
      }

      "return null" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = Array[Principal]()

        when(mockKeyManager.getClientAliases(keyType, issuers)).thenReturn(null)

        val clientAliases = keyManager.getClientAliases(keyType = keyType, issuers = issuers)
        clientAliases must beNull
      }

      "return null when issuers are null" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = null
        when(mockKeyManager.getServerAliases(keyType, issuers)).thenReturn(null)
        val serverAliases = keyManager.getServerAliases(keyType = keyType, issuers = issuers)
        serverAliases must beNull
      }
    }

    "getServerAliases" should {
      "return a result" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = Array[Principal]()

        when(mockKeyManager.getServerAliases(keyType, issuers)).thenReturn(Array("serverAliases"))

        val serverAliases = keyManager.getServerAliases(keyType = keyType, issuers = issuers)
        serverAliases must be_==(Array("serverAliases"))
      }

      "return null" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = Array[Principal]()

        when(mockKeyManager.getServerAliases(keyType, issuers)).thenReturn(null)

        val serverAliases = keyManager.getServerAliases(keyType = keyType, issuers = issuers)
        serverAliases must beNull
      }
    }

    "chooseServerAlias" should {
      "work fine" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = Array[Principal]()
        val socket         = mock(classOf[Socket])

        when(mockKeyManager.chooseServerAlias(keyType, issuers, socket)).thenReturn("serverAlias")

        val serverAlias = keyManager.chooseServerAlias(keyType = keyType, issuers = issuers, socket = socket)
        serverAlias must be_==("serverAlias")
      }

      "return null" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = Array[Principal]()
        val socket         = mock(classOf[Socket])

        when(mockKeyManager.chooseServerAlias(keyType, issuers, socket)).thenReturn(null)

        val serverAlias = keyManager.chooseServerAlias(keyType = keyType, issuers = issuers, socket = socket)
        serverAlias must beNull
      }

      "return null when issuers are null" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val keyType        = "derp"
        val issuers        = null
        val socket         = mock(classOf[Socket])
        when(mockKeyManager.chooseServerAlias(keyType, issuers, socket)).thenReturn(null)
        val serverAlias = keyManager.chooseServerAlias(keyType = keyType, issuers = issuers, socket = socket)
        serverAlias must beNull
      }
    }

    "getCertificateChain" should {
      "return null" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val alias          = "alias"

        when(mockKeyManager.getCertificateChain(alias)).thenReturn(null)

        val certChain = keyManager.getCertificateChain(alias = alias)
        certChain must beNull
      }
    }

    "getPrivateKey" should {
      "work fine" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val alias          = "alias"
        val privateKey     = mock(classOf[PrivateKey])

        when(mockKeyManager.getPrivateKey(alias)).thenReturn(privateKey)

        val actual = keyManager.getPrivateKey(alias = alias)
        actual must be_==(privateKey)
      }

      "return null" in {
        val mockKeyManager = mock(classOf[X509KeyManager])
        val keyManager     = new CompositeX509KeyManager(mkLogger, Seq(mockKeyManager))
        val alias          = "alias"

        when(mockKeyManager.getPrivateKey(alias)).thenReturn(null)

        val actual = keyManager.getPrivateKey(alias = alias)
        actual must beNull
      }
    }

  }
}
