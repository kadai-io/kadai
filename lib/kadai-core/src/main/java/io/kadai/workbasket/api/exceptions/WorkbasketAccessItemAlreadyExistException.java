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

package io.kadai.workbasket.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import java.util.Map;

/**
 * This exception is thrown when an already existing {@linkplain WorkbasketAccessItem} was tried to
 * be created.
 */
public class WorkbasketAccessItemAlreadyExistException extends KadaiException {

  public static final String ERROR_KEY = "WORKBASKET_ACCESS_ITEM_ALREADY_EXISTS";
  private final String accessId;
  private final String workbasketId;

  public WorkbasketAccessItemAlreadyExistException(String accessId, String workbasketId) {
    super(
        String.format(
            "WorkbasketAccessItem with access id '%s' and workbasket id '%s' already exists.",
            accessId, workbasketId),
        ErrorCode.of(
            ERROR_KEY,
            Map.ofEntries(
                Map.entry("accessId", ensureNullIsHandled(accessId)),
                Map.entry("workbasketId", ensureNullIsHandled(workbasketId)))));
    this.accessId = accessId;
    this.workbasketId = workbasketId;
  }

  public String getAccessId() {
    return accessId;
  }

  public String getWorkbasketId() {
    return workbasketId;
  }
}
