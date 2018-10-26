# Using the Default SSLContext

If you don't want to use the SSLContext that WS provides for you, and
want to use `SSLContext.getDefault`, please set:

```conf
ssl-config.default = true
```

If you are using the default SSLContext, then the only way to change
JSSE behavior is through manipulating the [JSSE system
properties](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#Customization).
