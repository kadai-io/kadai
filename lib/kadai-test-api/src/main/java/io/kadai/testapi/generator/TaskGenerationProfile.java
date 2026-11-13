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

package io.kadai.testapi.generator;

/**
 * Shapes the generated data like the long-running production system of an insurer.
 *
 * <p>Without a profile the generator keeps its original shape (almost all tasks open, a few months
 * of history, no attachments), which the GMT measurements rely on. With a profile:
 *
 * <ul>
 *   <li>only {@code openTaskRatio} of the tasks are open; most of the others are completed,
 *   <li>open tasks are young (a week on average, a few stay open for months), while finished tasks
 *       span the history that a steady inflow of tasks leaves behind,
 *   <li>bootstrapped classifications have service levels of 2 to 15 working days, so only a part of
 *       the open tasks is overdue,
 *   <li>no task is prioritized manually, so the priority job has to recalculate every open task,
 *   <li>most tasks carry scanned documents as attachments and references to related business
 *       objects such as the customer, the policy or the claim,
 *   <li>each domain has at least {@code workbasketsPerDomain} workbaskets, mostly personal ones
 *       owned by distinct clerks.
 * </ul>
 *
 * @param openTaskRatio share of the tasks in a non-final state, in {@code (0, 1]}
 * @param workbasketsPerDomain minimum number of workbaskets per domain; missing ones are created
 */
public record TaskGenerationProfile(double openTaskRatio, int workbasketsPerDomain) {

  public TaskGenerationProfile {
    if (!(openTaskRatio > 0 && openTaskRatio <= 1)) {
      throw new IllegalArgumentException("openTaskRatio must be in (0, 1], was " + openTaskRatio);
    }
    if (workbasketsPerDomain < 0) {
      throw new IllegalArgumentException(
          "workbasketsPerDomain must not be negative, was " + workbasketsPerDomain);
    }
  }
}
