/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.akka.util

import com.typesafe.sslconfig.util.NoDepsLogger

class AkkaLoggerBridge(clazz: Class[_]) extends NoDepsLogger {
  // TODO this init scheme won't work, needs ActorSystem here :~

  override def isDebugEnabled: Boolean = true

  override def warn(msg: String): Unit = println("WARN: " + msg)
  override def warn(msg: String, throwable: Throwable): Unit = println("WARN: " + msg)

  override def error(msg: String): Unit = println("ERROR: " + msg)
  override def error(msg: String, throwable: Throwable): Unit = println("ERROR: " + msg)

  override def info(msg: String): Unit = println("TRACE: " + msg)
  override def info(msg: String, throwable: Throwable): Unit = println("INFO: " + msg)

  override def debug(msg: String): Unit = println("DEBUG: " + msg)
  override def debug(msg: String, throwable: Throwable): Unit = println("DEBUG: " + msg)

  override def trace(msg: String): Unit = println("TRACE: " + msg)
}

