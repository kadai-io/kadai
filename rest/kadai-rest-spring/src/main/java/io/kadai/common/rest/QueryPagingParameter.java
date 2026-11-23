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

package io.kadai.common.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.api.BaseQuery;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.rest.models.PageMetadata;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import java.beans.ConstructorProperties;
import java.util.List;

public class QueryPagingParameter<T, Q extends BaseQuery<T, ?>>
    implements QueryParameter<Q, List<T>> {

  @Parameter(
      name = "page",
      description =
          "Request a specific page. Should be combined with 'page-size'; without it, the page "
              + "contains all results. Must be greater than or equal to 1.")
  @JsonProperty("page")
  @Min(1)
  private final Integer page;

  @Parameter(
      name = "page-size",
      description =
          "Defines the size for each page. This requires a specific requested 'page'. "
              + "Must be greater than or equal to 1. There is no upper bound.")
  @JsonProperty("page-size")
  @Min(1)
  private final Integer pageSize;

  @JsonIgnore private PageMetadata pageMetadata;

  /**
   * Creates a new paging parameter.
   *
   * <p>There is deliberately no upper bound for {@code pageSize}: a request without {@code page}
   * already returns the complete result set, so capping {@code pageSize} would not limit the
   * amount of returned data.
   *
   * @param page the requested page; must be {@code >= 1} if present
   * @param pageSize the size of each page; must be {@code >= 1} if present
   * @throws InvalidArgumentException if {@code page} or {@code pageSize} is less than 1
   */
  @ConstructorProperties({"page", "page-size"})
  public QueryPagingParameter(Integer page, Integer pageSize) throws InvalidArgumentException {
    verifyAtLeastOne("page", page);
    verifyAtLeastOne("page-size", pageSize);
    if (pageSize == null) {
      pageSize = Integer.MAX_VALUE;
    }
    this.page = page;
    this.pageSize = pageSize;
  }

  public Integer getPage() {
    return page;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public PageMetadata getPageMetadata() {
    return pageMetadata;
  }

  @Override
  public List<T> apply(Q query) {
    initPageMetaData(query);
    List<T> resultList;
    if (pageMetadata != null) {
      // computed as long, because (page - 1) * page-size can exceed the int range
      long offset = (pageMetadata.getNumber() - 1) * pageMetadata.getSize();
      resultList =
          offset >= pageMetadata.getTotalElements()
              ? List.of()
              : query.listPage(
                  Math.toIntExact(pageMetadata.getNumber()),
                  Math.toIntExact(pageMetadata.getSize()));
    } else {
      resultList = query.list();
    }
    return resultList;
  }

  private static void verifyAtLeastOne(String parameterName, Integer value)
      throws InvalidArgumentException {
    if (value != null && value < 1) {
      throw new InvalidArgumentException(
          String.format(
              "The query parameter '%s' must be greater than or equal to 1, but was %d.",
              parameterName, value));
    }
  }

  private void initPageMetaData(Q query) {
    if (page != null) {
      long totalElements = query.count();
      long maxPages = (long) Math.ceil(totalElements / pageSize.doubleValue());
      pageMetadata = new PageMetadata(pageSize, totalElements, maxPages, page);
    }
  }
}
