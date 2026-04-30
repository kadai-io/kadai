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

package io.kadai.spi.routing.api;

import io.kadai.common.api.KadaiInitializable;
import io.kadai.task.api.models.Task;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.Optional;

/**
 * The TaskRoutingProvider allows to determine the {@linkplain Workbasket} for a {@linkplain Task}
 * that has no {@linkplain Workbasket} on {@linkplain io.kadai.task.api.TaskService#createTask(Task)
 * creation}.
 */
public interface TaskRoutingProvider extends KadaiInitializable {

  /**
   * Determine the {@linkplain Workbasket#getId() id} of the {@linkplain Workbasket} for a given
   * {@linkplain Task}.This method will be invoked by KADAI when it is asked to {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task) create} a {@linkplain Task} that has no
   * {@linkplain Workbasket} assigned.
   *
   * <p>If more than one TaskRoutingProvider class is registered, KADAI calls them all and uses
   * their results only if they agree on the {@linkplain Workbasket}. This is, if more than one
   * {@linkplain Workbasket#getId() ids} are returned, KADAI uses them only if they are identical.
   * If different ids are returned, the {@linkplain Task} will not be {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task) created}.
   *
   * <p>If the {@linkplain Workbasket} cannot be computed, the method should return NULL. If every
   * registered TaskRoutingProvider return NULL, the {@linkplain Task} will not be {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task) created}
   *
   * <p>The behaviour is undefined if this method tries to apply persistent changes to any entity.
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task)}.
   *
   * @param task the {@linkplain Task} for which a {@linkplain Workbasket} must be determined
   * @return the {@linkplain Workbasket#getId() id} of the {@linkplain Workbasket}
   */
  default String determineWorkbasketId(Task task) {
    return null;
  }

  /**
   * This method is used instead of {@linkplain TaskRoutingProvider#determineWorkbasketId(Task)} if
   * the parameter kadai.routing.includeOwner is set to true. Use this if you want to initialize
   * the owner during Routing.
   *
   * <p>Determine the {@linkplain RoutingTarget} for a given {@linkplain Task}.This method will be
   * invoked by KADAI when it is asked to {@linkplain io.kadai.task.api.TaskService#createTask(Task)
   * create} a {@linkplain Task} that has no {@linkplain Workbasket} assigned.
   *
   * <p>If more than one TaskRoutingProvider class is registered, KADAI calls them all and uses
   * their results only if they agree on the {@linkplain RoutingTarget}. This is, if more than one
   * {@linkplain RoutingTarget RoutingTargets} are returned, KADAI uses them only if they are
   * identical.  If different RoutingTargets are returned, the {@linkplain Task} will not be
   * {@linkplain io.kadai.task.api.TaskService#createTask(Task) created}.
   *
   * <p>If the {@linkplain RoutingTarget} cannot be computed, the method should return an empty
   * {@linkplain RoutingTarget}. If every registered TaskRoutingProvider return an empty
   * {@linkplain RoutingTarget}, the {@linkplain Task} will not be {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task) created}
   *
   * <p>If the owner of the returned {@linkplain RoutingTarget} is null, then the owner of
   * {@linkplain Task} is kept. Else, the owner of {@linkplain Task} is overwritten by the owner
   * of the returned RoutingTarget.
   *
   * <p>The behaviour is undefined if this method tries to apply persistent changes to any entity.
   *
   * <p>This SPI is executed with the same {@linkplain io.kadai.common.api.security.UserPrincipal}
   * and {@linkplain io.kadai.common.api.security.GroupPrincipal} as in {@linkplain
   * io.kadai.task.api.TaskService#createTask(Task)}.
   *
   * @param task the {@linkplain Task} for which a {@linkplain RoutingTarget} must be determined
   * @return the {@linkplain RoutingTarget}
   */
  default Optional<RoutingTarget> determineRoutingTarget(Task task) {
    return Optional.empty();
  }
}
