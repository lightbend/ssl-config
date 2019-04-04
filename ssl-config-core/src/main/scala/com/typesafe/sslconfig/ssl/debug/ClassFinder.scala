/*
 * Copyright (C) 2015 - 2019 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.debug

import com.typesafe.sslconfig.util.NoDepsLogger

@deprecated("ClassFinder has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
trait ClassFinder {
  def logger: NoDepsLogger
  def initialResource: String
  def isValidClass(className: String): Boolean
  def findClasses: Set[Class[_]] = Set.empty
}