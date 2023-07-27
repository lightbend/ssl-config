/*
 * Copyright (C) 2015 - 2023 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.util._
import org.specs2.mutable.Specification

import javax.net.ssl._
import scala.collection.mutable

class LoggingSSLFactorySpec extends Specification {

  "logger" should {
    "work" in {
      val input =
        """
          |ssl-config {
          |  debug {
          |    ssl = true # setting this will turn on debugging for SSLEngine / SSLSocketFactory
          |    pluggability = true # print out a warning
          |  }
          |  trustManager = {
          |    stores = [
          |      {
          |        path: ${java.home}/lib/security/cacerts,
          |        password = "changeit",
          |      }
          |    ]
          |  }
          |}
        """.stripMargin
      val config = ConfigFactory.systemProperties().withFallback(ConfigFactory.parseString(input).withFallback(ConfigFactory.defaultReference())).resolve()

      //val str = config.root.render(ConfigRenderOptions.defaults())
      //println(str)

      val messagesList = mutable.Buffer[String]()
      val loggerFactory: LoggerFactory = new LoggerFactory {
        override def apply(clazz: Class[_]) = new PrintlnLogger(messagesList)
        override def apply(name: String) = new PrintlnLogger(messagesList)
      }
      val parser = new SSLConfigParser(EnrichedConfig(config.getConfig("ssl-config")), getClass.getClassLoader, Some(loggerFactory))
      val info = parser.parse()

      val keyManagerFactory: KeyManagerFactoryWrapper = new DefaultKeyManagerFactoryWrapper(KeyManagerFactory.getDefaultAlgorithm)
      val trustManagerFactory: TrustManagerFactoryWrapper = new DefaultTrustManagerFactoryWrapper(TrustManagerFactory.getDefaultAlgorithm)

      val builder = new ConfigSSLContextBuilder(loggerFactory, info, keyManagerFactory, trustManagerFactory)
      val context = builder.build()

      val factory = context.getSocketFactory
      val socket = factory.createSocket()

      messagesList must contain("entry: createSocket()")
      messagesList must contain("pluggability is a deprecated debug setting and has no effect!")
    }
  }

  final class PrintlnLogger(list: mutable.Buffer[String]) extends NoDepsLogger {
    override def debug(msg: String): Unit = ()

    override def info(msg: String): Unit = ()

    override def warn(msg: String): Unit = {
      list.append(msg)
    }

    override def error(msg: String): Unit = ()

    override def error(msg: String, throwable: Throwable): Unit = ()

    override def isDebugEnabled: Boolean = true
  }

}
