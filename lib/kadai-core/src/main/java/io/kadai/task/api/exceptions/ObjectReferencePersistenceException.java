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
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.Task;
import java.util.Map;

/**
 * This exception is thrown when an {@linkplain ObjectReference} should be inserted to the DB, but
 * it does already exist. <br>
 * This may happen when a not inserted {@linkplain ObjectReference} with the same {@linkplain
 * ObjectReference#getId() id} will be added twice on a {@linkplain Task}. This can't happen if the
 * correct {@linkplain Task}-Methods will be used instead of the List ones.
 */
public class ObjectReferencePersistenceException extends KadaiException {
  public static final String ERROR_KEY = "OBJECT_REFERENCE_ALREADY_EXISTS";
  private final String objectReferenceId;
  private final String taskId;

  public ObjectReferencePersistenceException(
      String objectReferenceId, String taskId, Throwable cause) {
    super(
        String.format(
            "Cannot insert ObjectReference with id '%s' for Task with id '%s' "
                + "because it already exists.",
            objectReferenceId, taskId),
        ErrorCode.of(
            ERROR_KEY,
            Map.ofEntries(
                Map.entry("objectReferenceId", ensureNullIsHandled(objectReferenceId)),
                Map.entry("taskId", ensureNullIsHandled(taskId)))),
        cause);
    this.objectReferenceId = objectReferenceId;
    this.taskId = taskId;
  }

  public String getObjectReferenceId() {
    return objectReferenceId;
  }

  public String getTaskId() {
    return taskId;
  }
}
