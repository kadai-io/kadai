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
