/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.debug

import java.lang.reflect.Field
import java.security.PrivilegedExceptionAction
import com.typesafe.sslconfig.ssl.MonkeyPatcher

@deprecated("FixLoggingAction has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
abstract class FixLoggingAction extends PrivilegedExceptionAction[Unit] with MonkeyPatcher with ClassFinder {
  def newOptions: String

  def isValidField(field: Field, definedType: Class[_]): Boolean = {
    logger.warn("DebugConfiguration has been deprecated and does nothing.  Please use the javax.net.debug system property.")
    false
  }
}