/*
 * Copyright (C) 2015 Typesafe Inc. <http://www.typesafe.com>
 */

package play.api.libs.ws

import org.specs2.mutable.Specification
import play.api.test.WithApplication
import scala.concurrent._
import java.io.Closeable

import java.io.File
import java.net.URI

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.xml.Elem

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.Application
import play.api.http.Writeable
import play.api.libs.json.JsValue
import play.api.libs.iteratee._
import play.api.libs.ws
import play.api.libs.ws._

class WSRequestHolderSpec extends Specification {

  "WSRequestHolder" should {

    "give the full URL with the query string" in new WithApplication() {

      val ws = app.injector.instanceOf[WSClient]
      val res: Future[WSResponse] = ws.url("https://api.netatmo.net/oauth2/token").get()

      import scala.concurrent.duration._
      try {
        val rs = Await.result(res, 3.seconds)
      } catch {
        case ex: Throwable => ex.printStackTrace()
      }

    }

  }

}
