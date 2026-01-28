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

package io.kadai.workbasket.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiRuntimeException;
import io.kadai.workbasket.api.models.Workbasket;

/** This exception is thrown when a user is not authorized to query a {@linkplain Workbasket}. */
public class NotAuthorizedToQueryWorkbasketException extends KadaiRuntimeException {

  public NotAuthorizedToQueryWorkbasketException(
      String message, ErrorCode errorCode, Throwable cause) {
    super(message, errorCode, cause);
  }
}
