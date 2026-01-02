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

package io.kadai.spi.task.api;

import io.kadai.common.api.KadaiEngine;
import io.kadai.task.api.models.Task;

/**
 * The ReviewRequiredProvider allows to determine whether a {@linkplain Task} requires a review
 * instead of completion.
 */
public interface ReviewRequiredProvider {

  /**
   * Provide the active {@linkplain KadaiEngine} which is initialized for this KADAI installation.
   *
   * <p>This method is called during KADAI startup and allows the service provider to store the
   * active {@linkplain KadaiEngine} for later usage.
   *
   * @param kadaiEngine the active {@linkplain KadaiEngine} which is initialized for this
   *     installation
   */
  default void initialize(KadaiEngine kadaiEngine) {}

  /**
   * Determine if a {@linkplain Task} has to be reviewed instead of completed before {@linkplain
   * io.kadai.task.api.TaskService#completeTask(String)} is executed.
   *
   * <p>The behaviour is undefined if this method tries to apply persistent changes to any entity.
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in {@linkplain
   * io.kadai.task.api.TaskService#completeTask(String)}.
   *
   * @param task the {@linkplain Task} before {@linkplain
   *     io.kadai.task.api.TaskService#completeTask(String)} has started
   * @return true, if {@linkplain io.kadai.task.api.TaskService#requestReview(String)} should be
   *     executed instead of {@linkplain io.kadai.task.api.TaskService#completeTask(String)}. False,
   *     if {@linkplain io.kadai.task.api.TaskService#completeTask(String)} can be executed
   *     regularly
   */
  boolean reviewRequired(Task task);
}
