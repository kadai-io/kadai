package io.kadai.workbasket.rest;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.workbasket.api.WorkbasketAccessItemQuery;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.rest.WorkbasketAccessItemController.WorkbasketAccessItemQuerySortParameter;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemPagedRepresentationModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface WorkbasketAccessItemApi {

  /**
   * This endpoint retrieves a list of existing Workbasket Access Items. Filters can be applied.
   *
   * @title Get a list of all Workbasket Access Items
   * @param request the HTTP request
   * @param filterParameter the filter parameters
   * @param sortParameter the sort parameters
   * @param pagingParameter the paging parameters
   * @return the Workbasket Access Items with the given filter, sort and paging options.
   * @throws NotAuthorizedException if the user is not authorized.
   */
  @Operation(
      summary = "Get a list of all Workbasket Access Items",
      description =
          "This endpoint retrieves a list of existing Workbasket Access Items. Filters can be "
              + "applied.",
      parameters = {
        @Parameter(name = "sort-by", example = "WORKBASKET_KEY"),
        @Parameter(name = "order", example = "ASCENDING"),
        @Parameter(name = "access-id", example = "user-2-2")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "the Workbasket Access Items with the given filter, sort and paging options.",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema =
                      @Schema(implementation = WorkbasketAccessItemPagedRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))}),
      })
  @GetMapping(path = RestEndpoints.URL_WORKBASKET_ACCESS_ITEMS)
  ResponseEntity<WorkbasketAccessItemPagedRepresentationModel> getWorkbasketAccessItems(
      HttpServletRequest request,
      @ParameterObject WorkbasketAccessItemQueryFilterParameter filterParameter,
      @ParameterObject WorkbasketAccessItemQuerySortParameter sortParameter,
      @ParameterObject
          QueryPagingParameter<WorkbasketAccessItem, WorkbasketAccessItemQuery> pagingParameter)
      throws NotAuthorizedException;

  /**
   * This endpoint deletes all Workbasket Access Items for a provided Access Id.
   *
   * @title Delete a Workbasket Access Item
   * @param accessId the Access Id whose Workbasket Access Items should be removed
   * @return no content
   * @throws NotAuthorizedException if the user is not authorized.
   * @throws InvalidArgumentException if some argument is invalid.
   */
  @Operation(
      summary = "Delete a Workbasket Access Item",
      description = "This endpoint deletes all Workbasket Access Items for a provided Access Id.",
      parameters = {
        @Parameter(
            name = "accessId",
            description = "the Access Id whose Workbasket Access Items should be removed",
            example = "user-2-1",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "204",
            content = {@Content(schema = @Schema())}),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))}),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
      })
  @DeleteMapping(path = RestEndpoints.URL_WORKBASKET_ACCESS_ITEMS)
  ResponseEntity<Void> removeWorkbasketAccessItems(@RequestParam("access-id") String accessId)
      throws NotAuthorizedException, InvalidArgumentException;
}
