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

import io.kadai.common.api.exceptions.SystemException;
import java.util.function.BiFunction;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R, E extends Exception> {

  static <T, U, R> BiFunction<T, U, R> wrapping(
      CheckedBiFunction<T, U, R, ? extends Exception> checkedBiFunction) throws SystemException {
    return (t, u) -> {
      try {
        return checkedBiFunction.apply(t, u);
      } catch (Exception e) {
        throw new SystemException("Caught exception", e);
      }
    };
  }

  static <T, U, R, E extends Exception> BiFunction<T, U, R> rethrowing(
      CheckedBiFunction<T, U, R, E> checkedBiFunction) throws E {
    return (t, u) -> {
      try {
        return checkedBiFunction.apply(t, u);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception exception) {
        throwActual(exception);
        return null;
      }
    };
  }

  R apply(T t, U u) throws E;

  @SuppressWarnings("unchecked")
  private static <E extends Exception> void throwActual(Exception exception) throws E {
    throw (E) exception;
  }
}
