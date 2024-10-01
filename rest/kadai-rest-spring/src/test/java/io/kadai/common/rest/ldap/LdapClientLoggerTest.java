/*
 * Copyright [2024] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http:www.apache.org/licenses/LICENSE-2.0
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

class LdapClientLoggerTest {

  private Logger mockLogger;
  private LdapClientLogger ldapClientLogger;

  @BeforeEach
  void setUp() {
    mockLogger = mock(Logger.class);
    ldapClientLogger = new LdapClientLogger(mockLogger);
  }

  @Test
  void test_returns_delegate_logger_name() {
    when(mockLogger.getName()).thenReturn("DelegateLoggerName");
    assertEquals("DelegateLoggerName", ldapClientLogger.getName());
  }

  /*---TRACE---*/

  @Test
  void test_trace_enabled_returns_true() {
    when(mockLogger.isTraceEnabled()).thenReturn(true);
    assertTrue(ldapClientLogger.isTraceEnabled());
  }

  @Test
  void test_trace_disabled_returns_false() {
    when(mockLogger.isTraceEnabled()).thenReturn(false);
    assertFalse(ldapClientLogger.isTraceEnabled());
  }

  @Test
  void test_trace_enabled_with_marker_returns_true() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isTraceEnabled(testMarker)).thenReturn(true);
    assertTrue(ldapClientLogger.isTraceEnabled(testMarker));
  }

  @Test
  void test_trace_disabled_with_marker_returns_false() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isTraceEnabled(testMarker)).thenReturn(false);
    assertFalse(ldapClientLogger.isTraceEnabled(testMarker));
  }

  @Test
  void test_trace_logs_message_when_enabled() {
    when(mockLogger.isTraceEnabled()).thenReturn(true);

    ldapClientLogger.trace("Test message");
    verify(mockLogger).trace("Test message");

    ldapClientLogger.trace("Test message: {}", () -> "arg1");
    verify(mockLogger).trace("Test message: {}", "arg1");

    ldapClientLogger.trace("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).trace("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.trace("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).trace("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.trace("Test message", testThrowable);
    verify(mockLogger).trace("Test message", testThrowable);

    ldapClientLogger.trace(testMarker, "Test message");
    verify(mockLogger).trace(testMarker, "Test message");

    ldapClientLogger.trace(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger).trace(testMarker, "Test message {}", "arg1");

    ldapClientLogger.trace(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).trace(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.trace(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).trace(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.trace(testMarker, "Test message", testThrowable);
    verify(mockLogger).trace(testMarker, "Test message", testThrowable);
  }

  @Test
  void test_not_trace_logs_message_when_disabled() {
    when(mockLogger.isTraceEnabled()).thenReturn(false);

    ldapClientLogger.trace("Test message");
    verify(mockLogger, never()).trace("Test message");

    ldapClientLogger.trace("Test message: {}", () -> "arg1");
    verify(mockLogger, never()).trace("Test message: {}", "arg1");

    ldapClientLogger.trace("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).trace("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.trace("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never()).trace("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.trace("Test message", testThrowable);
    verify(mockLogger, never()).trace("Test message", testThrowable);

    ldapClientLogger.trace(testMarker, "Test message");
    verify(mockLogger, never()).trace(testMarker, "Test message");

    ldapClientLogger.trace(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger, never()).trace(testMarker, "Test message {}", "arg1");

    ldapClientLogger.trace(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).trace(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.trace(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never())
        .trace(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.trace(testMarker, "Test message", testThrowable);
    verify(mockLogger, never()).trace(testMarker, "Test message", testThrowable);
  }

  /*---DEBUG---*/

  @Test
  void test_debug_enabled_returns_true() {
    when(mockLogger.isDebugEnabled()).thenReturn(true);
    assertTrue(ldapClientLogger.isDebugEnabled());
  }

  @Test
  void test_debug_disabled_returns_false() {
    when(mockLogger.isDebugEnabled()).thenReturn(false);
    assertFalse(ldapClientLogger.isDebugEnabled());
  }

  @Test
  void test_debug_enabled_with_marker_returns_true() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isDebugEnabled(testMarker)).thenReturn(true);
    assertTrue(ldapClientLogger.isDebugEnabled(testMarker));
  }

  @Test
  void test_debug_disabled_with_marker_returns_false() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isDebugEnabled(testMarker)).thenReturn(false);
    assertFalse(ldapClientLogger.isDebugEnabled(testMarker));
  }

  @Test
  void test_debug_logs_message_when_enabled() {
    when(mockLogger.isDebugEnabled()).thenReturn(true);

    ldapClientLogger.debug("Test message");
    verify(mockLogger).debug("Test message");

    ldapClientLogger.debug("Test message: {}", () -> "arg1");
    verify(mockLogger).debug("Test message: {}", "arg1");

    ldapClientLogger.debug("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).debug("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.debug("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).debug("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.debug("Test message", testThrowable);
    verify(mockLogger).debug("Test message", testThrowable);

    ldapClientLogger.debug(testMarker, "Test message");
    verify(mockLogger).debug(testMarker, "Test message");

    ldapClientLogger.debug(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger).debug(testMarker, "Test message {}", "arg1");

    ldapClientLogger.debug(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).debug(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.debug(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).debug(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.debug(testMarker, "Test message", testThrowable);
    verify(mockLogger).debug(testMarker, "Test message", testThrowable);
  }

  @Test
  void test_not_debug_logs_message_when_disabled() {
    when(mockLogger.isDebugEnabled()).thenReturn(false);

    ldapClientLogger.debug("Test message");
    verify(mockLogger, never()).debug("Test message");

    ldapClientLogger.debug("Test message: {}", () -> "arg1");
    verify(mockLogger, never()).debug("Test message: {}", "arg1");

    ldapClientLogger.debug("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).debug("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.debug("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never()).debug("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.debug("Test message", testThrowable);
    verify(mockLogger, never()).debug("Test message", testThrowable);

    ldapClientLogger.debug(testMarker, "Test message");
    verify(mockLogger, never()).debug(testMarker, "Test message");

    ldapClientLogger.debug(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger, never()).debug(testMarker, "Test message {}", "arg1");

    ldapClientLogger.debug(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).debug(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.debug(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never())
        .debug(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.debug(testMarker, "Test message", testThrowable);
    verify(mockLogger, never()).debug(testMarker, "Test message", testThrowable);
  }

  /*---INFO---*/

  @Test
  void test_info_enabled_returns_true() {
    when(mockLogger.isInfoEnabled()).thenReturn(true);
    assertTrue(ldapClientLogger.isInfoEnabled());
  }

  @Test
  void test_info_disabled_returns_false() {
    when(mockLogger.isInfoEnabled()).thenReturn(false);
    assertFalse(ldapClientLogger.isInfoEnabled());
  }

  @Test
  void test_info_enabled_with_marker_returns_true() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isInfoEnabled(testMarker)).thenReturn(true);
    assertTrue(ldapClientLogger.isInfoEnabled(testMarker));
  }

  @Test
  void test_info_disabled_with_marker_returns_false() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isInfoEnabled(testMarker)).thenReturn(false);
    assertFalse(ldapClientLogger.isInfoEnabled(testMarker));
  }

  @Test
  void test_info_logs_message_when_enabled() {
    when(mockLogger.isInfoEnabled()).thenReturn(true);

    ldapClientLogger.info("Test message");
    verify(mockLogger).info("Test message");

    ldapClientLogger.info("Test message: {}", () -> "arg1");
    verify(mockLogger).info("Test message: {}", "arg1");

    ldapClientLogger.info("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).info("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.info("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).info("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.info("Test message", testThrowable);
    verify(mockLogger).info("Test message", testThrowable);

    ldapClientLogger.info(testMarker, "Test message");
    verify(mockLogger).info(testMarker, "Test message");

    ldapClientLogger.info(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger).info(testMarker, "Test message {}", "arg1");

    ldapClientLogger.info(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).info(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.info(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).info(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.info(testMarker, "Test message", testThrowable);
    verify(mockLogger).info(testMarker, "Test message", testThrowable);
  }

  @Test
  void test_not_info_logs_message_when_disabled() {
    when(mockLogger.isInfoEnabled()).thenReturn(false);

    ldapClientLogger.info("Test message");
    verify(mockLogger, never()).info("Test message");

    ldapClientLogger.info("Test message: {}", () -> "arg1");
    verify(mockLogger, never()).info("Test message: {}", "arg1");

    ldapClientLogger.info("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).info("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.info("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never()).info("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.info("Test message", testThrowable);
    verify(mockLogger, never()).info("Test message", testThrowable);

    ldapClientLogger.info(testMarker, "Test message");
    verify(mockLogger, never()).info(testMarker, "Test message");

    ldapClientLogger.info(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger, never()).info(testMarker, "Test message {}", "arg1");

    ldapClientLogger.info(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).info(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.info(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never()).info(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.info(testMarker, "Test message", testThrowable);
    verify(mockLogger, never()).info(testMarker, "Test message", testThrowable);
  }

  /*---WARN---*/

  @Test
  void test_warn_enabled_returns_true() {
    when(mockLogger.isWarnEnabled()).thenReturn(true);
    assertTrue(ldapClientLogger.isWarnEnabled());
  }

  @Test
  void test_warn_disabled_returns_false() {
    when(mockLogger.isWarnEnabled()).thenReturn(false);
    assertFalse(ldapClientLogger.isWarnEnabled());
  }

  @Test
  void test_warn_enabled_with_marker_returns_true() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isWarnEnabled(testMarker)).thenReturn(true);
    assertTrue(ldapClientLogger.isWarnEnabled(testMarker));
  }

  @Test
  void test_warn_disabled_with_marker_returns_false() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isWarnEnabled(testMarker)).thenReturn(false);
    assertFalse(ldapClientLogger.isWarnEnabled(testMarker));
  }

  @Test
  void test_warn_logs_message_when_enabled() {
    when(mockLogger.isWarnEnabled()).thenReturn(true);

    ldapClientLogger.warn("Test message");
    verify(mockLogger).warn("Test message");

    ldapClientLogger.warn("Test message: {}", () -> "arg1");
    verify(mockLogger).warn("Test message: {}", "arg1");

    ldapClientLogger.warn("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).warn("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.warn("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).warn("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.warn("Test message", testThrowable);
    verify(mockLogger).warn("Test message", testThrowable);

    ldapClientLogger.warn(testMarker, "Test message");
    verify(mockLogger).warn(testMarker, "Test message");

    ldapClientLogger.warn(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger).warn(testMarker, "Test message {}", "arg1");

    ldapClientLogger.warn(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).warn(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.warn(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).warn(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.warn(testMarker, "Test message", testThrowable);
    verify(mockLogger).warn(testMarker, "Test message", testThrowable);
  }

  @Test
  void test_not_warn_logs_message_when_disabled() {
    when(mockLogger.isWarnEnabled()).thenReturn(false);

    ldapClientLogger.warn("Test message");
    verify(mockLogger, never()).warn("Test message");

    ldapClientLogger.warn("Test message: {}", () -> "arg1");
    verify(mockLogger, never()).warn("Test message: {}", "arg1");

    ldapClientLogger.warn("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).warn("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.warn("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never()).warn("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.warn("Test message", testThrowable);
    verify(mockLogger, never()).warn("Test message", testThrowable);

    ldapClientLogger.warn(testMarker, "Test message");
    verify(mockLogger, never()).warn(testMarker, "Test message");

    ldapClientLogger.warn(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger, never()).warn(testMarker, "Test message {}", "arg1");

    ldapClientLogger.warn(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).warn(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.warn(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never()).warn(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.warn(testMarker, "Test message", testThrowable);
    verify(mockLogger, never()).warn(testMarker, "Test message", testThrowable);
  }

  /*---ERROR---*/

  @Test
  void test_error_enabled_returns_true() {
    when(mockLogger.isErrorEnabled()).thenReturn(true);
    assertTrue(ldapClientLogger.isErrorEnabled());
  }

  @Test
  void test_error_disabled_returns_false() {
    when(mockLogger.isErrorEnabled()).thenReturn(false);
    assertFalse(ldapClientLogger.isErrorEnabled());
  }

  @Test
  void test_error_enabled_with_marker_returns_true() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isErrorEnabled(testMarker)).thenReturn(true);
    assertTrue(ldapClientLogger.isErrorEnabled(testMarker));
  }

  @Test
  void test_error_disabled_with_marker_returns_false() {
    Marker testMarker = MarkerFactory.getMarker("Test marker");
    when(mockLogger.isErrorEnabled(testMarker)).thenReturn(false);
    assertFalse(ldapClientLogger.isErrorEnabled(testMarker));
  }

  @Test
  void test_error_logs_message_when_enabled() {
    when(mockLogger.isErrorEnabled()).thenReturn(true);

    ldapClientLogger.error("Test message");
    verify(mockLogger).error("Test message");

    ldapClientLogger.error("Test message: {}", () -> "arg1");
    verify(mockLogger).error("Test message: {}", "arg1");

    ldapClientLogger.error("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).error("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.error("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).error("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.error("Test message", testThrowable);
    verify(mockLogger).error("Test message", testThrowable);

    ldapClientLogger.error(testMarker, "Test message");
    verify(mockLogger).error(testMarker, "Test message");

    ldapClientLogger.error(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger).error(testMarker, "Test message {}", "arg1");

    ldapClientLogger.error(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger).error(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.error(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger).error(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.error(testMarker, "Test message", testThrowable);
    verify(mockLogger).error(testMarker, "Test message", testThrowable);
  }

  @Test
  void test_not_error_logs_message_when_disabled() {
    when(mockLogger.isErrorEnabled()).thenReturn(false);

    ldapClientLogger.error("Test message");
    verify(mockLogger, never()).error("Test message");

    ldapClientLogger.error("Test message: {}", () -> "arg1");
    verify(mockLogger, never()).error("Test message: {}", "arg1");

    ldapClientLogger.error("Test message: {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).error("Test message: {}, {}", "arg1", "arg2");

    ldapClientLogger.error("Test message: {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never()).error("Test message: {}, {}, {}", "arg1", "arg2", "arg3");

    Throwable testThrowable = new Throwable("Test throwable");
    Marker testMarker = MarkerFactory.getMarker("Test marker");

    ldapClientLogger.error("Test message", testThrowable);
    verify(mockLogger, never()).error("Test message", testThrowable);

    ldapClientLogger.error(testMarker, "Test message");
    verify(mockLogger, never()).error(testMarker, "Test message");

    ldapClientLogger.error(testMarker, "Test message {}", () -> "arg1");
    verify(mockLogger, never()).error(testMarker, "Test message {}", "arg1");

    ldapClientLogger.error(testMarker, "Test message {}, {}", () -> "arg1", () -> "arg2");
    verify(mockLogger, never()).error(testMarker, "Test message {}, {}", "arg1", "arg2");

    ldapClientLogger.error(
        testMarker, "Test message {}, {}, {}", () -> "arg1", () -> "arg2", () -> "arg3");
    verify(mockLogger, never())
        .error(testMarker, "Test message {}, {}, {}", "arg1", "arg2", "arg3");

    ldapClientLogger.error(testMarker, "Test message", testThrowable);
    verify(mockLogger, never()).error(testMarker, "Test message", testThrowable);
  }
}
