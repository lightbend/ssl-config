/*
 * Copyright (C) 2015 - 2023 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import org.bouncycastle.asn1.{ ASN1InputStream, ASN1Sequence }
import org.bouncycastle.asn1.x509.{ SubjectKeyIdentifier, SubjectPublicKeyInfo }
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.bc.BcX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.OperatorCreationException
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

import java.io.{ ByteArrayInputStream, IOException }
import java.security.{ PrivateKey, Security }
import java.security.cert.X509Certificate

object BCTools {
  {
    Security.addProvider(new BouncyCastleProvider());
  }

  @throws[OperatorCreationException]
  def signCertificate(algorithm: String, builder: X509v3CertificateBuilder, key: PrivateKey): X509Certificate = {
    val signer = new JcaContentSignerBuilder(algorithm).setProvider(BouncyCastleProvider.PROVIDER_NAME).build(key)
    val certificate = builder.build(signer)
    new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(certificate)
  }

  @throws[IOException]
  def createSubjectKeyIdentifier(key: java.security.Key): SubjectKeyIdentifier = {
    val inputStream = new ASN1InputStream(new ByteArrayInputStream(key.getEncoded))
    try {
      val seq = inputStream.readObject.asInstanceOf[ASN1Sequence]
      val info = SubjectPublicKeyInfo.getInstance(seq)
      new BcX509ExtensionUtils().createSubjectKeyIdentifier(info)
    } finally {
      if (inputStream != null) {
        inputStream.close()
      }
    }
  }

}
