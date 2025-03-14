package io.kadai.simplehistory.classification.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.history.api.KadaiEventConsumer;
import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;

public class ClassificationHistoryEventPersister
    implements KadaiEventConsumer<ClassificationHistoryEvent> {

  private ClassificationHistoryServiceImpl classificationHistoryService;

  @Override
  public void consume(ClassificationHistoryEvent event) {
    classificationHistoryService.createClassificationHistoryEvent(event);
  }

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    classificationHistoryService = new ClassificationHistoryServiceImpl();
    classificationHistoryService.initialize(kadaiEngine);
  }

  @Override
  public Class<ClassificationHistoryEvent> reify() {
    return ClassificationHistoryEvent.class;
  }
}
