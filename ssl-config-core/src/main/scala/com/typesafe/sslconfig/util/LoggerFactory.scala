/*
 * Copyright (C) 2015 - 2023 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.util

trait LoggerFactory {
  def apply(clazz: Class[_]): NoDepsLogger
  def apply(name: String): NoDepsLogger
}