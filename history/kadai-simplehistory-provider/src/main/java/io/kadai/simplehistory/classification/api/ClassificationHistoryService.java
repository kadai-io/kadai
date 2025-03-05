package io.kadai.simplehistory.classification.api;

import io.kadai.spi.history.api.events.classification.ClassificationHistoryEvent;
import io.kadai.spi.history.api.exceptions.ClassificationHistoryEventNotFoundException;

public interface ClassificationHistoryService {

  // region CREATE

  ClassificationHistoryEvent createClassificationHistoryEvent(
      ClassificationHistoryEvent event);

  // endregion

  // region READ

  ClassificationHistoryEvent getClassificationHistoryEvent(String eventId)
      throws ClassificationHistoryEventNotFoundException;

  // endregion

  ClassificationHistoryQuery createClassificationHistoryQuery();
}
