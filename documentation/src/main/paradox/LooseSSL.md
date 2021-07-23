# Loose Options

## We understand

Setting up SSL is not all that fun. It is not lost on anyone that
setting up even a single web service with HTTPS involves setting up
several certificates, reading boring cryptography documentation and dire
warnings.

Despite that, all the security features in SSL are there for good
reasons, and turning them off, even for development purposes, has led to
even less fun than setting up SSL properly.

## Please read this before turning anything off!

### Man in the Middle attacks are well known

The information security community is very well aware of how insecure
most internal networks are, and uses that to their advantage. A video
discussing attacks detailed a [wide range of possible
attacks](http://2012.video.sector.ca/page/6).

### Man in the Middle attacks are common

The average company can expect to have seven or eight [Man in the
Middle](https://sites.google.com/site/cse825maninthemiddle/) attacks
a year. In some cases, up to 300,000 users can be compromised [over
several
months](https://security.stackexchange.com/questions/12041/are-man-in-the-middle-attacks-extremely-rare).

### Attackers have a suite of tools that automatically exploit flaws

The days of the expert hacker are over. Most security professionals use
automated linux environments such as Kali Linux to do penetration
testing, packed with hundreds of tools to check for exploits. A video of
[Cain & Abel](https://www.youtube.com/watch?v=pfHsRscy540) shows
passwords being compromised in less than 20 seconds.

Hackers won't bother to see whether something will "look encrypted" or
not. Instead, they'll set up a machine with a toolkit that will run
through every possible exploit, and go out for coffee.

### Security is increasingly important and public

More and more information flows through computers every day. The public
and the media are taking increasing notice of the possibility that their
private communications can be intercepted. Google, Facebook, Yahoo, and
other leading companies have made secure communication a priority and
have devoted millions to ensuring that [data cannot be
read](https://www.eff.org/deeplinks/2013/11/encrypt-web-report-whos-doing-what).

### Ethernet / Password protected WiFi does not provide a meaningful level of security.

A networking auditing tool such as a [Wifi
Pineapple](https://wifipineapple.com/) costs around $100, picks up
all traffic sent over a wifi network, and is so good at intercepting
traffic that people have turned it on and started [intercepting traffic
accidentally](http://www.troyhunt.com/2013/04/the-beginners-guide-to-breaking-website.html).

### Companies have been sued for inadequate security

PCI compliance is not the only thing that companies have to worry about.
The FTC sued [Fandango and Credit
Karma](https://www.ftc.gov/news-events/press-releases/2014/03/fandango-credit-karma-settle-ftc-charges-they-deceived-consumers)
on charges that they failed to securely transmit information, including
credit card information.

### Correctly configured HTTPS clients are important

Sensitive, company confidential information goes over web services. A
paper discussing insecurities in WS clients was titled [The Most
Dangerous Code in the World: Validating SSL Certificates in Non-Browser
Software](https://www.cs.utexas.edu/~shmat/shmat_ccs12.pdf), and
lists poor default configuration and explicit disabling of security
options as the primary reason for exposure. The WS client has been
configured as much as possible to be secure by default, and there are
example configurations provided for your benefit.

## Mitigation

If you must turn on loose options, there are a couple of things you can
do to minimize your exposure.

**Custom Play WSClient**: You can create a [custom Play WSClient](https://www.playframework.com/documentation/2.4.x/ScalaWS)
specifically for the server, using the
[`WSConfigParser`](api/scala/play/api/libs/ws/WSConfigParser.html)
together with `ConfigFactory.parseString`, and ensure it is never used
outside that context.

**Environment Scoping**: You can define [environment variables in
HOCON](https://github.com/lightbend/config/blob/master/HOCON.md#substitution-fallback-to-environment-variables)
to ensure that any loose options are not hardcoded in configuration
files, and therefore cannot escape an development environment.

**Runtime / Deployment Checks**: You can add code to your deployment
scripts or program that checks that `ssl-config.loose` options are
not enabled in a production environment. The runtime mode can be found
in the [`Application.mode`](api/scala/play/api/Application.html)
method.

## Options

Finally, here are the options themselves.

### Disabling Certificate Verification

@@@ note

In most cases, people turn off certificate verification
because they haven't generated certificates. **There are other
options besides disabling certificate verification.**

 * @ref:[Quick Start to WS SSL](WSQuickStart.md) shows how to connect
directly to a server using a self signed certificate.
 * @ref:[Generating X.509 Certificates](CertificateGeneration.md) lists a
number of GUI applications that will generate certificates for
you.
 * @ref:[Example Configurations](ExampleSSLConfig.md) shows complete
configuration of TLS using self signed certificates.
 * If you want to view your application through HTTPS, you can use
[ngrok](https://ngrok.com/) to proxy your application.
 * If you need a certificate authority but don't want to pay money,
[StartSSL](https://www.startssl.com/?app=1) or
[CACert](http://www.cacert.org/) will give you a free
certificate.
 * If you want a self signed certificate and private key without
typing on the command line, you can use
[selfsignedcertificate.com](http://www.selfsignedcertificate.com/).

@@@

If you've read the above and you still want to completely disable
certificate verification, set the following;

```conf
ssl-config.loose.acceptAnyCertificate=true
```

With certificate verification completely disabled, you are vulnerable to
attack from anyone on the network using a tool such as
[mitmproxy](https://mitmproxy.org/).

@@@ note

By disabling certificate validation, you are also disabling
hostname verification!

@@@

### Disabling Hostname Verification

If you want to disable hostname verification, you can set a loose flag:

```conf
ssl-config.loose.disableHostnameVerification=true
```

With hostname verification disabled, a DNS proxy such as `dnschef` can
[easily intercept
communication](https://tersesystems.com/2014/03/31/testing-hostname-verification/).

@@@ note

By disabling hostname verification, you are also disabling
certificate verification!

@@@
