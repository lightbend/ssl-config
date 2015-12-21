/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.util

trait LoggerFactory {
  def apply(clazz: Class[_]): NoDepsLogger
  def apply(name: String): NoDepsLogger
}