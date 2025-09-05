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

package io.kadai.spi.task.api;

import io.kadai.common.api.KadaiEngine;
import io.kadai.task.api.models.Task;

/**
 * The AfterRequestReviewProvider allows to implement customized behaviour after a review has been
 * requested on a given {@linkplain Task}.
 */
public interface AfterRequestReviewProvider {

  /**
   * Provide the active {@linkplain KadaiEngine} which is initialized for this KADAI installation.
   *
   * <p>This method is called during KADAI startup and allows the service provider to store the
   * active {@linkplain KadaiEngine} for later usage.
   *
   * @param kadaiEngine the active {@linkplain KadaiEngine} which is initialized for this
   *     installation
   */
  void initialize(KadaiEngine kadaiEngine);

  /**
   * Perform any action after a review has been requested on a {@linkplain Task} through {@linkplain
   * io.kadai.task.api.TaskService#requestReview(String)} or {@linkplain
   * io.kadai.task.api.TaskService#forceRequestReview(String)}.
   *
   * <p>This SPI is executed within the same transaction staple as {@linkplain
   * io.kadai.task.api.TaskService#requestReview(String)}.
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in {@linkplain
   * io.kadai.task.api.TaskService#requestReview(String)}.
   *
   * @param task the {@linkplain Task} after {@linkplain
   *     io.kadai.task.api.TaskService#requestReview(String)} or {@linkplain
   *     io.kadai.task.api.TaskService#forceRequestReview(String)} has completed
   * @return the modified {@linkplain Task}. <b>IMPORTANT:</b> persistent changes to the {@linkplain
   *     Task} have to be managed by the service provider
   * @throws Exception if the service provider throws any exception
   */
  Task afterRequestReview(Task task) throws Exception;

  /**
   * Perform any action after a review has been requested on a {@linkplain Task} through {@linkplain
   * io.kadai.task.api.TaskService#requestReview(String)} or {@linkplain
   * io.kadai.task.api.TaskService#forceRequestReview(String)}.
   *
   * <p>This SPI is executed within the same transaction staple as {@linkplain
   * io.kadai.task.api.TaskService#requestReview(String)}.
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in {@linkplain
   * io.kadai.task.api.TaskService#requestReview(String)}.
   *
   * @param task the {@linkplain Task} after {@linkplain
   *     io.kadai.task.api.TaskService#requestReview(String)} or {@linkplain
   *     io.kadai.task.api.TaskService#forceRequestReview(String)} has completed
   * @param workbasketId the workbasketId the {@linkplain Task} should be moved to
   * @param ownerId the ownerId the {@linkplain Task} should be assigned to
   * @return the modified {@linkplain Task}. <b>IMPORTANT:</b> persistent changes to the {@linkplain
   *     Task} have to be managed by the service provider
   * @throws Exception if the service provider throws any exception
   */
  default Task afterRequestReview(Task task, String workbasketId, String ownerId) throws Exception {
    return afterRequestReview(task);
  }
}
