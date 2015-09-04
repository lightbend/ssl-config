/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.util

import java.util.Date

import scala.concurrent.duration.FiniteDuration

// Looks like JDK8 Instant, to make migration simpler
final class Instant(t: Date) {
  def +(duration: FiniteDuration): Instant =
    new Instant(new Date(t.getTime + duration.toMillis))

  def toDate: Date = new Date(t)
}

object Instant {
  def now = new Instant(new Date())
}
