package com.typesafe.sslconfig.ssl

import javax.net.ssl.{ HostnameVerifier, SSLSession }

final class NoopHostnameVerifier extends HostnameVerifier {
  def verify(hostname: String, sslSession: SSLSession): Boolean = true
}
