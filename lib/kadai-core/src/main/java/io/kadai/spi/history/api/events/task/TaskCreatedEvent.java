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

import io.kadai.task.api.models.Task;

/** Event fired if a task is created. */
public class TaskCreatedEvent extends TaskHistoryEvent {

  public TaskCreatedEvent(
      String id, Task task, String userId, String proxyAccessId, String details) {
    super(id, task, userId, proxyAccessId, details);
    eventType = TaskHistoryEventType.CREATED.getName();
    created = task.getCreated();
  }
}
