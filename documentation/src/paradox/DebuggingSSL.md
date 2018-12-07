# Debugging SSL Connections

In the event that an HTTPS connection does not go through, debugging
JSSE can be a hassle.

@@@ note

Prior to 0.3.8, the debug system relied on undocumented modification of internal JSSE debug settings that were normally set using
`javax.net.debug` and `java.security.debug` system properties on startup.  

This system has been removed, and the debug flags that do not have a direct correlation in the new system are deprecated.
@@@

WS SSL provides configuration options that will turn trace logging at a **warn** level for SSLContext, SSLEngine, TrustManager and KeyManager.

To configure, set the `ssl-config.debug` property in
`application.conf`:

```conf
ssl-config.debug = {
  # Enable all debugging
  all = false

  # Enable sslengine / socket tracing
  ssl = false

  # Enable SSLContext tracing
  sslctx = false

  # Enable key manager tracing
  keymanager = false

  # Enable trust manager tracing
  trustmanager = false
}
```

You can also set `javax.net.debug` and `java.security.debug` system properties directly at startup, using a `.jvmopts` file:

```bash
# Don't allow client to dictate terms - this can also be used for DoS attacks.
# Undocumented, defined in sun.security.ssl.Handshaker.java:205
-Djdk.tls.rejectClientInitiatedRenegotiation=true

# Add more details to the disabled algorithms list
# http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#DisabledAlgorithms
# and http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7133344
-Djava.security.properties=disabledAlgorithms.properties

# Enable this if you need to use OCSP or CRL
# http://docs.oracle.com/javase/8/docs/technotes/guides/security/certpath/CertPathProgGuide.html#AppC
#-Dcom.sun.security.enableCRLDP=true
#-Dcom.sun.net.ssl.checkRevocation=true

-Djavax.net.debug=ssl:handshake
-Djava.security.debug=certpath:x509:ocsp
```

Oracle has a number of sections on debugging JSSE issues:

 * [Debugging SSL/TLS
connections](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/ReadDebug.html)
* [Debugging
Utilities](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#Debug)
* [Troubleshooting
Security](https://docs.oracle.com/javase/8/docs/technotes/guides/security/troubleshooting-security.html)
 * [JSSE Debug Logging With
Timestamp](https://blogs.oracle.com/xuelei/entry/jsse_debug_logging_with_timestamp)
 * [How to Analyze Java SSL
Errors](http://www.smartjava.org/content/how-analyze-java-ssl-errors)
