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
import io.kadai.workbasket.api.models.Workbasket;
import java.util.List;
import java.util.Map;

/**
 * The {@code TaskDistributionProvider} interface defines a Service Provider Interface for
 * implementing custom {@linkplain Task} distribution strategies within the {@linkplain
 * KadaiEngine}.
 *
 * <p>This interface allows the system to distribute {@linkplain Task}s from a source to one or more
 * destination {@linkplain Workbasket}s based on a given strategy and additional context-specific
 * information.
 *
 * <p>The implementation of this interface must be registered as a service provider and will be
 * called by KADAI during task distribution operations.
 */
public interface TaskDistributionProvider {

  /**
   * Initializes the {@linkplain KadaiEngine} for the current KADAI installation.
   *
   * <p>This method is called during KADAI startup and allows the service provider to access and
   * store the active {@linkplain KadaiEngine} instance for later use during task distribution.
   *
   * @param kadaiEngine the active {@linkplain KadaiEngine} instance initialized for this
   *     installation
   */
  void initialize(KadaiEngine kadaiEngine);

  /**
   * Determines the distribution of tasks to one or more destination workbaskets based on the
   * provided parameters.
   *
   * <p>This method is invoked by KADAI to calculate the assignment of a set of task IDs to specific
   * destination workbaskets using a custom distribution strategy. The method does not directly
   * perform the distribution but returns the intended mapping of tasks to workbaskets.
   *
   * <p><b>Input Parameters:</b>
   *
   * <ul>
   *   <li>{@code taskIds (List<String>)}: A list of task IDs to be analyzed for distribution.
   *   <li>{@code destinationWorkbasketIds (List<String>)}: A list of destination workbasket IDs
   *       where the tasks are intended to be assigned.
   *   <li>{@code additionalInformation (Map<String, Object>)}: Additional context-specific details
   *       that can influence the distribution logic.
   * </ul>
   *
   * <p><b>Output:</b>
   *
   * <ul>
   *   <li>The method returns a {@code Map<String, List<String>>}, where each key represents a
   *       destination workbasket ID, and the corresponding value is a list of task IDs that should
   *       be assigned to that workbasket.
   *   <li>The returned mapping provides the intended distribution but does not execute any changes
   *       in the system.
   * </ul>
   *
   * <p><b>Contract:</b>
   *
   * <ul>
   *   <li>The {@code taskIds} and {@code destinationWorkbasketIds} must not be null.
   * </ul>
   *
   * @param taskIds a list of task IDs to be analyzed for distribution
   * @param destinationWorkbasketIds a list of destination workbasket IDs where tasks are intended
   *     to be distributed
   * @param additionalInformation a map of additional details for customizing the distribution logic
   * @return a {@code Map<String, List<String>>} containing the destination workbasket IDs as keys
   *     and the corresponding task IDs to be assigned as values
   */
  Map<String, List<String>> distributeTasks(
      List<String> taskIds,
      List<String> destinationWorkbasketIds,
      Map<String, Object> additionalInformation);
}
