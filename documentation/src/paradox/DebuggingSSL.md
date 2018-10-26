# Debugging SSL Connections

In the event that an HTTPS connection does not go through, debugging
JSSE can be a hassle.

@@@ note

Setting the `ssl-config.debug` property in
`application.conf` is **no longer supported**.

@@@

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
