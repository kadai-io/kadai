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
import io.kadai.task.api.exceptions.TransferCheckException;
import io.kadai.task.api.models.Task;
import io.kadai.workbasket.api.models.Workbasket;

/**
 * The BeforeTransferTaskProvider allows to implement customized validation logic before a
 * {@linkplain Task} is transferred to a destination {@linkplain Workbasket}.
 *
 * <p>Implementations can deny the transfer by throwing a {@linkplain TransferCheckException}.
 */
public interface BeforeTransferTaskProvider {

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
   * Validate whether the transfer of a {@linkplain Task} to the given destination {@linkplain
   * Workbasket} is allowed.
   *
   * <p>This SPI is executed within the same transaction staple as the transfer operation (e.g.
   * {@linkplain io.kadai.task.api.TaskService#transfer(String, String)} or {@linkplain
   * io.kadai.task.api.TaskService#transferTasks(String, java.util.List)}).
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in the transfer operation.
   *
   * @param task the {@linkplain Task} that is about to be transferred
   * @param destinationWorkbasket the destination {@linkplain Workbasket} the {@linkplain Task} will
   *     be transferred to
   * @throws TransferCheckException if the transfer is not allowed
   */
  void checkTransferAllowed(Task task, Workbasket destinationWorkbasket)
      throws TransferCheckException;
}
