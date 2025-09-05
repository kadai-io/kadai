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

package io.kadai.task.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.rest.QueryParameter;
import io.kadai.task.api.TaskQuery;
import io.swagger.v3.oas.annotations.Parameter;
import java.beans.ConstructorProperties;
import java.util.Optional;
import java.util.function.Consumer;

public class TaskQueryGroupByParameter implements QueryParameter<TaskQuery, Void> {
  // region groupBy
  @JsonProperty("group-by")
  @Parameter(name = "group-by")
  private final TaskQueryGroupBy groupByPor;

  @JsonProperty("group-by-sor")
  @Parameter(name = "group-by-sor")
  private final String groupBySor;

  @ConstructorProperties({"group-by", "group-by-sor"})
  public TaskQueryGroupByParameter(TaskQueryGroupBy groupBy, String groupBySor)
      throws InvalidArgumentException {
    this.groupByPor = groupBy;
    this.groupBySor = groupBySor;
    validateGroupByParameters();
  }

  public String getGroupBySor() {
    return groupBySor;
  }

  public TaskQueryGroupBy getGroupByPor() {
    return groupByPor;
  }

  // endregion

  // region constructor

  @Override
  public Void apply(TaskQuery query) {

    Optional.ofNullable(groupBySor).ifPresent(query::groupBySor);
    Optional.ofNullable(groupByPor)
        .ifPresent(taskQueryGroupBy -> taskQueryGroupBy.applyGroupByForQuery(query));

    return null;
  }

  // endregion

  private void validateGroupByParameters() throws InvalidArgumentException {
    if (groupByPor != null && groupBySor != null) {
      throw new InvalidArgumentException(
          "Only one of the following can be provided: Either group-by or group-by-sor");
    }
  }

  public enum TaskQueryGroupBy {
    POR_VALUE(TaskQuery::groupByPor);
    private final Consumer<TaskQuery> consumer;

    TaskQueryGroupBy(Consumer<TaskQuery> consumer) {
      this.consumer = consumer;
    }

    public void applyGroupByForQuery(TaskQuery query) {
      consumer.accept(query);
    }
  }
}
