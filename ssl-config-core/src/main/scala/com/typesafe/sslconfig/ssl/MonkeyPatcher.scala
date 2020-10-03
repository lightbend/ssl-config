/*
 * Copyright (C) 2015 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package com.typesafe.sslconfig.ssl

import java.lang.reflect.Field

@deprecated("MonkeyPatcher has been deprecated and does nothing.  Please use the javax.net.debug system property.", "0.4.0")
trait MonkeyPatcher {
  def monkeyPatchField(field: Field, newObject: AnyRef): Unit = ()
}
