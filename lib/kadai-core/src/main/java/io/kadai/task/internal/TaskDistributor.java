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

package io.kadai.task.internal;

import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.util.Pair;
import io.kadai.spi.task.api.TaskDistributionProvider;
import io.kadai.spi.task.internal.TaskDistributionManager;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is responsible for the distribution of {@linkplain Task}s from a {@linkplain
 * Workbasket} to another {@linkplain Workbasket} based on a DistributionStrategy implemented by a
 * {@linkplain TaskDistributionProvider}.
 */
public class TaskDistributor {

  private final InternalKadaiEngine kadaiEngine;
  private final WorkbasketService workbasketService;
  private final TaskServiceImpl taskService;
  private final TaskDistributionManager taskDistributionManager;

  public TaskDistributor(InternalKadaiEngine kadaiEngine, TaskServiceImpl taskService) {
    this.kadaiEngine = kadaiEngine;
    this.taskService = taskService;
    this.workbasketService = kadaiEngine.getEngine().getWorkbasketService();
    this.taskDistributionManager = kadaiEngine.getTaskDistributionManager();
  }

  public BulkOperationResults<String, KadaiException> distribute(
      String sourceWorkbasketId,
      List<String> taskIds,
      List<String> destinationWorkbasketIds,
      String distributionStrategyName,
      Map<String, Object> additionalInformation)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {

    return distributeTasks(
        sourceWorkbasketId,
        taskIds,
        distributionStrategyName,
        destinationWorkbasketIds,
        additionalInformation);
  }

  public BulkOperationResults<String, KadaiException> distribute(
      String sourceWorkbasketId,
      List<String> destinationWorkbasketIds,
      String distributionStrategyName,
      Map<String, Object> additionalInformation)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {

    return distributeTasks(
        sourceWorkbasketId,
        null,
        distributionStrategyName,
        destinationWorkbasketIds,
        additionalInformation);
  }

  public BulkOperationResults<String, KadaiException> distribute(String sourceWorkbasketId)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {

    return distributeTasks(sourceWorkbasketId, null, null, null, null);
  }

  public BulkOperationResults<String, KadaiException> distribute(
      String sourceWorkbasketId, List<String> taskIds)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {

    return distributeTasks(sourceWorkbasketId, taskIds, null, null, null);
  }

  public BulkOperationResults<String, KadaiException> distributeWithStrategy(
      String sourceWorkbasketId,
      List<String> taskIds,
      String distributionStrategyName,
      Map<String, Object> additionalInformation)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {

    return distributeTasks(
        sourceWorkbasketId, taskIds, distributionStrategyName, null, additionalInformation);
  }

  public BulkOperationResults<String, KadaiException> distributeWithStrategy(
      String sourceWorkbasketId,
      String distributionStrategyName,
      Map<String, Object> additionalInformation)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {

    return distributeTasks(
        sourceWorkbasketId, null, distributionStrategyName, null, additionalInformation);
  }

  public BulkOperationResults<String, KadaiException> distributeWithDestinations(
      String sourceWorkbasketId, List<String> destinationWorkbasketIds)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {

    return distributeTasks(sourceWorkbasketId, null, null, destinationWorkbasketIds, null);
  }

  public BulkOperationResults<String, KadaiException> distributeWithDestinations(
      String sourceWorkbasketId, List<String> taskIds, List<String> destinationWorkbasketIds)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {

    return distributeTasks(sourceWorkbasketId, taskIds, null, destinationWorkbasketIds, null);
  }

