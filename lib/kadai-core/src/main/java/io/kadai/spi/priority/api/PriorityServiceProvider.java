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

package io.kadai.spi.priority.api;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiInitializable;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskSummary;
import java.util.OptionalInt;

/**
 * The PriorityServiceProvider allows to determine the priority of a {@linkplain Task} according to
 * custom logic.
 */
public interface PriorityServiceProvider extends KadaiInitializable {

  @Override
  default void initialize(KadaiEngine kadaiEngine) {}

  /**
   * Determine the {@linkplain Task#getPriority() priority} of a certain {@linkplain Task} during
   * execution of {@linkplain io.kadai.task.api.TaskService#createTask(Task)} and {@linkplain
   * io.kadai.task.api.TaskService#updateTask(Task)}. This priority overwrites the priority from
   * Classification-driven logic.
   *
   * <p>The implemented method must calculate the {@linkplain Task#getPriority() priority}
   * efficiently. There can be a huge amount of {@linkplain Task Tasks} the SPI has to handle.
   *
   * <p>The behaviour is undefined if this method tries to apply persistent changes to any entity.
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task)} or {@linkplain
   * io.kadai.task.api.TaskService#updateTask(Task)}.
   *
   * @param taskSummary the {@linkplain TaskSummary} to compute the {@linkplain Task#getPriority()
   *     priority} for
   * @return the computed {@linkplain Task#getPriority() priority}
   */
  OptionalInt calculatePriority(TaskSummary taskSummary);
}
