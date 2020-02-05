/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl.tracing

import java.security.Principal
import java.security.cert.X509Certificate

import com.typesafe.sslconfig.ssl
import com.typesafe.sslconfig.util.{ LoggerFactory, NoDepsLogger }

import scala.util.control.NonFatal

private[sslconfig] trait TraceLogger {

  def isLogEnabled(methodName: String, parameters: Map[String, Any]): Boolean

  def tracer[T, E <: AnyRef](methodName: String, parameters: Map[String, Any], function: () => T)(implicit loggerFactory: LoggerFactory): T = {
    val logger = loggerFactory(getClass)
    val methodParams = parameters.mapValues(mapValue).mkString(",")
    val enabled = isLogEnabled(methodName, parameters)
    if (enabled) {
      entry(logger, methodName, methodParams)
    }
    try {
      val result = function()
      if (enabled) {
        exit(logger, result, methodName, methodParams)
      }
      result
    } catch {
      case NonFatal(e) =>
        if (enabled) {
          exception(logger, e, methodName, methodParams)
        }
        throw e
    }
  }

  private def mapValue(value: Any): String = {
    value match {
      case v: Array[X509Certificate] =>
        s"Array(${ssl.debugChain(v).mkString(", ")})"
      case v: Array[Principal] =>
        s"Array(${v.mkString(", ")})"
      case v: Array[_] =>
        s"Array(${v.mkString(", ")})"
      case v: Any =>
        v.toString
      case null =>
        null
    }
  }

  private def entry(logger: NoDepsLogger, methodName: String, methodParams: String): Unit = {
    logger.warn(s"entry: $methodName($methodParams)")
  }

  private def exit[R](logger: NoDepsLogger, result: R, methodName: String, methodParams: String): R = {
    logger.warn(s"exit: $methodName($methodParams) = ${mapValue(result)}")
    result
  }

  private def exit(logger: NoDepsLogger, methodName: String, methodParams: String): Unit = {
    logger.warn(s"exit: $methodName($methodParams)")
  }

  private def exception(logger: NoDepsLogger, e: Throwable, methodName: String, methodParams: String): Unit = {
    logger.error(s"exception: $methodName($methodParams)", e)
  }
}
