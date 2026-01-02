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

package io.kadai.task.rest.models;

import io.kadai.common.api.exceptions.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.hateoas.RepresentationModel;

/** EntityModel class for BulkOperationResults. */
public class BulkOperationResultsRepresentationModel
    extends RepresentationModel<BulkOperationResultsRepresentationModel> {

  /** Map of keys to the stored information. */
  protected Map<String, ErrorCode> tasksWithErrors = new HashMap<>();

  public Map<String, ErrorCode> getTasksWithErrors() {
    return tasksWithErrors;
  }

  public void setTasksWithErrors(Map<String, ErrorCode> tasksWithErrors) {
    this.tasksWithErrors = tasksWithErrors;
  }
}
