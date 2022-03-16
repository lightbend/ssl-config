/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import javax.security.cert.X509Certificate

import com.typesafe.sslconfig.util.LoggerFactory

@deprecated("not operative", "0.5.0")
class AlgorithmChecker(mkLogger: LoggerFactory, val signatureConstraints: Set[AlgorithmConstraint], val keyConstraints: Set[AlgorithmConstraint]) {
  def checkKeyAlgorithms(x509Cert: X509Certificate): Unit = ()
}