  private BulkOperationResults<String, KadaiException> distributeTasks(
      String sourceWorkbasketId,
      List<String> taskIds,
      String distributionStrategyName,
      List<String> destinationWorkbasketIds,
      Map<String, Object> additionalInformation)
      throws InvalidArgumentException,
          NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException {

    try {
      kadaiEngine.openConnection();

      workbasketService.checkAuthorization(sourceWorkbasketId, WorkbasketPermission.DISTRIBUTE);

      BulkOperationResults<String, KadaiException> operationResults = new BulkOperationResults<>();
      Pair<List<String>, BulkOperationResults<String, KadaiException>> result =
          resolveTaskIds(sourceWorkbasketId, taskIds);
      taskIds = result.getLeft();
      operationResults.addAllErrors(result.getRight());

      if (taskIds.isEmpty()) {
        return operationResults;
      }

      destinationWorkbasketIds =
          resolveDestinationWorkbasketIds(sourceWorkbasketId, destinationWorkbasketIds);

      Map<String, List<String>> newTaskDistribution =
          taskDistributionManager.distributeTasks(
              taskIds, destinationWorkbasketIds, additionalInformation, distributionStrategyName);

      Map<String, Boolean> taskTransferFlags = getTaskTransferFlags(taskIds);

      BulkOperationResults<String, KadaiException> operationResultsFromTransfer;
      operationResultsFromTransfer = transferTasks(newTaskDistribution, taskTransferFlags);

      operationResults.addAllErrors(operationResultsFromTransfer);

      return operationResults;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  private Pair<List<String>, BulkOperationResults<String, KadaiException>> resolveTaskIds(
      String sourceWorkbasketId, List<String> taskIds) {

    BulkOperationResults<String, KadaiException> operationResults = new BulkOperationResults<>();

    if (taskIds == null) {
      return Pair.of(getTaskIdsFromWorkbasket(sourceWorkbasketId), operationResults);
    } else {
      checkIfTasksInSameWorkbasket(taskIds);

      List<String> existingTaskIds =
          taskService.createTaskQuery().idIn(taskIds.toArray(new String[0])).list().stream()
              .map(TaskSummary::getId)
              .toList();

      List<String> validTaskIds = new ArrayList<>();
      for (String taskId : taskIds) {
        if (existingTaskIds.contains(taskId)) {
          validTaskIds.add(taskId);
        } else {
          operationResults.addError(taskId, new TaskNotFoundException(taskId));
        }
      }

      return Pair.of(validTaskIds, operationResults);
    }
  }

  private List<String> resolveDestinationWorkbasketIds(
      String sourceWorkbasketId, List<String> destinationWorkbasketIds)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException {
    if (destinationWorkbasketIds == null) {
      return getDestinationWorkbasketIds(sourceWorkbasketId);
    }
    checkIfDestinationWorkbasketIdsExist(destinationWorkbasketIds);
    return destinationWorkbasketIds;
  }

  private Map<String, Boolean> getTaskTransferFlags(List<String> taskIds) {
    return taskService.createTaskQuery().idIn(taskIds.toArray(new String[0])).list().stream()
        .collect(Collectors.toMap(TaskSummary::getId, TaskSummary::isTransferred));
  }

  private BulkOperationResults<String, KadaiException> transferTasks(
      Map<String, List<String>> newTaskDistribution, Map<String, Boolean> taskTransferFlags) {
    BulkOperationResults<String, KadaiException> operationResults = new BulkOperationResults<>();
    newTaskDistribution.forEach(
        (newDestinationWorkbasketId, taskIdsForDestination) -> {
          Map<Boolean, List<String>> partitionedTaskIds =
              taskIdsForDestination.stream()
                  .collect(Collectors.partitioningBy(taskTransferFlags::get));

          List<String> transferredTaskIds = partitionedTaskIds.getOrDefault(true, List.of());
          List<String> notTransferredTaskIds = partitionedTaskIds.getOrDefault(false, List.of());

          try {
            if (!transferredTaskIds.isEmpty()) {
              BulkOperationResults<String, KadaiException> transferResults =
                  taskService.transferTasksWithOwner(
                      newDestinationWorkbasketId, transferredTaskIds, null, true);
              transferResults.getErrorMap().forEach(operationResults::addError);
            }
            if (!notTransferredTaskIds.isEmpty()) {
              BulkOperationResults<String, KadaiException> transferResults =
                  taskService.transferTasksWithOwner(
                      newDestinationWorkbasketId, notTransferredTaskIds, null, false);
              transferResults.getErrorMap().forEach(operationResults::addError);
            }
          } catch (NotAuthorizedOnWorkbasketException | WorkbasketNotFoundException e) {
            taskIdsForDestination.forEach(taskId -> operationResults.addError(taskId, e));
          }
        });
    return operationResults;
  }

  List<String> getTaskIdsFromWorkbasket(String sourceWorkbasketId) {
    return kadaiEngine
        .getEngine()
        .runAsAdmin(() -> taskService.createTaskQuery().workbasketIdIn(sourceWorkbasketId).list())
        .stream()
        .map(TaskSummary::getId)
        .toList();
  }

  List<String> getDestinationWorkbasketIds(String sourceWorkbasketId)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {
    return workbasketService.getDistributionTargets(sourceWorkbasketId).stream()
        .map(WorkbasketSummary::getId)
        .toList();
  }

  private void checkIfTasksInSameWorkbasket(List<String> taskIds) {
    List<TaskSummary> taskSummariesToDistribute =
        taskService.createTaskQuery().idIn(taskIds.toArray(new String[0])).list();

    Set<String> workbasketIds =
        taskSummariesToDistribute.stream()
            .map(taskSummary -> taskSummary.getWorkbasketSummary().getId())
            .collect(Collectors.toSet());

    if (workbasketIds.size() > 1) {
      throw new InvalidArgumentException("Not all tasks are in the same workbasket.");
    }
  }

  private void checkIfDestinationWorkbasketIdsExist(List<String> destinationWorkbasketIds)
      throws WorkbasketNotFoundException, InvalidArgumentException {

    List<String> existingWorkbasketIds =
        workbasketService
            .createWorkbasketQuery()
            .idIn(destinationWorkbasketIds.toArray(new String[0]))
            .list()
            .stream()
            .map(WorkbasketSummary::getId)
            .toList();

    for (String workbasketId : destinationWorkbasketIds) {
      if (!existingWorkbasketIds.contains(workbasketId)) {
        throw new WorkbasketNotFoundException(workbasketId);
      }
    }
  }
}
