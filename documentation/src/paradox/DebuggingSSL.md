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

You can also set `javax.net.debug` and `java.security.debug` system properties directly.

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
