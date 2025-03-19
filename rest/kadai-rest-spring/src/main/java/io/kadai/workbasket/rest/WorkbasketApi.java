package io.kadai.workbasket.rest;

import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.workbasket.api.WorkbasketQuery;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketAccessItemAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketInUseException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.rest.WorkbasketController.WorkbasketQuerySortParameter;
import io.kadai.workbasket.rest.models.DistributionTargetsCollectionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemCollectionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketSummaryPagedRepresentationModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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

public interface WorkbasketApi {

  /**
   * This endpoint retrieves a list of existing Workbaskets. Filters can be applied.
   *
   * @title Get a list of all Workbaskets
   * @param request the HTTP request
   * @param filterParameter the filter parameters
   * @param sortParameter the sort parameters
   * @param pagingParameter the paging parameters
   * @return the Workbaskets with the given filter, sort and paging options.
   */
  @Operation(
      summary = "Get a list of all Workbaskets",
      description =
          "This endpoint retrieves a list of existing Workbaskets. Filters can be applied.",
      parameters = {@Parameter(name = "type", example = "PERSONAL")},
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Found all Workbaskets",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema =
                      @Schema(implementation = WorkbasketSummaryPagedRepresentationModel.class))
            })
      })
  @GetMapping(path = RestEndpoints.URL_WORKBASKET)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<WorkbasketSummaryPagedRepresentationModel> getWorkbaskets(
      HttpServletRequest request,
      @ParameterObject WorkbasketQueryFilterParameter filterParameter,
      @ParameterObject WorkbasketQuerySortParameter sortParameter,
      @ParameterObject QueryPagingParameter<WorkbasketSummary, WorkbasketQuery> pagingParameter);

  /**
   * This endpoint retrieves a single Workbasket.
   *
   * @title Get a single Workbasket
   * @param workbasketId the Id of the requested Workbasket
   * @return the requested Workbasket
   * @throws WorkbasketNotFoundException if the requested Workbasket is not found
   * @throws NotAuthorizedOnWorkbasketException if the current user has no permissions to access the
   *     requested Workbasket
   */
  @Operation(
      summary = "Get a single Workbasket",
      description = "This endpoint retrieves a single Workbasket.",
      parameters = {
        @Parameter(
            name = "workbasketId",
            description = "the Id of the requested Workbasket",
            required = true,
            example = "WBI:100000000000000000000000000000000001")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the requested Workbasket",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = WorkbasketRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            })
      })
  @GetMapping(path = RestEndpoints.URL_WORKBASKET_ID, produces = MediaTypes.HAL_JSON_VALUE)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<WorkbasketRepresentationModel> getWorkbasket(
      @PathVariable("workbasketId") String workbasketId)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint deletes an existing Workbasket.
   *
   * <p>Returned HTTP Status codes:
   *
   * <ul>
   *   <li><b>204 NO_CONTENT</b> - Workbasket has been deleted successfully
   *   <li><b>202 ACCEPTED</b> - Workbasket still contains completed Tasks. It has been marked for
   *       deletion and will be deleted automatically as soon as all completed Tasks are deleted.
   *   <li><b>423 LOCKED</b> - Workbasket contains non-completed Tasks and cannot be deleted.
   * </ul>
   *
   * @title Delete a Workbasket
   * @param workbasketId the Id of the Workbasket which should be deleted
   * @return the deleted Workbasket
   * @throws NotAuthorizedOnWorkbasketException if the current user is not authorized to delete this
   *     Workbasket.
   * @throws InvalidArgumentException if the requested Workbasket Id is null or empty
   * @throws WorkbasketNotFoundException if the requested Workbasket is not found
   * @throws WorkbasketInUseException if the Workbasket contains tasks.
   * @throws NotAuthorizedException if the current user has not correct permissions
   */
  @Operation(
      summary = "Delete a Workbasket",
      description = "This endpoint deletes an existing Workbasket",
      parameters = {
        @Parameter(
            name = "workbasketId",
            description = "the Id of the requested Workbasket",
            required = true,
            example = "WBI:100000000000000000000000000000000002")
      },
      responses = {
        @ApiResponse(
            responseCode = "204",
            description = "<b>204 NO_CONTENT</b> - Workbasket has been deleted successfully",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "202",
            description =
                "<b>202 ACCEPTED</b> - Workbasket still contains completed Tasks. It has been "
                    + "marked for deletion and will be deleted automatically as soon as all "
                    + "completed Tasks are deleted.",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "423",
            description =
                "<b>423 LOCKED</b> - Workbasket contains non-completed Tasks and cannot be "
                    + "deleted.",
            content = @Content(schema = @Schema(implementation = WorkbasketInUseException.class))),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
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
                            NotAuthorizedException.class,
                            NotAuthorizedOnWorkbasketException.class
                          }))
            })
      })
  @DeleteMapping(path = RestEndpoints.URL_WORKBASKET_ID)
  @Transactional(rollbackFor = Exception.class, noRollbackFor = WorkbasketNotFoundException.class)
  ResponseEntity<WorkbasketRepresentationModel> deleteWorkbasket(
      @PathVariable("workbasketId") String workbasketId)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          WorkbasketInUseException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint creates a persistent Workbasket.
   *
   * @title Create a new Workbasket
   * @param workbasketRepresentationModel the Workbasket which should be created.
   * @return the created Workbasket
   * @throws InvalidArgumentException if some required properties of the Workbasket are not set.
   * @throws NotAuthorizedException if the current user is not member of role BUSINESS_ADMIN or
   *     ADMIN
   * @throws WorkbasketAlreadyExistException if the Workbasket exists already
   * @throws DomainNotFoundException if the domain does not exist in the configuration.
   */
  @Operation(
      summary = "Create a new Workbasket",
      description = "This endpoint creates a persistent Workbasket.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the Workbasket which should be created.",
              content =
                  @Content(
                      schema = @Schema(implementation = WorkbasketRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                          {
                            "key" : "asdasdasd",
                            "name" : "this is a wonderful workbasket name",
                            "domain" : "DOMAIN_A",
                            "type" : "GROUP",
                            "markedForDeletion" : false
                          }"""))),
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "the created Workbasket",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = WorkbasketRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))}),
        @ApiResponse(
            responseCode = "409",
            description = "WORKBASKET_ALREADY_EXISTS",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketAlreadyExistException.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "DOMAIN_NOT_FOUND",
            content = {@Content(schema = @Schema(implementation = DomainNotFoundException.class))})
      })
  @PostMapping(path = RestEndpoints.URL_WORKBASKET)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<WorkbasketRepresentationModel> createWorkbasket(
      @RequestBody WorkbasketRepresentationModel workbasketRepresentationModel)
      throws InvalidArgumentException,
          NotAuthorizedException,
          WorkbasketAlreadyExistException,
          DomainNotFoundException;

  /**
   * This endpoint updates a given Workbasket.
   *
   * @title Update a Workbasket
   * @param workbasketId the Id of the Workbasket which should be updated.
   * @param workbasketRepresentationModel the new Workbasket for the requested id.
   * @return the updated Workbasket
   * @throws InvalidArgumentException if the requested Id and the Id within the new Workbasket do
   *     not match.
   * @throws WorkbasketNotFoundException if the requested workbasket does not
   * @throws NotAuthorizedException if the current user is not authorized to update the Workbasket
   * @throws ConcurrencyException if an attempt is made to update the Workbasket and another user
   *     updated it already
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct permissions
   */
  @Operation(
      summary = "Update a Workbasket",
      description = "This endpoint creates a persistent Workbasket.",
      parameters = {
        @Parameter(
            name = "workbasketId",
            description = "the Id of the requested Workbasket",
            required = true,
            example = "WBI:100000000000000000000000000000000001")
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the new Workbasket for the requested id",
              content =
                  @Content(
                      schema = @Schema(implementation = WorkbasketRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                          {
                            "workbasketId" : \
                          "WBI:100000000000000000000000000000000001",
                            "key" : "GPK_KSC",
                            "name" : "new name",
                            "domain" : "DOMAIN_A",
                            "type" : "GROUP",
                            "description" : "Gruppenpostkorb KSC",
                            "owner" : "teamlead-1",
                            "custom1" : "ABCQVW",
                            "custom2" : "",
                            "custom3" : "xyz4",
                            "custom4" : "",
                            "custom5" : "",
                            "custom6" : "",
                            "custom7" : "",
                            "custom8" : "",
                            "orgLevel1" : "",
                            "orgLevel2" : "",
                            "orgLevel3" : "",
                            "orgLevel4" : "",
                            "markedForDeletion" : false,
                            "created" : "2018-02-01T12:00:00.000Z",
                            "modified" : "2018-02-01T12:00:00.000Z"
                          }"""))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the requested Workbasket",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = WorkbasketRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
            }),
        @ApiResponse(
            responseCode = "409",
            description = "ENTITY_NOT_UP_TO_DATE",
            content = {@Content(schema = @Schema(implementation = ConcurrencyException.class))}),
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
  @PutMapping(path = RestEndpoints.URL_WORKBASKET_ID)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<WorkbasketRepresentationModel> updateWorkbasket(
      @PathVariable("workbasketId") String workbasketId,
      @RequestBody WorkbasketRepresentationModel workbasketRepresentationModel)
      throws WorkbasketNotFoundException,
          NotAuthorizedException,
          ConcurrencyException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint retrieves all Workbasket Access Items for a given Workbasket.
   *
   * @title Get all Workbasket Access Items
   * @param workbasketId the Id of the requested Workbasket.
   * @return the access items for the requested Workbasket.
   * @throws NotAuthorizedException if the current user is not member of role BUSINESS_ADMIN or
   *     ADMIN
   * @throws WorkbasketNotFoundException if the requested Workbasket does not exist.
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct permissions
   */
  @Operation(
      summary = "Get all Workbasket Access Items",
      description = "This endpoint retrieves all Workbasket Access Items for a given Workbasket.",
      parameters = {
        @Parameter(
            name = "workbasketId",
            description = "the Id of the requested Workbasket",
            required = true,
            example = "WBI:100000000000000000000000000000000001")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the access items for the requested Workbasket.",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema =
                      @Schema(
                          implementation = WorkbasketAccessItemCollectionRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED, NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
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
  @GetMapping(
      path = RestEndpoints.URL_WORKBASKET_ID_ACCESS_ITEMS,
      produces = MediaTypes.HAL_JSON_VALUE)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<WorkbasketAccessItemCollectionRepresentationModel> getWorkbasketAccessItems(
      @PathVariable("workbasketId") String workbasketId)
      throws WorkbasketNotFoundException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint replaces all Workbasket Access Items for a given Workbasket with the provided
   * ones.
   *
   * @title Set all Workbasket Access Items
   * @param workbasketId the Id of the Workbasket whose Workbasket Access Items will be replaced
   * @param workbasketAccessItemRepModels the new Workbasket Access Items.
   * @return the new Workbasket Access Items for the requested Workbasket
   * @throws NotAuthorizedException if the current user is not member of role BUSINESS_ADMIN or
   *     ADMIN
   * @throws InvalidArgumentException if the new Workbasket Access Items are not provided.
   * @throws WorkbasketNotFoundException TODO: this is never thrown.
   * @throws WorkbasketAccessItemAlreadyExistException if a duplicate Workbasket Access Item exists
   *     in the provided list.
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct permissions
   */
  @Operation(
      summary = "Set all Workbasket Access Items",
      description =
          "This endpoint replaces all Workbasket Access Items for a given Workbasket with the "
              + "provided",
      parameters = {
        @Parameter(
            name = "workbasketId",
            description = "the Id of the Workbasket whose Workbasket Access Items will be replaced",
            required = true,
            example = "WBI:100000000000000000000000000000000001")
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the new Workbasket Access Items.",
              content =
                  @Content(
                      schema =
                          @Schema(
                              implementation =
                                  WorkbasketAccessItemCollectionRepresentationModel.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                          {
                            "accessItems" : [ {
                              "workbasketId" : \
                          "WBI:100000000000000000000000000000000001",
                              "accessId" : "new-access-id",
                              "accessName" : "new-access-name",
                              "permRead" : false,
                              "permReadTasks" : false,
                              "permOpen" : true,
                              "permAppend" : false,
                              "permEditTasks" : false,
                              "permTransfer" : false,
                              "permDistribute" : false,
                              "permCustom1" : false,
                              "permCustom2" : false,
                              "permCustom3" : false,
                              "permCustom4" : false,
                              "permCustom5" : false,
                              "permCustom6" : false,
                              "permCustom7" : false,
                              "permCustom8" : false,
                              "permCustom9" : false,
                              "permCustom10" : false,
                              "permCustom11" : false,
                              "permCustom12" : false
                            } ]
                          }"""))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the new Workbasket Access Items for the requested Workbasket",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema =
                      @Schema(
                          implementation = WorkbasketAccessItemCollectionRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
            }),
        @ApiResponse(
            responseCode = "409",
            description = "WORKBASKET_ACCESS_ITEM_ALREADY_EXISTS",
            content = {
              @Content(
                  schema =
                      @Schema(implementation = WorkbasketAccessItemAlreadyExistException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED, NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
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
  @PutMapping(path = RestEndpoints.URL_WORKBASKET_ID_ACCESS_ITEMS)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<WorkbasketAccessItemCollectionRepresentationModel> setWorkbasketAccessItems(
      @PathVariable("workbasketId") String workbasketId,
      @RequestBody WorkbasketAccessItemCollectionRepresentationModel workbasketAccessItemRepModels)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          WorkbasketAccessItemAlreadyExistException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint retrieves all Distribution Targets for a requested Workbasket.
   *
   * @title Get all Distribution Targets for a Workbasket
   * @param workbasketId the Id of the Workbasket whose Distribution Targets will be retrieved
   * @return the Distribution Targets for the requested Workbasket
   * @throws WorkbasketNotFoundException if the requested Workbasket does not exist.
   * @throws NotAuthorizedOnWorkbasketException if the current user has no read permission for the
   *     specified Workbasket
   */
  @Operation(
      summary = "Get all Distribution Targets for a Workbasket",
      description = "This endpoint retrieves all Distribution Targets for a requested Workbasket.",
      parameters = {
        @Parameter(
            name = "workbasketId",
            description = "the Id of the Workbasket whose Distribution Targets will be retrieved",
            required = true,
            example = "WBI:100000000000000000000000000000000002")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the Distribution Targets for the requested Workbasket",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema =
                      @Schema(
                          implementation = DistributionTargetsCollectionRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
            content = {
              @Content(schema = @Schema(implementation = NotAuthorizedOnWorkbasketException.class))
            })
      })
  @GetMapping(
      path = RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION,
      produces = MediaTypes.HAL_JSON_VALUE)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<DistributionTargetsCollectionRepresentationModel> getDistributionTargets(
      @PathVariable("workbasketId") String workbasketId)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint replaces all Distribution Targets for a given Workbasket with the provided ones.
   *
   * @title Set all Distribution Targets for a Workbasket
   * @param sourceWorkbasketId the source Workbasket
   * @param targetWorkbasketIds the destination Workbaskets.
   * @return the new Distribution Targets for the requested Workbasket.
   * @throws WorkbasketNotFoundException if any Workbasket was not found (either source or target)
   * @throws NotAuthorizedOnWorkbasketException if the current user doesn't have READ permission for
   *     the source Workbasket
   * @throws NotAuthorizedException if the current user has not correct permissions
   */
  @Operation(
      summary = "Set all Distribution Targets for a Workbasket",
      description =
          "This endpoint replaces all Distribution Targets for a given Workbasket with the "
              + "provided ones.",
      parameters = {
        @Parameter(
            name = "workbasketId",
            description = "the source Workbasket",
            required = true,
            example = "WBI:100000000000000000000000000000000001")
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "the destination Workbaskets.",
              content =
                  @Content(
                      array = @ArraySchema(schema = @Schema(implementation = String.class)),
                      examples =
                          @ExampleObject(
                              value =
                                  "[ \"WBI:100000000000000000000000000000000002\", "
                                      + "\"WBI:100000000000000000000000000000000003\" ]"))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the new Distribution Targets for the requested Workbasket.",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema =
                      @Schema(
                          implementation = DistributionTargetsCollectionRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED, NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
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
  @PutMapping(path = RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<DistributionTargetsCollectionRepresentationModel>
      setDistributionTargetsForWorkbasketId(
          @PathVariable("workbasketId") String sourceWorkbasketId,
          @RequestBody List<String> targetWorkbasketIds)
          throws WorkbasketNotFoundException,
              NotAuthorizedException,
              NotAuthorizedOnWorkbasketException;

  /**
   * This endpoint removes all Distribution Target references for a provided Workbasket.
   *
   * @title Remove a Workbasket as Distribution Target
   * @param targetWorkbasketId the Id of the requested Workbasket.
   * @return no content
   * @throws WorkbasketNotFoundException if the requested Workbasket does not exist.
   * @throws NotAuthorizedException if the requested user ist not ADMIN or BUSINESS_ADMIN.
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct permissions
   */
  @Operation(
      summary = "Remove a Workbasket as Distribution Target",
      description =
          "This endpoint removes all Distribution Target references for a provided Workbasket.",
      parameters = {
        @Parameter(
            name = "workbasketId",
            description = "the Id of the requested Workbasket.",
            required = true,
            example = "WBI:100000000000000000000000000000000007")
      },
      responses = {
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED, NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN",
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
  @DeleteMapping(path = RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<Void> removeDistributionTargetForWorkbasketId(
      @PathVariable("workbasketId") String targetWorkbasketId)
      throws WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          NotAuthorizedException;
}
