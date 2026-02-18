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
 */

package io.kadai.task.api.exceptions;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.task.api.models.Task;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * This exception is thrown when a specific {@linkplain Task} violates its service level, e.g. when
 * the combination of planned and due does not match the configured service level.
 */
public class ServiceLevelViolationException extends KadaiException {

  public static final String ERROR_KEY = "SERVICE_LEVEL_VIOLATION";

  private final Instant planned;
  private final Instant due;
  private final Duration serviceLevel;

  public ServiceLevelViolationException(Instant planned, Instant due, Duration serviceLevel) {
    super(
        String.format(
            "Cannot update a task with given planned %s and due date %s not matching the service "
                + "level %s.",
            planned, due, serviceLevel),
        ErrorCode.of(
            ERROR_KEY,
            Map.of(
                "planned", ensureNullIsHandled(planned),
                "due", ensureNullIsHandled(due),
                "serviceLevel", ensureNullIsHandled(serviceLevel))));
    this.planned = planned;
    this.due = due;
    this.serviceLevel = serviceLevel;
  }

  public Instant getPlanned() {
    return planned;
  }

  public Instant getDue() {
    return due;
  }

  public Duration getServiceLevel() {
    return serviceLevel;
  }
}
