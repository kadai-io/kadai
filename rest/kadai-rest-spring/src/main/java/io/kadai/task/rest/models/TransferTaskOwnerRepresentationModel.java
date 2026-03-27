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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.beans.ConstructorProperties;
import java.util.List;

public class TransferTaskOwnerRepresentationModel {

  /** The list of Task IDs to transfer. */
  @JsonProperty("taskIds")
  private final List<String> taskIds;

  @ConstructorProperties({"taskIds"})
  public TransferTaskOwnerRepresentationModel(List<String> taskIds) {
    this.taskIds = taskIds;
  }

  public List<String> getTaskIds() {
    return taskIds;
  }
}
