/*
 * Copyright [2026] [envite consulting GmbH]
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

package io.kadai.common.internal.util;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.SystemException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(Lifecycle.PER_CLASS)
public class LambdaCheckedExceptionsTest {

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CheckedConsumerTest {
    @ParameterizedTest
    @ValueSource(
        classes = {
          IOException.class,
          SQLException.class,
          ClassNotFoundException.class,
          InterruptedException.class,
          NoSuchMethodException.class,
          TimeoutException.class,
          ConcurrencyException.class,
          DomainNotFoundException.class
        })
    <T extends Exception> void should_RethrowExactException_WhenConsumerThrowsCheckedException(
        Class<T> clazz) throws Exception {
      final T expected = clazz.getDeclaredConstructor(String.class).newInstance("Some message");

      final ThrowingCallable call =
          () ->
              CheckedConsumer.rethrowing(
                      o -> {
                        throw expected;
                      })
                  .accept(new Object());

      assertThatExceptionOfType(clazz).isThrownBy(call).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(
        classes = {
          NullPointerException.class,
          ArrayIndexOutOfBoundsException.class,
          ArithmeticException.class,
          ClassCastException.class,
          IllegalArgumentException.class,
          IllegalStateException.class,
          NoSuchElementException.class,
          UnsupportedOperationException.class,
          SystemException.class,
        })
    <T extends RuntimeException>
        void should_RethrowExactException_WhenConsumerThrowsUncheckedException(Class<T> clazz)
            throws Exception {
      final T expected = clazz.getDeclaredConstructor(String.class).newInstance("Some message");

      final ThrowingCallable call =
          () ->
              CheckedConsumer.rethrowing(
                      o -> {
                        throw expected;
                      })
                  .accept(new Object());

      assertThatExceptionOfType(clazz).isThrownBy(call).isEqualTo(expected);
    }

    @Test
    void should_NotThrowException_WhenConsumerThrowsNoException() {
      final ThrowingCallable call = () -> CheckedConsumer.rethrowing(o -> {}).accept(new Object());

      assertThatNoException().isThrownBy(call);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CheckedFunctionTest {
    @ParameterizedTest
    @ValueSource(
        classes = {
          IOException.class,
          SQLException.class,
          ClassNotFoundException.class,
          InterruptedException.class,
          NoSuchMethodException.class,
          TimeoutException.class,
          ConcurrencyException.class,
          DomainNotFoundException.class
        })
    <T extends Exception> void should_RethrowExactException_WhenFunctionThrowsCheckedException(
        Class<T> clazz) throws Exception {
      final T expected = clazz.getDeclaredConstructor(String.class).newInstance("Some message");

      final ThrowingCallable call =
          () ->
              CheckedFunction.rethrowing(
                      o -> {
                        throw expected;
                      })
                  .apply(new Object());

      assertThatExceptionOfType(clazz).isThrownBy(call).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(
        classes = {
          NullPointerException.class,
          ArrayIndexOutOfBoundsException.class,
          ArithmeticException.class,
          ClassCastException.class,
          IllegalArgumentException.class,
          IllegalStateException.class,
          NoSuchElementException.class,
          UnsupportedOperationException.class,
          SystemException.class,
        })
    <T extends RuntimeException>
        void should_RethrowExactException_WhenFunctionThrowsUncheckedException(Class<T> clazz)
            throws Exception {
      final T expected = clazz.getDeclaredConstructor(String.class).newInstance("Some message");

      final ThrowingCallable call =
          () ->
              CheckedFunction.rethrowing(
                      o -> {
                        throw expected;
                      })
                  .apply(new Object());

      assertThatExceptionOfType(clazz).isThrownBy(call).isEqualTo(expected);
    }

    @Test
    void should_NotThrowException_WhenFunctionThrowsNoException() {
      final ThrowingCallable call = () -> CheckedFunction.rethrowing(o -> o).apply(new Object());

      assertThatNoException().isThrownBy(call);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CheckedRunnableTest {
    @ParameterizedTest
    @ValueSource(
        classes = {
          IOException.class,
          SQLException.class,
          ClassNotFoundException.class,
          InterruptedException.class,
          NoSuchMethodException.class,
          TimeoutException.class,
          ConcurrencyException.class,
          DomainNotFoundException.class
        })
    <T extends Exception> void should_RethrowExactException_WhenRunnableThrowsCheckedException(
        Class<T> clazz) throws Exception {
      final T expected = clazz.getDeclaredConstructor(String.class).newInstance("Some message");

      final ThrowingCallable call =
          () ->
              CheckedRunnable.rethrowing(
                      () -> {
                        throw expected;
                      })
                  .run();

      assertThatExceptionOfType(clazz).isThrownBy(call).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(
        classes = {
          NullPointerException.class,
          ArrayIndexOutOfBoundsException.class,
          ArithmeticException.class,
          ClassCastException.class,
          IllegalArgumentException.class,
          IllegalStateException.class,
          NoSuchElementException.class,
          UnsupportedOperationException.class,
          SystemException.class,
        })
    <T extends RuntimeException>
        void should_RethrowExactException_WhenRunnableThrowsUncheckedException(Class<T> clazz)
            throws Exception {
      final T expected = clazz.getDeclaredConstructor(String.class).newInstance("Some message");

      final ThrowingCallable call =
          () ->
              CheckedRunnable.rethrowing(
                      () -> {
                        throw expected;
                      })
                  .run();

      assertThatExceptionOfType(clazz).isThrownBy(call).isEqualTo(expected);
    }

    @Test
    void should_NotThrowException_WhenRunnableThrowsNoException() {
      final ThrowingCallable call = () -> CheckedRunnable.rethrowing(Object::new).run();

      assertThatNoException().isThrownBy(call);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CheckedSupplierTest {
    @ParameterizedTest
    @ValueSource(
        classes = {
          IOException.class,
          SQLException.class,
          ClassNotFoundException.class,
          InterruptedException.class,
          NoSuchMethodException.class,
          TimeoutException.class,
          ConcurrencyException.class,
          DomainNotFoundException.class
        })
    <T extends Exception> void should_RethrowExactException_WhenSupplierThrowsCheckedException(
        Class<T> clazz) throws Exception {
      final T expected = clazz.getDeclaredConstructor(String.class).newInstance("Some message");

      final ThrowingCallable call =
          () ->
              CheckedSupplier.rethrowing(
                      () -> {
                        throw expected;
                      })
                  .get();

      assertThatExceptionOfType(clazz).isThrownBy(call).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(
        classes = {
          NullPointerException.class,
          ArrayIndexOutOfBoundsException.class,
          ArithmeticException.class,
          ClassCastException.class,
          IllegalArgumentException.class,
          IllegalStateException.class,
          NoSuchElementException.class,
          UnsupportedOperationException.class,
          SystemException.class,
        })
    <T extends RuntimeException>
        void should_RethrowExactException_WhenSupplierThrowsUncheckedException(Class<T> clazz)
            throws Exception {
      final T expected = clazz.getDeclaredConstructor(String.class).newInstance("Some message");

      final ThrowingCallable call =
          () ->
              CheckedSupplier.rethrowing(
                      () -> {
                        throw expected;
                      })
                  .get();

      assertThatExceptionOfType(clazz).isThrownBy(call).isEqualTo(expected);
    }

    @Test
    void should_NotThrowException_WhenSupplierThrowsNoException() {
      final ThrowingCallable call = () -> CheckedSupplier.rethrowing(Object::new).get();

      assertThatNoException().isThrownBy(call);
    }
  }
}
