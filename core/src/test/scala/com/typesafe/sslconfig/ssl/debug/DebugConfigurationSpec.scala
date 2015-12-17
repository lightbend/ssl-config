/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.ssl.debug

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.NoDepsLogger
import org.specs2.mutable.{ Specification, After }

object DebugConfigurationSpec extends Specification with After {

  def after = {
    System.clearProperty("java.security.debug")
    System.clearProperty("javax.net.debug")
  }

  sequential // global settings, must be sequential

  // Loggers not needed, but useful to doublecheck that the code is doing what it should.
  // ./build test-only com.typesafe.sslconfig.ssl.debug.DebugConfigurationSpec
  val internalDebugLogger = NoDepsLogger.get("com.typesafe.sslconfig.ssl.debug.FixInternalDebugLogging")
  val certpathDebugLogger = NoDepsLogger.get("com.typesafe.sslconfig.ssl.debug.FixCertpathDebugLogging")

  "configure" should {

    "turn on java.security.debug code" in {
      Option(System.getProperty("java.security.debug")) must beLike {
        case Some(value) => value must beEmpty
        case None        => ok
      }

      val debugConfig = SSLDebugConfig(certpath = true)
      val config = new DebugConfiguration()
      config.configure(debugConfig)

      System.getProperty("java.security.debug") must contain("certpath")
    }

    "turn off java.security.debug code" in {
      System.setProperty("java.security.debug", "certpath")

      val debugConfig = SSLDebugConfig(certpath = false)
      val config = new DebugConfiguration()
      config.configure(debugConfig)

      System.getProperty("java.security.debug") must not contain ("certpath")
    }

    "turn on javax.ssl.debug code" in {
      Option(System.getProperty("javax.net.debug")) must beLike {
        case Some(value) => value must beEmpty
        case None        => ok
      }

      val debugConfig = SSLDebugConfig(ssl = true)
      val config = new DebugConfiguration()
      config.configure(debugConfig)

      System.getProperty("javax.net.debug") must contain("ssl")
    }

    "turn off javax.ssl.debug code" in {
      System.setProperty("javax.net.debug", "ssl")

      val debugConfig = SSLDebugConfig(ssl = false)
      val config = new DebugConfiguration()
      config.configure(debugConfig)

      System.getProperty("javax.net.debug") must not contain ("ssl")
    }
  }

}
