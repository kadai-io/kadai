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
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.util.IdGenerator;
import io.kadai.task.api.exceptions.NotAuthorizedOnTaskCommentException;
import io.kadai.task.api.exceptions.TaskCommentNotFoundException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.TaskComment;
import io.kadai.task.internal.models.MinimalTaskSummary;
import io.kadai.task.internal.models.TaskCommentImpl;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.UserMapper;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TaskCommentServiceImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskCommentServiceImpl.class);

  private final InternalKadaiEngine kadaiEngine;
  private final TaskServiceImpl taskService;
  private final TaskCommentMapper taskCommentMapper;
  private final TaskMapper taskMapper;
  private final UserMapper userMapper;

  TaskCommentServiceImpl(
      InternalKadaiEngine kadaiEngine,
      TaskCommentMapper taskCommentMapper,
      UserMapper userMapper,
      TaskMapper taskMapper,
      TaskServiceImpl taskService) {
    this.kadaiEngine = kadaiEngine;
    this.taskService = taskService;
    this.taskCommentMapper = taskCommentMapper;
    this.userMapper = userMapper;
    this.taskMapper = taskMapper;
  }

  TaskComment newTaskComment(String taskId) {

    TaskCommentImpl taskComment = new TaskCommentImpl();
    taskComment.setTaskId(taskId);

    return taskComment;
  }

  TaskComment updateTaskComment(TaskComment taskCommentToUpdate)
      throws ConcurrencyException,
          TaskCommentNotFoundException,
          TaskNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnTaskCommentException,
          NotAuthorizedOnWorkbasketException {

    String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();

    TaskCommentImpl taskCommentImplToUpdate = (TaskCommentImpl) taskCommentToUpdate;

    try {

      kadaiEngine.openConnection();

      TaskComment originalTaskComment = getTaskComment(taskCommentImplToUpdate.getId());

      if (originalTaskComment.getCreator().equals(userId)
              && taskCommentImplToUpdate.getCreator().equals(originalTaskComment.getCreator())
          || kadaiEngine.getEngine().isUserInRole(KadaiRole.ADMIN)
          || kadaiEngine.getEngine().isUserInRole(KadaiRole.TASK_ADMIN)) {

        checkModifiedHasNotChanged(originalTaskComment, taskCommentImplToUpdate);

        taskCommentImplToUpdate.setModified(Instant.now());

        taskCommentMapper.update(taskCommentImplToUpdate);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug(
              "Method updateTaskComment() updated taskComment '{}' for user '{}'.",
              taskCommentImplToUpdate.getId(),
              userId);
        }

      } else {
        throw new NotAuthorizedOnTaskCommentException(userId, taskCommentImplToUpdate.getId());
      }
    } finally {
      kadaiEngine.returnConnection();
    }

    return taskCommentImplToUpdate;
  }

  TaskComment createTaskComment(TaskComment taskCommentToCreate)
      throws TaskNotFoundException, InvalidArgumentException, NotAuthorizedOnWorkbasketException {

    TaskCommentImpl taskCommentImplToCreate = (TaskCommentImpl) taskCommentToCreate;

    try {

      kadaiEngine.openConnection();

      taskService.getTask(taskCommentImplToCreate.getTaskId());

      validateNoneExistingTaskCommentId(taskCommentImplToCreate.getId());

      initDefaultTaskCommentValues(taskCommentImplToCreate);

      taskCommentMapper.insert(taskCommentImplToCreate);

      taskMapper.incrementNumberOfComments(taskCommentImplToCreate.getTaskId(), Instant.now());

    } finally {
      kadaiEngine.returnConnection();
    }

    return taskCommentImplToCreate;
  }

  public BulkOperationResults<String, KadaiException> createTaskCommentsBulk(
          Collection<String> taskIds, String text) throws InvalidArgumentException {

    if (taskIds == null) {
      throw new InvalidArgumentException("taskIds must not be null");
    }
    if (text == null || text.isEmpty()) {
      throw new InvalidArgumentException("text must not be null/empty");
    }

    try {
      kadaiEngine.openConnection();
      BulkOperationResults<String, KadaiException> errors = new BulkOperationResults<>();
      Instant now = Instant.now();

      Set<String> existingTaskIds = taskMapper.findExistingTasks(taskIds, null).stream()
              .map(MinimalTaskSummary::getTaskId)
              .collect(Collectors.toSet());

      taskIds.stream()
              .filter(id -> !existingTaskIds.contains(id))
              .forEach(id -> errors.addError(
                      id, new TaskNotFoundException(id)));

      List<TaskCommentImpl> toInsert = existingTaskIds.stream()
              .map(id -> {
                TaskCommentImpl comment = (TaskCommentImpl) newTaskComment(id);
                comment.setTextField(text);
                comment.setId(null);
                initDefaultTaskCommentValues(comment);
                return comment;
              })
              .toList();

      for (TaskCommentImpl comment : toInsert) {
        taskCommentMapper.insert(comment);
        taskMapper.incrementNumberOfComments(comment.getTaskId(), now);
      }
      return errors;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  void deleteTaskComment(String taskCommentId)
      throws TaskCommentNotFoundException,
          TaskNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnTaskCommentException,
          NotAuthorizedOnWorkbasketException {

    String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();

    try {

      kadaiEngine.openConnection();

      TaskComment taskCommentToDelete = getTaskComment(taskCommentId);

      if (taskCommentToDelete.getCreator().equals(userId)
          || kadaiEngine.getEngine().isUserInRole(KadaiRole.ADMIN)
          || kadaiEngine.getEngine().isUserInRole(KadaiRole.TASK_ADMIN)) {

        taskCommentMapper.delete(taskCommentId);
        taskMapper.decrementNumberOfComments(taskCommentToDelete.getTaskId(), Instant.now());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("taskComment {} deleted", taskCommentToDelete.getId());
        }

      } else {
        throw new NotAuthorizedOnTaskCommentException(userId, taskCommentToDelete.getId());
      }

    } finally {
      kadaiEngine.returnConnection();
    }
  }

  List<TaskComment> getTaskComments(String taskId)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException {

    try {

      kadaiEngine.openConnection();

      taskService.getTask(taskId);

      List<TaskComment> taskComments = taskService.createTaskCommentQuery().taskIdIn(taskId).list();

      if (taskComments.isEmpty() && LOGGER.isDebugEnabled()) {
        LOGGER.debug("getTaskComments() found no comments for the provided taskId");
      }

      return taskComments;

    } finally {
      kadaiEngine.returnConnection();
    }
  }

  TaskComment getTaskComment(String taskCommentId)
      throws TaskCommentNotFoundException,
          TaskNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException {

    TaskCommentImpl result;

    verifyTaskCommentIdIsNotNullOrEmpty(taskCommentId);

    try {

      kadaiEngine.openConnection();

      result = taskCommentMapper.findById(taskCommentId);

      if (result == null) {
        throw new TaskCommentNotFoundException(taskCommentId);
      }

      taskService.getTask(result.getTaskId());

      if (kadaiEngine.getEngine().getConfiguration().isAddAdditionalUserInfo()) {
        User creator = userMapper.findById(result.getCreator());
        if (creator != null) {
          result.setCreatorFullName(creator.getFullName());
        }
      }

      return result;

    } finally {
      kadaiEngine.returnConnection();
    }
  }

  private void checkModifiedHasNotChanged(
      TaskComment oldTaskComment, TaskComment taskCommentImplToUpdate) throws ConcurrencyException {

    if (!oldTaskComment.getModified().equals(taskCommentImplToUpdate.getModified())) {
      throw new ConcurrencyException(taskCommentImplToUpdate.getId());
    }
  }

  private void initDefaultTaskCommentValues(TaskCommentImpl taskCommentImplToCreate) {

    Instant now = Instant.now();

    taskCommentImplToCreate.setId(
        IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_COMMENT));
    taskCommentImplToCreate.setModified(now);
    taskCommentImplToCreate.setCreated(now);

    String creator = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
    if (kadaiEngine.getEngine().getConfiguration().isSecurityEnabled() && creator == null) {
      throw new SystemException(
          "KadaiSecurity is enabled, but the current UserId is"
              + " NULL while creating a TaskComment.");
    }
    taskCommentImplToCreate.setCreator(creator);
  }

  private void validateNoneExistingTaskCommentId(String taskCommentId)
      throws InvalidArgumentException {

    if (taskCommentId != null && !taskCommentId.equals("")) {
      throw new InvalidArgumentException(
          String.format(
              "taskCommentId must be null/empty for creation, but found %s", taskCommentId));
    }
  }

  private void verifyTaskCommentIdIsNotNullOrEmpty(String taskCommentId)
      throws InvalidArgumentException {

    if (taskCommentId == null || taskCommentId.isEmpty()) {
      throw new InvalidArgumentException(
          "taskCommentId must not be null/empty for retrieval/deletion");
    }
  }
}
