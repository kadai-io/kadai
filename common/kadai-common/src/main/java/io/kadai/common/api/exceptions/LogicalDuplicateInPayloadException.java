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
import java.util.Map;

/**
 * This exception is thrown when the payload for a request to a REST-Endpoint contains logical
 * duplicates.
 */
public class LogicalDuplicateInPayloadException extends KadaiException {

  public static final String ERROR_KEY = "LOGICAL_DUPLICATE_IN_PAYLOAD";
  private final Serializable duplicate;

  public LogicalDuplicateInPayloadException(Serializable duplicate) {
    super(
        "The payload contains a logical duplicate.",
        ErrorCode.of(ERROR_KEY, Map.of("duplicate", ensureNullIsHandled(duplicate))));
    this.duplicate = duplicate;
  }

  public Object getDuplicate() {
    return duplicate;
  }
}
