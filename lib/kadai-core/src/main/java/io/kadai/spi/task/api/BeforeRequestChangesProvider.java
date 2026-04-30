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

import io.kadai.common.api.KadaiInitializable;
import io.kadai.task.api.models.Task;

/**
 * The BeforeRequestChangesProvider allows to implement customized behaviour before changes have
 * been requested on a given {@linkplain Task}.
 */
public interface BeforeRequestChangesProvider extends KadaiInitializable {

  /**
   * Perform any action before changes have been requested on a {@linkplain Task} through
   * {@linkplain io.kadai.task.api.TaskService#requestChanges(String)} or {@linkplain
   * io.kadai.task.api.TaskService#forceRequestChanges(String)}.
   *
   * <p>This SPI is executed within the same transaction staple as {@linkplain
   * io.kadai.task.api.TaskService#requestChanges(String)}.
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in {@linkplain
   * io.kadai.task.api.TaskService#requestChanges(String)}.
   *
   * @param task the {@linkplain Task} before {@linkplain
   *     io.kadai.task.api.TaskService#requestChanges(String)} or {@linkplain
   *     io.kadai.task.api.TaskService#forceRequestChanges(String)} has started
   * @return the modified {@linkplain Task}. <b>IMPORTANT:</b> persistent changes to the {@linkplain
   *     Task} have to be managed by the service provider
   * @throws Exception if the service provider throws any exception
   */
  Task beforeRequestChanges(Task task) throws Exception;
}
