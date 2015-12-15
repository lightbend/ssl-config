/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package com.typesafe.sslconfig.util

import java.util.concurrent.TimeUnit

import com.typesafe.config._

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

class PlayConfig(val underlying: Config) {

  /**
   * Get the config at the given path.
   */
  def get[A](path: String)(implicit loader: ConfigLoader[A]): A = {
    loader.load(underlying, path)
  }

  /**
   * Get an optional configuration item.
   *
   * If the value of the item is null, this will return None, otherwise returns Some.
   *
   * @throws com.typesafe.config.ConfigException.Missing if the value is undefined (as opposed to null) this will still
   *         throw an exception.
   */
  def getOptional[A: ConfigLoader](path: String): Option[A] = {
    try {
      Option(get(path))
    } catch {
      case e: ConfigException.Missing => None
    }
  }

  /**
   * Get a prototyped sequence of objects.
   *
   * Each object in the sequence will fallback to the object loaded from prototype.$path.
   */
  def getPrototypedSeq(path: String, prototypePath: String = "prototype.$path"): Seq[PlayConfig] = {
    val prototype = underlying.getConfig(prototypePath.replace("$path", path))
    get[Seq[Config]](path).map { config =>
      new PlayConfig(config.withFallback(prototype))
    }
  }

  /**
   * Get a prototyped map of objects.
   *
   * Each value in the map will fallback to the object loaded from prototype.$path.
   */
  def getPrototypedMap(path: String, prototypePath: String = "prototype.$path"): Map[String, PlayConfig] = {
    val prototype = if (prototypePath.isEmpty) {
      underlying
    } else {
      underlying.getConfig(prototypePath.replace("$path", path))
    }
    get[Map[String, Config]](path).map {
      case (key, config) => key -> new PlayConfig(config.withFallback(prototype))
    }.toMap
  }

  /**
   * Get an optional deprecated configuration item.
   *
   * If the deprecated configuration item is defined, it will be returned, and a warning will be logged.
   *
   * Otherwise, the configuration from path will be looked up.
   *
   * If the value of the item is null, this will return None, otherwise returns Some.
   */
  def getOptionalDeprecated[A: ConfigLoader](path: String, deprecated: String): Option[A] = {
    if (underlying.hasPath(deprecated)) {
      reportDeprecation(path, deprecated)
      getOptional[A](deprecated)
    } else {
      getOptional[A](path)
    }
  }

  /**
   * Get a deprecated configuration item.
   *
   * If the deprecated configuration item is defined, it will be returned, and a warning will be logged.
   *
   * Otherwise, the configuration from path will be looked up.
   */
  def getDeprecated[A: ConfigLoader](path: String, deprecated: String): A = {
    if (underlying.hasPath(deprecated)) {
      reportDeprecation(path, deprecated)
      get[A](deprecated)
    } else {
      get[A](path)
    }
  }

  /**
   * Get a deprecated configuration.
   *
   * If the deprecated configuration is defined, it will be returned, falling back to the new configuration, and a
   * warning will be logged.
   *
   * Otherwise, the configuration from path will be looked up and used as is.
   */
  def getDeprecatedWithFallback(path: String, deprecated: String, parent: String = ""): PlayConfig = {
    val config = get[Config](path)
    val merged = if (underlying.hasPath(deprecated)) {
      reportDeprecation(path, deprecated)
      get[Config](deprecated).withFallback(config)
    } else config
    new PlayConfig(merged)
  }

  /**
   * Creates a configuration error for a specific configuration key.
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * throw configuration.reportError("engine.connectionUrl", "Cannot connect!")
   * }}}
   *
   * @param path the configuration key, related to this error
   * @param message the error message
   * @param e the related exception
   * @return a configuration exception
   */
  def reportError(path: String, message: String, e: Option[Throwable] = None) = {
    //Configuration.configError(if (underlying.hasPath(path)) underlying.getValue(path).origin else underlying.root.origin, message, e)
    e.get
  }

  /**
   * Get the immediate subkeys of this configuration.
   */
  def subKeys: Set[String] = underlying.root().keySet().asScala.toSet

  def reportDeprecation(path: String, deprecated: String): Unit = {
    val origin = underlying.getValue(deprecated).origin
    //Logger.warn(s"${origin.description}: $deprecated is deprecated, use $path instead")
  }
}

object PlayConfig {
  def apply(underlying: Config) = new PlayConfig(underlying)
  //def apply(configuration: Configuration) = new PlayConfig(configuration.underlying)
}

/**
 * A config loader
 */
trait ConfigLoader[A] { self =>
  def load(config: Config, path: String): A
  def map[B](f: A => B): ConfigLoader[B] = new ConfigLoader[B] {
    def load(config: Config, path: String): B = {
      f(self.load(config, path))
    }
  }
}

object ConfigLoader {

  def apply[A](f: Config => String => A): ConfigLoader[A] = new ConfigLoader[A] {
    def load(config: Config, path: String): A = f(config)(path)
  }

  import scala.collection.JavaConverters._

  private def toScala[A](as: java.util.List[A]): Seq[A] = as.asScala

  implicit val stringLoader = ConfigLoader(_.getString)
  implicit val seqStringLoader = ConfigLoader(_.getStringList).map(toScala)

  implicit val intLoader = ConfigLoader(_.getInt)
  implicit val seqIntLoader = ConfigLoader(_.getIntList).map(toScala(_).map(_.toInt))

  implicit val booleanLoader = ConfigLoader(_.getBoolean)
  implicit val seqBooleanLoader = ConfigLoader(_.getBooleanList).map(toScala(_).map(_.booleanValue()))

  implicit val finiteDurationLoader: ConfigLoader[FiniteDuration] = ConfigLoader(config => config.getDuration(_, TimeUnit.MILLISECONDS))
    .map(millis => FiniteDuration(millis, TimeUnit.MILLISECONDS))
  implicit val seqFiniteDurationLoader: ConfigLoader[Seq[FiniteDuration]] = ConfigLoader(config => config.getDurationList(_, TimeUnit.MILLISECONDS))
    .map(toScala(_).map(millis => FiniteDuration(millis, TimeUnit.MILLISECONDS)))

  implicit val doubleLoader = ConfigLoader(_.getDouble)
  implicit val seqDoubleLoader = ConfigLoader(_.getDoubleList).map(toScala)

  implicit val longLoader = ConfigLoader(_.getLong)
  implicit val seqLongLoader = ConfigLoader(_.getLongList).map(toScala)

  implicit val configLoader: ConfigLoader[Config] = ConfigLoader(_.getConfig)
  implicit val seqConfigLoader: ConfigLoader[Seq[Config]] = ConfigLoader(_.getConfigList).map(_.asScala)

  implicit val playConfigLoader = configLoader.map(new PlayConfig(_))
  implicit val seqPlayConfigLoader = seqConfigLoader.map(_.map(new PlayConfig(_)))

  implicit def mapLoader[A](implicit valueLoader: ConfigLoader[A]): ConfigLoader[Map[String, A]] = new ConfigLoader[Map[String, A]] {
    def load(config: Config, path: String): Map[String, A] = {
      val obj = config.getObject(path)
      val conf = obj.toConfig
      obj.keySet().asScala.map { key =>
        key -> valueLoader.load(conf, key)
      }.toMap
    }
  }
}
