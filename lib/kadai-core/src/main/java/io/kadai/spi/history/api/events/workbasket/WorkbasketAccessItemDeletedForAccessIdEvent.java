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

package io.kadai.spi.history.api.events.workbasket;

import io.kadai.workbasket.api.models.Workbasket;
import java.time.Instant;

public class WorkbasketAccessItemDeletedForAccessIdEvent extends WorkbasketHistoryEvent {

  public WorkbasketAccessItemDeletedForAccessIdEvent(
      String id, Workbasket workbasket, String userId) {
    super(id, workbasket, userId, null);
    eventType = WorkbasketHistoryEventType.ACCESS_ITEM_DELETED_FOR_ACCESS_ID.getName();
    created = Instant.now();
  }
}
