package io.kadai.simplehistory.task.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;

public class TaskHistoryEventPersister implements KadaiEventConsumer<TaskHistoryEvent> {

  private TaskHistoryServiceImpl taskHistoryService;

  @Override
  public void consume(TaskHistoryEvent event) {
    taskHistoryService.createTaskHistoryEvent(event);
  }

  @Override
  public Class<TaskHistoryEvent> reify() {
    return TaskHistoryEvent.class;
  }

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    this.taskHistoryService = new TaskHistoryServiceImpl();
    taskHistoryService.initialize(kadaiEngine);
  }
}
