/*
 * Copyright [2025] [envite consulting GmbH]
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

package io.kadai.task.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.task.api.models.Task;
import java.util.Map;

/**
 * This exception is thrown when a {@linkplain Task} is going to be created, but a {@linkplain Task}
 * with the same {@linkplain Task#getExternalId() external id} does already exist.
 */
public class TaskAlreadyExistException extends KadaiException {

  public static final String ERROR_KEY = "TASK_ALREADY_EXISTS";
  private final String externalId;

  public TaskAlreadyExistException(String externalId) {
    super(
        String.format("Task with external id '%s' already exists", externalId),
        ErrorCode.of(ERROR_KEY, Map.of("externalTaskId", ensureNullIsHandled(externalId))));
    this.externalId = externalId;
  }

  public String getExternalId() {
    return externalId;
  }
}
