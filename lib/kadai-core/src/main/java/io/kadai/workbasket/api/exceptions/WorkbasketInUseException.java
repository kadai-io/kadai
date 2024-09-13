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

package io.kadai.workbasket.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.Map;

/**
 * This exception is thrown when a specific {@linkplain Workbasket} does have content and is tried
 * to be deleted.
 */
public class WorkbasketInUseException extends KadaiException {

  public static final String ERROR_KEY = "WORKBASKET_IN_USE";
  private final String workbasketId;

  public WorkbasketInUseException(String workbasketId) {
    super(
        String.format(
            "Workbasket '%s' contains non-completed Tasks and can't be marked for deletion.",
            workbasketId),
        ErrorCode.of(ERROR_KEY, Map.of("workbasketId", ensureNullIsHandled(workbasketId))));
    this.workbasketId = workbasketId;
  }

  public String getWorkbasketId() {
    return workbasketId;
  }
}
