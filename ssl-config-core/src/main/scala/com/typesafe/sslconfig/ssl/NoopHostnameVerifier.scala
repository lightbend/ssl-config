/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import javax.net.ssl.{ HostnameVerifier, SSLSession }

final class NoopHostnameVerifier extends HostnameVerifier {
  def verify(hostname: String, sslSession: SSLSession): Boolean = true
}
