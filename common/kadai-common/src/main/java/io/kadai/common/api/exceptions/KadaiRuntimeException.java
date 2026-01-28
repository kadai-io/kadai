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

package io.kadai.common.api.exceptions;

import java.io.Serializable;

/** The common base class for KADAI's runtime exceptions. */
public class KadaiRuntimeException extends RuntimeException {

  private final ErrorCode errorCode;

  protected KadaiRuntimeException(String message, ErrorCode errorCode) {
    this(message, errorCode, null);
  }

  protected KadaiRuntimeException(String message, ErrorCode errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  protected static Serializable ensureNullIsHandled(Serializable o) {
    return o == null ? "null" : o;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + " [errorCode="
        + errorCode
        + ", message="
        + getMessage()
        + "]";
  }
}
