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

import org.slf4j.Logger;
import org.slf4j.Marker;

public class LdapClientLogger implements Logger {

  private final Logger delegateLogger;

  public LdapClientLogger(Logger delegateLogger) {
    this.delegateLogger = delegateLogger;
  }

  @Override
  public String getName() {
    return delegateLogger.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return delegateLogger.isTraceEnabled();
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return delegateLogger.isTraceEnabled(marker);
  }

  @Override
  public void trace(String msg) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(msg);
    }
  }

  @Override
  public void trace(String format, Object arg) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(format, arg);
    }
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(format, arg1, arg2);
    }
  }

  @Override
  public void trace(String format, Object... arguments) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(format, arguments);
    }
  }

  @Override
  public void trace(String msg, Throwable t) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(msg, t);
    }
  }

  @Override
  public void trace(Marker marker, String msg) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, msg);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, format, arg);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, format, arg1, arg2);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object... argArray) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, format, argArray);
    }
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isTraceEnabled()) {
      delegateLogger.trace(marker, msg, t);
    }
  }

  @Override
  public boolean isDebugEnabled() {
    return delegateLogger.isDebugEnabled();
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return delegateLogger.isDebugEnabled(marker);
  }

  @Override
  public void debug(String msg) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(msg);
    }
  }

  @Override
  public void debug(String format, Object arg) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(format, arg);
    }
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(format, arg1, arg2);
    }
  }

  @Override
  public void debug(String format, Object... arguments) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(format, arguments);
    }
  }

  @Override
  public void debug(String msg, Throwable t) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(msg, t);
    }
  }

  @Override
  public void debug(Marker marker, String msg) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, msg);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, format, arg);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, format, arg1, arg2);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, format, arguments);
    }
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isDebugEnabled()) {
      delegateLogger.debug(marker, msg, t);
    }
  }

  @Override
  public boolean isInfoEnabled() {
    return delegateLogger.isInfoEnabled();
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return delegateLogger.isInfoEnabled(marker);
  }

  @Override
  public void info(String msg) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(msg);
    }
  }

  @Override
  public void info(String format, Object arg) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(format, arg);
    }
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(format, arg1, arg2);
    }
  }

  @Override
  public void info(String format, Object... arguments) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(format, arguments);
    }
  }

  @Override
  public void info(String msg, Throwable t) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(msg, t);
    }
  }

  @Override
  public void info(Marker marker, String msg) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, msg);
    }
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, format, arg);
    }
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, format, arg1, arg2);
    }
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, format, arguments);
    }
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isInfoEnabled()) {
      delegateLogger.info(marker, msg, t);
    }
  }

  @Override
  public boolean isWarnEnabled() {
    return delegateLogger.isWarnEnabled();
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return delegateLogger.isWarnEnabled(marker);
  }

  @Override
  public void warn(String msg) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(msg);
    }
  }

  @Override
  public void warn(String format, Object arg) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(format, arg);
    }
  }

  @Override
  public void warn(String format, Object... arguments) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(format, arguments);
    }
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(format, arg1, arg2);
    }
  }

  @Override
  public void warn(String msg, Throwable t) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(msg, t);
    }
  }

  @Override
  public void warn(Marker marker, String msg) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, msg);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, format, arg);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, format, arg1, arg2);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, format, arguments);
    }
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isWarnEnabled()) {
      delegateLogger.warn(marker, msg, t);
    }
  }

  @Override
  public boolean isErrorEnabled() {
    return delegateLogger.isErrorEnabled();
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return delegateLogger.isErrorEnabled(marker);
  }

  @Override
  public void error(String msg) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(msg);
    }
  }

  @Override
  public void error(String format, Object arg) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(format, arg);
    }
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(format, arg1, arg2);
    }
  }

  @Override
  public void error(String format, Object... arguments) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(format, arguments);
    }
  }

  @Override
  public void error(String msg, Throwable t) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(msg, t);
    }
  }

  @Override
  public void error(Marker marker, String msg) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, msg);
    }
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, format, arg);
    }
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, format, arg1, arg2);
    }
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, format, arguments);
    }
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    if (delegateLogger.isErrorEnabled()) {
      delegateLogger.error(marker, msg, t);
    }
  }
}
