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

package io.kadai.classification.rest;

import io.kadai.classification.api.ClassificationQuery;
import io.kadai.common.api.BaseQuery.SortDirection;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.rest.QuerySortParameter;
import java.beans.ConstructorProperties;
import java.util.List;

public class ClassificationQuerySortParameter
    extends QuerySortParameter<ClassificationQuery, ClassificationQuerySortBy> {

  @ConstructorProperties({"sort-by", "order"})
  public ClassificationQuerySortParameter(
      List<ClassificationQuerySortBy> sortBy, List<SortDirection> order)
      throws InvalidArgumentException {
    super(sortBy, order);
  }
}
