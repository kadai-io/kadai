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

package io.kadai.simplehistory.task.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.task.TaskDeletedEvent;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;

public class TaskHistoryEventConsumer implements KadaiEventConsumer<TaskHistoryEvent> {

  private KadaiEngine kadaiEngine;
  private TaskHistoryServiceImpl taskHistoryService;

  @Override
  public void consume(TaskHistoryEvent event) {
    final boolean deletionEnabled =
        kadaiEngine.getConfiguration().isDeleteHistoryEventsOnTaskDeletionEnabled();
    if (event instanceof TaskDeletedEvent && deletionEnabled) {
      final String taskEventId = event.getTaskId();
      try {
        taskHistoryService.deleteTaskHistoryEventsByTaskId(taskEventId);
      } catch (NotAuthorizedException e) {
        final String msg =
            String.format(
                "Caught exception while trying to delete TaskHistoryEvents for task-event-id '%s'",
                taskEventId);
        throw new SystemException(msg, e);
      }
    } else {
      taskHistoryService.createTaskHistoryEvent(event);
    }
  }

  @Override
  public Class<TaskHistoryEvent> reify() {
    return TaskHistoryEvent.class;
  }

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
    this.taskHistoryService = new TaskHistoryServiceImpl();
    taskHistoryService.initialize(kadaiEngine);
  }
}
