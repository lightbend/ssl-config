# SSL Config

@@@ note

ssl-config was originally part of Play's WS module.

@@@

[Play WS](https://www.playframework.com/documentation/2.4.x/ScalaWS) allows you to set up HTTPS completely from a
configuration file, without the need to write code. It does this by
layering the Java Secure Socket Extension (JSSE) with a configuration
layer and with reasonable defaults.

JDK 1.8 contains an implementation of JSSE which is [significantly more
advanced](https://docs.oracle.com/javase/8/docs/technotes/guides/security/enhancements-8.html)
than previous versions, and should be used if security is a priority.

Copyright (C) 2009-2020 Lightbend Inc. <[https://www.lightbend.com](https://www.lightbend.com)>

@@toc { depth=2 }

@@@ index

* [WSQuickStart](WSQuickStart.md)
* [CertificateGeneration](CertificateGeneration.md)
* [KeyStores](KeyStores.md)
* [Protocols](Protocols.md)
* [CertificateRevocation](CertificateRevocation.md)
* [HostnameVerification](HostnameVerification.md)
* [ExampleSSLConfig](ExampleSSLConfig.md)
* [DefaultContext](DefaultContext.md)
* [DebuggingSSL](DebuggingSSL.md)
* [LooseSSL](LooseSSL.md)
* [TestingSSL](TestingSSL.md)

@@@

## Further Reading

JSSE is a complex product. For convenience, the JSSE materials for JDK
1.8 are provided here:

-  [JSSE Reference
  Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html)
-  [JSSE Crypto
  Spec](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#SSLTLS)
-  [SunJSSE
  Providers](https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#SunJSSEProvider)
-  [PKI Programmer's
  Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/security/certpath/CertPathProgGuide.html)
-  [keytool](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)
