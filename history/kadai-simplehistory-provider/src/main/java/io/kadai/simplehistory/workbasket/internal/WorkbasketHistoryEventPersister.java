package io.kadai.simplehistory.workbasket.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.workbasket.WorkbasketHistoryEvent;

public class WorkbasketHistoryEventPersister implements KadaiEventConsumer<WorkbasketHistoryEvent> {

  private WorkbasketHistoryServiceImpl workbasketHistoryService;

  @Override
  public void consume(WorkbasketHistoryEvent event) {
    workbasketHistoryService.createWorkbasketHistoryEvent(event);
  }

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    workbasketHistoryService = new WorkbasketHistoryServiceImpl();
    workbasketHistoryService.initialize(kadaiEngine);
  }

  @Override
  public Class<WorkbasketHistoryEvent> reify() {
    return WorkbasketHistoryEvent.class;
  }
}
