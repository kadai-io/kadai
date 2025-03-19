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

package io.kadai.common.internal.util;

import io.kadai.common.api.exceptions.SystemException;

@FunctionalInterface
public interface CheckedRunnable<E extends Exception> {

  static Runnable wrapping(CheckedRunnable<? extends Exception> checkedRunnable)
      throws SystemException {
    return () -> {
      try {
        checkedRunnable.run();
      } catch (Exception e) {
        throw new SystemException("Caught exception", e);
      }
    };
  }

  static <E extends Exception> Runnable rethrowing(CheckedRunnable<E> checkedRunnable) throws E {
    return () -> {
      try {
        checkedRunnable.run();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception exception) {
        throwActual(exception);
      }
    };
  }

  void run() throws E;

  @SuppressWarnings("unchecked")
  private static <E extends Exception> void throwActual(Exception exception) throws E {
    throw (E) exception;
  }
}
