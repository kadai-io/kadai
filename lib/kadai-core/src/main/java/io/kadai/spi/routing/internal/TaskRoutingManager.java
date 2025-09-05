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

package io.kadai.spi.routing.internal;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.internal.util.CheckedFunction;
import io.kadai.common.internal.util.LogSanitizer;
import io.kadai.common.internal.util.SpiLoader;
import io.kadai.spi.routing.api.RoutingTarget;
import io.kadai.spi.routing.api.TaskRoutingProvider;
import io.kadai.task.api.models.Task;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads TaskRoutingProvider SPI implementation(s) and passes requests to determine workbasketids to
 * them.
 */
public final class TaskRoutingManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskRoutingManager.class);
  private final List<TaskRoutingProvider> taskRoutingProviders;

  public TaskRoutingManager(KadaiEngine kadaiEngine) {
    taskRoutingProviders = SpiLoader.load(TaskRoutingProvider.class);
    for (TaskRoutingProvider taskRoutingProvider : taskRoutingProviders) {
      taskRoutingProvider.initialize(kadaiEngine);
      LOGGER.info("Registered TaskRouter provider: {}", taskRoutingProvider.getClass().getName());
    }

    if (taskRoutingProviders.isEmpty()) {
      LOGGER.info("No TaskRouter provider found. Running without Task routing.");
    }
  }

  /**
   * Determines a workbasket id for a given task. Algorithm: The task that needs a workbasket id is
   * passed to all registered TaskRoutingProviders. If they return no or more than one workbasketId,
   * null is returned, otherwise we return the workbasketId that was returned from the
   * TaskRoutingProviders.
   *
   * @param task the task for which a workbasketId is to be determined.
   * @return the id of the workbasket in which the task is to be created.
   */
  public String determineWorkbasketId(Task task) {
    String workbasketId = null;
    if (isEnabled()) {
      Set<String> workbasketIds =
          taskRoutingProviders.stream()
              .map(
                  CheckedFunction.wrapping(
                      taskRoutingProvider -> taskRoutingProvider.determineWorkbasketId(task)))
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      if (workbasketIds.isEmpty()) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(
              "No TaskRouter determined a workbasket for task {}.",
              LogSanitizer.stripLineBreakingChars(task));
        }
      } else if (workbasketIds.size() > 1) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(
              "The TaskRouters determined more than one workbasket for task {}",
              LogSanitizer.stripLineBreakingChars(task));
        }
      } else {
        workbasketId = workbasketIds.iterator().next();
      }
    }
    return workbasketId;
  }

  /**
   * Determines a {@linkplain RoutingTarget} for a given {@linkplain Task}.
   * Algorithm: see {@linkplain TaskRoutingManager#determineWorkbasketId(Task)}.
   *
   * @param task the {@linkplain Task} for which a {@linkplain RoutingTarget} is to be determined
   * @return the {@linkplain RoutingTarget} for the creation of the {@linkplain Task}
   */
  public Optional<RoutingTarget> determineRoutingTarget(Task task) {
    RoutingTarget routingTarget = null;
    if (isEnabled()) {
      Set<RoutingTarget> routingTargets =
          taskRoutingProviders.stream()
              .map(
                  CheckedFunction.wrapping(
                      taskRoutingProvider -> taskRoutingProvider.determineRoutingTarget(task)))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(Collectors.toSet());
      if (routingTargets.isEmpty()) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(
              "No TaskRouter determined a workbasket for task {}.",
              LogSanitizer.stripLineBreakingChars(task));
        }
      } else if (routingTargets.size() > 1) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(
              "The TaskRouters determined more than one workbasket for task {}",
              LogSanitizer.stripLineBreakingChars(task));
        }
      } else {
        routingTarget = routingTargets.iterator().next();
      }
    }
    return Optional.ofNullable(routingTarget);
  }

  public boolean isEnabled() {
    return !taskRoutingProviders.isEmpty();
  }
}
