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
 * The TaskEndstatePreprocessor allows to implement customized behaviour before the given
 * {@linkplain Task} goes into an {@linkplain io.kadai.task.api.TaskState#END_STATES end state}
 * (cancelled, terminated or completed).
 */
public interface TaskEndstatePreprocessor {

  /**
   * Perform any action before a {@linkplain Task} goes into an {@linkplain
   * io.kadai.task.api.TaskState#END_STATES end state}. A {@linkplain Task} goes into an end state
   * at the end of the following methods: {@linkplain
   * io.kadai.task.api.TaskService#completeTask(String)}, {@linkplain
   * io.kadai.task.api.TaskService#cancelTask(String)}, {@linkplain
   * io.kadai.task.api.TaskService#terminateTask(String)}.
   *
   * <p>This SPI is executed within the same transaction staple as {@linkplain
   * io.kadai.task.api.TaskService#completeTask(String)}, {@linkplain
   * io.kadai.task.api.TaskService#cancelTask(String)}, {@linkplain
   * io.kadai.task.api.TaskService#terminateTask(String)}.
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in the methods mentioned above.
   *
   * @param taskToProcess the {@linkplain Task} to preprocess
   * @return the modified {@linkplain Task}. <b>IMPORTANT:</b> persistent changes to the {@linkplain
   *     Task} have to be managed by the service provider
   */
  Task processTaskBeforeEndstate(Task taskToProcess);
}
