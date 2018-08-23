/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import org.specs2.mutable._

object DebugBuilderSpec extends Specification {

  "JavaSecurityDebugBuilder" should {

    "match nothing by default" in {
      val config = SSLDebugConfig().withCertPath(false)
      val builder = new JavaSecurityDebugBuilder(config)
      val actual = builder.build()

      actual.trim must beEmpty
    }

    "match certpath" in {
      val config = SSLDebugConfig().withCertPath(true)
      val builder = new JavaSecurityDebugBuilder(config)
      val actual = builder.build()

      actual.trim.split("\\s+").toSeq must containTheSameElementsAs(Seq("certpath"))
    }

    "match certpath + ocsp" in {
      val config = SSLDebugConfig().withCertPath(true).withOcsp(true)
      val builder = new JavaSecurityDebugBuilder(config)
      val actual = builder.build()

      actual.trim.split("\\s+").toSeq must containTheSameElementsAs(Seq("certpath", "ocsp"))
    }
  }

  "JavaxNetDebugBuilder" should {
    "match nothing by default" in {
      val config = SSLDebugConfig()
      val builder = new JavaxNetDebugBuilder(config)
      val actual = builder.build()

      actual.trim must beEmpty
    }

    "match all" in {
      val config = SSLDebugConfig().withAll(true)
      val builder = new JavaxNetDebugBuilder(config)
      val actual = builder.build()

      actual.trim.split("\\s+").toSeq must containTheSameElementsAs(Seq("all"))
    }

    "match some random combinations" in {
      val config = SSLDebugConfig().withSsl(true).withDefaultContext(true).withHandshake(Some(SSLDebugHandshakeOptions().withData(true)))
      val builder = new JavaxNetDebugBuilder(config)
      val actual: String = builder.build()

      actual.trim.split("\\s+").toSeq must containTheSameElementsAs(Seq("ssl", "defaultctx", "handshake", "data"))
    }

  }
}
