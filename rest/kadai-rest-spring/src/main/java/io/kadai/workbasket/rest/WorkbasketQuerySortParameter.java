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

package io.kadai.workbasket.rest;

import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.rest.QuerySortParameter;
import io.kadai.workbasket.api.WorkbasketQuery;
import java.beans.ConstructorProperties;
import java.util.List;

public class WorkbasketQuerySortParameter
    extends QuerySortParameter<WorkbasketQuery, WorkbasketQuerySortBy> {

  @ConstructorProperties({"sort-by", "order"})
  public WorkbasketQuerySortParameter(List<WorkbasketQuerySortBy> sortBy, List<SortDirection> order)
      throws InvalidArgumentException {
    super(sortBy, order);
  }
}
