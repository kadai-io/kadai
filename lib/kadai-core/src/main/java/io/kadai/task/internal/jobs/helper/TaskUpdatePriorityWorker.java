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

package io.kadai.task.internal.jobs.helper;

import static java.util.Objects.nonNull;

import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.internal.KadaiEngineImpl;
import io.kadai.spi.priority.internal.PriorityServiceManager;
import io.kadai.task.api.TaskQueryColumnName;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.TaskSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.IntPredicate;

public class TaskUpdatePriorityWorker {

  private final SqlConnectionRunner sqlConnectionRunner;
  private final KadaiEngine kadaiEngine;
  private final PriorityServiceManager priorityServiceManager;

  public TaskUpdatePriorityWorker(KadaiEngine kadaiEngine) {
    this.kadaiEngine = kadaiEngine;
    sqlConnectionRunner = new SqlConnectionRunner(kadaiEngine);
    priorityServiceManager = ((KadaiEngineImpl) kadaiEngine).getPriorityServiceManager();
  }

  public static IntPredicate hasDifferentPriority(TaskSummary taskSummary) {
    return prio -> nonNull(taskSummary) && prio != taskSummary.getPriority();
  }

  public List<String> executeBatch(List<String> taskIds) {
    List<String> updatedTaskIds = new ArrayList<>();
    sqlConnectionRunner.runWithConnection(
        connection -> {
          TaskUpdatePriorityBatchStatement taskUpdateBatch =
              new TaskUpdatePriorityBatchStatement(connection);

          List<TaskSummary> list = getTaskSummariesByIds(taskIds);
          for (TaskSummary taskSummary : list) {
            OptionalInt calculatedPriority = getCalculatedPriority(taskSummary);
            if (calculatedPriority.isPresent()) {
              final String taskId = taskSummary.getId();
              updatedTaskIds.add(taskId);
              taskUpdateBatch.addPriorityUpdate(taskId, calculatedPriority.getAsInt());
            }
          }
          taskUpdateBatch.executeBatch();

          // don't forget to save changes
          if (!connection.getAutoCommit()) {
            connection.commit();
          }
        });

    return updatedTaskIds;
  }

  /**
   * This will return all relevant task ids. This may result in a LOT! of ids
   *
   * @return list of task ids.
   */
  public List<String> getAllRelevantTaskIds() {
    return kadaiEngine
        .getTaskService()
        .createTaskQuery()
        .stateNotIn(TaskState.END_STATES)
        .listValues(TaskQueryColumnName.ID, SortDirection.ASCENDING);
  }

  public List<TaskSummary> getTaskSummariesByIds(List<String> taskIds) {
    return kadaiEngine
        .getTaskService()
        .createTaskQuery()
        .idIn(taskIds.toArray(new String[0]))
        .list();
  }

  public OptionalInt getCalculatedPriority(TaskSummary taskSummary) {
    OptionalInt computedPriority = priorityServiceManager.calculatePriorityOfTask(taskSummary);
    if (computedPriority.isPresent()
        && hasDifferentPriority(taskSummary).test(computedPriority.getAsInt())) {
      return computedPriority;
    }
    return OptionalInt.empty();
  }
}
