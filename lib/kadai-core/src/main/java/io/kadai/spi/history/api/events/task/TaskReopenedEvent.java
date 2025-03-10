package io.kadai.spi.history.api.events.task;

import io.kadai.task.api.models.Task;

/** Event fired if a task is reopened. */
public class TaskReopenedEvent extends TaskHistoryEvent {

  public TaskReopenedEvent(String id, Task task, String userId, String details) {
    super(id, task, userId, details);
    eventType = (TaskHistoryEventType.REOPENED.getName());
    created = task.getClaimed();
  }
}
