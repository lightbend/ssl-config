/*
 * Copyright (C) 2015 - 2023 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.util

/**
 * Simple logger interface in order to keep the core of this library zero-dependencies.
 */
abstract class NoDepsLogger {

  def isDebugEnabled: Boolean

  def debug(msg: String): Unit

  def info(msg: String): Unit

  def warn(msg: String): Unit

  def error(msg: String): Unit
  def error(msg: String, throwable: Throwable): Unit
}

object NoopLogger {
  private val _noop = new NoopLogger

  def factory(): LoggerFactory = new LoggerFactory {
    override def apply(clazz: Class[_]) = _noop
    override def apply(name: String) = _noop
  }
}
final class NoopLogger extends NoDepsLogger {

  override def debug(msg: String): Unit = ()

  override def info(msg: String): Unit = ()

  override def warn(msg: String): Unit = ()

  override def error(msg: String): Unit = ()
  override def error(msg: String, throwable: Throwable): Unit = ()

  override def isDebugEnabled: Boolean = false
}
object PrintlnLogger {
  def factory(): LoggerFactory = new LoggerFactory {
    override def apply(clazz: Class[_]) = new PrintlnLogger(clazz.getName)
    override def apply(name: String) = new PrintlnLogger(name)
  }
}
final class PrintlnLogger(name: String) extends NoDepsLogger {

  override def debug(msg: String): Unit = println(s"[DEBUG][$name] $msg")

  override def info(msg: String): Unit = println(s"[INFO][$name] $msg")

  override def warn(msg: String): Unit = println(s"[WARN][$name] $msg")

  override def error(msg: String): Unit = println(s"[ERROR][$name] $msg")
  override def error(msg: String, throwable: Throwable): Unit = println(s"[ERROR][$name] $msg")

  override def isDebugEnabled: Boolean = true
}