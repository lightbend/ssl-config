/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig

import javax.net.ssl.SSLContext

import com.typesafe.config.{ Config, ConfigFactory }

// TODO to be renamed ;-)
object TheSSL {

  def apply(): SSLContext = apply(ConfigFactory.load().getConfig("sslconfig"))

  def apply(config: Config): SSLContext = ???

}
