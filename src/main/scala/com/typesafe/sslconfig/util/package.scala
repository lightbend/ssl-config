/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config
//
//package object util {
//
//  implicit final class RichConfig(val c: Config) extends AnyVal {
//    import scala.concurrent.duration._
//    import scala.collection.JavaConverters._
//
//    def getFiniteDuration(path: String): FiniteDuration =
//      c.getDuration(path, TimeUnit.MILLISECONDS).millis
//
//    def getStringSeq(path: String): Seq[String] =
//      c.getStringList(path).asScala
//
//    def getOption[T](path: String): Option[T] = // could be more fancy via type classes, not spending time on it though
//      if (c.hasPath(path)) Some(implicitly[Get[T]].get(path, c)) else None
//
//    def getPrototypedSeq(path: String, prototypePath: String = "prototype.$path"): Seq[Config] = {
//      val prototype = c.getConfig(prototypePath.replace("$path", path))
//      get[Seq[Config]](path).map { config =>
//        config.withFallback(prototype)
//      }
//    }
//
//    def get[T](path: String): T =
//      implicitly[Get[T]].get(path, c)
//
//  }
//
//  sealed trait Get[T] { def get(path: String, c: Config): T }
//  implicit object GetString extends Get[String] { def get(path: String, c: Config) = c.getString(path) }
//  implicit object GetBoolean extends Get[Boolean] { def get(path: String, c: Config) = c.getBoolean(path) }
//
//}