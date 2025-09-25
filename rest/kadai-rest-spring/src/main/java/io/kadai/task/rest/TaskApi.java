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

package io.kadai.task.rest;

import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.task.api.TaskQuery;
import io.kadai.task.api.exceptions.AttachmentPersistenceException;
import io.kadai.task.api.exceptions.InvalidCallbackStateException;
import io.kadai.task.api.exceptions.InvalidOwnerException;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.exceptions.ObjectReferencePersistenceException;
import io.kadai.task.api.exceptions.ReopenTaskWithCallbackException;
import io.kadai.task.api.exceptions.ServiceLevelViolationException;
import io.kadai.task.api.exceptions.TaskAlreadyExistException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.task.rest.models.BulkOperationResultsRepresentationModel;
import io.kadai.task.rest.models.DistributionTasksRepresentationModel;
import io.kadai.task.rest.models.IsReadRepresentationModel;
import io.kadai.task.rest.models.TaskBulkUpdateRepresentationModel;
import io.kadai.task.rest.models.TaskIdListRepresentationModel;
import io.kadai.task.rest.models.TaskPatchRepresentationModel;
import io.kadai.task.rest.models.TaskRepresentationModel;
import io.kadai.task.rest.models.TaskSummaryCollectionRepresentationModel;
import io.kadai.task.rest.models.TaskSummaryPagedRepresentationModel;
import io.kadai.task.rest.models.TransferTaskRepresentationModel;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface TaskApi {

  /**
   * This endpoint creates a persistent Task.
   *
   * @param taskRepresentationModel the Task which should be created.
   * @return the created Task
   * @throws WorkbasketNotFoundException if the referenced Workbasket does not exist
   * @throws ClassificationNotFoundException if the referenced Classification does not exist
   * @throws NotAuthorizedOnWorkbasketException if the current user is not authorized to append a
   *     Task to the referenced Workbasket
   * @throws TaskAlreadyExistException if the requested Task already exists.
   * @throws InvalidArgumentException if any input is semantically wrong.
   * @throws AttachmentPersistenceException if an Attachment with ID will be added multiple times
   *     without using the task-methods
   * @throws ObjectReferencePersistenceException if an ObjectReference with ID will be added
   *     multiple times without using the task-methods
   * @throws ServiceLevelViolationException if due and planned do not match service level
   * @title Create a new Task
   */
  @Operation(
      summary = "Create a new Task",
      description = "This endpoint creates a persistent Task.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the Task which should be created.",
              content =
                  @Content(
                      mediaType = MediaTypes.HAL_JSON_VALUE,
                      schema = @Schema(implementation = TaskRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  "{"
                                      + "\"priority\" : 0,"
                                      + "\"manualPriority\" : -1,"
                                      + "\"classificationSummary\" : {"
                                      + "\"key\" : \"L11010\","
                                      + "\"priority\" : 0"
                                      + "},"
                                      + "\"workbasketSummary\" : {"
                                      + "\"workbasketId\" : "
                                      + "\"WBI:100000000000000000000000000000000004\","
                                      + "\"markedForDeletion\" : false"
                                      + "},"
                                      + "\"primaryObjRef\" : {"
                                      + "\"company\" : \"MyCompany1\","
                                      + "\"system\" : \"MySystem1\","
                                      + "\"systemInstance\" : \"MyInstance1\","
                                      + "\"type\" : \"MyType1\","
                                      + "\"value\" : \"00000001\""
                                      + "},"
                                      + "\"secondaryObjectReferences\" : [ {"
                                      + "\"company\" : \"company\","
                                      + "\"system\" : \"system\","
                                      + "\"systemInstance\" : \"systemInstance\","
                                      + "\"type\" : \"type\","
                                      + "\"value\" : \"value\""
                                      + "} ],"
                                      + "\"customAttributes\" : [ ],"
                                      + "\"callbackInfo\" : [ ],"
                                      + "\"attachments\" : [ ],"
                                      + "\"read\" : false,"
                                      + "\"transferred\" : false"
                                      + "}"))),
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "the created Task",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = TaskRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description =
                "CLASSIFICATION_WITH_ID_NOT_FOUND, CLASSIFICATION_WITH_KEY_NOT_FOUND, "
                    + "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            ClassificationNotFoundException.class,
                            WorkbasketNotFoundException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "409",
            description =
                "TASK_ALREADY_EXISTS, ATTACHMENT_ALREADY_EXISTS, OBJECT_REFERENCE_ALREADY_EXISTS",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            TaskAlreadyExistException.class,
                            AttachmentPersistenceException.class,
                            ObjectReferencePersistenceException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "422",
            description = "SERVICE_LEVEL_VIOLATION",
            content = {
              @Content(schema = @Schema(implementation = ServiceLevelViolationException.class))
            })
      })
  @PostMapping(path = RestEndpoints.URL_TASKS)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> createTask(
      @RequestBody TaskRepresentationModel taskRepresentationModel)
      throws WorkbasketNotFoundException,
          ClassificationNotFoundException,
          TaskAlreadyExistException,
          InvalidArgumentException,
          ServiceLevelViolationException,
          AttachmentPersistenceException,
          ObjectReferencePersistenceException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint retrieves a list of existing Tasks. Filters can be applied.
   *
   * @title Get a list of all Tasks
   * @param request the HTTP request
   * @param filterParameter the filter parameters
   * @param filterCustomFields the filter parameters regarding TaskCustomFields
   * @param filterCustomIntFields the filter parameters regarding TaskCustomIntFields * @param
   * @param groupByParameter the group by parameters
   * @param sortParameter the sort parameters
   * @param pagingParameter the paging parameters
   * @return the Tasks with the given filter, sort and paging options.
   * @throws InvalidArgumentException if the query parameter "owner-is-null" has values
   */
  @Operation(
      summary = "Get a list of all Tasks",
      description = "This endpoint retrieves a list of existing Tasks. Filters can be applied.",
      parameters = {
        @Parameter(name = "por-type", example = "VNR"),
        @Parameter(name = "por-value", example = "22334455"),
        @Parameter(name = "sort-by", example = "NAME")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the Tasks with the given filter, sort and paging options.",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = TaskSummaryPagedRepresentationModel.class))
            })
      })
  @GetMapping(path = RestEndpoints.URL_TASKS)
  ResponseEntity<TaskSummaryPagedRepresentationModel> getTasks(
      HttpServletRequest request,
      @ParameterObject TaskQueryFilterParameter filterParameter,
      @ParameterObject TaskQueryFilterCustomFields filterCustomFields,
      @ParameterObject TaskQueryFilterCustomIntFields filterCustomIntFields,
      @ParameterObject TaskQueryGroupByParameter groupByParameter,
      @ParameterObject TaskQuerySortParameter sortParameter,
      @ParameterObject QueryPagingParameter<TaskSummary, TaskQuery> pagingParameter);

  @GetMapping(path = RestEndpoints.URL_TASKS_ID)
  ResponseEntity<TaskRepresentationModel> getTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint claims a Task if possible.
   *
   * @param taskId the Id of the Task which should be claimed
   * @param userName TODO: this is currently not used
   * @return the claimed Task
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidTaskStateException if the state of the requested Task is not READY.
   * @throws InvalidOwnerException if the Task is already claimed by someone else.
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permissions for the
   *     requested Task.
   * @title Claim a Task
   */
  @Operation(
      summary = "Claim a Task",
      description = "This endpoint claims a Task if possible.",
      parameters = {
        @Parameter(
            name = "taskId",
            required = true,
            description = "the Id of the Task which should be claimed",
            example = "TKI:000000000000000000000000000000000003")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the claimed Task",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = TaskRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {InvalidOwnerException.class, InvalidTaskStateException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_CLAIM)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> claimTask(
      @PathVariable("taskId") String taskId, @RequestBody(required = false) String userName)
      throws TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException;

  /**
   * This endpoint force claims a Task if possible even if it is already claimed by someone else.
   *
   * @param taskId the Id of the Task which should be force claimed
   * @param userName TODO: this is currently not used
   * @return the force claimed Task
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidTaskStateException if the state of Task with taskId is in an END_STATE.
   * @throws InvalidOwnerException cannot be thrown.
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permissions for the
   *     requested Task.
   * @title Force claim a Task
   */
  @Operation(
      summary = "Force claim a Task",
      description =
          "This endpoint force claims a Task if possible even if it is already claimed by someone "
              + "else.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the Id of the Task which should be force claimed",
            required = true,
            example = "TKI:000000000000000000000000000000000003")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the force claimed Task",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = TaskRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {InvalidOwnerException.class, InvalidTaskStateException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_CLAIM_FORCE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> forceClaimTask(
      @PathVariable("taskId") String taskId, @RequestBody(required = false) String userName)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint selects the first Task returned by the Task Query and claims it.
   *
   * @param filterParameter the filter parameters
   * @param filterCustomFields the filter parameters regarding TaskCustomFields
   * @param filterCustomIntFields the filter parameters regarding TaskCustomIntFields
   * @param sortParameter the sort parameters
   * @return the claimed Task or 404 if no Task is found
   * @throws InvalidOwnerException if the Task is already claimed by someone else
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permission for the
   *     Workbasket the Task is in
   * @title Select and claim a Task
   */
  @Operation(
      summary = "Select and claim a Task",
      description =
          "This endpoint selects the first Task returned by the Task Query and claims it.",
      parameters = {@Parameter(name = "custom14", example = "abc")},
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the claimed Task",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = TaskRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "if no Task is found",
            content = {@Content(schema = @Schema())}),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER",
            content = {@Content(schema = @Schema(implementation = InvalidOwnerException.class))}),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            })
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_SELECT_AND_CLAIM)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> selectAndClaimTask(
      @ParameterObject TaskQueryFilterParameter filterParameter,
      @ParameterObject TaskQueryFilterCustomFields filterCustomFields,
      @ParameterObject TaskQueryFilterCustomIntFields filterCustomIntFields,
      @ParameterObject TaskQuerySortParameter sortParameter)
      throws InvalidOwnerException, NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint cancels the claim of an existing Task if it was claimed by the current user
   * before.
   *
   * @param taskId the Id of the requested Task.
   * @param keepOwner flag whether or not to keep the owner despite the cancel claim
   * @return the unclaimed Task.
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidTaskStateException if the Task is already in an end state.
   * @throws InvalidOwnerException if the Task is claimed by a different user.
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permission for the
   *     Workbasket the Task is in
   * @title Cancel a claimed Task
   */
  @Operation(
      summary = "Cancel a claimed Task",
      description =
          "This endpoint cancels the claim of an existing Task if it was claimed by the current "
              + "user before.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the Id of the requested Task.",
            required = true,
            example = "TKI:000000000000000000000000000000000002"),
        @Parameter(
            name = "keepOwner",
            description = "flag whether or not to keep the owner despite the cancel claim")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the unclaimed Task",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = TaskRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {InvalidOwnerException.class, InvalidTaskStateException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @DeleteMapping(path = RestEndpoints.URL_TASKS_ID_CLAIM)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> cancelClaimTask(
      @PathVariable("taskId") String taskId,
      @RequestParam(value = "keepOwner", defaultValue = "false") boolean keepOwner)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint force cancels the claim of an existing Task.
   *
   * @param taskId the Id of the requested Task.
   * @param keepOwner flag whether or not to keep the owner despite the cancel claim
   * @return the unclaimed Task.
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidTaskStateException if the Task is already in an end state.
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permission for the
   *     Workbasket the Task is in
   * @title Force cancel a claimed Task
   */
  @Operation(
      summary = "Force cancel a claimed Task",
      description = "This endpoint force cancels the claim of an existing Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the Id of the requested Task.",
            required = true,
            example = "TKI:000000000000000000000000000000000002"),
        @Parameter(
            name = "keepOwner",
            description = "flag whether or not to keep the owner despite the cancel claim.")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the unclaimed Task.",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_STATE",
            content = {
              @Content(schema = @Schema(implementation = InvalidTaskStateException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @DeleteMapping(path = RestEndpoints.URL_TASKS_ID_CLAIM_FORCE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> forceCancelClaimTask(
      @PathVariable("taskId") String taskId,
      @RequestParam(value = "keepOwner", defaultValue = "false") boolean keepOwner)
      throws TaskNotFoundException, InvalidTaskStateException, NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint request a review on the specified Task.
   *
   * @param taskId taskId the id of the relevant Task
   * @param body the body of the request, that can contain the workbasketId and the ownerId
   * @return the Task after a review has been requested
   * @throws InvalidTaskStateException if the state of the Task with taskId is not CLAIMED
   * @throws TaskNotFoundException if the Task with taskId wasn't found
   * @throws InvalidOwnerException if the Task is claimed by another user
   * @throws NotAuthorizedOnWorkbasketException if the current user has no READ permissions for the
   *     Workbasket the Task is in
   * @title Request a review on a Task
   */
  @Operation(
      summary = "Request a review on a Task",
      description = "This endpoint requests a review on the specified Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the id of the relevant Task",
            required = true,
            example = "TKI:000000000000000000000000000000000032")
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = false,
              description = "Optional JSON body with workbasketId and ownerId",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema =
                          @Schema(
                              example =
                                  """
        {
          "workbasketId": "WBI:000000000000000000000000000000000001",
          "ownerId": "user-1-1"
        }
      """))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the Task after a review has been requested",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {InvalidOwnerException.class, InvalidTaskStateException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> requestReview(
      @PathVariable("taskId") String taskId,
      @RequestBody(required = false) Map<String, String> body)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint force request a review on the specified Task.
   *
   * @param taskId taskId the id of the relevant Task
   * @return the Task after a review has been requested
   * @throws InvalidTaskStateException if the state of the Task with taskId is not CLAIMED
   * @throws TaskNotFoundException if the Task with taskId wasn't found
   * @throws InvalidOwnerException cannot be thrown
   * @throws NotAuthorizedOnWorkbasketException if the current user has no READ permissions for the
   *     Workbasket the Task is in
   * @title Force request a review on a Task
   */
  @Operation(
      summary = "Force request a review on a Task",
      description = "This endpoint force requests a review on the specified Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the id of the relevant Task",
            required = true,
            example = "TKI:000000000000000000000000000000000101")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the Task after a review has been requested",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {InvalidOwnerException.class, InvalidTaskStateException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW_FORCE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> forceRequestReview(@PathVariable("taskId") String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint request changes on the specified Task.
   *
   * @param taskId the id of the relevant Task
   * @param body the body of the request, that can contain the workbasketId and the ownerId
   * @return the Task after changes have been requested
   * @throws InvalidTaskStateException if the state of the Task with taskId is not IN_REVIEW
   * @throws TaskNotFoundException if the Task with taskId wasn't found
   * @throws InvalidOwnerException if the Task is claimed by another user
   * @throws NotAuthorizedOnWorkbasketException if the current user has no READ permissions for the
   *     Workbasket the Task is in
   * @title Request changes on a Task
   */
  @Operation(
      summary = "Request changes on a Task",
      description = "This endpoint requests changes on the specified Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the id of the relevant Task",
            required = true,
            example = "TKI:000000000000000000000000000000000136")
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = false,
              description = "Optional JSON body with workbasketId and ownerId",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema =
                          @Schema(
                              example =
                                  """
        {
          "workbasketId": "WBI:000000000000000000000000000000000001",
          "ownerId": "user-1-1"
        }
      """))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Changes requested successfully",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {InvalidOwnerException.class, InvalidTaskStateException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_REQUEST_CHANGES)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> requestChanges(
      @PathVariable("taskId") String taskId,
      @RequestBody(required = false) Map<String, String> body)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint force requests changes on a Task.
   *
   * @param taskId the Id of the Task on which a review should be requested
   * @return the change requested Task
   * @throws InvalidTaskStateException if the Task with taskId is in an end state
   * @throws TaskNotFoundException if the Task with taskId wasn't found
   * @throws InvalidOwnerException cannot be thrown
   * @throws NotAuthorizedOnWorkbasketException if the current user has no READ permissions for the
   *     Workbasket the Task is in
   * @title Force request changes on a Task
   */
  @Operation(
      summary = "Force request changes on a Task",
      description = "This endpoint force requests changes on the specified Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the Id of the Task on which a review should be requested",
            required = true,
            example = "TKI:000000000000000000000000000000000100")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the change requested Task",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {InvalidOwnerException.class, InvalidTaskStateException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_REQUEST_CHANGES_FORCE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> forceRequestChanges(@PathVariable("taskId") String taskId)
      throws InvalidTaskStateException,
          TaskNotFoundException,
          InvalidOwnerException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint completes a Task.
   *
   * @param taskId Id of the requested Task to complete.
   * @return the completed Task
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidOwnerException if current user is not the owner of the Task or an administrator.
   * @throws InvalidTaskStateException if Task wasn't claimed previously.
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permission for the
   *     Workbasket the Task is in
   * @title Complete a Task
   */
  @Operation(
      summary = "Complete a Task",
      description = "This endpoint completes a Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "Id of the requested Task to complete.",
            example = "TKI:000000000000000000000000000000000003",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the completed Task",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {InvalidOwnerException.class, InvalidTaskStateException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_COMPLETE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> completeTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidOwnerException,
          InvalidTaskStateException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint completes multiple Tasks.
   *
   * <p>Processes all provided task IDs and marks them as completed. In case of success (no error),
   * the task ID will not appear in the response. The response contains only the IDs that failed,
   * along with their error codes.
   *
   * @param completeTasksRepresentationModel containing the list of task IDs that are to be
   *     completed.
   * @return BulkOperationResultsRepresentationModel containing the list of failed task IDs with
   *     error codes.
   */
  @Operation(
      summary = "Bulk complete Tasks",
      description = "This endpoint completes multiple Tasks in one call.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "List with the IDs of the tasks to be completed",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = TaskIdListRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                          {
                            "taskIds": [
                              "TKI:000000000000000000000000000000000003",
                              "TKI:000000000000000000000000000000000002"
                            ]
                          }
                          """))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of failed IDs with their error codes",
            content =
                @Content(
                    schema =
                        @Schema(implementation = BulkOperationResultsRepresentationModel.class)))
      })
  @PatchMapping(path = RestEndpoints.URL_TASKS_BULK_COMPLETE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<BulkOperationResultsRepresentationModel> bulkComplete(
      @RequestBody TaskIdListRepresentationModel completeTasksRepresentationModel);

  /**
   * This endpoint force completes a Task.
   *
   * @param taskId Id of the requested Task to force complete.
   * @return the force completed Task
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidOwnerException cannot be thrown.
   * @throws InvalidTaskStateException if the state of the Task with taskId is TERMINATED or
   *     CANCELED
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permission for the
   *     Workbasket the Task is in
   * @title Force complete a Task
   */
  @Operation(
      summary = "Force complete a Task",
      description = "This endpoint force completes a Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "Id of the requested Task to force complete.",
            example = "TKI:000000000000000000000000000000000003",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the force completed Task",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_OWNER, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {InvalidOwnerException.class, InvalidTaskStateException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_COMPLETE_FORCE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> forceCompleteTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidOwnerException,
          InvalidTaskStateException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint force completes multiple Tasks.
   *
   * @param completeTasksRepresentationModel Containing the list of task IDs that are to be
   *     completed.
   * @return BulkOperationResultsRepresentationModel containing the list of failed task IDs with
   *     error codes.
   */
  @Operation(
      summary = "Bulk force complete Tasks",
      description = "This endpoint force‑completes multiple Tasks in one call.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "List with the IDs of the tasks to be completed",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = TaskIdListRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                              {
                                "taskIds": [
                                  "TKI:000000000000000000000000000000000003",
                                  "TKI:000000000000000000000000000000000002"
                                ]
                              }
                              """))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of failed IDs with their error codes",
            content =
                @Content(
                    schema =
                        @Schema(implementation = BulkOperationResultsRepresentationModel.class)))
      })
  @PatchMapping(path = RestEndpoints.URL_TASKS_BULK_COMPLETE_FORCE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<BulkOperationResultsRepresentationModel> bulkForceComplete(
      @RequestBody TaskIdListRepresentationModel completeTasksRepresentationModel);

  /**
   * This endpoint cancels a Task. Cancellation marks a Task as obsolete. The actual work the Task
   * was referring to is no longer required
   *
   * @param taskId Id of the requested Task to cancel.
   * @return the cancelled Task
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidTaskStateException if the task is not in state READY or CLAIMED
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permission for the
   *     Workbasket the Task is in
   * @title Cancel a Task
   */
  @Operation(
      summary = "Cancel a Task",
      description =
          "This endpoint cancels a Task. Cancellation marks a Task as obsolete. The actual work "
              + "the Task was referring to is no longer required",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "Id of the requested Task to cancel.",
            example = "TKI:000000000000000000000000000000000026",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the cancelled Task",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_STATE",
            content = {
              @Content(schema = @Schema(implementation = InvalidTaskStateException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_CANCEL)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> cancelTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException, InvalidTaskStateException;

  /**
   * This endpoint terminates a Task. Termination is an administrative action to complete a Task.
   *
   * @param taskId Id of the requested Task to terminate.
   * @return the terminated Task
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidTaskStateException if the task is not in state READY or CLAIMED
   * @throws NotAuthorizedException if the current user isn't an administrator (ADMIN/TASKADMIN)
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct permissions
   * @title Terminate a Task
   */
  @Operation(
      summary = "Terminate a Task",
      description =
          "This endpoint terminates a Task. Termination is an administrative action to complete a "
              + "Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "Id of the requested Task to terminate.",
            required = true,
            example = "TKI:000000000000000000000000000000000000")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the terminated Task",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_STATE",
            content = {
              @Content(schema = @Schema(implementation = InvalidTaskStateException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN, NOT_AUTHORIZED",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            NotAuthorizedOnWorkbasketException.class,
                            NotAuthorizedException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_TERMINATE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> terminateTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint transfers a given Task to a given Workbasket, if possible.
   *
   * @title Transfer a Task to another Workbasket
   * @param taskId the Id of the Task which should be transferred
   * @param workbasketId the Id of the destination Workbasket
   * @param transferTaskRepresentationModel sets the transfer flag of the Task (default: true) and
   *     owner of the task
   * @return the successfully transferred Task.
   * @throws TaskNotFoundException if the requested Task does not exist
   * @throws WorkbasketNotFoundException if the requested Workbasket does not exist
   * @throws NotAuthorizedOnWorkbasketException if the current user has no authorization to transfer
   *     the Task.
   * @throws InvalidTaskStateException if the Task is in a state which does not allow transferring.
   */
  @Operation(
      summary = "Transfer a Task to another Workbasket",
      description = "This endpoint transfers a Task to a given Workbasket, if possible.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the Id of the Task which should be transferred",
            example = "TKI:000000000000000000000000000000000004",
            required = true),
        @Parameter(
            name = "workbasketId",
            description = "the Id of the destination Workbasket",
            example = "WBI:100000000000000000000000000000000001",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "sets the transfer flag of the task (default: true) and owner of the task",
              content =
                  @Content(
                      schema = @Schema(implementation = TaskRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                  {
                    "owner": "user-1-1",
                    "setTransferFlag": false
                  }
              """))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the successfully transferred Task.",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_STATE",
            content = {
              @Content(schema = @Schema(implementation = InvalidTaskStateException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description =
                "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND, TASK_NOT_FOUND",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {WorkbasketNotFoundException.class, TaskNotFoundException.class}))
            })
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_TRANSFER_WORKBASKET_ID)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> transferTask(
      @PathVariable("taskId") String taskId,
      @PathVariable("workbasketId") String workbasketId,
      @RequestBody(required = false)
          TransferTaskRepresentationModel transferTaskRepresentationModel)
      throws TaskNotFoundException,
          WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException;

  /**
   * This endpoint transfers a list of Tasks listed in the body to a given Workbasket, if possible.
   * Tasks that can be transfered without throwing an exception get transferred independent of other
   * Tasks. If the transfer of a Task throws an exception, then the Task will remain in the old
   * Workbasket.
   *
   * @title Transfer Tasks to another Workbasket
   * @param workbasketId the Id of the destination Workbasket
   * @param transferTaskRepresentationModel JSON formatted request body containing the TaskIds,
   *     owner and setTransferFlag of tasks to be transferred; owner and setTransferFlag are
   *     optional, while the TaskIds are mandatory
   * @return the taskIds and corresponding ErrorCode of tasks failed to be transferred
   * @throws WorkbasketNotFoundException if the requested Workbasket does not exist
   * @throws NotAuthorizedOnWorkbasketException if the current user has no authorization to transfer
   *     the Task
   */
  @Operation(
      summary = "Transfer Tasks to another Workbasket",
      description =
          "This endpoint transfers a list of Tasks listed in the body to a given Workbasket, if "
              + "possible. Tasks that can be transfered without throwing an exception get "
              + "transferred independent of other Tasks. If the transfer of a Task throws an "
              + "exception, then the Task will remain in the old Workbasket.",
      parameters = {
        @Parameter(
            name = "workbasketId",
            description = "the Id of the destination Workbasket",
            example = "WBI:100000000000000000000000000000000001",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "JSON formatted request body containing the TaskIds, owner and setTransferFlag of"
                      + " tasks to be transferred; owner and setTransferFlag are optional, while "
                      + "the TaskIds are mandatory",
              content =
                  @Content(
                      schema = @Schema(implementation = TransferTaskRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                          {
                            "owner": "user-1-1",
                            "setTransferFlag": true,
                            "taskIds": ["TKI:000000000000000000000000000000000001", \
                            "TKI:000000000000000000000000000000000002"]
                          }
                      """))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "the taskIds and corresponding ErrorCode of tasks failed to be transferred",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema =
                        @Schema(implementation = BulkOperationResultsRepresentationModel.class))),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
            })
      })
  @PostMapping(path = RestEndpoints.URL_TRANSFER_WORKBASKET_ID)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<BulkOperationResultsRepresentationModel> transferTasks(
      @PathVariable("workbasketId") String workbasketId,
      @RequestBody TransferTaskRepresentationModel transferTaskRepresentationModel)
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException;

  /**
   * This endpoint reopens the requested Task.
   *
   * @title Reopen a Task
   * @param taskId the id of the Task which should be reopened
   * @return the reopened Task
   * @throws TaskNotFoundException if the requested Task does not exist
   * @throws NotAuthorizedOnWorkbasketException if the current user is not authorized
   * @throws InvalidTaskStateException if the Task is not in a non-final end-state
   * @throws ReopenTaskWithCallbackException if the Task has a callback attached
   */
  @Operation(
      summary = "Reopen a Task",
      description = "This endpoint reopens a Task if it is a non-final end-state",
      parameters = {
        @Parameter(
            name = "taskId",
            required = true,
            description = "the Id of the Task which should be reopened",
            example = "TKI:000000000000000000000000000000000003")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the reopened Task",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = TaskRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_STATE, REOPEN_TASK_WITH_CALLBACK",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            InvalidTaskStateException.class,
                            ReopenTaskWithCallbackException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_REOPEN)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> reopenTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          NotAuthorizedOnWorkbasketException,
          InvalidTaskStateException,
          ReopenTaskWithCallbackException;

  /**
   * Distributes tasks to one or more destination workbaskets based on the provided distribution
   * strategy and additional information. The tasks to be distributed are passed in the request
   * body.
   *
   * <p>This endpoint provides flexible distribution options:
   *
   * <ul>
   *   <li>If destination workbasket IDs are provided, tasks will be distributed to those specific
   *       workbaskets.
   *   <li>If a distribution strategy name is provided, the specified strategy will determine how
   *       tasks are allocated.
   *   <li>Additional information can be passed to further customize the distribution logic.
   * </ul>
   *
   * <p>Tasks that cannot be distributed (e.g., due to errors) will remain in their original
   * workbaskets.
   *
   * @title Distribute Tasks
   * @param distributionTasksRepresentationModel a JSON-formatted request body containing:
   *     <ul>
   *       <li>{@code taskIds} ({@code List<String>}): A list of task IDs to be distributed from a
   *           given sourceworkbasket (optional).
   *       <li>{@code destinationWorkbasketIds} ({@code List<String>}): A list of destination
   *           workbasket IDs (optional).
   *       <li>{@code distributionStrategyName} ({@code String}): The name of the distribution
   *           strategy to use (optional).
   *       <li>{@code additionalInformation} ({@code Map<String, Object>}): Additional details to
   *           customize the distribution logic (optional).
   *     </ul>
   *
   * @param workbasketId the ID of the workbasket from which the tasks are to be distributed.
   * @return a {@link ResponseEntity} containing a {@link BulkOperationResultsRepresentationModel}
   *     that includes:
   *     <ul>
   *       <li>A map of task IDs and corresponding error codes for failed distributions.
   *     </ul>
   *
   * @throws WorkbasketNotFoundException if the destination workbaskets cannot be found.
   * @throws NotAuthorizedOnWorkbasketException if the current user lacks permission to perform this
   *     operation.
   * @throws InvalidTaskStateException if one or more tasks are in a state that prevents
   *     distribution.
   * @throws TaskNotFoundException if specific tasks referenced in the operation cannot be found.
   * @throws InvalidArgumentException if no task IDs are provided.
   */
  @Operation(
      summary = "Distribute Tasks to Destination Workbaskets",
      description =
          """
        This endpoint distributes tasks to one or more destination workbaskets based on the
        provided distribution strategy and additional information.

        - If destination workbasket IDs are provided, tasks will be distributed to those specific
          workbaskets.
        - If a distribution strategy name is provided, the specified strategy will determine how
          tasks are allocated.
        - Additional information can be passed to further customize the distribution logic.

        Tasks that cannot be distributed (e.g., due to errors) will remain in their original
        workbaskets.
      """,
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "The JSON-formatted request body containing the details for task distribution.",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = DistributionTasksRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                        {
                          "taskIds": ["TKI:abcdef1234567890abcdef1234567890"],
                          "destinationWorkbasketIds": [
                              "WBI:abcdef1234567890abcdef1234567890",
                              "WBI:1234567890abcdef1234567890abcdef"
                          ],
                          "distributionStrategyName": "CustomDistributionStrategy"
                          }
      """))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "The bulk operation result containing details of successfully "
                    + "distributed tasks or errors.",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema =
                        @Schema(implementation = BulkOperationResultsRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_STATE",
            content = {
              @Content(schema = @Schema(implementation = InvalidTaskStateException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description =
                "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND, TASK_NOT_FOUND",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {WorkbasketNotFoundException.class, TaskNotFoundException.class}))
            })
      })
  ResponseEntity<BulkOperationResultsRepresentationModel> distributeTasks(
      @RequestBody DistributionTasksRepresentationModel distributionTasksRepresentationModel,
      @PathVariable("workbasketId") String workbasketId)
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException,
          InvalidArgumentException;

  /**
   * This endpoint updates a requested Task.
   *
   * @param taskId the Id of the Task which should be updated
   * @param taskRepresentationModel the new Task for the requested id.
   * @return the updated Task
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws ClassificationNotFoundException if the updated Classification does not exist.
   * @throws InvalidArgumentException if any semantically invalid parameter was provided
   * @throws ConcurrencyException if the Task has been updated by a different process in the
   *     meantime
   * @throws NotAuthorizedOnWorkbasketException if the current user is not authorized.
   * @throws AttachmentPersistenceException if the modified Task contains two attachments with the
   *     same id.
   * @throws ObjectReferencePersistenceException if the modified Task contains two object references
   *     with the same id.
   * @throws InvalidTaskStateException if an attempt is made to change the owner of the Task and the
   *     Task is not in state READY.
   * @throws ServiceLevelViolationException if planned and due do not match service level
   * @title Update a Task
   */
  @Operation(
      summary = "Update a Task",
      description = "This endpoint updates a requested Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the Id of the Task which should be updated",
            example = "TKI:000000000000000000000000000000000003",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the new Task for the requested id.",
              content =
                  @Content(
                      schema = @Schema(implementation = TaskRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                          {
                            "taskId": \
                          "TKI:000000000000000000000000000000000003",
                            "externalId": \
                          "ETI:000000000000000000000000000000000003",
                            "created": "2018-02-01T12:00:00.000Z",
                            "modified": "2018-02-01T12:00:00.000Z",
                            "planned": "2024-05-27T15:27:56.595Z",
                            "received": "2024-05-29T15:27:56.595Z",
                            "due": "2024-05-29T15:27:56.595Z",
                            "name": "Widerruf",
                            "creator": "creator_user_id",
                            "description": "new description",
                            "priority": 2,
                            "manualPriority": -1,
                            "state": "READY",
                            "classificationSummary": {
                              "classificationId": \
                          "CLI:100000000000000000000000000000000003",
                              "key": "L1050",
                              "applicationEntryPoint": "",
                              "category": "EXTERNAL",
                              "domain": "DOMAIN_A",
                              "name": "Widerruf",
                              "parentId": "",
                              "parentKey": "",
                              "priority": 1,
                              "serviceLevel": "P13D",
                              "type": "TASK",
                              "custom1": "VNR,RVNR,KOLVNR",
                              "custom2": "",
                              "custom3": "",
                              "custom4": "",
                              "custom5": "",
                              "custom6": "",
                              "custom7": "",
                              "custom8": ""
                            },
                            "workbasketSummary": {
                              "workbasketId": \
                          "WBI:100000000000000000000000000000000001",
                              "key": "GPK_KSC",
                              "name": "Gruppenpostkorb KSC",
                              "domain": "DOMAIN_A",
                              "type": "GROUP",
                              "description": "Gruppenpostkorb KSC",
                              "owner": "teamlead-1",
                              "custom1": "ABCQVW",
                              "custom2": "",
                              "custom3": "xyz4",
                              "custom4": "",
                              "custom5": "",
                              "custom6": "",
                              "custom7": "",
                              "custom8": "",
                              "orgLevel1": "",
                              "orgLevel2": "",
                              "orgLevel3": "",
                              "orgLevel4": "",
                              "markedForDeletion": false
                            },
                            "businessProcessId": "PI_0000000000003",
                            "parentBusinessProcessId": \
                          "DOC_0000000000000000003",
                            "primaryObjRef": {
                              "company": "00",
                              "system": "PASystem",
                              "systemInstance": "00",
                              "type": "VNR",
                              "value": "11223344"
                            },
                            "custom1": "efg",
                            "custom14": "abc",
                            "customInt1": 1,
                            "customInt2": 2,
                            "customInt3": 3,
                            "customInt4": 4,
                            "customInt5": 5,
                            "customInt6": 6,
                            "customInt7": 7,
                            "customInt8": 8,
                            "secondaryObjectReferences": [],
                            "customAttributes": [],
                            "callbackInfo": [],
                            "attachments": [],
                            "read": false,
                            "transferred": false
                          }"""))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the updated Task",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            InvalidArgumentException.class,
                            InvalidTaskStateException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description =
                "CLASSIFICATION_WITH_ID_NOT_FOUND, CLASSIFICATION_WITH_KEY_NOT_FOUND, "
                    + "TASK_NOT_FOUND",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            ClassificationNotFoundException.class,
                            TaskNotFoundException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "409",
            description =
                "ENTITY_NOT_UP_TO_DATE, ATTACHMENT_ALREADY_EXISTS, OBJECT_REFERENCE_ALREADY_EXISTS",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            ConcurrencyException.class,
                            AttachmentPersistenceException.class,
                            ObjectReferencePersistenceException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "422",
            description = "SERVICE_LEVEL_VIOLATION",
            content = {
              @Content(schema = @Schema(implementation = ServiceLevelViolationException.class))
            })
      })
  @PutMapping(path = RestEndpoints.URL_TASKS_ID)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> updateTask(
      @PathVariable("taskId") String taskId,
      @RequestBody TaskRepresentationModel taskRepresentationModel)
      throws TaskNotFoundException,
          ClassificationNotFoundException,
          InvalidArgumentException,
          ConcurrencyException,
          NotAuthorizedOnWorkbasketException,
          AttachmentPersistenceException,
          InvalidTaskStateException,
          ObjectReferencePersistenceException,
          ServiceLevelViolationException;

  /**
   * This endpoint allows bulk update of tasks. Owner and Attachments cannot be updated in bulk.
   *
   * @param requestModel JSON formatted request body containing the TaskIds and the fields to update
   *     along with its values
   * @return the taskIds and corresponding ErrorCode of tasks failed to be updated
   */
  @Operation(
      summary = "Updates multiple Tasks",
      description =
          "This endpoint updates a list of Tasks listed in the body with the fields and values "
              + "listed in the body, if possible. If the update of a Task fails, it will not be"
              + " updated, but other valid updates are still applied (i.e., fails partially).",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "JSON formatted request body containing the TaskIds and fields to update along "
                      + "with its values",
              content =
                  @Content(
                      schema = @Schema(implementation = TaskPatchRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                                  {
                                    "taskIds": [
                                      "TKI:000000000000000000000000000000000003",
                                      "TKI:000000000000000000000000000000000004"
                                    ],
                                    "fieldsToUpdate": {
                                      "created": "2024-01-01T10:00:00.000Z",
                                      "claimed": "2024-01-02T11:00:00.000Z",
                                      "completed": "2024-01-03T12:00:00.000Z",
                                      "modified": "2024-01-04T13:00:00.000Z",
                                      "planned": "2024-01-05T14:00:00.000Z",
                                      "received": "2024-01-06T15:00:00.000Z",
                                      "name": "Bulk Updated Task",
                                      "creator": "bulk-updater",
                                      "note": "Bulk update note",
                                      "description": "Bulk update description",
                                      "priority": 50,
                                      "manualPriority": 25,
                                      "state": "READY",
                                      "numberOfComments": 10,
                                      "businessProcessId": "BPI-BULK-001",
                                      "parentBusinessProcessId": "PBPI-BULK-001",
                                      "isRead": true,
                                      "isTransferred": false,
                                      "isReopened": true,
                                      "groupByCount": 5,
                                      "custom1": "bulk-custom1",
                                      "custom2": "bulk-custom2",
                                      "custom3": "bulk-custom3",
                                      "custom4": "bulk-custom4",
                                      "custom5": "bulk-custom5",
                                      "custom6": "bulk-custom6",
                                      "custom7": "bulk-custom7",
                                      "custom8": "bulk-custom8",
                                      "custom9": "bulk-custom9",
                                      "custom10": "bulk-custom10",
                                      "custom11": "bulk-custom11",
                                      "custom12": "bulk-custom12",
                                      "custom13": "bulk-custom13",
                                      "custom14": "bulk-custom14",
                                      "custom15": "bulk-custom15",
                                      "custom16": "bulk-custom16",
                                      "customInt1": 1001,
                                      "customInt2": 1002,
                                      "customInt3": 1003,
                                      "customInt4": 1004,
                                      "customInt5": 1005,
                                      "customInt6": 1006,
                                      "customInt7": 1007,
                                      "customInt8": 1008,
                                      "classificationSummary": {
                                        "classificationId":
                                        "CLI:100000000000000000000000000000000003",
                                        "key": "L1050",
                                        "applicationEntryPoint": "entry-point-app",
                                        "category": "EXTERNAL",
                                        "domain": "DOMAIN_A",
                                        "name": "Widerruf",
                                        "parentId": "",
                                        "parentKey": "",
                                        "priority": 1,
                                        "serviceLevel": "P13D",
                                        "type": "TASK",
                                        "custom1": "VNR,RVNR,KOLVNR",
                                        "custom2": "",
                                        "custom3": "",
                                        "custom4": "",
                                        "custom5": "",
                                        "custom6": "",
                                        "custom7": "",
                                        "custom8": ""
                                      },
                                      "customAttributes": [
                                        { "key": "bulk-attr-key1", "value": "bulk-attr-value1" },
                                        { "key": "bulk-attr-key2", "value": "bulk-attr-value2" }
                                      ],
                                      "callbackInfo": [
                                        { "key": "bulk-callback-key1", "value":
                                        "bulk-callback-value1" },
                                        { "key": "bulk-callback-key2", "value":
                                        "bulk-callback-value2" }
                                      ],
                                      "secondaryObjectReferences": [
                                        {
                                          "company": "SecondaryCompany0",
                                          "system": "SecondarySystem0",
                                          "systemInstance": "SecondaryInstance0",
                                          "type": "SecondaryType0",
                                          "value": "00000000"
                                        },
                                        {
                                          "company": "SecondaryCompany1",
                                          "system": "SecondarySystem1",
                                          "systemInstance": "SecondaryInstance1",
                                          "type": "SecondaryType1",
                                          "value": "00000001"
                                        }
                                      ],
                                      "primaryObjRef": {
                                        "company": "MyCompany1",
                                        "system": "MySystem1",
                                        "systemInstance": "MyInstance1",
                                        "type": "MyType1",
                                        "value": "00000001"
                                      }
                                    }
                                  }
                                  """))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the taskIds and corresponding ErrorCode of tasks failed to be updated",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema =
                        @Schema(implementation = BulkOperationResultsRepresentationModel.class)))
      })
  @PatchMapping(path = RestEndpoints.URL_TASKS_BULK_UPDATE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<BulkOperationResultsRepresentationModel> bulkUpdateTasks(
      @RequestBody TaskBulkUpdateRepresentationModel requestModel);

  /**
   * This endpoint sets the 'isRead' property of a Task.
   *
   * @param taskId Id of the requested Task to set read or unread.
   * @param isRead if true, the Task property isRead is set to true, else it's set to false
   * @return the updated Task
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permission for the
   *     Workbasket the Task is in
   * @title Set a Task read or unread
   */
  @Operation(
      summary = "Set a Task read or unread",
      description = "This endpoint sets the 'isRead' property of a Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "Id of the requested Task to set read or unread.",
            example = "TKI:000000000000000000000000000000000025",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "if true, the Task property isRead is set to true, else it's set to false",
              content =
                  @Content(
                      schema = @Schema(implementation = IsReadRepresentationModel.class),
                      examples = @ExampleObject(value = "{\"is-read\": true}"))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the updated Task",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskRepresentationModel.class))),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_TASKS_ID_SET_READ)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> setTaskRead(
      @PathVariable("taskId") String taskId, @RequestBody IsReadRepresentationModel isRead)
      throws TaskNotFoundException, NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint deletes a Task.
   *
   * @title Delete a Task
   * @param taskId the Id of the Task which should be deleted.
   * @return the deleted Task.
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidTaskStateException If the Task is not in an END_STATE
   * @throws NotAuthorizedException if the current user isn't an administrator (ADMIN)
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct permissions
   * @throws InvalidCallbackStateException some comment
   */
  @Operation(
      summary = "Delete a Task",
      description = "This endpoint deletes a Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the Id of the Task which should be deleted.",
            required = true,
            example = "TKI:000000000000000000000000000000000039")
      },
      responses = {
        @ApiResponse(responseCode = "204", content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_CALLBACK_STATE, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            InvalidTaskStateException.class,
                            InvalidCallbackStateException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))}),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN, NOT_AUTHORIZED",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            NotAuthorizedOnWorkbasketException.class,
                            NotAuthorizedException.class
                          }))
            })
      })
  @DeleteMapping(path = RestEndpoints.URL_TASKS_ID)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> deleteTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException,
          InvalidCallbackStateException;

  /**
   * This endpoint force deletes a Task even if it's not completed.
   *
   * @title Force delete a Task
   * @param taskId the Id of the Task which should be force deleted.
   * @return the force deleted Task.
   * @throws TaskNotFoundException if the requested Task does not exist.
   * @throws InvalidTaskStateException If the Task is not TERMINATED or CANCELLED and the Callback
   *     state of the Task is CALLBACK_PROCESSING_REQUIRED
   * @throws NotAuthorizedException if the current user isn't an administrator (ADMIN) Task.
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct
   * @throws InvalidCallbackStateException some comment
   */
  @Operation(
      summary = "Force delete a Task",
      description = "This endpoint force deletes a Task.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "the Id of the Task which should be force deleted.",
            example = "TKI:000000000000000000000000000000000005",
            required = true)
      },
      responses = {
        @ApiResponse(responseCode = "204", content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "400",
            description = "TASK_INVALID_CALLBACK_STATE, TASK_INVALID_STATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            InvalidTaskStateException.class,
                            InvalidCallbackStateException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN, NOT_AUTHORIZED",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            NotAuthorizedOnWorkbasketException.class,
                            NotAuthorizedException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))})
      })
  @DeleteMapping(path = RestEndpoints.URL_TASKS_ID_FORCE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskRepresentationModel> forceDeleteTask(@PathVariable("taskId") String taskId)
      throws TaskNotFoundException,
          InvalidTaskStateException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException,
          InvalidCallbackStateException;

  /**
   * This endpoint deletes an aggregation of Tasks and returns the deleted Tasks. Filters can be
   * applied.
   *
   * @title Delete multiple Tasks
   * @param filterParameter the filter parameters
   * @param filterCustomFields the filter parameters regarding TaskCustomFields
   * @param filterCustomIntFields the filter parameters regarding TaskCustomIntFields
   * @return the deleted task summaries
   * @throws InvalidArgumentException TODO: this is never thrown
   * @throws NotAuthorizedException if the current user is not authorized to delete the requested
   *     Tasks.
   */
  @Operation(
      summary = "Delete multiple Tasks",
      description =
          "This endpoint deletes an aggregation of Tasks and returns the deleted Tasks. Filters "
              + "can be applied.",
      parameters = {
        @Parameter(
            name = "task-id",
            examples = {
              @ExampleObject(value = "TKI:000000000000000000000000000000000036"),
              @ExampleObject(value = "TKI:000000000000000000000000000000000037"),
              @ExampleObject(value = "TKI:000000000000000000000000000000000038")
            })
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the deleted task summaries",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema =
                        @Schema(implementation = TaskSummaryCollectionRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))})
      })
  @DeleteMapping(path = RestEndpoints.URL_TASKS)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskSummaryCollectionRepresentationModel> deleteTasks(
      @ParameterObject TaskQueryFilterParameter filterParameter,
      @ParameterObject TaskQueryFilterCustomFields filterCustomFields,
      @ParameterObject TaskQueryFilterCustomIntFields filterCustomIntFields)
      throws InvalidArgumentException, NotAuthorizedException;
}
