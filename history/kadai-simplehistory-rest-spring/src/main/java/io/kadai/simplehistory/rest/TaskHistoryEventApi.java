package io.kadai.simplehistory.rest;

import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.simplehistory.impl.task.TaskHistoryQuery;
import io.kadai.simplehistory.rest.TaskHistoryEventController.TaskHistoryQuerySortParameter;
import io.kadai.simplehistory.rest.models.TaskHistoryEventPagedRepresentationModel;
import io.kadai.simplehistory.rest.models.TaskHistoryEventRepresentationModel;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.exceptions.KadaiHistoryEventNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface TaskHistoryEventApi {

  /**
   * This endpoint retrieves a list of existing Task History Events. Filters can be applied.
   *
   * @title Get a list of all Task History Events
   * @param request the HTTP request
   * @param filterParameter the filter parameters
   * @param sortParameter the sort parameters
   * @param pagingParameter the paging parameters
   * @return the Task History Events with the given filter, sort and paging options.
   */
  @Operation(
      summary = "Get a list of all Task History Events",
      description =
          "This endpoint retrieves a list of existing Task History Events. Filters can be applied.",
      parameters = {
        @Parameter(name = "page", example = "1"),
        @Parameter(name = "page-size", example = "3")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the Task History Events with the given filter, sort and paging options.",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = TaskHistoryEventPagedRepresentationModel.class))
            })
      })
  @GetMapping(path = HistoryRestEndpoints.URL_HISTORY_EVENTS, produces = MediaTypes.HAL_JSON_VALUE)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<TaskHistoryEventPagedRepresentationModel> getTaskHistoryEvents(
      HttpServletRequest request,
      @ParameterObject TaskHistoryQueryFilterParameter filterParameter,
      @ParameterObject TaskHistoryQuerySortParameter sortParameter,
      @ParameterObject QueryPagingParameter<TaskHistoryEvent, TaskHistoryQuery> pagingParameter);

  /**
   * This endpoint retrieves a single Task History Event.
   *
   * @title Get a single Task History Event
   * @param historyEventId the Id of the requested Task History Event.
   * @return the requested Task History Event
   * @throws KadaiHistoryEventNotFoundException If a Task History Event can't be found by the
   *     provided historyEventId
   */
  @Operation(
      summary = "Get a single Task History Event",
      description = "This endpoint retrieves a single Task History Event.",
      parameters = {
        @Parameter(
            name = "historyEventId",
            description = "the Id of the requested Task History Event.",
            example = "THI:000000000000000000000000000000000000",
            required = true),
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the requested Task History Event",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema = @Schema(implementation = TaskHistoryEventRepresentationModel.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "HISTORY_EVENT_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = KadaiHistoryEventNotFoundException.class))
            }),
      })
  @GetMapping(path = HistoryRestEndpoints.URL_HISTORY_EVENTS_ID)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<TaskHistoryEventRepresentationModel> getTaskHistoryEvent(
      @PathVariable("historyEventId") String historyEventId)
      throws KadaiHistoryEventNotFoundException;
}
