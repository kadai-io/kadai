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

package io.kadai.task.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.task.api.models.Task;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.Map;

/**
 * This exception is thrown when a BeforeTransferTaskProvider denies the transfer of a
 * {@linkplain Task} to a destination {@linkplain Workbasket}.
 */
public class TransferCheckException extends KadaiException {

  public static final String ERROR_KEY = "TASK_TRANSFER_CHECK_FAILED";

  private final String reason;
  private final String sourceWorkbasketId;
  private final String destinationWorkbasketId;

  public TransferCheckException(
      String reason, String sourceWorkbasketId, String destinationWorkbasketId) {
    super(
        String.format(
            "Transfer of Task from Workbasket '%s' to Workbasket '%s' was denied: %s",
            sourceWorkbasketId, destinationWorkbasketId, reason),
        ErrorCode.of(
            ERROR_KEY,
            Map.ofEntries(
                Map.entry("reason", ensureNullIsHandled(reason)),
                Map.entry("sourceWorkbasketId", ensureNullIsHandled(sourceWorkbasketId)),
                Map.entry(
                    "destinationWorkbasketId", ensureNullIsHandled(destinationWorkbasketId)))));
    this.reason = reason;
    this.sourceWorkbasketId = sourceWorkbasketId;
    this.destinationWorkbasketId = destinationWorkbasketId;
  }

  public String getReason() {
    return reason;
  }

  public String getSourceWorkbasketId() {
    return sourceWorkbasketId;
  }

  public String getDestinationWorkbasketId() {
    return destinationWorkbasketId;
  }
}


