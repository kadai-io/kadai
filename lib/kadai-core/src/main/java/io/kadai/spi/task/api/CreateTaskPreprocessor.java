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

import io.kadai.task.api.models.Task;

/**
 * The CreateTaskPreprocessor allows to implement customized behaviour before the given {@linkplain
 * Task} has been created.
 */
public interface CreateTaskPreprocessor {

  /**
   * Perform any action before a {@linkplain Task} has been created through {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task)}.
   *
   * <p>This SPI is executed within the same transaction staple as {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task)}.
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task)}.
   *
   * @param taskToProcess the {@linkplain Task} to preprocess
   */
  void processTaskBeforeCreation(Task taskToProcess);
}
