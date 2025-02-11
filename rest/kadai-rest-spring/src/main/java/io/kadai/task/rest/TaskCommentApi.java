package io.kadai.task.rest;

import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.task.api.TaskCommentQuery;
import io.kadai.task.api.exceptions.NotAuthorizedOnTaskCommentException;
import io.kadai.task.api.exceptions.TaskCommentNotFoundException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.TaskComment;
import io.kadai.task.rest.TaskCommentController.TaskCommentQuerySortParameter;
import io.kadai.task.rest.models.TaskCommentCollectionRepresentationModel;
import io.kadai.task.rest.models.TaskCommentRepresentationModel;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface TaskCommentApi {

  /**
   * This endpoint retrieves a Task Comment.
   *
   * @title Get a single Task Comment
   * @param taskCommentId the Id of the Task Comment
   * @return the Task Comment
   * @throws NotAuthorizedOnWorkbasketException if the user is not authorized for the requested Task
   *     Comment
   * @throws TaskNotFoundException TODO: this is never thrown
   * @throws TaskCommentNotFoundException if the requested Task Comment is not found
   * @throws InvalidArgumentException if the requested Id is null or empty
   */
  @Operation(
      summary = "Get a single Task Comment",
      description = "This endpoint retrieves a Task Comment.",
      parameters = {
        @Parameter(
            name = "taskCommentId",
            description = "The Id of the Task Comment",
            example = "TCI:000000000000000000000000000000000000",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the Task Comment",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskCommentRepresentationModel.class))),
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
            description = "TASK_NOT_FOUND, TASK_COMMENT_NOT_FOUND",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            TaskNotFoundException.class,
                            TaskCommentNotFoundException.class
                          }))
            })
      })
  @GetMapping(path = RestEndpoints.URL_TASK_COMMENT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<TaskCommentRepresentationModel> getTaskComment(
      @PathVariable("taskCommentId") String taskCommentId)
      throws TaskNotFoundException,
          TaskCommentNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint retrieves all Task Comments for a specific Task. Further filters can be applied.
   *
   * @title Get a list of all Task Comments for a specific Task
   * @param taskId the Id of the Task whose comments are requested
   * @param request the HTTP request
   * @param filterParameter the filter parameters
   * @param sortParameter the sort parameters
   * @param pagingParameter the paging parameters
   * @return a list of Task Comments
   */
  @Operation(
      summary = "Get a list of all Task Comments for a specific Task",
      description =
          "This endpoint retrieves all Task Comments for a specific Task. Further filters can be "
              + "applied.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "The Id of the Task whose comments are requested",
            example = "TKI:000000000000000000000000000000000000",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "a list of Task Comments",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema =
                        @Schema(implementation = TaskCommentCollectionRepresentationModel.class)))
      })
  @GetMapping(path = RestEndpoints.URL_TASK_COMMENTS)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<TaskCommentCollectionRepresentationModel> getTaskComments(
      @PathVariable("taskId") String taskId,
      HttpServletRequest request,
      @ParameterObject TaskCommentQueryFilterParameter filterParameter,
      @ParameterObject TaskCommentQuerySortParameter sortParameter,
      @ParameterObject QueryPagingParameter<TaskComment, TaskCommentQuery> pagingParameter);

  /**
   * This endpoint deletes a given Task Comment.
   *
   * @title Delete a Task Comment
   * @param taskCommentId the Id of the Task Comment which should be deleted
   * @return no content, if everything went well.
   * @throws NotAuthorizedException if the current user is not authorized to delete a Task Comment
   * @throws TaskNotFoundException If the given Task Id in the Task Comment does not refer to an
   *     existing task.
   * @throws TaskCommentNotFoundException if the requested Task Comment does not exist
   * @throws InvalidArgumentException if the requested Task Comment Id is null or empty
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct permissions
   * @throws NotAuthorizedOnTaskCommentException if the current user has not correct permissions
   */
  @Operation(
      summary = "Delete a Task Comment",
      description = "This endpoint deletes a given Task Comment.",
      parameters = {
        @Parameter(
            name = "taskCommentId",
            description = "The Id of the Task Comment which should be deleted",
            example = "TCI:000000000000000000000000000000000001",
            required = true)
      },
      responses = {
        @ApiResponse(responseCode = "204", content = @Content(schema = @Schema())),
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
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN, "
                    + "NOT_AUTHORIZED_ON_TASK_COMMENT, NOT_AUTHORIZED",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            NotAuthorizedOnWorkbasketException.class,
                            NotAuthorizedOnTaskCommentException.class,
                            NotAuthorizedException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND, TASK_COMMENT_NOT_FOUND",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            TaskNotFoundException.class,
                            TaskCommentNotFoundException.class
                          }))
            })
      })
  @DeleteMapping(path = RestEndpoints.URL_TASK_COMMENT)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskCommentRepresentationModel> deleteTaskComment(
      @PathVariable("taskCommentId") String taskCommentId)
      throws TaskNotFoundException,
          TaskCommentNotFoundException,
          InvalidArgumentException,
          NotAuthorizedOnTaskCommentException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint updates a given Task Comment.
   *
   * @title Update a Task Comment
   * @param taskCommentId the Task Comment which should be updated.
   * @param taskCommentRepresentationModel the new comment for the requested id.
   * @return the updated Task Comment
   * @throws NotAuthorizedException if the current user does not have access to the Task Comment
   * @throws TaskNotFoundException if the referenced Task within the Task Comment does not exist
   * @throws TaskCommentNotFoundException if the requested Task Comment does not exist
   * @throws InvalidArgumentException if the Id in the path and in the request body does not match
   * @throws ConcurrencyException if the requested Task Comment has been updated in the meantime by
   *     a different process.
   * @throws NotAuthorizedOnTaskCommentException if the current user has not correct permissions
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct permissions
   */
  @Operation(
      summary = "Update a Task Comment",
      description = "This endpoint updates a given Task Comment.",
      parameters = {
        @Parameter(
            name = "taskCommentId",
            description = "The Id of the Task Comment which should be updated",
            example = "TCI:000000000000000000000000000000000000",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "The new comment for the requested id",
              content =
                  @Content(
                      schema = @Schema(implementation = TaskCommentRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                          {
                            "taskCommentId": \
                          "TCI:000000000000000000000000000000000000",
                            "taskId": \
                          "TKI:000000000000000000000000000000000000",
                            "textField": "updated text in textfield",
                            "creator": "user-1-1",
                            "creatorFullName": "Mustermann, Max",
                            "created": "2017-01-29T15:55:00Z",
                            "modified": "2018-01-30T15:55:00Z"
                          }"""))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the updated Task Comment",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskCommentRepresentationModel.class))),
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
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN, "
                    + "NOT_AUTHORIZED_ON_TASK_COMMENT, NOT_AUTHORIZED",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            NotAuthorizedOnWorkbasketException.class,
                            NotAuthorizedOnTaskCommentException.class,
                            NotAuthorizedException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "TASK_NOT_FOUND, TASK_COMMENT_NOT_FOUND",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            TaskNotFoundException.class,
                            TaskCommentNotFoundException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "409",
            description = "ENTITY_NOT_UP_TO_DATE",
            content = {@Content(schema = @Schema(implementation = ConcurrencyException.class))}),
      })
  @PutMapping(path = RestEndpoints.URL_TASK_COMMENT)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskCommentRepresentationModel> updateTaskComment(
      @PathVariable("taskCommentId") String taskCommentId,
      @RequestBody TaskCommentRepresentationModel taskCommentRepresentationModel)
      throws TaskNotFoundException,
          TaskCommentNotFoundException,
          InvalidArgumentException,
          ConcurrencyException,
          NotAuthorizedException,
          NotAuthorizedOnTaskCommentException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint creates a Task Comment.
   *
   * @title Create a new Task Comment
   * @param taskId the Id of the Task where a Task Comment should be created.
   * @param taskCommentRepresentationModel the Task Comment to create.
   * @return the created Task Comment
   * @throws NotAuthorizedOnWorkbasketException if the current user is not authorized to create a
   *     Task Comment
   * @throws InvalidArgumentException if the Task Comment Id is null or empty
   * @throws TaskNotFoundException if the requested task does not exist
   */
  @Operation(
      summary = "Create a new Task Comment",
      description = "This endpoint creates a Task Comment.",
      parameters = {
        @Parameter(
            name = "taskId",
            description = "The Id of the Task where a Task Comment should be created",
            example = "TKI:000000000000000000000000000000000000",
            required = true)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "The Task Comment to create",
              content =
                  @Content(
                      schema = @Schema(implementation = TaskCommentRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                          {
                            "taskId": \
                          "TKI:000000000000000000000000000000000000",
                            "textField": "some text in textfield"
                          }"""))),
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "the created Task Comment",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = TaskCommentRepresentationModel.class))),
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
            description = "TASK_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = TaskNotFoundException.class))}),
      })
  @PostMapping(path = RestEndpoints.URL_TASK_COMMENTS)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<TaskCommentRepresentationModel> createTaskComment(
      @PathVariable("taskId") String taskId,
      @RequestBody TaskCommentRepresentationModel taskCommentRepresentationModel)
      throws InvalidArgumentException, TaskNotFoundException, NotAuthorizedOnWorkbasketException;
}
