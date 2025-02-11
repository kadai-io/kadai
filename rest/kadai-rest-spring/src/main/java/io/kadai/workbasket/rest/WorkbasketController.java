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

package io.kadai.workbasket.rest;

import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.QuerySortBy;
import io.kadai.common.rest.QuerySortParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.common.rest.util.QueryParamsValidator;
import io.kadai.workbasket.api.WorkbasketCustomField;
import io.kadai.workbasket.api.WorkbasketQuery;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketAccessItemAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketInUseException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.rest.assembler.WorkbasketAccessItemRepresentationModelAssembler;
import io.kadai.workbasket.rest.assembler.WorkbasketRepresentationModelAssembler;
import io.kadai.workbasket.rest.assembler.WorkbasketSummaryRepresentationModelAssembler;
import io.kadai.workbasket.rest.models.DistributionTargetsCollectionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemCollectionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketSummaryPagedRepresentationModel;
import jakarta.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.List;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
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

/** Controller for all {@link Workbasket} related endpoints. */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class WorkbasketController implements WorkbasketApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkbasketController.class);

  private final WorkbasketService workbasketService;
  private final WorkbasketRepresentationModelAssembler workbasketRepresentationModelAssembler;
  private final WorkbasketSummaryRepresentationModelAssembler
      workbasketSummaryRepresentationModelAssembler;
  private final WorkbasketAccessItemRepresentationModelAssembler
      workbasketAccessItemRepresentationModelAssembler;

  @Autowired
  WorkbasketController(
      WorkbasketService workbasketService,
      WorkbasketRepresentationModelAssembler workbasketRepresentationModelAssembler,
      WorkbasketSummaryRepresentationModelAssembler workbasketSummaryRepresentationModelAssembler,
      WorkbasketAccessItemRepresentationModelAssembler
          workbasketAccessItemRepresentationModelAssembler) {
    this.workbasketService = workbasketService;
    this.workbasketRepresentationModelAssembler = workbasketRepresentationModelAssembler;
    this.workbasketSummaryRepresentationModelAssembler =
        workbasketSummaryRepresentationModelAssembler;

    this.workbasketAccessItemRepresentationModelAssembler =
        workbasketAccessItemRepresentationModelAssembler;
  }

  @GetMapping(path = RestEndpoints.URL_WORKBASKET)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<WorkbasketSummaryPagedRepresentationModel> getWorkbaskets(
      HttpServletRequest request,
      @ParameterObject WorkbasketQueryFilterParameter filterParameter,
      @ParameterObject WorkbasketQuerySortParameter sortParameter,
      @ParameterObject QueryPagingParameter<WorkbasketSummary, WorkbasketQuery> pagingParameter) {

    QueryParamsValidator.validateParams(
        request,
        WorkbasketQueryFilterParameter.class,
        QuerySortParameter.class,
        QueryPagingParameter.class);

    WorkbasketQuery query = workbasketService.createWorkbasketQuery();
    filterParameter.apply(query);
    sortParameter.apply(query);

    List<WorkbasketSummary> workbasketSummaries = pagingParameter.apply(query);
    WorkbasketSummaryPagedRepresentationModel pagedModels =
        workbasketSummaryRepresentationModelAssembler.toPagedModel(
            workbasketSummaries, pagingParameter.getPageMetadata());

    return ResponseEntity.ok(pagedModels);
  }

  @GetMapping(path = RestEndpoints.URL_WORKBASKET_ID, produces = MediaTypes.HAL_JSON_VALUE)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<WorkbasketRepresentationModel> getWorkbasket(
      @PathVariable("workbasketId") String workbasketId)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException {
    Workbasket workbasket = workbasketService.getWorkbasket(workbasketId);

    return ResponseEntity.ok(workbasketRepresentationModelAssembler.toModel(workbasket));
  }

  @DeleteMapping(path = RestEndpoints.URL_WORKBASKET_ID)
  @Transactional(rollbackFor = Exception.class, noRollbackFor = WorkbasketNotFoundException.class)
  public ResponseEntity<WorkbasketRepresentationModel> deleteWorkbasket(
      @PathVariable("workbasketId") String workbasketId)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          WorkbasketInUseException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException {

    boolean workbasketDeleted = workbasketService.deleteWorkbasket(workbasketId);

    if (workbasketDeleted) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Workbasket successfully deleted.");
      }
      return ResponseEntity.noContent().build();
    } else {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "Workbasket was only marked for deletion and will be physically deleted later on.");
      }
      return ResponseEntity.accepted().build();
    }
  }

  @PostMapping(path = RestEndpoints.URL_WORKBASKET)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<WorkbasketRepresentationModel> createWorkbasket(
      @RequestBody WorkbasketRepresentationModel workbasketRepresentationModel)
      throws InvalidArgumentException,
          NotAuthorizedException,
          WorkbasketAlreadyExistException,
          DomainNotFoundException {
    Workbasket workbasket =
        workbasketRepresentationModelAssembler.toEntityModel(workbasketRepresentationModel);
    workbasket = workbasketService.createWorkbasket(workbasket);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(workbasketRepresentationModelAssembler.toModel(workbasket));
  }

  @PutMapping(path = RestEndpoints.URL_WORKBASKET_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<WorkbasketRepresentationModel> updateWorkbasket(
      @PathVariable("workbasketId") String workbasketId,
      @RequestBody WorkbasketRepresentationModel workbasketRepresentationModel)
      throws WorkbasketNotFoundException,
          NotAuthorizedException,
          ConcurrencyException,
          InvalidArgumentException,
          NotAuthorizedOnWorkbasketException {
    if (!workbasketId.equals(workbasketRepresentationModel.getWorkbasketId())) {
      throw new InvalidArgumentException(
          "Target-WB-ID('"
              + workbasketId
              + "') is not identical with the WB-ID of to object which should be updated. ID=('"
              + workbasketRepresentationModel.getWorkbasketId()
              + "')");
    }
    Workbasket workbasket =
        workbasketRepresentationModelAssembler.toEntityModel(workbasketRepresentationModel);
    workbasket = workbasketService.updateWorkbasket(workbasket);

    return ResponseEntity.ok(workbasketRepresentationModelAssembler.toModel(workbasket));
  }

  @GetMapping(
      path = RestEndpoints.URL_WORKBASKET_ID_ACCESS_ITEMS,
      produces = MediaTypes.HAL_JSON_VALUE)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<WorkbasketAccessItemCollectionRepresentationModel> getWorkbasketAccessItems(
      @PathVariable("workbasketId") String workbasketId)
      throws WorkbasketNotFoundException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException {
    List<WorkbasketAccessItem> accessItems =
        workbasketService.getWorkbasketAccessItems(workbasketId);

    return ResponseEntity.ok(
        workbasketAccessItemRepresentationModelAssembler.toKadaiCollectionModelForSingleWorkbasket(
            workbasketId, accessItems));
  }

  @PutMapping(path = RestEndpoints.URL_WORKBASKET_ID_ACCESS_ITEMS)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<WorkbasketAccessItemCollectionRepresentationModel> setWorkbasketAccessItems(
      @PathVariable("workbasketId") String workbasketId,
      @RequestBody WorkbasketAccessItemCollectionRepresentationModel workbasketAccessItemRepModels)
      throws InvalidArgumentException,
          WorkbasketNotFoundException,
          WorkbasketAccessItemAlreadyExistException,
          NotAuthorizedException,
          NotAuthorizedOnWorkbasketException {
    if (workbasketAccessItemRepModels == null) {
      throw new InvalidArgumentException("Can't create something with NULL body-value.");
    }

    List<WorkbasketAccessItem> wbAccessItems =
        workbasketAccessItemRepModels.getContent().stream()
            .map(workbasketAccessItemRepresentationModelAssembler::toEntityModel)
            .toList();
    workbasketService.setWorkbasketAccessItems(workbasketId, wbAccessItems);
    List<WorkbasketAccessItem> updatedWbAccessItems =
        workbasketService.getWorkbasketAccessItems(workbasketId);

    return ResponseEntity.ok(
        workbasketAccessItemRepresentationModelAssembler.toKadaiCollectionModelForSingleWorkbasket(
            workbasketId, updatedWbAccessItems));
  }

  @GetMapping(
      path = RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION,
      produces = MediaTypes.HAL_JSON_VALUE)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<DistributionTargetsCollectionRepresentationModel> getDistributionTargets(
      @PathVariable("workbasketId") String workbasketId)
      throws WorkbasketNotFoundException, NotAuthorizedOnWorkbasketException {
    List<WorkbasketSummary> distributionTargets =
        workbasketService.getDistributionTargets(workbasketId);
    DistributionTargetsCollectionRepresentationModel distributionTargetRepModels =
        workbasketSummaryRepresentationModelAssembler.toKadaiCollectionModel(distributionTargets);

    return ResponseEntity.ok(distributionTargetRepModels);
  }

  @PutMapping(path = RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<DistributionTargetsCollectionRepresentationModel>
      setDistributionTargetsForWorkbasketId(
          @PathVariable("workbasketId") String sourceWorkbasketId,
          @RequestBody List<String> targetWorkbasketIds)
          throws WorkbasketNotFoundException,
              NotAuthorizedException,
              NotAuthorizedOnWorkbasketException {
    workbasketService.setDistributionTargets(sourceWorkbasketId, targetWorkbasketIds);

    List<WorkbasketSummary> distributionTargets =
        workbasketService.getDistributionTargets(sourceWorkbasketId);

    return ResponseEntity.ok(
        workbasketSummaryRepresentationModelAssembler.toKadaiCollectionModel(distributionTargets));
  }

  @DeleteMapping(path = RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<Void> removeDistributionTargetForWorkbasketId(
      @PathVariable("workbasketId") String targetWorkbasketId)
      throws WorkbasketNotFoundException,
          NotAuthorizedOnWorkbasketException,
          NotAuthorizedException {
    List<WorkbasketSummary> sourceWorkbaskets =
        workbasketService.getDistributionSources(targetWorkbasketId);
    for (WorkbasketSummary source : sourceWorkbaskets) {
      workbasketService.removeDistributionTarget(source.getId(), targetWorkbasketId);
    }

    return ResponseEntity.noContent().build();
  }

  public enum WorkbasketQuerySortBy implements QuerySortBy<WorkbasketQuery> {
    NAME(WorkbasketQuery::orderByName),
    KEY(WorkbasketQuery::orderByKey),
    OWNER(WorkbasketQuery::orderByOwner),
    TYPE(WorkbasketQuery::orderByType),
    DESCRIPTION(WorkbasketQuery::orderByDescription),
    CUSTOM_1((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_1, sort)),
    CUSTOM_2((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_2, sort)),
    CUSTOM_3((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_3, sort)),
    CUSTOM_4((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_4, sort)),
    CUSTOM_5((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_5, sort)),
    CUSTOM_6((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_6, sort)),
    CUSTOM_7((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_7, sort)),
    CUSTOM_8((query, sort) -> query.orderByCustomAttribute(WorkbasketCustomField.CUSTOM_8, sort)),
    DOMAIN(WorkbasketQuery::orderByDomain),
    ORG_LEVEL_1(WorkbasketQuery::orderByOrgLevel1),
    ORG_LEVEL_2(WorkbasketQuery::orderByOrgLevel2),
    ORG_LEVEL_3(WorkbasketQuery::orderByOrgLevel3),
    ORG_LEVEL_4(WorkbasketQuery::orderByOrgLevel4);

    private final BiConsumer<WorkbasketQuery, SortDirection> consumer;

    WorkbasketQuerySortBy(BiConsumer<WorkbasketQuery, SortDirection> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void applySortByForQuery(WorkbasketQuery query, SortDirection sortDirection) {
      consumer.accept(query, sortDirection);
    }
  }

  // Unfortunately this class is necessary, since spring can not inject the generic 'sort-by'
  // parameter from the super class.
  public static class WorkbasketQuerySortParameter
      extends QuerySortParameter<WorkbasketQuery, WorkbasketQuerySortBy> {

    @ConstructorProperties({"sort-by", "order"})
    public WorkbasketQuerySortParameter(
        List<WorkbasketQuerySortBy> sortBy, List<SortDirection> order)
        throws InvalidArgumentException {
      super(sortBy, order);
    }

    // this getter is necessary for the documentation!
    @Override
    public List<WorkbasketQuerySortBy> getSortBy() {
      return super.getSortBy();
    }
  }
}
