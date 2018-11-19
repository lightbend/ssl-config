/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.util._
import javax.net.ssl._
import org.specs2.mutable.Specification

import scala.collection.mutable

class LoggingSSLFactorySpec extends Specification {

  "logger" should {
    "work" in {
      val input =
        """
          |ssl-config {
          |  debug {
          |    ssl = true # setting this will turn on debugging for SSLEngine / SSLSocketFactory
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

      val parser = new SSLConfigParser(EnrichedConfig(config.getConfig("ssl-config")), getClass.getClassLoader)
      val info = parser.parse()

      val keyManagerFactory: KeyManagerFactoryWrapper = new DefaultKeyManagerFactoryWrapper(KeyManagerFactory.getDefaultAlgorithm)
      val trustManagerFactory: TrustManagerFactoryWrapper = new DefaultTrustManagerFactoryWrapper(TrustManagerFactory.getDefaultAlgorithm)

      val messagesList = mutable.Buffer[String]()
      val loggerFactory = new LoggerFactory {
        override def apply(clazz: Class[_]) = new PrintlnLogger(messagesList)
        override def apply(name: String) = new PrintlnLogger(messagesList)
      }
      val builder = new ConfigSSLContextBuilder(loggerFactory, info, keyManagerFactory, trustManagerFactory)
      val context = builder.build()

      val factory = context.getSocketFactory
      val socket = factory.createSocket()

      messagesList must contain("entry: createSocket()")
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
