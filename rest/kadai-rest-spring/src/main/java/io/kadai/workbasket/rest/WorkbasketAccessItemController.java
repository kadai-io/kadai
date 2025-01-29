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
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.QuerySortBy;
import io.kadai.common.rest.QuerySortParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.common.rest.ldap.LdapClient;
import io.kadai.common.rest.util.QueryParamsValidator;
import io.kadai.workbasket.api.WorkbasketAccessItemQuery;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketAccessItem;
import io.kadai.workbasket.rest.assembler.WorkbasketAccessItemRepresentationModelAssembler;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemPagedRepresentationModel;
import jakarta.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.List;
import java.util.function.BiConsumer;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for Workbasket access. */
@RestController
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class WorkbasketAccessItemController implements WorkbasketAccessItemApi {

  private final LdapClient ldapClient;
  private final WorkbasketService workbasketService;
  private final WorkbasketAccessItemRepresentationModelAssembler modelAssembler;

  @Autowired
  public WorkbasketAccessItemController(
      LdapClient ldapClient,
      WorkbasketService workbasketService,
      WorkbasketAccessItemRepresentationModelAssembler modelAssembler) {
    this.ldapClient = ldapClient;
    this.workbasketService = workbasketService;
    this.modelAssembler = modelAssembler;
  }

  @GetMapping(path = RestEndpoints.URL_WORKBASKET_ACCESS_ITEMS)
  public ResponseEntity<WorkbasketAccessItemPagedRepresentationModel> getWorkbasketAccessItems(
      HttpServletRequest request,
      @ParameterObject WorkbasketAccessItemQueryFilterParameter filterParameter,
      @ParameterObject WorkbasketAccessItemQuerySortParameter sortParameter,
      @ParameterObject
          QueryPagingParameter<WorkbasketAccessItem, WorkbasketAccessItemQuery> pagingParameter)
      throws NotAuthorizedException {

    QueryParamsValidator.validateParams(
        request,
        WorkbasketAccessItemQueryFilterParameter.class,
        QuerySortParameter.class,
        QueryPagingParameter.class);

    WorkbasketAccessItemQuery query = workbasketService.createWorkbasketAccessItemQuery();
    filterParameter.apply(query);
    sortParameter.apply(query);

    List<WorkbasketAccessItem> workbasketAccessItems = pagingParameter.apply(query);

    WorkbasketAccessItemPagedRepresentationModel pagedResources =
        modelAssembler.toPagedModel(workbasketAccessItems, pagingParameter.getPageMetadata());

    return ResponseEntity.ok(pagedResources);
  }

  @DeleteMapping(path = RestEndpoints.URL_WORKBASKET_ACCESS_ITEMS)
  public ResponseEntity<Void> removeWorkbasketAccessItems(
      @RequestParam("access-id") String accessId)
      throws NotAuthorizedException, InvalidArgumentException {
    if (ldapClient.isUser(accessId)) {
      List<WorkbasketAccessItem> workbasketAccessItemList =
          workbasketService.createWorkbasketAccessItemQuery().accessIdIn(accessId).list();

      if (workbasketAccessItemList != null && !workbasketAccessItemList.isEmpty()) {
        workbasketService.deleteWorkbasketAccessItemsForAccessId(accessId);
      }
    } else {
      throw new InvalidArgumentException(
          String.format(
              "AccessId '%s' is not a user. " + "You can remove all access items for users only.",
              accessId));
    }

    return ResponseEntity.noContent().build();
  }

  public enum WorkbasketAccessItemSortBy implements QuerySortBy<WorkbasketAccessItemQuery> {
    WORKBASKET_KEY(WorkbasketAccessItemQuery::orderByWorkbasketKey),
    ACCESS_ID(WorkbasketAccessItemQuery::orderByAccessId);

    private final BiConsumer<WorkbasketAccessItemQuery, SortDirection> consumer;

    WorkbasketAccessItemSortBy(BiConsumer<WorkbasketAccessItemQuery, SortDirection> consumer) {
      this.consumer = consumer;
    }

    @Override
    public void applySortByForQuery(WorkbasketAccessItemQuery query, SortDirection sortDirection) {
      consumer.accept(query, sortDirection);
    }
  }

  // Unfortunately this class is necessary, since spring can not inject the generic 'sort-by'
  // parameter from the super class.
  public static class WorkbasketAccessItemQuerySortParameter
      extends QuerySortParameter<WorkbasketAccessItemQuery, WorkbasketAccessItemSortBy> {

    @ConstructorProperties({"sort-by", "order"})
    public WorkbasketAccessItemQuerySortParameter(
        List<WorkbasketAccessItemSortBy> sortBy, List<SortDirection> order)
        throws InvalidArgumentException {
      super(sortBy, order);
    }

    // this getter is necessary for the documentation!
    @Override
    public List<WorkbasketAccessItemSortBy> getSortBy() {
      return super.getSortBy();
    }
  }
}
