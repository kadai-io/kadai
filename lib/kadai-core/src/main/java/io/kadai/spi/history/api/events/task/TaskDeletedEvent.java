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

package io.kadai.spi.history.api.events.task;

import io.kadai.task.api.models.TaskSummary;
import java.time.Instant;

public class TaskDeletedEvent extends TaskHistoryEvent {

  public TaskDeletedEvent(
      String id, TaskSummary taskSummary, String taskId, String userId, String proxyAccessId) {
    super(id, taskSummary, userId, proxyAccessId, null);
    eventType = TaskHistoryEventType.DELETED.getName();
    created = Instant.now();
    super.taskId = taskId;
  }
}
