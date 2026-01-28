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

/** This exception is thrown when a generic KADAI problem is encountered. */
public class SystemException extends KadaiRuntimeException {

  public static final String ERROR_KEY = "CRITICAL_SYSTEM_ERROR";

  public SystemException(String msg) {
    this(msg, null);
  }

  public SystemException(String msg, Throwable cause) {
    super(msg, ErrorCode.of(ERROR_KEY), cause);
  }
}
