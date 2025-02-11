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

import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.QuerySortBy;
import io.kadai.common.rest.QuerySortParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.common.rest.util.QueryParamsValidator;
import io.kadai.task.api.TaskCommentQuery;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.exceptions.NotAuthorizedOnTaskCommentException;
import io.kadai.task.api.exceptions.TaskCommentNotFoundException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.TaskComment;
import io.kadai.task.rest.assembler.TaskCommentRepresentationModelAssembler;
import io.kadai.task.rest.models.TaskCommentCollectionRepresentationModel;
import io.kadai.task.rest.models.TaskCommentRepresentationModel;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import jakarta.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

/** Controller for all {@link TaskComment} related endpoints. */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class TaskCommentController implements TaskCommentApi {

  private final TaskService taskService;
  private final TaskCommentRepresentationModelAssembler taskCommentRepresentationModelAssembler;

  @Autowired
  TaskCommentController(
      TaskService taskService,
      TaskCommentRepresentationModelAssembler taskCommentRepresentationModelAssembler) {
    this.taskService = taskService;
    this.taskCommentRepresentationModelAssembler = taskCommentRepresentationModelAssembler;
  }

  @GetMapping(path = RestEndpoints.URL_TASK_COMMENT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<TaskCommentRepresentationModel> getTaskComment(
      @PathVariable("taskCommentId") String taskCommentId)
      throws TaskNotFoundException,
          TaskCommentNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException {
    TaskComment taskComment = taskService.getTaskComment(taskCommentId);

    TaskCommentRepresentationModel taskCommentRepresentationModel =
        taskCommentRepresentationModelAssembler.toModel(taskComment);

    return ResponseEntity.ok(taskCommentRepresentationModel);
  }

  @GetMapping(path = RestEndpoints.URL_TASK_COMMENTS)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<TaskCommentCollectionRepresentationModel> getTaskComments(
      @PathVariable("taskId") String taskId,
      HttpServletRequest request,
      @ParameterObject TaskCommentQueryFilterParameter filterParameter,
      @ParameterObject TaskCommentQuerySortParameter sortParameter,
      @ParameterObject QueryPagingParameter<TaskComment, TaskCommentQuery> pagingParameter) {

    QueryParamsValidator.validateParams(
        request,
        TaskCommentQueryFilterParameter.class,
        QuerySortParameter.class,
        QueryPagingParameter.class);

    TaskCommentQuery query = taskService.createTaskCommentQuery();

    query.taskIdIn(taskId);
    filterParameter.apply(query);
    sortParameter.apply(query);

    List<TaskComment> taskComments = pagingParameter.apply(query);

    TaskCommentCollectionRepresentationModel taskCommentListResource =
        taskCommentRepresentationModelAssembler.toKadaiCollectionModel(taskComments);

    return ResponseEntity.ok(taskCommentListResource);
  }

  @DeleteMapping(path = RestEndpoints.URL_TASK_COMMENT)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskCommentRepresentationModel> deleteTaskComment(
      @PathVariable("taskCommentId") String taskCommentId)
      throws TaskNotFoundException,
          TaskCommentNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnTaskCommentException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException {
    taskService.deleteTaskComment(taskCommentId);

    return ResponseEntity.noContent().build();
  }

  @PutMapping(path = RestEndpoints.URL_TASK_COMMENT)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskCommentRepresentationModel> updateTaskComment(
      @PathVariable("taskCommentId") String taskCommentId,
      @RequestBody TaskCommentRepresentationModel taskCommentRepresentationModel)
      throws TaskNotFoundException,
          TaskCommentNotFoundException,
          InvalidArgumentException,
          ConcurrencyException,
          NotAuthorizedOnTaskCommentException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException {
    if (!taskCommentId.equals(taskCommentRepresentationModel.getTaskCommentId())) {
      throw new InvalidArgumentException(
          String.format(
              "TaskCommentId ('%s') is not identical with the id"
                  + " of the object in the payload which should be updated",
              taskCommentId));
    }

    TaskComment taskComment =
        taskCommentRepresentationModelAssembler.toEntityModel(taskCommentRepresentationModel);

    taskComment = taskService.updateTaskComment(taskComment);

    return ResponseEntity.ok(taskCommentRepresentationModelAssembler.toModel(taskComment));
  }

  @PostMapping(path = RestEndpoints.URL_TASK_COMMENTS)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<TaskCommentRepresentationModel> createTaskComment(
      @PathVariable("taskId") String taskId,
      @RequestBody TaskCommentRepresentationModel taskCommentRepresentationModel)
      throws InvalidArgumentException, TaskNotFoundException, NotAuthorizedOnWorkbasketException {
    taskCommentRepresentationModel.setTaskId(taskId);

    TaskComment taskCommentFromResource =
        taskCommentRepresentationModelAssembler.toEntityModel(taskCommentRepresentationModel);
    TaskComment createdTaskComment = taskService.createTaskComment(taskCommentFromResource);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(taskCommentRepresentationModelAssembler.toModel(createdTaskComment));
  }

  public enum TaskCommentQuerySortBy implements QuerySortBy<TaskCommentQuery> {
    CREATED(TaskCommentQuery::orderByCreated),
    MODIFIED(TaskCommentQuery::orderByModified);

    private final BiConsumer<TaskCommentQuery, SortDirection> consumer;

    TaskCommentQuerySortBy(BiConsumer<TaskCommentQuery, SortDirection> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void applySortByForQuery(TaskCommentQuery query, SortDirection sortDirection) {
      consumer.accept(query, sortDirection);
    }
  }

  public static class TaskCommentQuerySortParameter
      extends QuerySortParameter<TaskCommentQuery, TaskCommentQuerySortBy> {

    @ConstructorProperties({"sort-by", "order"})
    public TaskCommentQuerySortParameter(
        List<TaskCommentQuerySortBy> sortBy, List<SortDirection> order)
        throws InvalidArgumentException {
      super(sortBy, order);
    }

    // this getter is necessary for the documentation!
    @Override
    public List<TaskCommentQuerySortBy> getSortBy() {
      return super.getSortBy();
    }
  }
}
