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

package io.kadai.task.internal;

import static java.util.Map.entry;

import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.util.EnumUtil;
import io.kadai.common.internal.util.IdGenerator;
import io.kadai.common.internal.util.ObjectAttributeChangeDetector;
import io.kadai.spi.history.api.events.task.TaskTransferredEvent;
import io.kadai.spi.history.internal.HistoryEventManager;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.task.internal.models.TaskImpl;
import io.kadai.task.internal.models.TaskSummaryImpl;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.internal.WorkbasketQueryImpl;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** This class is responsible for the transfer of Tasks to another Workbasket. */
final class TaskTransferrer {

  private final InternalKadaiEngine kadaiEngine;
  private final WorkbasketService workbasketService;
  private final TaskServiceImpl taskService;
  private final TaskMapper taskMapper;
  private final HistoryEventManager historyEventManager;

  TaskTransferrer(
      InternalKadaiEngine kadaiEngine, TaskMapper taskMapper, TaskServiceImpl taskService) {
    this.kadaiEngine = kadaiEngine;
    this.taskService = taskService;
    this.taskMapper = taskMapper;
    this.workbasketService = kadaiEngine.getEngine().getWorkbasketService();
    this.historyEventManager = kadaiEngine.getHistoryEventManager();
  }

  Task transfer(String taskId, String destinationWorkbasketId, boolean setTransferFlag)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    WorkbasketSummary destinationWorkbasket =
        workbasketService.getWorkbasket(destinationWorkbasketId).asSummary();
    return transferSingleTask(taskId, destinationWorkbasket, null, setTransferFlag);
  }

  Task transfer(
      String taskId,
      String destinationWorkbasketKey,
      String destinationDomain,
      boolean setTransferFlag)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    WorkbasketSummary destinationWorkbasket =
        workbasketService.getWorkbasket(destinationWorkbasketKey, destinationDomain).asSummary();
    return transferSingleTask(taskId, destinationWorkbasket, null, setTransferFlag);
  }

  BulkOperationResults<String, KadaiException> transfer(
      List<String> taskIds, String destinationWorkbasketId, boolean setTransferFlag)
      throws WorkbasketNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException {
    WorkbasketSummary destinationWorkbasket =
        workbasketService.getWorkbasket(destinationWorkbasketId).asSummary();
    checkDestinationWorkbasket(destinationWorkbasket);

    return transferMultipleTasks(taskIds, destinationWorkbasket, null, setTransferFlag);
  }

  BulkOperationResults<String, KadaiException> transfer(
      List<String> taskIds,
      String destinationWorkbasketKey,
      String destinationDomain,
      boolean setTransferFlag)
      throws WorkbasketNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException {
    WorkbasketSummary destinationWorkbasket =
        workbasketService.getWorkbasket(destinationWorkbasketKey, destinationDomain).asSummary();
    checkDestinationWorkbasket(destinationWorkbasket);

    return transferMultipleTasks(taskIds, destinationWorkbasket, null, setTransferFlag);
  }

  BulkOperationResults<String, KadaiException> transferWithOwner(
      List<String> taskIds, String destinationWorkbasketId, String owner, boolean setTransferFlag)
      throws WorkbasketNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException {
    WorkbasketSummary destinationWorkbasket =
        workbasketService.getWorkbasket(destinationWorkbasketId).asSummary();
    checkDestinationWorkbasket(destinationWorkbasket);

    return transferMultipleTasks(taskIds, destinationWorkbasket, owner, setTransferFlag);
  }

  BulkOperationResults<String, KadaiException> transferWithOwner(
      List<String> taskIds,
      String destinationWorkbasketKey,
      String destinationDomain,
      String owner,
      boolean setTransferFlag)
      throws WorkbasketNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException {
    WorkbasketSummary destinationWorkbasket =
        workbasketService.getWorkbasket(destinationWorkbasketKey, destinationDomain).asSummary();
    checkDestinationWorkbasket(destinationWorkbasket);

    return transferMultipleTasks(taskIds, destinationWorkbasket, owner, setTransferFlag);
  }

  Task transferWithOwner(
      String taskId, String destinationWorkbasketId, String owner, boolean setTransferFlag)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    WorkbasketSummary destinationWorkbasket =
        workbasketService.getWorkbasket(destinationWorkbasketId).asSummary();
    return transferSingleTask(taskId, destinationWorkbasket, owner, setTransferFlag);
  }

  Task transferWithOwner(
      String taskId,
      String destinationWorkbasketKey,
      String destinationDomain,
      String owner,
      boolean setTransferFlag)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    WorkbasketSummary destinationWorkbasket =
        workbasketService.getWorkbasket(destinationWorkbasketKey, destinationDomain).asSummary();
    return transferSingleTask(taskId, destinationWorkbasket, owner, setTransferFlag);
  }

  private Task transferSingleTask(
      String taskId, WorkbasketSummary destinationWorkbasket, String owner, boolean setTransferFlag)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    try {
      kadaiEngine.openConnection();
      TaskImpl task = (TaskImpl) taskService.getTask(taskId);
      TaskImpl oldTask = task.copy();
      oldTask.setId(task.getId());
      oldTask.setExternalId(task.getExternalId());

      WorkbasketSummary originWorkbasket = task.getWorkbasketSummary();
      checkPreconditionsForTransferTask(task, destinationWorkbasket, originWorkbasket);

      applyTransferValuesForTask(task, destinationWorkbasket, owner, setTransferFlag);
      taskMapper.update(task);
      if (historyEventManager.isEnabled()) {
        createTransferredEvent(
            oldTask, task, originWorkbasket.getId(), destinationWorkbasket.getId());
      }

      return task;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  private BulkOperationResults<String, KadaiException> transferMultipleTasks(
      List<String> taskToBeTransferred,
      WorkbasketSummary destinationWorkbasket,
      String owner,
      boolean setTransferFlag)
      throws InvalidArgumentException {
    if (taskToBeTransferred == null || taskToBeTransferred.isEmpty()) {
      throw new InvalidArgumentException("TaskIds must not be null or empty.");
    }
    BulkOperationResults<String, KadaiException> bulkLog = new BulkOperationResults<>();
    List<String> taskIds = new ArrayList<>(taskToBeTransferred);

    try {
      kadaiEngine.openConnection();

      List<TaskSummary> taskSummaries =
          kadaiEngine
              .getEngine()
              .runAsAdmin(
                  () -> taskService.createTaskQuery().idIn(taskIds.toArray(new String[0])).list());
      taskSummaries =
          filterOutTasksWhichDoNotMatchTransferCriteria(taskIds, taskSummaries, bulkLog);
      updateTransferableTasks(taskSummaries, destinationWorkbasket, owner, setTransferFlag);

      return bulkLog;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  private void checkPreconditionsForTransferTask(
      Task task, WorkbasketSummary destinationWorkbasket, WorkbasketSummary originWorkbasket)
      throws WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    if (task.getState().isEndState()) {
      throw new InvalidTaskStateException(
          task.getId(), task.getState(), EnumUtil.allValuesExceptFor(TaskState.END_STATES));
    }
    workbasketService.checkAuthorization(originWorkbasket.getId(), WorkbasketPermission.TRANSFER);
    checkDestinationWorkbasket(destinationWorkbasket);
  }

  private void checkDestinationWorkbasket(WorkbasketSummary destinationWorkbasket)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException {
    workbasketService.checkAuthorization(
        destinationWorkbasket.getId(), WorkbasketPermission.APPEND);

    if (destinationWorkbasket.isMarkedForDeletion()) {
      throw new WorkbasketNotFoundException(destinationWorkbasket.getId());
    }
  }

  private List<TaskSummary> filterOutTasksWhichDoNotMatchTransferCriteria(
      List<String> taskIds,
      List<TaskSummary> taskSummaries,
      BulkOperationResults<String, KadaiException> bulkLog) {

    Map<String, TaskSummary> taskIdToTaskSummary =
        taskSummaries.stream().collect(Collectors.toMap(TaskSummary::getId, Function.identity()));

    Set<String> workbasketIds = getSourceWorkbasketIdsWithTransferPermission(taskSummaries);

    List<TaskSummary> filteredOutTasks = new ArrayList<>(taskIds.size());

    for (String taskId : new HashSet<>(taskIds)) {
      TaskSummary taskSummary = taskIdToTaskSummary.get(taskId);
      Optional<KadaiException> error =
          checkTaskForTransferCriteria(workbasketIds, taskId, taskSummary);
      if (error.isPresent()) {
        bulkLog.addError(taskId, error.get());
      } else {
        filteredOutTasks.add(taskSummary);
      }
    }
    return filteredOutTasks;
  }

  private Optional<KadaiException> checkTaskForTransferCriteria(
      Set<String> sourceWorkbasketIds, String taskId, TaskSummary taskSummary) {
    KadaiException error = null;
    if (taskId == null || taskId.isEmpty()) {
      error = new TaskNotFoundException(null);
    } else if (taskSummary == null) {
      error = new TaskNotFoundException(taskId);
    } else if (taskSummary.getState().isEndState()) {
      error =
          new InvalidTaskStateException(
              taskId, taskSummary.getState(), EnumUtil.allValuesExceptFor(TaskState.END_STATES));
    } else if (!sourceWorkbasketIds.contains(taskSummary.getWorkbasketSummary().getId())) {
      error =
          new NotAuthorizedOnWorkbasketException(
              kadaiEngine.getEngine().getCurrentUserContext().getUserid(),
              taskSummary.getWorkbasketSummary().getId(),
              WorkbasketPermission.TRANSFER);
    }
    return Optional.ofNullable(error);
  }

  private Set<String> getSourceWorkbasketIdsWithTransferPermission(
      List<TaskSummary> taskSummaries) {
    if (taskSummaries.isEmpty()) {
      return Collections.emptySet();
    }

    WorkbasketQueryImpl query = (WorkbasketQueryImpl) workbasketService.createWorkbasketQuery();
    query.setUsedToAugmentTasks(true);
    String[] workbasketIds =
        taskSummaries.stream()
            .map(TaskSummary::getWorkbasketSummary)
            .map(WorkbasketSummary::getId)
            .distinct()
            .toArray(String[]::new);

    List<WorkbasketSummary> sourceWorkbaskets =
        query.callerHasPermissions(WorkbasketPermission.TRANSFER).idIn(workbasketIds).list();
    return sourceWorkbaskets.stream().map(WorkbasketSummary::getId).collect(Collectors.toSet());
  }

  private void updateTransferableTasks(
      List<TaskSummary> taskSummaries,
      WorkbasketSummary destinationWorkbasket,
      String owner,
      boolean setTransferFlag) {
    Map<TaskState, List<TaskSummary>> summariesByState = groupTasksByState(taskSummaries);
    for (Map.Entry<TaskState, List<TaskSummary>> entry : summariesByState.entrySet()) {
      TaskState goalState = entry.getKey();
      List<TaskSummary> taskSummariesWithSameGoalState = entry.getValue();
      if (!taskSummariesWithSameGoalState.isEmpty()) {
        TaskImpl updateObject = new TaskImpl();
        updateObject.setState(goalState);
        applyTransferValuesForTask(updateObject, destinationWorkbasket, owner, setTransferFlag);
        taskMapper.updateTransfered(
            taskSummariesWithSameGoalState.stream()
                .map(TaskSummary::getId)
                .collect(Collectors.toSet()),
            updateObject);

        if (historyEventManager.isEnabled()) {
          taskSummaries.forEach(
              oldSummary -> {
                TaskSummaryImpl newSummary = (TaskSummaryImpl) oldSummary.copy();
                newSummary.setId(oldSummary.getId());
                newSummary.setExternalId(oldSummary.getExternalId());
                applyTransferValuesForTask(
                    newSummary, destinationWorkbasket, owner, setTransferFlag);

                createTransferredEvent(
                    oldSummary,
                    newSummary,
                    oldSummary.getWorkbasketSummary().getId(),
                    newSummary.getWorkbasketSummary().getId());
              });
        }
      }
    }
  }

  private void applyTransferValuesForTask(
      TaskSummaryImpl task, WorkbasketSummary workbasket, String owner, boolean setTransferFlag) {
    task.setRead(false);
    task.setTransferred(setTransferFlag);
    task.setState(getStateAfterTransfer(task));
    task.setOwner(owner);
    task.setWorkbasketSummary(workbasket);
    task.setDomain(workbasket.getDomain());
    task.setModified(Instant.now());
  }

  private void createTransferredEvent(
      TaskSummary oldTask,
      TaskSummary newTask,
      String originWorkbasketId,
      String destinationWorkbasketId) {
    String details = ObjectAttributeChangeDetector.determineChangesInAttributes(oldTask, newTask);
    historyEventManager.createEvent(
        new TaskTransferredEvent(
            IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
            newTask,
            originWorkbasketId,
            destinationWorkbasketId,
            kadaiEngine.getEngine().getCurrentUserContext().getUserid(),
            details));
  }

  private TaskState getStateAfterTransfer(TaskSummary taskSummary) {
    TaskState stateBeforeTransfer = taskSummary.getState();
    if (stateBeforeTransfer.equals(TaskState.CLAIMED)) {
      return TaskState.READY;
    }
    if (stateBeforeTransfer.equals(TaskState.IN_REVIEW)) {
      return TaskState.READY_FOR_REVIEW;
    } else {
      return stateBeforeTransfer;
    }
  }

  private Map<TaskState, List<TaskSummary>> groupTasksByState(List<TaskSummary> taskSummaries) {
    Map<TaskState, List<TaskSummary>> result =
        Map.ofEntries(
            entry((TaskState.READY), new ArrayList<>()),
            entry((TaskState.READY_FOR_REVIEW), new ArrayList<>()));
    for (TaskSummary taskSummary : taskSummaries) {
      List<TaskSummary> relevantSummaries = result.get(getStateAfterTransfer(taskSummary));
      relevantSummaries.add(taskSummary);
    }
    return result;
  }
}
