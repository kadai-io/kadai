/*
 * Copyright [2024] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package io.kadai.common.rest.ldap;

import java.util.Arrays;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class LdapClientLogger {

  private final Logger delegateLogger;

  public LdapClientLogger(Logger delegateLogger) {
    this.delegateLogger = delegateLogger;
  }

  public String getName() {
    return delegateLogger.getName();
  }

  public boolean isTraceEnabled() {
    return delegateLogger.isTraceEnabled();
  }

  public boolean isTraceEnabled(Marker marker) {
    return delegateLogger.isTraceEnabled(marker);
  }

  public void trace(String msg) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(msg);
    }
  }

  public void trace(String format, Supplier<Object> arg) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(format, arg.get());
    }
  }

  public void trace(String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(format, arg1.get(), arg2.get());
    }
  }

  @SafeVarargs
  public final void trace(String format, Supplier<Object>... arguments) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void trace(String msg, Throwable t) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(msg, t);
    }
  }

  public void trace(Marker marker, String msg) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, msg);
    }
  }

  public void trace(Marker marker, String format, Supplier<Object> arg) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, format, arg.get());
    }
  }

  public void trace(Marker marker, String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, format, arg1.get(), arg2.get());
    }
  }

  @SafeVarargs
  public final void trace(Marker marker, String format, Supplier<Object>... arguments) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void trace(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, msg, t);
    }
  }

  public boolean isDebugEnabled() {
    return delegateLogger.isDebugEnabled();
  }

  public boolean isDebugEnabled(Marker marker) {
    return delegateLogger.isDebugEnabled(marker);
  }

  public void debug(String msg) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(msg);
    }
  }

  public void debug(String format, Supplier<Object> arg) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(format, arg.get());
    }
  }

  public void debug(String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(format, arg1.get(), arg2.get());
    }
  }

  @SafeVarargs
  public final void debug(String format, Supplier<Object>... arguments) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void debug(String msg, Throwable t) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(msg, t);
    }
  }

  public void debug(Marker marker, String msg) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, msg);
    }
  }

  public void debug(Marker marker, String format, Supplier<Object> arg) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, format, arg.get());
    }
  }

  public void debug(Marker marker, String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, format, arg1.get(), arg2.get());
    }
  }

  @SafeVarargs
  public final void debug(Marker marker, String format, Supplier<Object>... arguments) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void debug(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, msg, t);
    }
  }

  public boolean isInfoEnabled() {
    return delegateLogger.isInfoEnabled();
  }

  public boolean isInfoEnabled(Marker marker) {
    return delegateLogger.isInfoEnabled(marker);
  }

  public void info(String msg) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(msg);
    }
  }

  public void info(String format, Supplier<Object> arg) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(format, arg.get());
    }
  }

  public void info(String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(format, arg1.get(), arg2.get());
    }
  }

  @SafeVarargs
  public final void info(String format, Supplier<Object>... arguments) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void info(String msg, Throwable t) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(msg, t);
    }
  }

  public void info(Marker marker, String msg) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, msg);
    }
  }

  public void info(Marker marker, String format, Supplier<Object> arg) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, format, arg.get());
    }
  }

  public void info(Marker marker, String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, format, arg1.get(), arg2.get());
    }
  }

  @SafeVarargs
  public final void info(Marker marker, String format, Supplier<Object>... arguments) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void info(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, msg, t);
    }
  }

  public boolean isWarnEnabled() {
    return delegateLogger.isWarnEnabled();
  }

  public boolean isWarnEnabled(Marker marker) {
    return delegateLogger.isWarnEnabled(marker);
  }

  public void warn(String msg) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(msg);
    }
  }

  public void warn(String format, Supplier<Object> arg) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(format, arg.get());
    }
  }

  @SafeVarargs
  public final void warn(String format, Supplier<Object>... arguments) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void warn(String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(format, arg1.get(), arg2.get());
    }
  }

  public void warn(String msg, Throwable t) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(msg, t);
    }
  }

  public void warn(Marker marker, String msg) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, msg);
    }
  }

  public void warn(Marker marker, String format, Supplier<Object> arg) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, format, arg.get());
    }
  }

  public void warn(Marker marker, String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, format, arg1.get(), arg2.get());
    }
  }

  @SafeVarargs
  public final void warn(Marker marker, String format, Supplier<Object>... arguments) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void warn(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, msg, t);
    }
  }

  public boolean isErrorEnabled() {
    return delegateLogger.isErrorEnabled();
  }

  public boolean isErrorEnabled(Marker marker) {
    return delegateLogger.isErrorEnabled(marker);
  }

  public void error(String msg) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(msg);
    }
  }

  public void error(String format, Supplier<Object> arg) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(format, arg.get());
    }
  }

  public void error(String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(format, arg1.get(), arg2.get());
    }
  }

  @SafeVarargs
  public final void error(String format, Supplier<Object>... arguments) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void error(String msg, Throwable t) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(msg, t);
    }
  }

  public void error(Marker marker, String msg) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, msg);
    }
  }

  public void error(Marker marker, String format, Supplier<Object> arg) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, format, arg.get());
    }
  }

  public void error(Marker marker, String format, Supplier<Object> arg1, Supplier<Object> arg2) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, format, arg1.get(), arg2.get());
    }
  }

  @SafeVarargs
  public final void error(Marker marker, String format, Supplier<Object>... arguments) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }
  }

  public void error(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, msg, t);
    }
  }
}
