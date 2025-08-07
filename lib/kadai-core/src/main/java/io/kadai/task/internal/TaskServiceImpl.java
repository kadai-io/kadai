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

import static io.kadai.task.api.TaskState.CANCELLED;
import static io.kadai.task.api.TaskState.CLAIMED;
import static io.kadai.task.api.TaskState.CLAIMED_STATES;
import static io.kadai.task.api.TaskState.END_STATES;
import static io.kadai.task.api.TaskState.IN_REVIEW;
import static io.kadai.task.api.TaskState.READY;
import static io.kadai.task.api.TaskState.READY_FOR_REVIEW;
import static io.kadai.task.api.TaskState.TERMINATED;
import static java.util.function.Predicate.not;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.classification.api.models.Classification;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.KadaiRole;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.common.internal.util.CheckedConsumer;
import io.kadai.common.internal.util.CheckedFunction;
import io.kadai.common.internal.util.CheckedSupplier;
import io.kadai.common.internal.util.CollectionUtil;
import io.kadai.common.internal.util.EnumUtil;
import io.kadai.common.internal.util.IdGenerator;
import io.kadai.common.internal.util.ObjectAttributeChangeDetector;
import io.kadai.common.internal.util.Pair;
import io.kadai.spi.history.api.events.task.TaskCancelledEvent;
import io.kadai.spi.history.api.events.task.TaskClaimCancelledEvent;
import io.kadai.spi.history.api.events.task.TaskClaimedEvent;
import io.kadai.spi.history.api.events.task.TaskClaimedReviewEvent;
import io.kadai.spi.history.api.events.task.TaskCompletedEvent;
import io.kadai.spi.history.api.events.task.TaskCreatedEvent;
import io.kadai.spi.history.api.events.task.TaskDeletedEvent;
import io.kadai.spi.history.api.events.task.TaskReopenedEvent;
import io.kadai.spi.history.api.events.task.TaskRequestChangesEvent;
import io.kadai.spi.history.api.events.task.TaskRequestReviewEvent;
import io.kadai.spi.history.api.events.task.TaskTerminatedEvent;
import io.kadai.spi.history.api.events.task.TaskUpdatedEvent;
import io.kadai.spi.history.internal.HistoryEventManager;
import io.kadai.spi.priority.internal.PriorityServiceManager;
import io.kadai.spi.routing.api.RoutingTarget;
import io.kadai.spi.task.internal.AfterRequestChangesManager;
import io.kadai.spi.task.internal.AfterRequestReviewManager;
import io.kadai.spi.task.internal.BeforeRequestChangesManager;
import io.kadai.spi.task.internal.BeforeRequestReviewManager;
import io.kadai.spi.task.internal.CreateTaskPreprocessorManager;
import io.kadai.spi.task.internal.ReviewRequiredManager;
import io.kadai.spi.task.internal.TaskEndstatePreprocessorManager;
import io.kadai.task.api.CallbackState;
import io.kadai.task.api.TaskCommentQuery;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskQuery;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.exceptions.AttachmentPersistenceException;
import io.kadai.task.api.exceptions.InvalidCallbackStateException;
import io.kadai.task.api.exceptions.InvalidOwnerException;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.exceptions.NotAuthorizedOnTaskCommentException;
import io.kadai.task.api.exceptions.ObjectReferencePersistenceException;
import io.kadai.task.api.exceptions.ReopenTaskWithCallbackException;
import io.kadai.task.api.exceptions.TaskAlreadyExistException;
import io.kadai.task.api.exceptions.TaskCommentNotFoundException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.Attachment;
import io.kadai.task.api.models.AttachmentSummary;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskComment;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.task.internal.ServiceLevelHandler.BulkLog;
import io.kadai.task.internal.models.AttachmentImpl;
import io.kadai.task.internal.models.AttachmentSummaryImpl;
import io.kadai.task.internal.models.MinimalTaskSummary;
import io.kadai.task.internal.models.ObjectReferenceImpl;
import io.kadai.task.internal.models.TaskImpl;
import io.kadai.task.internal.models.TaskSummaryImpl;
import io.kadai.user.api.models.User;
import io.kadai.user.internal.UserMapper;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.internal.WorkbasketQueryImpl;
import io.kadai.workbasket.internal.models.WorkbasketSummaryImpl;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is the implementation of TaskService. */
@SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
public class TaskServiceImpl implements TaskService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

  private final InternalKadaiEngine kadaiEngine;
  private final WorkbasketService workbasketService;
  private final ClassificationService classificationService;
  private final TaskMapper taskMapper;
  private final TaskTransferrer taskTransferrer;
  private final TaskDistributor taskDistributor;
  private final TaskCommentServiceImpl taskCommentService;
  private final ServiceLevelHandler serviceLevelHandler;
  private final AttachmentHandler attachmentHandler;
  private final AttachmentMapper attachmentMapper;
  private final ObjectReferenceMapper objectReferenceMapper;
  private final ObjectReferenceHandler objectReferenceHandler;
  private final UserMapper userMapper;
  private final HistoryEventManager historyEventManager;
  private final CreateTaskPreprocessorManager createTaskPreprocessorManager;
  private final PriorityServiceManager priorityServiceManager;
  private final ReviewRequiredManager reviewRequiredManager;
  private final BeforeRequestReviewManager beforeRequestReviewManager;
  private final AfterRequestReviewManager afterRequestReviewManager;
  private final BeforeRequestChangesManager beforeRequestChangesManager;
  private final AfterRequestChangesManager afterRequestChangesManager;
  private final TaskEndstatePreprocessorManager taskEndstatePreprocessorManager;

  public TaskServiceImpl(
      InternalKadaiEngine kadaiEngine,
      TaskMapper taskMapper,
      TaskCommentMapper taskCommentMapper,
      AttachmentMapper attachmentMapper,
      ObjectReferenceMapper objectReferenceMapper,
      UserMapper userMapper) {
    this.kadaiEngine = kadaiEngine;
    this.taskMapper = taskMapper;
    this.workbasketService = kadaiEngine.getEngine().getWorkbasketService();
    this.attachmentMapper = attachmentMapper;
    this.objectReferenceMapper = objectReferenceMapper;
    this.userMapper = userMapper;
    this.classificationService = kadaiEngine.getEngine().getClassificationService();
    this.historyEventManager = kadaiEngine.getHistoryEventManager();
    this.createTaskPreprocessorManager = kadaiEngine.getCreateTaskPreprocessorManager();
    this.priorityServiceManager = kadaiEngine.getPriorityServiceManager();
    this.reviewRequiredManager = kadaiEngine.getReviewRequiredManager();
    this.beforeRequestReviewManager = kadaiEngine.getBeforeRequestReviewManager();
    this.afterRequestReviewManager = kadaiEngine.getAfterRequestReviewManager();
    this.beforeRequestChangesManager = kadaiEngine.getBeforeRequestChangesManager();
    this.afterRequestChangesManager = kadaiEngine.getAfterRequestChangesManager();
    this.taskEndstatePreprocessorManager = kadaiEngine.getTaskEndstatePreprocessorManager();
    this.taskTransferrer = new TaskTransferrer(kadaiEngine, taskMapper, this);
    this.taskDistributor = new TaskDistributor(kadaiEngine, this);
    this.taskCommentService =
        new TaskCommentServiceImpl(kadaiEngine, taskCommentMapper, userMapper, taskMapper, this);
    this.serviceLevelHandler =
        new ServiceLevelHandler(kadaiEngine, taskMapper, attachmentMapper, this);
    this.attachmentHandler = new AttachmentHandler(attachmentMapper, classificationService);
    this.objectReferenceHandler = new ObjectReferenceHandler(objectReferenceMapper);
  }

  @Override
  public List<String> updateTasks(
      ObjectReference selectionCriteria, Map<TaskCustomField, String> customFieldsToUpdate)
      throws InvalidArgumentException {

    ObjectReferenceImpl.validate(selectionCriteria, "ObjectReference", "updateTasks call");
    validateCustomFields(customFieldsToUpdate);
    TaskCustomPropertySelector fieldSelector = new TaskCustomPropertySelector();
    TaskImpl updated = initUpdatedTask(customFieldsToUpdate, fieldSelector);

    try {
      kadaiEngine.openConnection();

      // use query in order to find only those tasks that are visible to the current user
      List<TaskSummary> taskSummaries = getTasksToChange(selectionCriteria);

      List<TaskSummary> tasksWithPermissions = new ArrayList<>();
      for (TaskSummary taskSummary : taskSummaries) {
        if (checkEditTasksPerm(taskSummary)) {
          tasksWithPermissions.add(taskSummary);
        }
      }

      List<String> changedTasks = new ArrayList<>();
      if (!tasksWithPermissions.isEmpty()) {
        changedTasks = tasksWithPermissions.stream().map(TaskSummary::getId).toList();
        taskMapper.updateTasks(changedTasks, updated, fieldSelector);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("updateTasks() updated the following tasks: {} ", changedTasks);
        }

      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("updateTasks() found no tasks for update ");
        }
      }
      return changedTasks;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public Task claim(String taskId)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return claim(taskId, false);
  }

  @Override
  public Task forceClaim(String taskId)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return claim(taskId, true);
  }

  @Override
  public Task cancelClaim(String taskId)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return this.cancelClaim(taskId, false, false);
  }

  @Override
  public Task forceCancelClaim(String taskId)
      throws TaskNotFoundException, InvalidTaskStateException, NotAuthorizedOnWorkbasketException {
    try {
      return this.cancelClaim(taskId, true, false);
    } catch (InvalidOwnerException e) {
      throw new SystemException("this should not have happened. You've discovered a new bug!", e);
    }
  }

  @Override
  public Task cancelClaim(String taskId, boolean keepOwner)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return this.cancelClaim(taskId, false, keepOwner);
  }

  @Override
  public Task forceCancelClaim(String taskId, boolean keepOwner)
      throws TaskNotFoundException, InvalidTaskStateException, NotAuthorizedOnWorkbasketException {
    try {
      return this.cancelClaim(taskId, true, keepOwner);
    } catch (InvalidOwnerException e) {
      throw new SystemException("this should not have happened. You've discovered a new bug!", e);
    }
  }

  @Override
  public Task requestReview(String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    return requestReview(taskId, null, null, false);
  }

  @Override
  public Task requestReviewWithWorkbasketId(String taskId, String workbasketId, String ownerId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    if ((workbasketId == null || workbasketId.isEmpty())) {
      throw new InvalidArgumentException("WorkbasketId must not be null or empty");
    }
    return requestReview(taskId, workbasketId, ownerId, false);
  }

  @Override
  public Task forceRequestReview(String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    return requestReview(taskId, null, null, true);
  }

  @Override
  public Task requestChanges(String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    return requestChanges(taskId, null, null, false);
  }

  @Override
  public Task requestChangesWithWorkbasketId(String taskId, String workbasketId, String ownerId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    if ((workbasketId == null || workbasketId.isEmpty())) {
      throw new InvalidArgumentException("WorkbasketId must not be null or empty");
    }
    return requestChanges(taskId, workbasketId, ownerId, false);
  }

  @Override
  public Task forceRequestChanges(String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    return requestChanges(taskId, null, null, true);
  }

  @Override
  public Task completeTask(String taskId)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return completeTask(taskId, false);
  }

  @Override
  public Task forceCompleteTask(String taskId)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return completeTask(taskId, true);
  }

  @Override
  public Task createTask(Task taskToCreate)
      throws WorkbasketNotFoundException,
          ClassificationNotFoundException,
          TaskAlreadyExistException,
          InvalidArgumentException,
          AttachmentPersistenceException,
          ObjectReferencePersistenceException,
          NotAuthorizedOnWorkbasketException {

    TaskImpl task = preprocessTaskCreation(taskToCreate);

    try {
      kadaiEngine.openConnection();

      Workbasket workbasket = resolveWorkbasket(task);
      if (workbasket.isMarkedForDeletion()) {
        throw new WorkbasketNotFoundException(workbasket.getId());
      }
      task.setWorkbasketSummary(workbasket.asSummary());
      task.setDomain(workbasket.getDomain());

      if (!kadaiEngine.getEngine().isUserInRole(KadaiRole.TASK_ROUTER)) {
        workbasketService.checkAuthorization(
            task.getWorkbasketSummary().getId(), WorkbasketPermission.APPEND);
      }

      Classification classification =
          getClassificationByKeyAndDomain(task.getClassificationKey(), workbasket.getDomain());
      task.setClassificationSummary(classification.asSummary());

      ObjectReferenceImpl.validate(task.getPrimaryObjRef(), "primary ObjectReference", "Task");
      applyTaskSettingsOnTaskCreation(task, classification);

      persistCreatedTask(task);

      createTaskCreatedHistoryEvent(task);

      return task;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public Task getTask(String id) throws NotAuthorizedOnWorkbasketException, TaskNotFoundException {
    try {
      kadaiEngine.openConnection();

      TaskImpl resultTask = taskMapper.findById(id);
      if (resultTask != null) {
        WorkbasketQueryImpl query = (WorkbasketQueryImpl) workbasketService.createWorkbasketQuery();
        query.setUsedToAugmentTasks(true);
        String workbasketId = resultTask.getWorkbasketSummary().getId();
        List<WorkbasketSummary> workbaskets =
            query.idIn(workbasketId).callerHasPermissions(WorkbasketPermission.READTASKS).list();
        if (workbaskets.isEmpty()) {
          throw new NotAuthorizedOnWorkbasketException(
              kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
              workbasketId,
              WorkbasketPermission.READ,
              WorkbasketPermission.READTASKS);
        } else {
          resultTask.setWorkbasketSummary(workbaskets.get(0));
        }

        List<AttachmentImpl> attachmentImpls =
            attachmentMapper.findAttachmentsByTaskId(resultTask.getId());
        if (attachmentImpls == null) {
          attachmentImpls = new ArrayList<>();
        }
        List<ObjectReferenceImpl> secondaryObjectReferences =
            objectReferenceMapper.findObjectReferencesByTaskId(resultTask.getId());
        if (secondaryObjectReferences == null) {
          secondaryObjectReferences = new ArrayList<>();
        }
        Map<String, ClassificationSummary> classificationSummariesById =
            findClassificationForTaskImplAndAttachments(resultTask, attachmentImpls);
        addClassificationSummariesToAttachments(attachmentImpls, classificationSummariesById);
        resultTask.setAttachments(new ArrayList<>(attachmentImpls));
        resultTask.setSecondaryObjectReferences(new ArrayList<>(secondaryObjectReferences));
        String classificationId = resultTask.getClassificationSummary().getId();
        ClassificationSummary classification = classificationSummariesById.get(classificationId);
        if (classification == null) {
          throw new SystemException(
              "Could not find a Classification for task " + resultTask.getId());
        }

        resultTask.setClassificationSummary(classification);

        if (resultTask.getOwner() != null
            && !resultTask.getOwner().isEmpty()
            && kadaiEngine.getEngine().getConfiguration().isAddAdditionalUserInfo()) {
          User owner = userMapper.findById(resultTask.getOwner());
          if (owner != null) {
            resultTask.setOwnerLongName(owner.getLongName());
          }
        }
        return resultTask;
      } else {
        throw new TaskNotFoundException(id);
      }
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public Task transfer(String taskId, String destinationWorkbasketId, boolean setTransferFlag)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return taskTransferrer.transfer(taskId, destinationWorkbasketId, setTransferFlag);
  }

  @Override
  public Task transfer(String taskId, String workbasketKey, String domain, boolean setTransferFlag)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return taskTransferrer.transfer(taskId, workbasketKey, domain, setTransferFlag);
  }

  @Override
  public Task transferWithOwner(
      String taskId, String destinationWorkbasketId, String owner, boolean setTransferFlag)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return taskTransferrer.transferWithOwner(
        taskId, destinationWorkbasketId, owner, setTransferFlag);
  }

  @Override
  public Task transferWithOwner(
      String taskId, String workbasketKey, String domain, String owner, boolean setTransferFlag)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    return taskTransferrer.transferWithOwner(taskId, workbasketKey, domain, owner, setTransferFlag);
  }

  @Override
  public Task setTaskRead(String taskId, boolean isRead)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException {
    TaskImpl task;
    try {
      kadaiEngine.openConnection();
      task = (TaskImpl) getTask(taskId);
      task.setRead(isRead);
      task.setModified(Instant.now());
      taskMapper.update(task);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Method setTaskRead() set read property of Task '{}' to {} ", task, isRead);
      }
      return task;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public TaskQuery createTaskQuery() {
    return new TaskQueryImpl(kadaiEngine);
  }

  @Override
  public TaskCommentQuery createTaskCommentQuery() {
    return new TaskCommentQueryImpl(kadaiEngine);
  }

  @Override
  public Task newTask() {
    return newTask(null);
  }

  @Override
  public Task newTask(String workbasketId) {
    TaskImpl task = new TaskImpl();
    WorkbasketSummaryImpl wb = new WorkbasketSummaryImpl();
    wb.setId(workbasketId);
    task.setWorkbasketSummary(wb);
    task.setCallbackState(CallbackState.NONE);
    return task;
  }

  @Override
  public Task newTask(String workbasketKey, String domain) {
    TaskImpl task = new TaskImpl();
    WorkbasketSummaryImpl wb = new WorkbasketSummaryImpl();
    wb.setKey(workbasketKey);
    wb.setDomain(domain);
    task.setWorkbasketSummary(wb);
    return task;
  }

  @Override
  public TaskComment newTaskComment(String taskId) {
    return taskCommentService.newTaskComment(taskId);
  }

  @Override
  public Attachment newAttachment() {
    return new AttachmentImpl();
  }

  @Override
  public ObjectReference newObjectReference() {
    return new ObjectReferenceImpl();
  }

  @Override
  public ObjectReference newObjectReference(
      String company, String system, String systemInstance, String type, String value) {
    return new ObjectReferenceImpl(company, system, systemInstance, type, value);
  }

  @Override
  public Task updateTask(Task task)
      throws InvalidArgumentException,
          TaskNotFoundException,
          ConcurrencyException,
          AttachmentPersistenceException,
          ObjectReferencePersistenceException,
          ClassificationNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
    TaskImpl newTaskImpl = (TaskImpl) task;
    try {
      kadaiEngine.openConnection();
      TaskImpl oldTaskImpl = (TaskImpl) getTask(newTaskImpl.getId());

      checkConcurrencyAndSetModified(newTaskImpl, oldTaskImpl);
      if (!checkEditTasksPerm(oldTaskImpl)) {
        throw new NotAuthorizedOnWorkbasketException(
            kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
            oldTaskImpl.getWorkbasketSummary().getId(),
            WorkbasketPermission.EDITTASKS);
      }

      attachmentHandler.insertAndDeleteAttachmentsOnTaskUpdate(newTaskImpl, oldTaskImpl);
      objectReferenceHandler.insertAndDeleteObjectReferencesOnTaskUpdate(newTaskImpl, oldTaskImpl);
      ObjectReferenceImpl.validate(
          newTaskImpl.getPrimaryObjRef(), "primary ObjectReference", "Task");

      standardUpdateActions(oldTaskImpl, newTaskImpl);

      priorityServiceManager
          .calculatePriorityOfTask(newTaskImpl)
          .ifPresent(newTaskImpl::setPriority);

      taskMapper.update(newTaskImpl);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Method updateTask() updated task '{}' for user '{}'.", task.getId(), userId);
      }

      if (historyEventManager.isEnabled()) {

        String changeDetails =
            ObjectAttributeChangeDetector.determineChangesInAttributes(oldTaskImpl, newTaskImpl);

        historyEventManager.createEvent(
            new TaskUpdatedEvent(
                IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                task,
                userId,
                kadaiEngine.getEngine().getCurrentUserContext().getUserContext().getProxyAccessId(),
                changeDetails));
      }

    } finally {
      kadaiEngine.returnConnection();
    }
    return task;
  }

  @Override
  public BulkOperationResults<String, KadaiException> transferTasks(
      String destinationWorkbasketId, List<String> taskIds, boolean setTransferFlag)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException {
    return taskTransferrer.transfer(taskIds, destinationWorkbasketId, setTransferFlag);
  }

  @Override
  public BulkOperationResults<String, KadaiException> transferTasks(
      String destinationWorkbasketKey,
      String destinationWorkbasketDomain,
      List<String> taskIds,
      boolean setTransferFlag)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException {
    return taskTransferrer.transfer(
        taskIds, destinationWorkbasketKey, destinationWorkbasketDomain, setTransferFlag);
  }

  @Override
  public BulkOperationResults<String, KadaiException> transferTasksWithOwner(
      String destinationWorkbasketId, List<String> taskIds, String owner, boolean setTransferFlag)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException {
    return taskTransferrer.transferWithOwner(
        taskIds, destinationWorkbasketId, owner, setTransferFlag);
  }

  @Override
  public BulkOperationResults<String, KadaiException> transferTasksWithOwner(
      String destinationWorkbasketKey,
      String destinationWorkbasketDomain,
      List<String> taskIds,
      String owner,
      boolean setTransferFlag)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException {
    return taskTransferrer.transferWithOwner(
        taskIds, destinationWorkbasketKey, destinationWorkbasketDomain, owner, setTransferFlag);
  }

  @Override
  public Task reopen(String taskId)
      throws TaskNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException,
          ReopenTaskWithCallbackException {
    TaskImpl task;
    try {
      kadaiEngine.openConnection();
      String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
      task = (TaskImpl) getTask(taskId);
      if (!checkEditTasksPerm(task)) {
        throw new NotAuthorizedOnWorkbasketException(
            userId, task.getWorkbasketSummary().getId(), WorkbasketPermission.EDITTASKS);
      }

      final TaskImpl oldTask = duplicateTaskExactly(task);

      final TaskState[] nonFinalEndStates =
          Arrays.stream(END_STATES).filter(not(TaskState::isFinalState)).toArray(TaskState[]::new);

      if (!task.getState().in(nonFinalEndStates)) {
        throw new InvalidTaskStateException(oldTask.getId(), oldTask.getState(), nonFinalEndStates);
      }

      if (oldTask.getCallbackState() != null && oldTask.getCallbackState() != CallbackState.NONE) {
        throw new ReopenTaskWithCallbackException(oldTask.getId());
      }

      String userLongName = null;
      if (kadaiEngine.getEngine().getConfiguration().isAddAdditionalUserInfo()) {
        User user = userMapper.findById(userId);
        if (user != null) {
          userLongName = user.getLongName();
        }
      }

      reopenActionsOnTask(task, userId, userLongName, Instant.now());
      taskMapper.update(task);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Task '{}' reopened by user '{}'.", task.getId(), userId);
      }

      if (historyEventManager.isEnabled()) {
        String changeDetails =
            ObjectAttributeChangeDetector.determineChangesInAttributes(oldTask, task);
        historyEventManager.createEvent(
            new TaskReopenedEvent(
                IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                task,
                userId,
                kadaiEngine.getEngine().getCurrentUserContext().getUserContext().getProxyAccessId(),
                changeDetails));
      }
    } finally {
      kadaiEngine.returnConnection();
    }

    return task;
  }

  @Override
  public BulkOperationResults<String, KadaiException> distribute(
      String sourceWorkbasketId,
      List<String> taskIds,
      List<String> destinationWorkbasketIds,
      String distributionStrategyName,
      Map<String, Object> additionalInformation)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          TaskNotFoundException,
          InvalidTaskStateException {
    return taskDistributor.distribute(
        sourceWorkbasketId,
        taskIds,
        destinationWorkbasketIds,
        distributionStrategyName,
        additionalInformation);
  }

  @Override
  public BulkOperationResults<String, KadaiException> distribute(
      String sourceWorkbasketId,
      List<String> destinationWorkbasketIds,
      String distributionStrategyName,
      Map<String, Object> additionalInformation)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          TaskNotFoundException,
          InvalidTaskStateException {
    return taskDistributor.distribute(
        sourceWorkbasketId,
        destinationWorkbasketIds,
        distributionStrategyName,
        additionalInformation);
  }

  @Override
  public BulkOperationResults<String, KadaiException> distribute(String sourceWorkbasketId)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          TaskNotFoundException,
          InvalidTaskStateException {
    return taskDistributor.distribute(sourceWorkbasketId);
  }

  @Override
  public BulkOperationResults<String, KadaiException> distribute(
      String sourceWorkbasketId, List<String> taskIds)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          TaskNotFoundException,
          InvalidTaskStateException {
    return taskDistributor.distribute(sourceWorkbasketId, taskIds);
  }

  @Override
  public BulkOperationResults<String, KadaiException> distributeWithStrategy(
      String sourceWorkbasketId,
      String distributionStrategyName,
      Map<String, Object> additionalInformation)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          TaskNotFoundException {
    return taskDistributor.distributeWithStrategy(
        sourceWorkbasketId, distributionStrategyName, additionalInformation);
  }

  @Override
  public BulkOperationResults<String, KadaiException> distributeWithStrategy(
      String sourceWorkbasketId,
      List<String> taskIds,
      String distributionStrategyName,
      Map<String, Object> additionalInformation)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          TaskNotFoundException {
    return taskDistributor.distributeWithStrategy(
        sourceWorkbasketId, taskIds, distributionStrategyName, additionalInformation);
  }

  @Override
  public BulkOperationResults<String, KadaiException> distributeWithDestinations(
      String sourceWorkbasketId, List<String> destinationWorkbasketIds)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          TaskNotFoundException {
    return taskDistributor.distributeWithDestinations(sourceWorkbasketId, destinationWorkbasketIds);
  }

  @Override
  public BulkOperationResults<String, KadaiException> distributeWithDestinations(
      String sourceWorkbasketId, List<String> taskIds, List<String> destinationWorkbasketIds)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          TaskNotFoundException {
    return taskDistributor.distributeWithDestinations(
        sourceWorkbasketId, taskIds, destinationWorkbasketIds);
  }

  @Override
  public void deleteTask(String taskId)
      throws TaskNotFoundException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException,
          InvalidCallbackStateException {
    deleteTask(taskId, false);
  }

  @Override
  public void forceDeleteTask(String taskId)
      throws TaskNotFoundException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException,
          InvalidCallbackStateException {
    deleteTask(taskId, true);
  }

  @Override
  public Optional<Task> selectAndClaim(TaskQuery taskQuery)
      throws NotAuthorizedOnWorkbasketException {
    ((TaskQueryImpl) taskQuery).selectAndClaimEquals(true);
    try {
      return kadaiEngine.executeInDatabaseConnection(
          CheckedSupplier.rethrowing(
              () ->
                  Optional.ofNullable(taskQuery.single())
                      .map(TaskSummary::getId)
                      .map(CheckedFunction.rethrowing(this::claim))));
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public BulkOperationResults<String, KadaiException> deleteTasks(List<String> taskIds)
      throws InvalidArgumentException, NotAuthorizedException {

    kadaiEngine.getEngine().checkRoleMembership(KadaiRole.ADMIN);

    try {
      kadaiEngine.openConnection();
      if (taskIds == null) {
        throw new InvalidArgumentException("List of TaskIds must not be null.");
      }
      taskIds = new ArrayList<>(taskIds);

      BulkOperationResults<String, KadaiException> bulkLog = new BulkOperationResults<>();

      if (taskIds.isEmpty()) {
        return bulkLog;
      }

      List<MinimalTaskSummary> taskSummaries = taskMapper.findExistingTasks(taskIds, null);

      Iterator<String> taskIdIterator = taskIds.iterator();
      while (taskIdIterator.hasNext()) {
        removeSingleTaskForTaskDeletionById(bulkLog, taskSummaries, taskIdIterator);
      }

      if (!taskIds.isEmpty()) {
        attachmentMapper.deleteMultipleByTaskIds(taskIds);
        objectReferenceMapper.deleteMultipleByTaskIds(taskIds);
        taskMapper.deleteMultiple(taskIds);

        if (kadaiEngine.getEngine().isHistoryEnabled()
            && kadaiEngine
                .getEngine()
                .getConfiguration()
                .isDeleteHistoryEventsOnTaskDeletionEnabled()) {
          historyEventManager.deleteEvents(taskIds);
        }
        if (historyEventManager.isEnabled()) {
          taskIds.forEach(this::createTaskDeletedEvent);
        }
      }
      return bulkLog;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public BulkOperationResults<String, KadaiException> completeTasks(List<String> taskIds)
      throws InvalidArgumentException {
    return completeTasks(taskIds, false);
  }

  @Override
  public BulkOperationResults<String, KadaiException> forceCompleteTasks(List<String> taskIds)
      throws InvalidArgumentException {
    return completeTasks(taskIds, true);
  }

  @Override
  public List<String> updateTasks(
      List<String> taskIds, Map<TaskCustomField, String> customFieldsToUpdate)
      throws InvalidArgumentException {

    validateCustomFields(customFieldsToUpdate);
    TaskCustomPropertySelector fieldSelector = new TaskCustomPropertySelector();
    TaskImpl updatedTask = initUpdatedTask(customFieldsToUpdate, fieldSelector);

    try {
      kadaiEngine.openConnection();

      // use query in order to find only those tasks that are visible to the current user
      List<TaskSummary> taskSummaries = getTasksToChange(taskIds);

      List<TaskSummary> tasksWithPermissions = new ArrayList<>();
      for (TaskSummary taskSummary : taskSummaries) {
        if (checkEditTasksPerm(taskSummary)) {
          tasksWithPermissions.add(taskSummary);
        }
      }

      List<String> changedTasks = new ArrayList<>();
      if (!tasksWithPermissions.isEmpty()) {
        changedTasks = tasksWithPermissions.stream().map(TaskSummary::getId).toList();
        taskMapper.updateTasks(changedTasks, updatedTask, fieldSelector);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("updateTasks() updated the following tasks: {} ", changedTasks);
        }

      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("updateTasks() found no tasks for update ");
        }
      }
      return changedTasks;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public Task cancelTask(String taskId)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException, InvalidTaskStateException {

    TaskImpl cancelledTask;

    try {
      kadaiEngine.openConnection();
      if (taskId == null || taskId.isEmpty()) {
        throw new TaskNotFoundException(taskId);
      }
      cancelledTask = (TaskImpl) getTask(taskId);
      TaskState state = cancelledTask.getState();
      if (state.isEndState()) {
        throw new InvalidTaskStateException(taskId, state, EnumUtil.allValuesExceptFor(END_STATES));
      }

      terminateCancelCommonActions(cancelledTask, CANCELLED);
      cancelledTask =
          (TaskImpl) taskEndstatePreprocessorManager.processTaskBeforeEndstate(cancelledTask);
      taskMapper.update(cancelledTask);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "Task '{}' cancelled by user '{}'.",
            cancelledTask.getId(),
            kadaiEngine.getEngine().getCurrentUserContext().getUserId());
      }
      if (historyEventManager.isEnabled()) {
        historyEventManager.createEvent(
            new TaskCancelledEvent(
                IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                cancelledTask,
                kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
                kadaiEngine
                    .getEngine()
                    .getCurrentUserContext()
                    .getUserContext()
                    .getProxyAccessId()));
      }
    } finally {
      kadaiEngine.returnConnection();
    }

    return cancelledTask;
  }

  @Override
  public TaskComment createTaskComment(TaskComment taskComment)
      throws TaskNotFoundException, InvalidArgumentException, NotAuthorizedOnWorkbasketException {
    return taskCommentService.createTaskComment(taskComment);
  }

  @Override
  public BulkOperationResults<String, KadaiException> createTaskCommentsBulk(
      List<String> taskIds, String text) throws InvalidArgumentException {
    return taskCommentService.createTaskCommentsBulk(taskIds, text);
  }

  @Override
  public TaskComment updateTaskComment(TaskComment taskComment)
      throws ConcurrencyException,
          TaskCommentNotFoundException,
          TaskNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnTaskCommentException,
          NotAuthorizedOnWorkbasketException {
    return taskCommentService.updateTaskComment(taskComment);
  }

  @Override
  public void deleteTaskComment(String taskCommentId)
      throws TaskCommentNotFoundException,
          TaskNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnTaskCommentException,
          NotAuthorizedOnWorkbasketException {
    taskCommentService.deleteTaskComment(taskCommentId);
  }

  @Override
  public TaskComment getTaskComment(String taskCommentid)
      throws TaskCommentNotFoundException,
          TaskNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException {
    return taskCommentService.getTaskComment(taskCommentid);
  }

  @Override
  public List<TaskComment> getTaskComments(String taskId)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException {

    return taskCommentService.getTaskComments(taskId);
  }

  @Override
  public BulkOperationResults<String, KadaiException> setCallbackStateForTasks(
      List<String> externalIds, CallbackState state) {

    try {
      kadaiEngine.openConnection();

      BulkOperationResults<String, KadaiException> bulkLog = new BulkOperationResults<>();

      if (externalIds == null || externalIds.isEmpty()) {
        return bulkLog;
      }

      List<MinimalTaskSummary> taskSummaries = taskMapper.findExistingTasks(null, externalIds);

      Iterator<String> taskIdIterator = new ArrayList<>(externalIds).iterator();
      while (taskIdIterator.hasNext()) {
        removeSingleTaskForCallbackStateByExternalId(bulkLog, taskSummaries, taskIdIterator, state);
      }
      if (!externalIds.isEmpty()) {
        taskMapper.setCallbackStateMultiple(externalIds, state);
      }
      return bulkLog;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public BulkOperationResults<String, KadaiException> setOwnerOfTasks(
      String owner, List<String> taskIds) {

    BulkOperationResults<String, KadaiException> bulkLog = new BulkOperationResults<>();
    if (taskIds == null || taskIds.isEmpty()) {
      return bulkLog;
    }

    try {
      kadaiEngine.openConnection();
      Pair<List<MinimalTaskSummary>, BulkLog> existingAndAuthorizedTasks =
          getMinimalTaskSummaries(taskIds);
      bulkLog.addAllErrors(existingAndAuthorizedTasks.getRight());
      Pair<List<String>, BulkLog> taskIdsToUpdate =
          filterOutTasksWhichAreInInvalidState(existingAndAuthorizedTasks.getLeft());
      bulkLog.addAllErrors(taskIdsToUpdate.getRight());

      if (!taskIdsToUpdate.getLeft().isEmpty()) {
        taskMapper.setOwnerOfTasks(owner, taskIdsToUpdate.getLeft(), Instant.now());
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "Received the Request to set owner on {} tasks, actually modified tasks = {}"
                + ", could not set owner on {} tasks.",
            taskIds.size(),
            taskIdsToUpdate.getLeft().size(),
            bulkLog.getFailedIds().size());
      }

      return bulkLog;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public BulkOperationResults<String, KadaiException> setPlannedPropertyOfTasks(
      Instant planned, List<String> argTaskIds) {

    BulkLog bulkLog = new BulkLog();
    if (argTaskIds == null || argTaskIds.isEmpty()) {
      return bulkLog;
    }
    try {
      kadaiEngine.openConnection();
      Pair<List<MinimalTaskSummary>, BulkLog> resultsPair = getMinimalTaskSummaries(argTaskIds);
      List<MinimalTaskSummary> tasksToModify = resultsPair.getLeft();
      bulkLog.addAllErrors(resultsPair.getRight());
      BulkLog errorsFromProcessing =
          serviceLevelHandler.setPlannedPropertyOfTasksImpl(planned, tasksToModify);
      bulkLog.addAllErrors(errorsFromProcessing);
      return bulkLog;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  @Override
  public Task terminateTask(String taskId)
      throws TaskNotFoundException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {

    kadaiEngine.getEngine().checkRoleMembership(KadaiRole.ADMIN, KadaiRole.TASK_ADMIN);

    TaskImpl terminatedTask;

    try {
      kadaiEngine.openConnection();
      if (taskId == null || taskId.isEmpty()) {
        throw new TaskNotFoundException(taskId);
      }
      terminatedTask = (TaskImpl) getTask(taskId);
      TaskState state = terminatedTask.getState();
      if (state.isEndState()) {
        throw new InvalidTaskStateException(taskId, state, EnumUtil.allValuesExceptFor(END_STATES));
      }

      terminateCancelCommonActions(terminatedTask, TERMINATED);
      terminatedTask =
          (TaskImpl) taskEndstatePreprocessorManager.processTaskBeforeEndstate(terminatedTask);
      taskMapper.update(terminatedTask);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "Task '{}' cancelled by user '{}'.",
            terminatedTask.getId(),
            kadaiEngine.getEngine().getCurrentUserContext().getUserId());
      }
      if (historyEventManager.isEnabled()) {
        historyEventManager.createEvent(
            new TaskTerminatedEvent(
                IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                terminatedTask,
                kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
                kadaiEngine
                    .getEngine()
                    .getCurrentUserContext()
                    .getUserContext()
                    .getProxyAccessId()));
      }

    } finally {
      kadaiEngine.returnConnection();
    }
    return terminatedTask;
  }

  public List<String> findTasksIdsAffectedByClassificationChange(String classificationId) {
    // tasks directly affected
    List<TaskSummary> tasksAffectedDirectly =
        createTaskQuery().classificationIdIn(classificationId).stateIn(READY, CLAIMED).list();

    // tasks indirectly affected via attachments
    List<Pair<String, Instant>> affectedPairs =
        tasksAffectedDirectly.stream()
            .map(t -> Pair.of(t.getId(), t.getPlanned()))
            .collect(Collectors.toList());
    // tasks indirectly affected via attachments
    List<Pair<String, Instant>> taskIdsAndPlannedFromAttachments =
        attachmentMapper.findTaskIdsAndPlannedAffectedByClassificationChange(classificationId);

    List<String> taskIdsFromAttachments =
        taskIdsAndPlannedFromAttachments.stream().map(Pair::getLeft).toList();
    List<Pair<String, Instant>> filteredTaskIdsAndPlannedFromAttachments =
        taskIdsFromAttachments.isEmpty()
            ? new ArrayList<>()
            : taskMapper.filterTaskIdsForReadyAndClaimed(taskIdsFromAttachments);
    affectedPairs.addAll(filteredTaskIdsAndPlannedFromAttachments);
    //  sort all affected tasks according to the planned instant
    List<String> affectedTaskIds =
        affectedPairs.stream()
            .sorted(Comparator.comparing(Pair::getRight))
            .distinct()
            .map(Pair::getLeft)
            .toList();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "the following tasks are affected by the update of classification {} : {}",
          classificationId,
          affectedTaskIds);
    }
    return affectedTaskIds;
  }

  public void refreshPriorityAndDueDatesOfTasksOnClassificationUpdate(
      List<String> taskIds, boolean serviceLevelChanged, boolean priorityChanged) {
    Pair<List<MinimalTaskSummary>, BulkLog> resultsPair = getMinimalTaskSummaries(taskIds);
    List<MinimalTaskSummary> tasks = resultsPair.getLeft();
    try {
      kadaiEngine.openConnection();
      Set<String> adminAccessIds =
          kadaiEngine.getEngine().getConfiguration().getRoleMap().get(KadaiRole.ADMIN);
      if (adminAccessIds.contains(kadaiEngine.getEngine().getCurrentUserContext().getUserId())) {
        serviceLevelHandler.refreshPriorityAndDueDatesOfTasks(
            tasks, serviceLevelChanged, priorityChanged);
      } else {
        kadaiEngine
            .getEngine()
            .runAsAdmin(
                () ->
                    serviceLevelHandler.refreshPriorityAndDueDatesOfTasks(
                        tasks, serviceLevelChanged, priorityChanged));
      }
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  Pair<List<MinimalTaskSummary>, BulkLog> filterTasksAuthorizedForAndLogErrorsForNotAuthorized(
      List<MinimalTaskSummary> existingTasks) {
    BulkLog bulkLog = new BulkLog();
    // check authorization only for non-admin or task-admin users
    if (kadaiEngine.getEngine().isUserInRole(KadaiRole.ADMIN, KadaiRole.TASK_ADMIN)) {
      return Pair.of(existingTasks, bulkLog);
    } else {
      List<String> accessIds = kadaiEngine.getEngine().getCurrentUserContext().getAccessIds();
      List<Pair<String, String>> taskAndWorkbasketIdsNotAuthorizedFor =
          taskMapper.getTaskAndWorkbasketIdsNotAuthorizedFor(existingTasks, accessIds);
      String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();

      for (Pair<String, String> taskAndWorkbasketIds : taskAndWorkbasketIdsNotAuthorizedFor) {
        bulkLog.addError(
            taskAndWorkbasketIds.getLeft(),
            new NotAuthorizedOnWorkbasketException(
                userId, taskAndWorkbasketIds.getRight(), WorkbasketPermission.READ));
      }

      Set<String> taskIdsToRemove =
          taskAndWorkbasketIdsNotAuthorizedFor.stream()
              .map(Pair::getLeft)
              .collect(Collectors.toSet());
      List<MinimalTaskSummary> tasksAuthorizedFor =
          existingTasks.stream().filter(not(t -> taskIdsToRemove.contains(t.getTaskId()))).toList();
      return Pair.of(tasksAuthorizedFor, bulkLog);
    }
  }

  Pair<List<MinimalTaskSummary>, BulkLog> getMinimalTaskSummaries(Collection<String> argTaskIds) {
    BulkLog bulkLog = new BulkLog();
    // remove duplicates
    Set<String> taskIds = new HashSet<>(argTaskIds);
    // get existing tasks
    List<MinimalTaskSummary> minimalTaskSummaries = taskMapper.findExistingTasks(taskIds, null);
    bulkLog.addAllErrors(addExceptionsForNonExistingTasksToBulkLog(taskIds, minimalTaskSummaries));
    Pair<List<MinimalTaskSummary>, BulkLog> filteredPair =
        filterTasksAuthorizedForAndLogErrorsForNotAuthorized(minimalTaskSummaries);
    bulkLog.addAllErrors(filteredPair.getRight());
    return Pair.of(filteredPair.getLeft(), bulkLog);
  }

  BulkLog addExceptionsForNonExistingTasksToBulkLog(
      Collection<String> requestTaskIds, List<MinimalTaskSummary> existingMinimalTaskSummaries) {
    BulkLog bulkLog = new BulkLog();
    Set<String> existingTaskIds =
        existingMinimalTaskSummaries.stream()
            .map(MinimalTaskSummary::getTaskId)
            .collect(Collectors.toSet());
    requestTaskIds.stream()
        .filter(not(existingTaskIds::contains))
        .forEach(taskId -> bulkLog.addError(taskId, new TaskNotFoundException(taskId)));
    return bulkLog;
  }

  List<TaskSummary> augmentTaskSummariesByContainedSummariesWithPartitioning(
      List<TaskSummaryImpl> taskSummaries) {
    // splitting Augmentation into steps of maximal 32000 tasks
    // reason: DB2 has a maximum for parameters in a query
    return CollectionUtil.partitionBasedOnSize(taskSummaries, 32000).stream()
        .map(this::appendComplexAttributesToTaskSummariesWithoutPartitioning)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private static Predicate<TaskSummaryImpl> addErrorToBulkLog(
      CheckedConsumer<TaskSummaryImpl, KadaiException> checkedConsumer,
      BulkOperationResults<String, KadaiException> bulkLog) {
    return summary -> {
      try {
        checkedConsumer.accept(summary);
        return true;
      } catch (KadaiException e) {
        bulkLog.addError(summary.getId(), e);
        return false;
      }
    };
  }

  private static void terminateCancelCommonActions(TaskImpl task, TaskState targetState) {
    Instant now = Instant.now();
    task.setModified(now);
    task.setCompleted(now);
    task.setState(targetState);
  }

  private static void claimActionsOnTask(
      TaskSummaryImpl task, String userId, String userLongName, Instant now) {
    task.setOwner(userId);
    task.setOwnerLongName(userLongName);
    task.setModified(now);
    task.setClaimed(now);
    task.setRead(true);
    if (Set.of(READY_FOR_REVIEW, IN_REVIEW).contains(task.getState())) {
      task.setState(IN_REVIEW);
    } else {
      task.setState(CLAIMED);
    }
  }

  private static void cancelClaimActionsOnTask(
      TaskSummaryImpl task, Instant now, boolean keepOwner) {
    if (!keepOwner) {
      task.setOwner(null);
      task.setOwnerLongName(null);
    }
    task.setModified(now);
    task.setClaimed(null);
    task.setRead(true);
    if (task.getState() == IN_REVIEW) {
      task.setState(READY_FOR_REVIEW);
    } else {
      task.setState(READY);
    }
  }

  private void reopenActionsOnTask(
      TaskSummaryImpl task, String userId, String userLongName, Instant now) {
    task.setOwner(userId);
    task.setOwnerLongName(userLongName);
    task.setModified(now);
    task.setClaimed(now);
    task.setState(CLAIMED);
    task.setCompleted(null);
    task.setRead(false);
    task.setReopened(true);
    if (!task.isManualPriorityActive()) {
      priorityServiceManager.calculatePriorityOfTask(task).ifPresent(task::setPriority);
    }
  }

  private static void completeActionsOnTask(TaskSummaryImpl task, String userId, Instant now) {
    task.setCompleted(now);
    task.setModified(now);
    task.setState(TaskState.COMPLETED);
    task.setOwner(userId);
  }

  private static void checkIfTaskIsTerminatedOrCancelled(TaskSummary task)
      throws InvalidTaskStateException {
    if (task.getState().in(CANCELLED, TERMINATED)) {
      throw new InvalidTaskStateException(
          task.getId(), task.getState(), EnumUtil.allValuesExceptFor(CANCELLED, TERMINATED));
    }
  }

  private TaskImpl preprocessTaskCreation(Task taskToCreate) {
    if (createTaskPreprocessorManager.isEnabled()) {
      taskToCreate = createTaskPreprocessorManager.processTaskBeforeCreation(taskToCreate);
    }
    TaskImpl task = (TaskImpl) taskToCreate;

    if (task.getId() != null && !task.getId().isEmpty()) {
      throw new InvalidArgumentException("taskId must be empty when creating a task");
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Task {} cannot be found, so it can be created.", task.getId());
    }
    return task;
  }

  private Workbasket resolveWorkbasket(TaskImpl task)
      throws WorkbasketNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException {

    if (task.getWorkbasketSummary() != null && task.getWorkbasketSummary().getId() != null) {
      return workbasketService.getWorkbasket(task.getWorkbasketSummary().getId());
    }
    if (task.getWorkbasketKey() != null) {
      return workbasketService.getWorkbasket(task.getWorkbasketKey(), task.getDomain());
    }

    RoutingTarget routingTarget = calculateWorkbasketDuringTaskCreation(task);
    String owner = routingTarget.getOwner() == null ? task.getOwner() : routingTarget.getOwner();
    task.setOwner(owner);
    return workbasketService.getWorkbasket(routingTarget.getWorkbasketId());
  }

  private Classification getClassificationByKeyAndDomain(
      String taskClassificationKey, String workbasketDomain)
      throws ClassificationNotFoundException, InvalidArgumentException {
    // we do use the key and not the id to make sure that we use the classification from the right
    // domain.
    // otherwise we would have to check the classification and its domain for validity.
    if (taskClassificationKey == null || taskClassificationKey.isEmpty()) {
      throw new InvalidArgumentException("classificationKey of task must not be empty");
    }

    return this.classificationService.getClassification(taskClassificationKey, workbasketDomain);
  }

  private void persistCreatedTask(TaskImpl task)
      throws TaskAlreadyExistException, PersistenceException {
    try {
      this.taskMapper.insert(task);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Method createTask() created Task '{}'.", task.getId());
      }
    } catch (PersistenceException e) {
      // Error messages:
      // Postgres: Cause: org.postgresql.util.PSQLException:
      //                  ERROR: duplicate key value violates unique constraint "uc_external_id"
      // DB/2:     Cause: com.ibm.db2.jcc.am.SqlIntegrityConstraintViolationException:
      //                  DB2 SQL Error: SQLCODE=-803, SQLSTATE=23505, SQLERRMC=2;KADAI.TASK,
      //                  DRIVER=4.22.29
      // H2:       Cause: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or
      //                  primary key violation: "UC_EXTERNAL_ID_INDEX_2 ON KADAI.TASK(EXTERNAL_ID)
      boolean isExternalIdViolation =
          Optional.ofNullable(e.getMessage())
              .map(String::toLowerCase)
              .filter(
                  msg ->
                      (msg.contains("org.postgresql.util.psqlexception")
                              && msg.contains("uc_external_id"))
                          || (msg.contains(
                                  "com.ibm.db2.jcc.am.sqlintegrityconstraintviolationexception")
                              && msg.contains("sqlcode=-803"))
                          || (msg.contains(
                                  "org.h2.jdbc.jdbcsqlintegrityconstraintviolationexception")
                              && msg.contains("uc_external_id_index")))
              .isPresent();
      if (isExternalIdViolation) {
        throw new TaskAlreadyExistException(task.getExternalId());
      }
      throw e;
    }
  }

  private void createTaskCreatedHistoryEvent(TaskImpl createdTask) {
    if (historyEventManager.isEnabled()) {
      String details =
          ObjectAttributeChangeDetector.determineChangesInAttributes(newTask(), createdTask);
      historyEventManager.createEvent(
          new TaskCreatedEvent(
              IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
              createdTask,
              kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
              kadaiEngine.getEngine().getCurrentUserContext().getUserContext().getProxyAccessId(),
              details));
    }
  }

  private static boolean taskIsNotClaimed(TaskSummary task) {
    return task.getClaimed() == null || !task.getState().isClaimedState();
  }

  private Pair<List<String>, BulkLog> filterOutTasksWhichAreInInvalidState(
      Collection<MinimalTaskSummary> minimalTaskSummaries) {
    List<String> filteredTasks = new ArrayList<>(minimalTaskSummaries.size());
    BulkLog bulkLog = new BulkLog();

    for (MinimalTaskSummary taskSummary : minimalTaskSummaries) {
      if (!taskSummary.getTaskState().in(READY, READY_FOR_REVIEW)) {
        bulkLog.addError(
            taskSummary.getTaskId(),
            new InvalidTaskStateException(
                taskSummary.getTaskId(), taskSummary.getTaskState(), READY, READY_FOR_REVIEW));
      } else {
        filteredTasks.add(taskSummary.getTaskId());
      }
    }
    return Pair.of(filteredTasks, bulkLog);
  }

  private List<TaskSummaryImpl> appendComplexAttributesToTaskSummariesWithoutPartitioning(
      List<TaskSummaryImpl> taskSummaries) {
    Set<String> taskIds =
        taskSummaries.stream().map(TaskSummaryImpl::getId).collect(Collectors.toSet());

    if (taskIds.isEmpty()) {
      taskIds = null;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "augmentTaskSummariesByContainedSummariesWithoutPartitioning() with sublist {} "
              + "about to query for attachmentSummaries ",
          taskSummaries);
    }

    List<AttachmentSummaryImpl> attachmentSummaries =
        attachmentMapper.findAttachmentSummariesByTaskIds(taskIds);
    Map<String, ClassificationSummary> classificationSummariesById =
        findClassificationsForTasksAndAttachments(taskSummaries, attachmentSummaries);
    addClassificationSummariesToAttachments(attachmentSummaries, classificationSummariesById);
    addClassificationSummariesToTaskSummaries(taskSummaries, classificationSummariesById);
    addAttachmentSummariesToTaskSummaries(taskSummaries, attachmentSummaries);
    Map<String, WorkbasketSummary> workbasketSummariesById = findWorkbasketsForTasks(taskSummaries);
    List<ObjectReferenceImpl> objectReferences =
        objectReferenceMapper.findObjectReferencesByTaskIds(taskIds);

    addWorkbasketSummariesToTaskSummaries(taskSummaries, workbasketSummariesById);
    addObjectReferencesToTaskSummaries(taskSummaries, objectReferences);

    return taskSummaries;
  }

  private BulkOperationResults<String, KadaiException> completeTasks(
      List<String> taskIds, boolean forced) throws InvalidArgumentException {
    try {
      kadaiEngine.openConnection();
      if (taskIds == null) {
        throw new InvalidArgumentException("TaskIds can't be used as NULL-Parameter.");
      }
      BulkOperationResults<String, KadaiException> bulkLog = new BulkOperationResults<>();

      Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
      Stream<TaskSummaryImpl> filteredSummaries =
          filterNotExistingTaskIds(taskIds, bulkLog)
              .filter(task -> task.getState() != TaskState.COMPLETED)
              .filter(
                  addErrorToBulkLog(TaskServiceImpl::checkIfTaskIsTerminatedOrCancelled, bulkLog));
      if (!forced) {
        filteredSummaries =
            filteredSummaries.filter(
                addErrorToBulkLog(this::checkPreconditionsForCompleteTask, bulkLog));
      } else {
        String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
        String userLongName;
        if (kadaiEngine.getEngine().getConfiguration().isAddAdditionalUserInfo()) {
          User user = userMapper.findById(userId);
          if (user != null) {
            userLongName = user.getLongName();
          } else {
            userLongName = null;
          }
        } else {
          userLongName = null;
        }
        filteredSummaries =
            filteredSummaries.filter(
                addErrorToBulkLog(
                    summary -> {
                      if (taskIsNotClaimed(summary)) {
                        checkPreconditionsForClaimTask(summary, true);
                        claimActionsOnTask(summary, userId, userLongName, now);
                      }
                    },
                    bulkLog));
      }

      updateTasksToBeCompleted(filteredSummaries, now);

      return bulkLog;
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  private Stream<TaskSummaryImpl> filterNotExistingTaskIds(
      List<String> taskIds, BulkOperationResults<String, KadaiException> bulkLog) {

    Map<String, TaskSummaryImpl> taskSummaryMap =
        getTasksToChange(taskIds).stream()
            .collect(Collectors.toMap(TaskSummary::getId, TaskSummaryImpl.class::cast));
    return taskIds.stream()
        .map(id -> Pair.of(id, taskSummaryMap.get(id)))
        .filter(
            pair -> {
              if (pair.getRight() != null) {
                return true;
              }
              String taskId = pair.getLeft();
              bulkLog.addError(taskId, new TaskNotFoundException(taskId));
              return false;
            })
        .map(Pair::getRight);
  }

  private void checkConcurrencyAndSetModified(TaskImpl newTaskImpl, TaskImpl oldTaskImpl)
      throws ConcurrencyException {
    // TODO: not safe to rely only on different timestamps.
    // With fast execution below 1ms there will be no concurrencyException
    if (oldTaskImpl.getModified() != null
            && !oldTaskImpl.getModified().equals(newTaskImpl.getModified())
        || oldTaskImpl.getClaimed() != null
            && !oldTaskImpl.getClaimed().equals(newTaskImpl.getClaimed())
        || oldTaskImpl.getState() != null
            && !oldTaskImpl.getState().equals(newTaskImpl.getState())) {
      throw new ConcurrencyException(newTaskImpl.getId());
    }
    newTaskImpl.setModified(Instant.now());
  }

  private Task claim(String taskId, boolean forceClaim)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    TaskImpl task;
    try {
      kadaiEngine.openConnection();
      task = (TaskImpl) getTask(taskId);

      final TaskImpl oldTask = duplicateTaskExactly(task);
      Instant now = Instant.now();

      checkPreconditionsForClaimTask(task, forceClaim);

      String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
      String userLongName = null;
      if (kadaiEngine.getEngine().getConfiguration().isAddAdditionalUserInfo()) {
        User user = userMapper.findById(userId);
        if (user != null) {
          userLongName = user.getLongName();
        }
      }

      claimActionsOnTask(task, userId, userLongName, now);
      taskMapper.update(task);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Task '{}' claimed by user '{}'.", task.getId(), userId);
      }
      if (historyEventManager.isEnabled()) {
        String changeDetails =
            ObjectAttributeChangeDetector.determineChangesInAttributes(oldTask, task);

        if (Set.of(READY_FOR_REVIEW, IN_REVIEW).contains(task.getState())) {
          historyEventManager.createEvent(
              new TaskClaimedReviewEvent(
                  IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                  task,
                  userId,
                  kadaiEngine
                      .getEngine()
                      .getCurrentUserContext()
                      .getUserContext()
                      .getProxyAccessId(),
                  changeDetails));
        } else {
          historyEventManager.createEvent(
              new TaskClaimedEvent(
                  IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                  task,
                  userId,
                  kadaiEngine
                      .getEngine()
                      .getCurrentUserContext()
                      .getUserContext()
                      .getProxyAccessId(),
                  changeDetails));
        }
      }
    } finally {
      kadaiEngine.returnConnection();
    }
    return task;
  }

  private Task requestReview(String taskId, String workbasketId, String ownerId, boolean force)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
    TaskImpl task;
    try {
      kadaiEngine.openConnection();
      task = (TaskImpl) getTask(taskId);
      task = (TaskImpl) beforeRequestReviewManager.beforeRequestReview(task);

      final TaskImpl oldTask = duplicateTaskExactly(task);

      final TaskState[] allowedStates =
          force ? EnumUtil.allValuesExceptFor(END_STATES) : CLAIMED_STATES;
      if (task.getState().isEndState() || (!force && taskIsNotClaimed(task))) {
        throw new InvalidTaskStateException(task.getId(), task.getState(), allowedStates);
      }
      if (!force && !task.getOwner().equals(userId)) {
        throw new InvalidOwnerException(userId, task.getId());
      }

      task.setState(READY_FOR_REVIEW);
      task.setOwner(ownerId);
      task.setModified(Instant.now());

      taskMapper.requestReview(task);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Requested review for Task '{}' by user '{}'.", task.getId(), userId);
      }
      if (historyEventManager.isEnabled()) {
        String changeDetails =
            ObjectAttributeChangeDetector.determineChangesInAttributes(oldTask, task);

        historyEventManager.createEvent(
            new TaskRequestReviewEvent(
                IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                task,
                userId,
                kadaiEngine.getEngine().getCurrentUserContext().getUserContext().getProxyAccessId(),
                changeDetails));
      }

      task = (TaskImpl) afterRequestReviewManager.afterRequestReview(task, workbasketId, ownerId);
    } finally {
      kadaiEngine.returnConnection();
    }
    return task;
  }

  private Task requestChanges(String taskId, String workbasketId, String ownerId, boolean force)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
    TaskImpl task;
    try {
      kadaiEngine.openConnection();
      task = (TaskImpl) getTask(taskId);
      task = (TaskImpl) beforeRequestChangesManager.beforeRequestChanges(task);

      final TaskImpl oldTask = duplicateTaskExactly(task);

      if (force && task.getState().isEndState()) {
        throw new InvalidTaskStateException(
            task.getId(), task.getState(), EnumUtil.allValuesExceptFor(END_STATES));
      }
      if (!force && task.getState() != IN_REVIEW) {
        throw new InvalidTaskStateException(task.getId(), task.getState(), IN_REVIEW);
      }
      if (!force && !task.getOwner().equals(userId)) {
        throw new InvalidOwnerException(userId, task.getId());
      }

      task.setState(READY);
      task.setOwner(ownerId);
      task.setModified(Instant.now());

      taskMapper.requestChanges(task);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Requested changes for Task '{}' by user '{}'.", task.getId(), userId);
      }
      if (historyEventManager.isEnabled()) {
        String changeDetails =
            ObjectAttributeChangeDetector.determineChangesInAttributes(oldTask, task);

        historyEventManager.createEvent(
            new TaskRequestChangesEvent(
                IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                task,
                userId,
                kadaiEngine.getEngine().getCurrentUserContext().getUserContext().getProxyAccessId(),
                changeDetails));
      }
      task = (TaskImpl) afterRequestChangesManager.afterRequestChanges(task, workbasketId, ownerId);
    } finally {
      kadaiEngine.returnConnection();
    }
    return task;
  }

  private void checkPreconditionsForClaimTask(TaskSummary task, boolean forced)
      throws InvalidOwnerException, InvalidTaskStateException, NotAuthorizedOnWorkbasketException {
    TaskState state = task.getState();
    if (state.isEndState()) {
      throw new InvalidTaskStateException(
          task.getId(), task.getState(), EnumUtil.allValuesExceptFor(END_STATES));
    }

    String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
    if (!forced && state.isClaimedState() && !task.getOwner().equals(userId)) {
      throw new InvalidOwnerException(userId, task.getId());
    }
    if (!checkEditTasksPerm(task)) {
      throw new NotAuthorizedOnWorkbasketException(
          kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
          task.getWorkbasketSummary().getId(),
          WorkbasketPermission.EDITTASKS);
    }
  }

  private void checkPreconditionsForCompleteTask(TaskSummary task)
      throws InvalidOwnerException, InvalidTaskStateException, NotAuthorizedOnWorkbasketException {
    if (taskIsNotClaimed(task)) {
      throw new InvalidTaskStateException(task.getId(), task.getState(), CLAIMED_STATES);
    } else if (!kadaiEngine
            .getEngine()
            .getCurrentUserContext()
            .getAccessIds()
            .contains(task.getOwner())
        && !kadaiEngine.getEngine().isUserInRole(KadaiRole.ADMIN)) {
      throw new InvalidOwnerException(
          kadaiEngine.getEngine().getCurrentUserContext().getUserId(), task.getId());
    }
    if (!checkEditTasksPerm(task)) {
      throw new NotAuthorizedOnWorkbasketException(
          kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
          task.getWorkbasketSummary().getId(),
          WorkbasketPermission.EDITTASKS);
    }
  }

  private Task cancelClaim(String taskId, boolean forceUnclaim, boolean keepOwner)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
    TaskImpl task;
    try {
      kadaiEngine.openConnection();
      task = (TaskImpl) getTask(taskId);
      final TaskImpl oldTask = duplicateTaskExactly(task);

      TaskState state = task.getState();
      if (!checkEditTasksPerm(task)) {
        throw new NotAuthorizedOnWorkbasketException(
            kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
            task.getWorkbasketSummary().getId(),
            WorkbasketPermission.EDITTASKS);
      }
      if (state.isEndState()) {
        throw new InvalidTaskStateException(taskId, state, EnumUtil.allValuesExceptFor(END_STATES));
      }
      if (state.isClaimedState() && !forceUnclaim && !userId.equals(task.getOwner())) {
        throw new InvalidOwnerException(userId, taskId);
      }
      Instant now = Instant.now();
      cancelClaimActionsOnTask(task, now, keepOwner);
      taskMapper.update(task);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Task '{}' unclaimed by user '{}'.", task.getId(), userId);
      }
      if (historyEventManager.isEnabled()) {
        String changeDetails =
            ObjectAttributeChangeDetector.determineChangesInAttributes(oldTask, task);

        historyEventManager.createEvent(
            new TaskClaimCancelledEvent(
                IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                task,
                userId,
                kadaiEngine.getEngine().getCurrentUserContext().getUserContext().getProxyAccessId(),
                changeDetails));
      }
    } finally {
      kadaiEngine.returnConnection();
    }
    return task;
  }

  private Task completeTask(String taskId, boolean isForced)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    String userId = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
    TaskImpl task;
    try {
      kadaiEngine.openConnection();
      task = (TaskImpl) this.getTask(taskId);
      if (reviewRequiredManager.reviewRequired(task)) {
        return requestReview(taskId);
      }

      if (task.getState() == TaskState.COMPLETED) {
        return task;
      }

      checkIfTaskIsTerminatedOrCancelled(task);

      if (!isForced) {
        checkPreconditionsForCompleteTask(task);
      } else if (taskIsNotClaimed(task)) {
        task = (TaskImpl) this.forceClaim(taskId);
      }

      Instant now = Instant.now();
      completeActionsOnTask(task, userId, now);
      task = (TaskImpl) taskEndstatePreprocessorManager.processTaskBeforeEndstate(task);
      taskMapper.update(task);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Task '{}' completed by user '{}'.", task.getId(), userId);
      }
      if (historyEventManager.isEnabled()) {
        historyEventManager.createEvent(
            new TaskCompletedEvent(
                IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                task,
                userId,
                kadaiEngine
                    .getEngine()
                    .getCurrentUserContext()
                    .getUserContext()
                    .getProxyAccessId()));
      }
    } finally {
      kadaiEngine.returnConnection();
    }
    return task;
  }

  private void deleteTask(String taskId, boolean forceDelete)
      throws TaskNotFoundException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException,
          InvalidCallbackStateException {
    kadaiEngine.getEngine().checkRoleMembership(KadaiRole.ADMIN);
    TaskImpl task;
    try {
      kadaiEngine.openConnection();
      task = (TaskImpl) getTask(taskId);

      if (!task.getState().isEndState() && !forceDelete) {
        throw new InvalidTaskStateException(taskId, task.getState(), END_STATES);
      }
      if (!task.getState().in(TERMINATED, CANCELLED)
          && CallbackState.CALLBACK_PROCESSING_REQUIRED.equals(task.getCallbackState())) {
        throw new InvalidCallbackStateException(
            taskId,
            task.getCallbackState(),
            EnumUtil.allValuesExceptFor(CallbackState.CALLBACK_PROCESSING_REQUIRED));
      }

      attachmentMapper.deleteMultipleByTaskIds(Collections.singletonList(taskId));
      objectReferenceMapper.deleteMultipleByTaskIds(Collections.singletonList(taskId));
      taskMapper.delete(taskId);

      if (kadaiEngine.getEngine().isHistoryEnabled()
          && kadaiEngine
              .getEngine()
              .getConfiguration()
              .isDeleteHistoryEventsOnTaskDeletionEnabled()) {
        historyEventManager.deleteEvents(Collections.singletonList(taskId));
      }

      if (historyEventManager.isEnabled()) {
        createTaskDeletedEvent(taskId);
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Task {} deleted.", task.getId());
      }
    } finally {
      kadaiEngine.returnConnection();
    }
  }

  private void removeSingleTaskForTaskDeletionById(
      BulkOperationResults<String, KadaiException> bulkLog,
      List<MinimalTaskSummary> taskSummaries,
      Iterator<String> taskIdIterator) {
    String currentTaskId = taskIdIterator.next();
    if (currentTaskId == null || currentTaskId.equals("")) {
      bulkLog.addError("", new TaskNotFoundException(null));
      taskIdIterator.remove();
    } else {
      MinimalTaskSummary foundSummary =
          taskSummaries.stream()
              .filter(taskSummary -> currentTaskId.equals(taskSummary.getTaskId()))
              .findFirst()
              .orElse(null);
      if (foundSummary == null) {
        bulkLog.addError(currentTaskId, new TaskNotFoundException(currentTaskId));
        taskIdIterator.remove();
      } else if (!foundSummary.getTaskState().isEndState()) {
        bulkLog.addError(
            currentTaskId,
            new InvalidTaskStateException(currentTaskId, foundSummary.getTaskState(), END_STATES));
        taskIdIterator.remove();
      } else {
        if (!foundSummary.getTaskState().in(CANCELLED, TERMINATED)
            && CallbackState.CALLBACK_PROCESSING_REQUIRED.equals(foundSummary.getCallbackState())) {
          bulkLog.addError(
              currentTaskId,
              new InvalidCallbackStateException(
                  currentTaskId,
                  foundSummary.getCallbackState(),
                  EnumUtil.allValuesExceptFor(CallbackState.CALLBACK_PROCESSING_REQUIRED)));
          taskIdIterator.remove();
        }
      }
    }
  }

  private void removeSingleTaskForCallbackStateByExternalId(
      BulkOperationResults<String, KadaiException> bulkLog,
      List<MinimalTaskSummary> taskSummaries,
      Iterator<String> externalIdIterator,
      CallbackState desiredCallbackState) {
    String currentExternalId = externalIdIterator.next();
    if (currentExternalId == null || currentExternalId.equals("")) {
      bulkLog.addError("", new TaskNotFoundException(null));
      externalIdIterator.remove();
    } else {
      Optional<MinimalTaskSummary> foundSummary =
          taskSummaries.stream()
              .filter(taskSummary -> currentExternalId.equals(taskSummary.getExternalId()))
              .findFirst();
      if (foundSummary.isPresent()) {
        Optional<KadaiException> invalidStateException =
            desiredCallbackStateCanBeSetForFoundSummary(foundSummary.get(), desiredCallbackState);
        if (invalidStateException.isPresent()) {
          bulkLog.addError(currentExternalId, invalidStateException.get());
          externalIdIterator.remove();
        }
      } else {
        bulkLog.addError(currentExternalId, new TaskNotFoundException(currentExternalId));
        externalIdIterator.remove();
      }
    }
  }

  private Optional<KadaiException> desiredCallbackStateCanBeSetForFoundSummary(
      MinimalTaskSummary foundSummary, CallbackState desiredCallbackState) {

    CallbackState currentTaskCallbackState = foundSummary.getCallbackState();
    TaskState currentTaskState = foundSummary.getTaskState();

    switch (desiredCallbackState) {
      case CALLBACK_PROCESSING_COMPLETED:
        if (!currentTaskState.isEndState()) {
          return Optional.of(
              new InvalidTaskStateException(
                  foundSummary.getTaskId(), foundSummary.getTaskState(), END_STATES));
        }
        break;
      case CLAIMED:
        if (!currentTaskState.equals(CLAIMED)) {
          return Optional.of(
              new InvalidTaskStateException(
                  foundSummary.getTaskId(), foundSummary.getTaskState(), CLAIMED));
        }
        if (!currentTaskCallbackState.equals(CallbackState.CALLBACK_PROCESSING_REQUIRED)) {
          return Optional.of(
              new InvalidCallbackStateException(
                  foundSummary.getTaskId(),
                  currentTaskCallbackState,
                  CallbackState.CALLBACK_PROCESSING_REQUIRED));
        }
        break;
      case CALLBACK_PROCESSING_REQUIRED:
        if (currentTaskCallbackState.equals(CallbackState.CALLBACK_PROCESSING_COMPLETED)) {
          return Optional.of(
              new InvalidCallbackStateException(
                  foundSummary.getTaskId(),
                  currentTaskCallbackState,
                  EnumUtil.allValuesExceptFor(CallbackState.CALLBACK_PROCESSING_COMPLETED)));
        }
        break;
      default:
        return Optional.of(
            new InvalidCallbackStateException(
                foundSummary.getTaskId(),
                currentTaskCallbackState,
                CallbackState.CALLBACK_PROCESSING_COMPLETED,
                CallbackState.CLAIMED,
                CallbackState.CALLBACK_PROCESSING_REQUIRED));
    }
    return Optional.empty();
  }

  private void applyTaskSettingsOnTaskCreation(TaskImpl taskToCreate, Classification classification)
      throws InvalidArgumentException,
          ClassificationNotFoundException,
          AttachmentPersistenceException,
          ObjectReferencePersistenceException {
    final Instant now = Instant.now();
    taskToCreate.setId(IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK));
    if (taskToCreate.getExternalId() == null) {
      taskToCreate.setExternalId(IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_EXT_TASK));
    }
    taskToCreate.setState(READY);
    taskToCreate.setCreated(now);
    taskToCreate.setModified(now);
    taskToCreate.setRead(false);
    taskToCreate.setTransferred(false);
    taskToCreate.setReopened(false);

    String creator = kadaiEngine.getEngine().getCurrentUserContext().getUserId();
    if (kadaiEngine.getEngine().getConfiguration().isSecurityEnabled() && creator == null) {
      throw new SystemException(
          "KadaiSecurity is enabled, but the current UserId is NULL while creating a Task.");
    }
    taskToCreate.setCreator(creator);

    // if no business process id is provided, a unique id is created.
    if (taskToCreate.getBusinessProcessId() == null) {
      taskToCreate.setBusinessProcessId(
          IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_BUSINESS_PROCESS));
    }
    // null in case of manual tasks
    if (taskToCreate.getPlanned() == null
        && (classification == null || taskToCreate.getDue() == null)) {
      taskToCreate.setPlanned(now);
    }
    if (taskToCreate.getName() == null && classification != null) {
      taskToCreate.setName(classification.getName());
    }
    if (taskToCreate.getDescription() == null && classification != null) {
      taskToCreate.setDescription(classification.getDescription());
    }
    if (taskToCreate.getOwner() != null
        && kadaiEngine.getEngine().getConfiguration().isAddAdditionalUserInfo()) {
      User user = userMapper.findById(taskToCreate.getOwner());
      if (user != null) {
        taskToCreate.setOwnerLongName(user.getLongName());
      }
    }
    setDefaultTaskReceivedDateFromAttachments(taskToCreate);

    attachmentHandler.insertNewAttachmentsOnTaskCreation(taskToCreate);
    objectReferenceHandler.insertNewSecondaryObjectReferencesOnTaskCreation(taskToCreate);
    // This has to be called after the AttachmentHandler because the AttachmentHandler fetches
    // the Classifications of the Attachments.
    // This is necessary to guarantee that the following calculation is correct.
    serviceLevelHandler.updatePrioPlannedDueOfTask(taskToCreate, null);

    setCallbackStateOnTaskCreation(taskToCreate);
    priorityServiceManager
        .calculatePriorityOfTask(taskToCreate)
        .ifPresent(taskToCreate::setPriority);
  }

  private void setDefaultTaskReceivedDateFromAttachments(TaskImpl task) {
    if (task.getReceived() == null) {
      task.getAttachments().stream()
          .map(AttachmentSummary::getReceived)
          .filter(Objects::nonNull)
          .min(Instant::compareTo)
          .ifPresent(task::setReceived);
    }
  }

  private void setCallbackStateOnTaskCreation(TaskImpl task) throws InvalidArgumentException {
    Map<String, String> callbackInfo = task.getCallbackInfo();
    if (callbackInfo != null && callbackInfo.containsKey(Task.CALLBACK_STATE)) {
      String value = callbackInfo.get(Task.CALLBACK_STATE);
      if (value != null && !value.isEmpty()) {
        try {
          CallbackState state = CallbackState.valueOf(value);
          task.setCallbackState(state);
        } catch (Exception e) {
          LOGGER.warn(
              "Attempted to determine callback state from {} and caught exception", value, e);
          throw new InvalidArgumentException(
              String.format("Attempted to set callback state for task %s.", task.getId()), e);
        }
      }
    }
  }

  private void updateTasksToBeCompleted(Stream<TaskSummaryImpl> taskSummaries, Instant now) {

    List<TaskSummary> taskSummaryList =
        taskSummaries
            .map(
                summary -> {
                  completeActionsOnTask(
                      summary, kadaiEngine.getEngine().getCurrentUserContext().getUserId(), now);
                  return (TaskSummary) summary;
                })
            .toList();

    List<String> taskIds = taskSummaryList.stream().map(TaskSummary::getId).toList();

    List<String> updateClaimedTaskIds =
        taskSummaryList.stream()
            .filter(summary -> now.equals(summary.getClaimed()))
            .map(TaskSummary::getId)
            .toList();

    TaskSummary claimedReference =
        taskSummaryList.stream()
            .filter(summary -> updateClaimedTaskIds.contains(summary.getId()))
            .findFirst()
            .orElse(null);

    if (!taskSummaryList.isEmpty()) {
      taskMapper.updateCompleted(taskIds, taskSummaryList.get(0));
      if (!updateClaimedTaskIds.isEmpty()) {
        taskMapper.updateClaimed(updateClaimedTaskIds, claimedReference);
      }
      if (historyEventManager.isEnabled()) {
        createTasksCompletedEvents(taskSummaryList);
      }
    }
  }

  private RoutingTarget calculateWorkbasketDuringTaskCreation(TaskImpl task) {
    RoutingTarget routingTarget;
    if (!kadaiEngine.getEngine().getConfiguration().isIncludeOwnerWhenRouting()) {
      String workbasketId = kadaiEngine.getTaskRoutingManager().determineWorkbasketId(task);
      if (workbasketId == null) {
        throw new InvalidArgumentException("Cannot create a Task outside a Workbasket");
      }
      routingTarget = new RoutingTarget(workbasketId);
    } else {
      routingTarget =
          kadaiEngine
              .getTaskRoutingManager()
              .determineRoutingTarget(task)
              .orElseThrow(
                  () ->
                      new InvalidArgumentException(
                          "Cannot create a Task in an empty RoutingTarget"));
    }
    return routingTarget;
  }

  private Map<String, WorkbasketSummary> findWorkbasketsForTasks(
      List<? extends TaskSummary> taskSummaries) {
    if (taskSummaries == null || taskSummaries.isEmpty()) {
      return Collections.emptyMap();
    }

    Set<String> workbasketIds =
        taskSummaries.stream()
            .map(TaskSummary::getWorkbasketSummary)
            .map(WorkbasketSummary::getId)
            .collect(Collectors.toSet());

    return queryWorkbasketsForTasks(workbasketIds).stream()
        .collect(Collectors.toMap(WorkbasketSummary::getId, Function.identity()));
  }

  private Map<String, ClassificationSummary> findClassificationsForTasksAndAttachments(
      List<? extends TaskSummary> taskSummaries,
      List<? extends AttachmentSummaryImpl> attachmentSummaries) {
    if (taskSummaries == null || taskSummaries.isEmpty()) {
      return Collections.emptyMap();
    }

    Set<String> classificationIds =
        Stream.concat(
                taskSummaries.stream().map(TaskSummary::getClassificationSummary),
                attachmentSummaries.stream().map(AttachmentSummary::getClassificationSummary))
            .map(ClassificationSummary::getId)
            .collect(Collectors.toSet());

    return queryClassificationsForTasksAndAttachments(classificationIds).stream()
        .collect(Collectors.toMap(ClassificationSummary::getId, Function.identity()));
  }

  private Map<String, ClassificationSummary> findClassificationForTaskImplAndAttachments(
      TaskImpl task, List<AttachmentImpl> attachmentImpls) {
    return findClassificationsForTasksAndAttachments(
        Collections.singletonList(task), attachmentImpls);
  }

  private List<ClassificationSummary> queryClassificationsForTasksAndAttachments(
      Set<String> classificationIds) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "queryClassificationsForTasksAndAttachments() about to query classifications and exit");
    }
    return this.classificationService
        .createClassificationQuery()
        .idIn(classificationIds.toArray(new String[0]))
        .list();
  }

  private List<WorkbasketSummary> queryWorkbasketsForTasks(Set<String> workbasketIds) {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryWorkbasketsForTasks() about to query workbaskets and exit");
    }
    // perform classification query
    return this.workbasketService
        .createWorkbasketQuery()
        .idIn(workbasketIds.toArray(new String[0]))
        .list();
  }

  private void addClassificationSummariesToTaskSummaries(
      List<TaskSummaryImpl> tasks, Map<String, ClassificationSummary> classificationSummaryById) {

    if (tasks == null || tasks.isEmpty()) {
      return;
    }

    for (TaskSummaryImpl task : tasks) {
      String classificationId = task.getClassificationSummary().getId();
      ClassificationSummary classificationSummary = classificationSummaryById.get(classificationId);
      if (classificationSummary == null) {
        throw new SystemException(
            "Did not find a Classification for task (Id="
                + task.getId()
                + ",Classification="
                + task.getClassificationSummary().getId()
                + ")");
      }
      task.setClassificationSummary(classificationSummary);
    }
  }

  private void addWorkbasketSummariesToTaskSummaries(
      List<TaskSummaryImpl> tasks, Map<String, WorkbasketSummary> workbasketSummaryById) {
    if (tasks == null || tasks.isEmpty()) {
      return;
    }

    for (TaskSummaryImpl task : tasks) {
      String workbasketId = task.getWorkbasketSummary().getId();
      WorkbasketSummary workbasketSummary = workbasketSummaryById.get(workbasketId);
      if (workbasketSummary == null) {
        throw new SystemException(
            "Did not find a Workbasket for task (Id="
                + task.getId()
                + ",Workbasket="
                + task.getWorkbasketSummary().getId()
                + ")");
      }
      task.setWorkbasketSummary(workbasketSummary);
    }
  }

  private void addAttachmentSummariesToTaskSummaries(
      List<TaskSummaryImpl> taskSummaries, List<AttachmentSummaryImpl> attachmentSummaries) {

    if (taskSummaries == null || taskSummaries.isEmpty()) {
      return;
    }

    Map<String, TaskSummaryImpl> taskSummariesById =
        taskSummaries.stream()
            .collect(
                Collectors.toMap(
                    TaskSummary::getId,
                    Function.identity(),
                    // Currently, we still have a bug (TSK-1204), where the TaskQuery#list function
                    // returns the same task multiple times when that task has more than one
                    // attachment...Therefore, this MergeFunction is necessary.
                    (a, b) -> b));

    for (AttachmentSummaryImpl attachmentSummary : attachmentSummaries) {
      String taskId = attachmentSummary.getTaskId();
      TaskSummaryImpl taskSummary = taskSummariesById.get(taskId);
      if (taskSummary != null) {
        taskSummary.addAttachmentSummary(attachmentSummary);
      }
    }
  }

  private void addClassificationSummariesToAttachments(
      List<? extends AttachmentSummaryImpl> attachments,
      Map<String, ClassificationSummary> classificationSummariesById) {

    if (attachments == null || attachments.isEmpty()) {
      return;
    }

    for (AttachmentSummaryImpl attachment : attachments) {
      String classificationId = attachment.getClassificationSummary().getId();
      ClassificationSummary classificationSummary =
          classificationSummariesById.get(classificationId);

      if (classificationSummary == null) {
        throw new SystemException("Could not find a Classification for attachment " + attachment);
      }
      attachment.setClassificationSummary(classificationSummary);
    }
  }

  private void addObjectReferencesToTaskSummaries(
      List<TaskSummaryImpl> taskSummaries, List<ObjectReferenceImpl> objectReferences) {
    if (taskSummaries == null || taskSummaries.isEmpty()) {
      return;
    }

    Map<String, TaskSummaryImpl> taskSummariesById =
        taskSummaries.stream()
            .collect(
                Collectors.toMap(
                    TaskSummary::getId,
                    Function.identity(),
                    // The TaskQuery#list function
                    // returns the same task multiple times when that task has more than one
                    // object reference...Therefore, this MergeFunction is necessary.
                    (a, b) -> b));

    for (ObjectReferenceImpl objectReference : objectReferences) {
      String taskId = objectReference.getTaskId();
      TaskSummaryImpl taskSummary = taskSummariesById.get(taskId);
      if (taskSummary != null) {
        taskSummary.addSecondaryObjectReference(objectReference);
      }
    }
  }

  private TaskImpl initUpdatedTask(
      Map<TaskCustomField, String> customFieldsToUpdate, TaskCustomPropertySelector fieldSelector) {

    TaskImpl newTask = new TaskImpl();
    newTask.setModified(Instant.now());

    for (Entry<TaskCustomField, String> entry : customFieldsToUpdate.entrySet()) {
      TaskCustomField key = entry.getKey();
      fieldSelector.setCustomProperty(key, true);
      newTask.setCustomField(key, entry.getValue());
    }
    return newTask;
  }

  private void validateCustomFields(Map<TaskCustomField, String> customFieldsToUpdate)
      throws InvalidArgumentException {

    if (customFieldsToUpdate == null || customFieldsToUpdate.isEmpty()) {
      throw new InvalidArgumentException(
          "The customFieldsToUpdate argument to updateTasks must not be empty.");
    }
  }

  private List<TaskSummary> getTasksToChange(List<String> taskIds) {
    return createTaskQuery().idIn(taskIds.toArray(new String[0])).list();
  }

  private List<TaskSummary> getTasksToChange(ObjectReference selectionCriteria) {
    return createTaskQuery()
        .primaryObjectReferenceCompanyIn(selectionCriteria.getCompany())
        .primaryObjectReferenceSystemIn(selectionCriteria.getSystem())
        .primaryObjectReferenceSystemInstanceIn(selectionCriteria.getSystemInstance())
        .primaryObjectReferenceTypeIn(selectionCriteria.getType())
        .primaryObjectReferenceValueIn(selectionCriteria.getValue())
        .list();
  }

  private void standardUpdateActions(TaskImpl oldTaskImpl, TaskImpl newTaskImpl)
      throws InvalidArgumentException, ClassificationNotFoundException, InvalidTaskStateException {

    if (oldTaskImpl.getExternalId() == null
        || !oldTaskImpl.getExternalId().equals(newTaskImpl.getExternalId())) {
      throw new InvalidArgumentException(
          "A task's external Id cannot be changed via update of the task");
    }

    String newWorkbasketKey = newTaskImpl.getWorkbasketKey();
    if (newWorkbasketKey != null && !newWorkbasketKey.equals(oldTaskImpl.getWorkbasketKey())) {
      throw new InvalidArgumentException(
          "A task's Workbasket cannot be changed via update of the task");
    }

    if (newTaskImpl.getClassificationSummary() == null) {
      newTaskImpl.setClassificationSummary(oldTaskImpl.getClassificationSummary());
    }

    setDefaultTaskReceivedDateFromAttachments(newTaskImpl);

    updateClassificationSummary(newTaskImpl, oldTaskImpl);

    TaskImpl newTaskImpl1 =
        serviceLevelHandler.updatePrioPlannedDueOfTask(newTaskImpl, oldTaskImpl);

    // if no business process id is provided, use the id of the old task.
    if (newTaskImpl1.getBusinessProcessId() == null) {
      newTaskImpl1.setBusinessProcessId(oldTaskImpl.getBusinessProcessId());
    }

    // owner can only be changed if task is either in state ready or ready_for_review
    boolean isOwnerChanged = !Objects.equals(newTaskImpl1.getOwner(), oldTaskImpl.getOwner());
    if (isOwnerChanged && !oldTaskImpl.getState().in(READY, READY_FOR_REVIEW)) {
      throw new InvalidTaskStateException(
          oldTaskImpl.getId(), oldTaskImpl.getState(), READY, READY_FOR_REVIEW);
    }
    if (isOwnerChanged && kadaiEngine.getEngine().getConfiguration().isAddAdditionalUserInfo()) {
      User user = userMapper.findById(newTaskImpl.getOwner());
      if (user != null) {
        newTaskImpl.setOwnerLongName(user.getLongName());
      }
    }
  }

  private void updateClassificationSummary(TaskImpl newTaskImpl, TaskImpl oldTaskImpl)
      throws ClassificationNotFoundException {
    ClassificationSummary oldClassificationSummary = oldTaskImpl.getClassificationSummary();
    ClassificationSummary newClassificationSummary = newTaskImpl.getClassificationSummary();
    if (newClassificationSummary == null) {
      newClassificationSummary = oldClassificationSummary;
    }
    if (!oldClassificationSummary.getKey().equals(newClassificationSummary.getKey())) {
      Classification newClassification =
          this.classificationService.getClassification(
              newClassificationSummary.getKey(), newTaskImpl.getWorkbasketSummary().getDomain());
      newClassificationSummary = newClassification.asSummary();
      newTaskImpl.setClassificationSummary(newClassificationSummary);
    }
  }

  private void createTasksCompletedEvents(List<? extends TaskSummary> taskSummaries) {
    taskSummaries.forEach(
        task ->
            historyEventManager.createEvent(
                new TaskCompletedEvent(
                    IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
                    task,
                    kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
                    kadaiEngine
                        .getEngine()
                        .getCurrentUserContext()
                        .getUserContext()
                        .getProxyAccessId())));
  }

  private void createTaskDeletedEvent(String taskId) {
    historyEventManager.createEvent(
        new TaskDeletedEvent(
            IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK_HISTORY_EVENT),
            newTask().asSummary(),
            taskId,
            kadaiEngine.getEngine().getCurrentUserContext().getUserId(),
            kadaiEngine.getEngine().getCurrentUserContext().getUserContext().getProxyAccessId()));
  }

  private TaskImpl duplicateTaskExactly(TaskImpl task) {
    TaskImpl oldTask = task.copy();
    oldTask.setId(task.getId());
    oldTask.setExternalId(task.getExternalId());
    oldTask.setAttachments(task.getAttachments());
    oldTask.setSecondaryObjectReferences(task.getSecondaryObjectReferences());
    return oldTask;
  }

  private boolean checkEditTasksPerm(TaskSummary task) {
    WorkbasketQueryImpl query = (WorkbasketQueryImpl) workbasketService.createWorkbasketQuery();
    String workbasketId = task.getWorkbasketSummary().getId();
    WorkbasketSummary workbasket =
        query.idIn(workbasketId).callerHasPermissions(WorkbasketPermission.EDITTASKS).single();
    return workbasket != null;
  }
}
