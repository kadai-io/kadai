/*
 * Copyright [2024] [envite consulting GmbH]
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

package io.kadai.task.rest;

import static java.util.function.Predicate.not;

import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.QuerySortBy;
import io.kadai.common.rest.QuerySortParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.common.rest.util.QueryParamsValidator;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskQuery;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.exceptions.AttachmentPersistenceException;
import io.kadai.task.api.exceptions.InvalidCallbackStateException;
import io.kadai.task.api.exceptions.InvalidOwnerException;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.exceptions.ObjectReferencePersistenceException;
import io.kadai.task.api.exceptions.TaskAlreadyExistException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.task.rest.assembler.BulkOperationResultsRepresentationModelAssembler;
import io.kadai.task.rest.assembler.TaskRepresentationModelAssembler;
import io.kadai.task.rest.assembler.TaskSummaryRepresentationModelAssembler;
import io.kadai.task.rest.models.BulkOperationResultsRepresentationModel;
import io.kadai.task.rest.models.IsReadRepresentationModel;
import io.kadai.task.rest.models.TaskRepresentationModel;
import io.kadai.task.rest.models.TaskSummaryCollectionRepresentationModel;
import io.kadai.task.rest.models.TaskSummaryPagedRepresentationModel;
import io.kadai.task.rest.models.TransferTaskRepresentationModel;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all {@link Task} related endpoints. */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class TaskController implements TaskApi {

  private final TaskService taskService;
  private final TaskRepresentationModelAssembler taskRepresentationModelAssembler;
  private final TaskSummaryRepresentationModelAssembler taskSummaryRepresentationModelAssembler;
  private final BulkOperationResultsRepresentationModelAssembler
      bulkOperationResultsRepresentationModelAssembler;

  @Autowired
  TaskController(
      TaskService taskService,
      TaskRepresentationModelAssembler taskRepresentationModelAssembler,
      TaskSummaryRepresentationModelAssembler taskSummaryRepresentationModelAssembler,
      BulkOperationResultsRepresentationModelAssembler
          bulkOperationResultsRepresentationModelAssembler) {
    this.taskService = taskService;
    this.taskRepresentationModelAssembler = taskRepresentationModelAssembler;
    this.taskSummaryRepresentationModelAssembler = taskSummaryRepresentationModelAssembler;
    this.bulkOperationResultsRepresentationModelAssembler =
        bulkOperationResultsRepresentationModelAssembler;
  }

  // region CREATE

  @PostMapping(path = RestEndpoints.URL_TASKS)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> createTask(
      @RequestBody TaskRepresentationModel taskRepresentationModel)
      throws WorkbasketNotFoundException,
          ClassificationNotFoundException,
          TaskAlreadyExistException,
          InvalidArgumentException,
          AttachmentPersistenceException,
          ObjectReferencePersistenceException,
          NotAuthorizedOnWorkbasketException {

    if (!taskRepresentationModel.getAttachments().stream()
        .filter(att -> Objects.nonNull(att.getTaskId()))
        .filter(att -> !att.getTaskId().equals(taskRepresentationModel.getTaskId()))
        .toList()
        .isEmpty()) {
      throw new InvalidArgumentException(
          "An attachments' taskId must be empty or equal to the id of the task it belongs to");
    }

    Task fromResource = taskRepresentationModelAssembler.toEntityModel(taskRepresentationModel);
    Task createdTask = taskService.createTask(fromResource);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(taskRepresentationModelAssembler.toModel(createdTask));
  }

  // endregion

  // region READ

  @GetMapping(path = RestEndpoints.URL_TASKS)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<TaskSummaryPagedRepresentationModel> getTasks(
      HttpServletRequest request,
      @ParameterObject TaskQueryFilterParameter filterParameter,
      @ParameterObject TaskQueryFilterCustomFields filterCustomFields,
      @ParameterObject TaskQueryFilterCustomIntFields filterCustomIntFields,
      @ParameterObject TaskQueryGroupByParameter groupByParameter,
      @ParameterObject TaskQuerySortParameter sortParameter,
      @ParameterObject QueryPagingParameter<TaskSummary, TaskQuery> pagingParameter) {
    QueryParamsValidator.validateParams(
        request,
        TaskQueryFilterParameter.class,
        TaskQueryFilterCustomFields.class,
        TaskQueryFilterCustomIntFields.class,
        TaskQueryGroupByParameter.class,
        QuerySortParameter.class,
        QueryPagingParameter.class);

    if (QueryParamsValidator.hasQueryParameterValuesOrIsNotTrue(request, "owner-is-null")) {
      throw new InvalidArgumentException(
          "It is prohibited to use the param owner-is-null with values.");
    }

    TaskQuery query = taskService.createTaskQuery();

    filterParameter.apply(query);
    filterCustomFields.apply(query);
    filterCustomIntFields.apply(query);
    groupByParameter.apply(query);
    sortParameter.apply(query);

    List<TaskSummary> taskSummaries = pagingParameter.apply(query);

    TaskSummaryPagedRepresentationModel pagedModels =
        taskSummaryRepresentationModelAssembler.toPagedModel(
            taskSummaries, pagingParameter.getPageMetadata());
    return ResponseEntity.ok(pagedModels);
  }

  @GetMapping(path = RestEndpoints.URL_TASKS_ID)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> getTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException {
    Task task = taskService.getTask(taskId);

    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(task));
  }

  // endregion

  // region UPDATE

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_CLAIM)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> claimTask(
      @PathVariable("taskId") String taskId, @RequestBody(required = false) String userName)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    // TODO verify user
    Task updatedTask = taskService.claim(taskId);
    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(updatedTask));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_CLAIM_FORCE)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> forceClaimTask(
      @PathVariable("taskId") String taskId, @RequestBody(required = false) String userName)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    // TODO verify user
    Task updatedTask = taskService.forceClaim(taskId);
    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(updatedTask));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_SELECT_AND_CLAIM)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> selectAndClaimTask(
      @ParameterObject TaskQueryFilterParameter filterParameter,
      @ParameterObject TaskQueryFilterCustomFields filterCustomFields,
      @ParameterObject TaskQueryFilterCustomIntFields filterCustomIntFields,
      @ParameterObject TaskQuerySortParameter sortParameter)
      throws InvalidOwnerException, NotAuthorizedOnWorkbasketException {
    TaskQuery query = taskService.createTaskQuery();

    filterParameter.apply(query);
    filterCustomFields.apply(query);
    filterCustomIntFields.apply(query);
    sortParameter.apply(query);

    Optional<Task> selectedAndClaimedTask = taskService.selectAndClaim(query);

    return selectedAndClaimedTask
        .map(task -> ResponseEntity.ok(taskRepresentationModelAssembler.toModel(task)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping(path = RestEndpoints.URL_TASKS_ID_CLAIM)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> cancelClaimTask(
      @PathVariable("taskId") String taskId,
      @RequestParam(value = "keepOwner", defaultValue = "false") boolean keepOwner)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    Task updatedTask = taskService.cancelClaim(taskId, keepOwner);

    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(updatedTask));
  }

  @DeleteMapping(path = RestEndpoints.URL_TASKS_ID_CLAIM_FORCE)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> forceCancelClaimTask(
      @PathVariable("taskId") String taskId,
      @RequestParam(value = "keepOwner", defaultValue = "false") boolean keepOwner)
      throws TaskNotFoundException, InvalidTaskStateException, NotAuthorizedOnWorkbasketException {
    Task updatedTask = taskService.forceCancelClaim(taskId, keepOwner);
    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(updatedTask));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> requestReview(
      @PathVariable("taskId") String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    Task task = taskService.requestReview(taskId);
    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(task));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW_FORCE)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> forceRequestReview(
      @PathVariable("taskId") String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    Task task = taskService.forceRequestReview(taskId);
    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(task));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_REQUEST_CHANGES)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> requestChanges(
      @PathVariable("taskId") String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    Task task = taskService.requestChanges(taskId);
    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(task));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_REQUEST_CHANGES_FORCE)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> forceRequestChanges(
      @PathVariable("taskId") String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException {
    Task task = taskService.forceRequestChanges(taskId);
    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(task));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_COMPLETE)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> completeTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidOwnerException,
          InvalidTaskStateException,
          NotAuthorizedOnWorkbasketException {

    Task updatedTask = taskService.completeTask(taskId);

    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(updatedTask));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_COMPLETE_FORCE)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> forceCompleteTask(
      @PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidOwnerException,
          InvalidTaskStateException,
          NotAuthorizedOnWorkbasketException {

    Task updatedTask = taskService.forceCompleteTask(taskId);

    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(updatedTask));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_CANCEL)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> cancelTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException, InvalidTaskStateException {

    Task cancelledTask = taskService.cancelTask(taskId);

    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(cancelledTask));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_TERMINATE)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> terminateTask(
      @PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException {

    Task terminatedTask = taskService.terminateTask(taskId);

    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(terminatedTask));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_TRANSFER_WORKBASKET_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> transferTask(
      @PathVariable("taskId") String taskId,
      @PathVariable("workbasketId") String workbasketId,
      @RequestBody(required = false)
          TransferTaskRepresentationModel transferTaskRepresentationModel)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException {
    Task updatedTask;
    if (transferTaskRepresentationModel == null) {
      updatedTask = taskService.transfer(taskId, workbasketId);
    } else {
      updatedTask =
          taskService.transferWithOwner(
              taskId,
              workbasketId,
              transferTaskRepresentationModel.getOwner(),
              transferTaskRepresentationModel.getSetTransferFlag());
    }

    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(updatedTask));
  }

  @PostMapping(path = RestEndpoints.URL_TRANSFER_WORKBASKET_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<BulkOperationResultsRepresentationModel> transferTasks(
      @PathVariable("workbasketId") String workbasketId,
      @RequestBody TransferTaskRepresentationModel transferTaskRepresentationModel)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {
    List<String> taskIds = transferTaskRepresentationModel.getTaskIds();
    BulkOperationResults<String, KadaiException> result =
        taskService.transferTasksWithOwner(
            workbasketId,
            taskIds,
            transferTaskRepresentationModel.getOwner(),
            transferTaskRepresentationModel.getSetTransferFlag());

    BulkOperationResultsRepresentationModel repModel =
        bulkOperationResultsRepresentationModelAssembler.toModel(result);

    return ResponseEntity.ok(repModel);
  }

  @PutMapping(path = RestEndpoints.URL_TASKS_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> updateTask(
      @PathVariable("taskId") String taskId,
      @RequestBody TaskRepresentationModel taskRepresentationModel)
      throws TaskNotFoundException,
          ClassificationNotFoundException,
          InvalidArgumentException,
          ConcurrencyException,
          NotAuthorizedOnWorkbasketException,
          AttachmentPersistenceException,
          InvalidTaskStateException,
          ObjectReferencePersistenceException {
    if (!taskId.equals(taskRepresentationModel.getTaskId())) {
      throw new InvalidArgumentException(
          String.format(
              "TaskId ('%s') is not identical with the taskId of to "
                  + "object in the payload which should be updated. ID=('%s')",
              taskId, taskRepresentationModel.getTaskId()));
    }

    if (!taskRepresentationModel.getAttachments().stream()
        .filter(att -> Objects.nonNull(att.getTaskId()))
        .filter(att -> !att.getTaskId().equals(taskRepresentationModel.getTaskId()))
        .toList()
        .isEmpty()) {
      throw new InvalidArgumentException(
          "An attachments' taskId must be empty or equal to the id of the task it belongs to");
    }

    Task task = taskRepresentationModelAssembler.toEntityModel(taskRepresentationModel);
    task = taskService.updateTask(task);
    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(task));
  }

  @PostMapping(path = RestEndpoints.URL_TASKS_ID_SET_READ)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> setTaskRead(
      @PathVariable("taskId") String taskId, @RequestBody IsReadRepresentationModel isRead)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException {

    Task updatedTask = taskService.setTaskRead(taskId, isRead.getIsRead());

    return ResponseEntity.ok(taskRepresentationModelAssembler.toModel(updatedTask));
  }

  // endregion

  // region DELETE

  @DeleteMapping(path = RestEndpoints.URL_TASKS_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> deleteTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException,
          InvalidCallbackStateException {
    taskService.deleteTask(taskId);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(path = RestEndpoints.URL_TASKS_ID_FORCE)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskRepresentationModel> forceDeleteTask(
      @PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException,
          InvalidCallbackStateException {
    taskService.forceDeleteTask(taskId);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(path = RestEndpoints.URL_TASKS)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<TaskSummaryCollectionRepresentationModel> deleteTasks(
      @ParameterObject TaskQueryFilterParameter filterParameter,
      @ParameterObject TaskQueryFilterCustomFields filterCustomFields,
      @ParameterObject TaskQueryFilterCustomIntFields filterCustomIntFields)
      throws InvalidArgumentException, NotAuthorizedException {
    TaskQuery query = taskService.createTaskQuery();
    filterParameter.apply(query);
    filterCustomFields.apply(query);
    filterCustomIntFields.apply(query);

    List<TaskSummary> taskSummaries = query.list();

    List<String> taskIdsToDelete = taskSummaries.stream().map(TaskSummary::getId).toList();

    BulkOperationResults<String, KadaiException> result = taskService.deleteTasks(taskIdsToDelete);

    Set<String> failedIds = new HashSet<>(result.getFailedIds());
    List<TaskSummary> successfullyDeletedTaskSummaries =
        taskSummaries.stream().filter(not(summary -> failedIds.contains(summary.getId()))).toList();

    return ResponseEntity.ok(
        taskSummaryRepresentationModelAssembler.toKadaiCollectionModel(
            successfullyDeletedTaskSummaries));
  }

  // endregion

  // region TaskQuery

  public enum TaskQuerySortBy implements QuerySortBy<TaskQuery> {
    CLASSIFICATION_KEY(TaskQuery::orderByClassificationKey),
    CLASSIFICATION_NAME(TaskQuery::orderByClassificationName),
    POR_TYPE(TaskQuery::orderByPrimaryObjectReferenceType),
    POR_VALUE(TaskQuery::orderByPrimaryObjectReferenceValue),
    POR_COMPANY(TaskQuery::orderByPrimaryObjectReferenceCompany),
    POR_SYSTEM(TaskQuery::orderByPrimaryObjectReferenceSystem),
    POR_SYSTEM_INSTANCE(TaskQuery::orderByPrimaryObjectReferenceSystemInstance),
    STATE(TaskQuery::orderByState),
    NAME(TaskQuery::orderByName),
    DUE(TaskQuery::orderByDue),
    PLANNED(TaskQuery::orderByPlanned),
    RECEIVED(TaskQuery::orderByReceived),
    PRIORITY(TaskQuery::orderByPriority),
    CREATED(TaskQuery::orderByCreated),
    CLAIMED(TaskQuery::orderByClaimed),
    DOMAIN(TaskQuery::orderByDomain),
    TASK_ID(TaskQuery::orderByTaskId),
    MODIFIED(TaskQuery::orderByModified),
    CREATOR(TaskQuery::orderByCreator),
    NOTE(TaskQuery::orderByNote),
    OWNER(TaskQuery::orderByOwner),
    OWNER_LONG_NAME(TaskQuery::orderByOwnerLongName),
    BUSINESS_PROCESS_ID(TaskQuery::orderByBusinessProcessId),
    PARENT_BUSINESS_PROCESS_ID(TaskQuery::orderByParentBusinessProcessId),
    WORKBASKET_KEY(TaskQuery::orderByWorkbasketKey),
    CUSTOM_1((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_1, sort)),
    CUSTOM_2((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_2, sort)),
    CUSTOM_3((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_3, sort)),
    CUSTOM_4((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_4, sort)),
    CUSTOM_5((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_5, sort)),
    CUSTOM_6((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_6, sort)),
    CUSTOM_7((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_7, sort)),
    CUSTOM_8((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_8, sort)),
    CUSTOM_9((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_9, sort)),
    CUSTOM_10((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_10, sort)),
    CUSTOM_11((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_11, sort)),
    CUSTOM_12((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_12, sort)),
    CUSTOM_13((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_13, sort)),
    CUSTOM_14((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_14, sort)),
    CUSTOM_15((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_15, sort)),
    CUSTOM_16((query, sort) -> query.orderByCustomAttribute(TaskCustomField.CUSTOM_16, sort)),
    WORKBASKET_ID(TaskQuery::orderByWorkbasketId),
    WORKBASKET_NAME(TaskQuery::orderByWorkbasketName),
    ATTACHMENT_CLASSIFICATION_KEY(TaskQuery::orderByAttachmentClassificationKey),
    ATTACHMENT_CLASSIFICATION_NAME(TaskQuery::orderByAttachmentClassificationName),
    ATTACHMENT_CLASSIFICATION_ID(TaskQuery::orderByAttachmentClassificationId),
    ATTACHMENT_CHANNEL(TaskQuery::orderByAttachmentChannel),
    ATTACHMENT_REFERENCE(TaskQuery::orderByAttachmentReference),
    ATTACHMENT_RECEIVED(TaskQuery::orderByAttachmentReceived),
    COMPLETED(TaskQuery::orderByCompleted);

    private final BiConsumer<TaskQuery, SortDirection> consumer;

    TaskQuerySortBy(BiConsumer<TaskQuery, SortDirection> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void applySortByForQuery(TaskQuery query, SortDirection sortDirection) {
      consumer.accept(query, sortDirection);
    }
  }

  // Unfortunately this class is necessary, since spring can not inject the generic 'sort-by'
  // parameter from the super class.
  public static class TaskQuerySortParameter
      extends QuerySortParameter<TaskQuery, TaskQuerySortBy> {

    @ConstructorProperties({"sort-by", "order"})
    public TaskQuerySortParameter(List<TaskQuerySortBy> sortBy, List<SortDirection> order)
        throws InvalidArgumentException {
      super(sortBy, order);
    }

    // this getter is necessary for the documentation!
    @Override
    public List<TaskQuerySortBy> getSortBy() {
      return super.getSortBy();
    }
  }

  // endregion

}
