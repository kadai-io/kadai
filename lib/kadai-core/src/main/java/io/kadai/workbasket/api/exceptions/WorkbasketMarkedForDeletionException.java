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
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.Map;

/**
 * This exception is thrown when a {@linkplain Workbasket}, which was {@linkplain
 * Workbasket#isMarkedForDeletion() marked for deletion}, could not be deleted.
 */
public class WorkbasketMarkedForDeletionException extends KadaiException {

  public static final String ERROR_KEY = "WORKBASKET_MARKED_FOR_DELETION";
  private final String workbasketId;

  public WorkbasketMarkedForDeletionException(String workbasketId) {
    super(
        String.format(
            "Workbasket with id '%s' could not be deleted, but was marked for deletion.",
            workbasketId),
        ErrorCode.of(ERROR_KEY, Map.of("workbasketId", ensureNullIsHandled(workbasketId))));
    this.workbasketId = workbasketId;
  }

  public String getWorkbasketId() {
    return workbasketId;
  }
}
