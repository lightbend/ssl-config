/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package play.api.libs.ws

import javax.inject.{ Singleton, Inject, Provider }

import com.typesafe.config.Config
import com.typesafe.sslconfig.ssl.{ SSLConfigParser, SSLConfig }
import play.api.{ PlayConfig, Environment, Configuration }

import scala.concurrent.duration._
import com.typesafe.sslconfig.util._

/**
 * WS client config
 */
case class WSClientConfig(
  connectionTimeout: Duration = 2.minutes,
  idleTimeout: Duration = 2.minutes,
  requestTimeout: Duration = 2.minutes,
  followRedirects: Boolean = true,
  useProxyProperties: Boolean = true,
  userAgent: Option[String] = None,
  compressionEnabled: Boolean = false,
  ssl: SSLConfig = SSLConfig())

/**
 * This class creates a DefaultWSClientConfig object from the play.api.Configuration.
 */
class WSConfigParser(config: Config, classLoader: ClassLoader = Thread.currentThread().getContextClassLoader) {

  def get = parse()

  private def parse(): WSClientConfig = {
    val connectionTimeout = config.getFiniteDuration("timeout.connection")
    val idleTimeout = config.getFiniteDuration("timeout.idle")
    val requestTimeout = config.getFiniteDuration("timeout.request")

    val followRedirects = config.getBoolean("followRedirects")
    val useProxyProperties = config.getBoolean("useProxyProperties")

    val userAgent = config.getOption[String]("useragent")

    val compressionEnabled = config.getBoolean("compressionEnabled")

    val sslConfig = new SSLConfigParser(config.getConfig("ssl"), classLoader).parse()

    WSClientConfig(
      connectionTimeout = connectionTimeout,
      idleTimeout = idleTimeout,
      requestTimeout = requestTimeout,
      followRedirects = followRedirects,
      useProxyProperties = useProxyProperties,
      userAgent = userAgent,
      compressionEnabled = compressionEnabled,
      ssl = sslConfig)
  }
}
