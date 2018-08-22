/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.debug

import com.typesafe.sslconfig.ssl.SSLDebugConfig
import com.typesafe.sslconfig.util.NoopLogger
import org.specs2.mutable.{ After, Specification }

object DebugConfigurationSpec extends Specification with After {

  def after = {
    System.clearProperty("java.security.debug")
    System.clearProperty("javax.net.debug")
  }

  sequential // global settings, must be sequential

  // Loggers not needed, but useful to doublecheck that the code is doing what it should.
  // ./build test-only com.typesafe.sslconfig.ssl.debug.DebugConfigurationSpec
  val mkLogger = NoopLogger.factory()
  val internalDebugLogger = mkLogger.apply("com.typesafe.sslconfig.ssl.debug.FixInternalDebugLogging")
  val certpathDebugLogger = mkLogger.apply("com.typesafe.sslconfig.ssl.debug.FixCertpathDebugLogging")

  "configure" should {

    "turn on java.security.debug code" in {
      Option(System.getProperty("java.security.debug")) must beLike {
        case Some(value) => value must beEmpty
        case None        => ok
      }

      val debugConfig = SSLDebugConfig().withCertPath(true)
      val config = new DebugConfiguration(mkLogger)
      config.configure(debugConfig)

      System.getProperty("java.security.debug") must contain("certpath")
    }

    "turn off java.security.debug code" in {
      System.setProperty("java.security.debug", "certpath")

      val debugConfig = SSLDebugConfig().withCertPath(false)
      val config = new DebugConfiguration(mkLogger)
      config.configure(debugConfig)

      System.getProperty("java.security.debug") must not contain ("certpath")
    }

    "turn on javax.ssl.debug code" in {
      Option(System.getProperty("javax.net.debug")) must beLike {
        case Some(value) => value must beEmpty
        case None        => ok
      }

      val debugConfig = SSLDebugConfig().withSsl(true)
      val config = new DebugConfiguration(mkLogger)
      config.configure(debugConfig)

      System.getProperty("javax.net.debug") must contain("ssl")
    }

    "turn off javax.ssl.debug code" in {
      System.setProperty("javax.net.debug", "ssl")

      val debugConfig = SSLDebugConfig().withSsl(false)
      val config = new DebugConfiguration(mkLogger)
      config.configure(debugConfig)

      System.getProperty("javax.net.debug") must not contain ("ssl")
    }
  }

}
