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

import io.kadai.common.api.security.GroupPrincipal;
import io.kadai.common.api.security.UserPrincipal;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Task;

/**
 * The CreateTaskPostProcessor allows to implement customized behavior after the given {@linkplain
 * Task} has been created.
 */
public interface CreateTaskPostprocessor {
  /**
   * Perform any action after a {@linkplain Task} has been created through {@linkplain
   * TaskService#createTask(Task)}.
   *
   * <p>This SPI is executed within the same transaction staple as {@linkplain
   * TaskService#createTask(Task)}.
   *
   * <p>This SPI is executed with the same {@linkplain UserPrincipal}
   * and {@linkplain GroupPrincipal} as in {@linkplain
   * TaskService#createTask(Task)}.
   *
   * @param createdTask the {@linkplain Task} to postprocess
   * @return the postprocessed {@linkplain Task} - must not be null
   */
  Task processTaskAfterCreation(Task createdTask);
}
