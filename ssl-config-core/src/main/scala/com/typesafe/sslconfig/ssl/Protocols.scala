/*
 * Copyright (C) 2015 - 2023 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

object Protocols {

  /**
   * Protocols which are known to be insecure.
   */
  val deprecatedProtocols = Set("SSL", "SSLv2Hello", "SSLv3")

  val recommendedProtocols = Array("TLSv1.2", "TLSv1.1", "TLSv1")

  // Use 1.2 as a default in 1.7
  // https://docs.fedoraproject.org/en-US/Fedora_Security_Team//html/Defensive_Coding/sect-Defensive_Coding-TLS-Client-OpenJDK.html
  def recommendedProtocol = "TLSv1.2"

}
