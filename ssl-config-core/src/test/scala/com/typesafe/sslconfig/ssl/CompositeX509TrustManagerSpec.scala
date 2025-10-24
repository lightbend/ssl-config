/*
 * Copyright (C) 2015 - 2025 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

import com.typesafe.sslconfig.util.NoopLogger
import org.mockito.Mockito._
import org.specs2.mutable._

object CompositeX509TrustManagerSpec extends Specification {

  val mkLogger = NoopLogger.factory()

  "CompositeX509TrustManager" should {

    "getAcceptedIssuers" should {

      "throw exception when input exception" in {
        val mockTrustManager = mock(classOf[X509TrustManager])
        val trustManager     = new CompositeX509TrustManager(mkLogger, trustManagers = Seq(mockTrustManager))
        when(mockTrustManager.getAcceptedIssuers).thenThrow(new RuntimeException("fake"))

        trustManager.getAcceptedIssuers.must(throwA[CompositeCertificateException].like {
          case e: CompositeCertificateException =>
            val sourceExceptions = e.getSourceExceptions
            sourceExceptions(0).getMessage must be_==("fake")
        })
      }
    }
  }
}
