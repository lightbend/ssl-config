SSL Config
==========

<a href="https://travis-ci.org/lightbend/ssl-config"><img src="https://travis-ci.org/lightbend/ssl-config.svg"/></a>

Goal and purpose of this library is to make Play's WS library as well as Akka HTTP "secure by default".
Sadly, while Java's security has been steadily improving some settings are still left up to the user,
and certain algorithms which should never be used in a serious production system are still accepted by 
the default settings of the SSL/TLS infrastructure. These things are possible to fix, by providing specialized 
implementations and/or defining additional settings for the Java runtime to use – this is exactly the purpose of SSL Config.

Additional modules offer integration with Play WS (which by default utilises the Ning Async Http Client), 
Akka Http and any other library which may need support from this library.

Versions
========

The project is maintained on two branches:

- [`master`](https://github.com/lightbend/ssl-config/tree/master) which requires Java 8 and is used by Akka `2.4.x`.
- [`release-0.1`](https://github.com/lightbend/ssl-config/tree/release-0.1) which is Java 6 compatible 
  (does lots of manual improvements and checks that JDK6 didn't).
  Currently only the *legacy version* of Akka Streams & Http (which is `2.0.x`) uses this version. 

Latest versions:

```scala
// JDK8: 
"com.typesafe" %% "ssl-config-akka" % "0.2.4"
```

State of this project
=====================

ssl-config at this point in time is used primarily internally in Akka HTTP, and is being evolved
towards being "the place" one configures all possible SSL/TLS related settings, mostly focused on 
the client side of things. 

The project is hosted externally of either Akka or Play, in order to foster convergence and re-use
of the more tricky bits of configuring TLS.

Binary compatibility is **not guaranteed** between versions (in the `0.x.z` series) of ssl-config at this point in time.
We aim to stabilise the APIs and provide a stable release eventually. 

Documentation
=============

Docs are available on: https://lightbend.github.io/ssl-config

Recommended reading
===================

An excellent series by [Will Sargent](https://github.com/wsargent) about making
[Play's WS](https://www.playframework.com/documentation/2.4.x/ScalaWS) (from which this library originates) "secure by default":

- [Fixing the Most Dangerous Code in the World](https://tersesystems.com/blog/2014/01/13/fixing-the-most-dangerous-code-in-the-world/)
- [Fixing X.509 Certificates](https://tersesystems.com/blog/2014/03/20/fixing-x509-certificates/)
- [Fixing Certificate Revocation](https://tersesystems.com/blog/2014/03/22/fixing-certificate-revocation/)
- [Fixing Hostname Verification](https://tersesystems.com/blog/2014/03/23/fixing-hostname-verification/) 
- [Testing Hostname Verification](https://tersesystems.com/blog/2014/03/31/testing-hostname-verification)

Releasing
=========

Run `release.sh` script.

License
=======

Lightbend 2015-2018, Apache 2.0
