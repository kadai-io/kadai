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

package io.kadai.simplehistory.rest;

import io.kadai.common.api.KadaiEngine;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.QuerySortParameter;
import io.kadai.common.rest.util.QueryParamsValidator;
import io.kadai.simplehistory.impl.SimpleHistoryServiceImpl;
import io.kadai.simplehistory.impl.task.TaskHistoryQuery;
import io.kadai.simplehistory.rest.assembler.TaskHistoryEventRepresentationModelAssembler;
import io.kadai.simplehistory.rest.models.TaskHistoryEventPagedRepresentationModel;
import io.kadai.simplehistory.rest.models.TaskHistoryEventRepresentationModel;
import io.kadai.spi.history.api.events.task.TaskHistoryEvent;
import io.kadai.spi.history.api.exceptions.KadaiHistoryEventNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all TaskHistoryEvent related endpoints. */
@RestController
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class TaskHistoryEventController implements TaskHistoryEventApi {
  private final SimpleHistoryServiceImpl simpleHistoryService;
  private final TaskHistoryEventRepresentationModelAssembler assembler;

  @Autowired
  public TaskHistoryEventController(
      KadaiEngine kadaiEngine,
      SimpleHistoryServiceImpl simpleHistoryServiceImpl,
      TaskHistoryEventRepresentationModelAssembler assembler) {

    this.simpleHistoryService = simpleHistoryServiceImpl;
    this.simpleHistoryService.initialize(kadaiEngine);
    this.assembler = assembler;
  }

  @GetMapping(path = HistoryRestEndpoints.URL_HISTORY_EVENTS, produces = MediaTypes.HAL_JSON_VALUE)
  public ResponseEntity<TaskHistoryEventPagedRepresentationModel> getTaskHistoryEvents(
      HttpServletRequest request,
      @ParameterObject TaskHistoryQueryFilterParameter filterParameter,
      @ParameterObject TaskHistoryQuerySortParameter sortParameter,
      @ParameterObject QueryPagingParameter<TaskHistoryEvent, TaskHistoryQuery> pagingParameter) {

    QueryParamsValidator.validateParams(
        request,
        TaskHistoryQueryFilterParameter.class,
        QuerySortParameter.class,
        QueryPagingParameter.class);

    TaskHistoryQuery query = simpleHistoryService.createTaskHistoryQuery();
    filterParameter.apply(query);
    sortParameter.apply(query);

    List<TaskHistoryEvent> historyEvents = pagingParameter.apply(query);

    TaskHistoryEventPagedRepresentationModel pagedResources =
        assembler.toPagedModel(historyEvents, pagingParameter.getPageMetadata());

    return ResponseEntity.ok(pagedResources);
  }

  @GetMapping(path = HistoryRestEndpoints.URL_HISTORY_EVENTS_ID)
  public ResponseEntity<TaskHistoryEventRepresentationModel> getTaskHistoryEvent(
      @PathVariable("historyEventId") String historyEventId)
      throws KadaiHistoryEventNotFoundException {
    TaskHistoryEvent resultEvent = simpleHistoryService.getTaskHistoryEvent(historyEventId);

    TaskHistoryEventRepresentationModel taskEventResource = assembler.toModel(resultEvent);
    return new ResponseEntity<>(taskEventResource, HttpStatus.OK);
  }
}
