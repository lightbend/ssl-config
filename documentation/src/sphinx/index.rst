SSL Config
==========

.. note:: ssl-config was originally part of Play's WS module.

`Play WS <https://www.playframework.com/documentation/2.4.x/ScalaWS>`__ allows you to set up HTTPS completely from a
configuration file, without the need to write code. It does this by
layering the Java Secure Socket Extension (JSSE) with a configuration
layer and with reasonable defaults.

JDK 1.8 contains an implementation of JSSE which is `significantly more
advanced <https://docs.oracle.com/javase/8/docs/technotes/guides/security/enhancements-8.html>`__
than previous versions, and should be used if security is a priority.

Copyright (C) 2009-2016 Lightbend Inc. <https://www.lightbend.com>

.. toctree::
   :maxdepth: 2

   WSQuickStart.rst
   CertificateGeneration.rst
   KeyStores.rst
   Protocols.rst
   CipherSuites.rst
   CertificateRevocation.rst
   HostnameVerification.rst
   CertificateValidation.rst
   ExampleSSLConfig.rst
   DefaultContext.rst
   DebuggingSSL.rst
   LooseSSL.rst
   TestingSSL.rst

Further Reading
---------------

JSSE is a complex product. For convenience, the JSSE materials are
provided here:

JDK 1.8:

-  `JSSE Reference
  Guide <https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html>`__
-  `JSSE Crypto
  Spec <https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#SSLTLS>`__
-  `SunJSSE
  Providers <https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#SunJSSEProvider>`__
-  `PKI Programmer's
  Guide <https://docs.oracle.com/javase/8/docs/technotes/guides/security/certpath/CertPathProgGuide.html>`__
-  `keytool <https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html>`__
