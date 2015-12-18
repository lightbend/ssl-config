/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.util

import com.typesafe.config.{ Config, ConfigFactory }

/**
 * Simple logger interface in order to keep the core of this library zero-dependencies.
 */
abstract class NoDepsLogger {

  def isDebugEnabled: Boolean
  def trace(msg: String): Unit
  def debug(msg: String): Unit
  def debug(msg: String, throwable: Throwable): Unit

  def info(msg: String): Unit
  def info(msg: String, throwable: Throwable): Unit

  def warn(msg: String): Unit
  def warn(msg: String, throwable: Throwable): Unit

  def error(msg: String): Unit
  def error(msg: String, throwable: Throwable): Unit
}

object NoDepsLogger {
  def get(clazz: Class[_]): NoDepsLogger = {
    val conf = sslConfig
    val clazzName = conf.getString("logger")
    Class.forName(clazzName)
      .getConstructor(classOf[Class[_]])
      .newInstance(clazz)
      .asInstanceOf[NoDepsLogger] // very simple for now
  }

  def get(name: String): NoDepsLogger = {
    val conf = sslConfig
    val clazzName = conf.getString("logger")
    Class.forName(clazzName)
      .getConstructor(classOf[String])
      .newInstance(name)
      .asInstanceOf[NoDepsLogger] // very simple for now
  }

  private def sslConfig: Config =
    ConfigFactory.load().getConfig("ssl-config")
}

final class NoopLogger(clazz: Class[_]) extends NoDepsLogger {
  def this(s: String) = this(classOf[String]) // mock anyway

  override def trace(msg: String): Unit = ()

  override def debug(msg: String): Unit = ()
  override def debug(msg: String, throwable: Throwable): Unit = ()

  override def info(msg: String): Unit = ()
  override def info(msg: String, throwable: Throwable): Unit = ()

  override def warn(msg: String, throwable: Throwable): Unit = ()
  override def warn(msg: String): Unit = ()

  override def error(msg: String): Unit = ()
  override def error(msg: String, throwable: Throwable): Unit = ()

  override def isDebugEnabled: Boolean = false
}