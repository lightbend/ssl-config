/*
 * Copyright (C) 2015 - 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.debug

import java.security.AccessController

import com.typesafe.sslconfig.util.LoggerFactory

import scala.util.control.NonFatal

/**
 * This fixes logging for the SSL Debug class.
 */
class FixInternalDebugLogging(mkLogger: LoggerFactory) {

  private val logger = mkLogger("com.typesafe.sslconfig.ssl.debug.FixInternalDebugLogging")

  class MonkeyPatchInternalSslDebugAction(val newOptions: String) extends FixLoggingAction {

    override val logger = mkLogger("com.typesafe.sslconfig.ssl.debug.FixInternalDebugLogging.MonkeyPatchInternalSslDebugAction")

    val initialResource = "/sun/security/ssl/Debug.class"

    val debugClassName = "sun.security.ssl.Debug"

    /**
     * Returns true if this class has an instance of the class returned by debugClassName, false otherwise.
     *
     * @param className the name of the class.
     * @return true if this class should be returned in the set of findClasses, false otherwise.
     */
    def isValidClass(className: String): Boolean = {
      if (className.startsWith("com.sun.net.ssl.internal.ssl")) return true
      if (className.startsWith("sun.security.ssl")) return true
      false
    }

    /**
     * Returns true if newOptions is not null and newOptions is not empty.  If false, then debug values
     *
     * @return
     */
    def isUsingDebug: Boolean = (newOptions != null) && (!newOptions.isEmpty)

    def run(): Unit = {
      System.setProperty("javax.net.debug", newOptions)

      val debugType: Class[_] = Thread.currentThread().getContextClassLoader.loadClass(debugClassName)

      val newDebug: AnyRef = debugType.newInstance().asInstanceOf[AnyRef]
      logger.debug(s"run: debugType = $debugType")
      val debugValue = if (isUsingDebug) newDebug else null

      var isPatched = false
      for {
        debugClass <- findClasses
        debugField <- debugClass.getDeclaredFields
      } {
        if (isValidField(debugField, debugType)) {
          logger.debug(s"run: patching $debugClass with $debugValue")
          monkeyPatchField(debugField, debugValue)
          isPatched = true
        }
      }

      // Add an assertion here in case the class location changes, so the tests fail...
      if (!isPatched) {
        throw new IllegalStateException("No debug classes found!")
      }

      // Switch out the args (for certpath loggers that AREN'T static and final)
      // This will result in those classes using the base Debug class which will write to System.out, but
      // I don't know how to switch out the Debug.getInstance method itself without using a java agent.
      val argsField = debugType.getDeclaredField("args")
      monkeyPatchField(argsField, newOptions)
    }
  }

  def apply(newOptions: String): Unit = {
    logger.debug(s"apply: newOptions = ${newOptions}")

    try {
      val action = new MonkeyPatchInternalSslDebugAction(newOptions)
      AccessController.doPrivileged(action)
    } catch {
      case NonFatal(e) =>
        throw new IllegalStateException("InternalDebug configuration error", e)
    }
  }
}
