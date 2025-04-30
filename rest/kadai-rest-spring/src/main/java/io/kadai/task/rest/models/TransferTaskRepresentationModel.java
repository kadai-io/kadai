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

package io.kadai.task.rest.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.beans.ConstructorProperties;
import java.util.List;

public class TransferTaskRepresentationModel {

  /** The value to set the Task property owner. */
  @JsonProperty("owner")
  private final String owner;

  /** The value to set the Task property setTransferFlag. */
  @JsonProperty("setTransferFlag")
  private final Boolean setTransferFlag;

  /** The value to set the Task property taskIds. */
  @JsonProperty("taskIds")
  private final List<String> taskIds;

  @ConstructorProperties({"setTransferFlag", "owner", "taskIds"})
  public TransferTaskRepresentationModel(
      Boolean setTransferFlag, String owner, List<String> taskIds) {
    this.setTransferFlag = setTransferFlag == null || setTransferFlag;
    this.owner = owner;
    this.taskIds = taskIds;
  }

  public Boolean getSetTransferFlag() {
    return setTransferFlag;
  }

  public String getOwner() {
    return owner;
  }

  public List<String> getTaskIds() {
    return taskIds;
  }
}
