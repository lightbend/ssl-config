# Configuring Certificate Revocation

Certificate Revocation in JSSE can be done through two means:
certificate revocation lists (CRLs) and OCSP.

Certificate Revocation can be very useful in situations where a server's
private keys are compromised, as in the case of
[Heartbleed](http://heartbleed.com).

Certificate Revocation is disabled by default in JSSE. It is defined in
two places:

 * [PKI Programmer's Guide, Appendix
C](https://docs.oracle.com/javase/8/docs/technotes/guides/security/certpath/CertPathProgGuide.html#AppC)
 * [Enable OCSP
Checking](https://blogs.oracle.com/xuelei/entry/enable_ocsp_checking)

To enable OCSP, you must set the following system properties on the
command line:

```
java -Dcom.sun.security.enableCRLDP=true -Dcom.sun.net.ssl.checkRevocation=true
```

After doing the above, you can enable certificate revocation in the
client:

```conf
ssl-config.checkRevocation = true
```

Setting `checkRevocation` will set the internal `ocsp.enable`
security property automatically:

```scala
java.security.Security.setProperty("ocsp.enable", "true")
```

And this will set OCSP checking when making HTTPS requests.

@@@ note

Enabling OCSP requires a round trip to the OCSP responder.
This adds a notable overhead on HTTPS calls, and can make calls up
to [33%
slower](https://blog.cloudflare.com/ocsp-stapling-how-cloudflare-just-made-ssl-30).
The mitigation technique, OCSP stapling, is not supported in JSSE.

@@@

Or, if you wish to use a static CRL list, you can define a list of URLs:

```conf
ssl-config.revocationLists = [ "http://example.com/crl" ]
```

## Further Reading

 * [Fixing Certificate
Revocation](https://tersesystems.com/2014/03/22/fixing-certificate-revocation/)
