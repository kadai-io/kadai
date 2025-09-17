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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kadai.task.rest.models.TaskRepresentationModel.CustomAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    description =
        "Patch model for Task with all fields nullable - null values are ignored during updates. "
            + "Note: Some complex fields like workbasketSummary, primaryObjRef, "
            + "secondaryObjectReferences, and attachments are not yet supported in bulk updates and"
            + " will be ignored.")
@JsonIgnoreProperties("attachmentSummaries")
public class TaskPatchRepresentationModel extends BaseTaskRepresentationModel {

  @Schema(name = "priority", description = "The priority of the task.")
  protected Integer priority;

  @Schema(
      name = "manualPriority",
      description =
          "The manual priority of the task. If the value of manualPriority is zero or greater, the "
              + "priority is automatically set to manualPriority. In this case, all computations of"
              + " priority are disabled. If the value of manualPriority is negative, Tasks are not"
              + " prioritized manually.")
  protected Integer manualPriority;

  @Schema(name = "isRead", description = "Indicator if the task has been read.")
  protected Boolean isRead;

  @Schema(
      name = "secondaryObjectReferences",
      description = "Secondary object references of the task.")
  protected List<ObjectReferenceRepresentationModel> secondaryObjectReferences;

  public List<CustomAttribute> getCustomAttributes() {
    return customAttributes;
  }

  @Schema(name = "customAttributes", description = "Additional information of the task.")
  private List<CustomAttribute> customAttributes;

  @Schema(name = "callbackInfo", description = "Callback Information of the task.")
  private List<CustomAttribute> callbackInfo;

  // Getters and setters for the additional fields
  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Integer getManualPriority() {
    return manualPriority;
  }

  public void setManualPriority(Integer manualPriority) {
    this.manualPriority = manualPriority;
  }

  public Boolean getIsRead() {
    return isRead;
  }

  public void setIsRead(Boolean isRead) {
    this.isRead = isRead;
  }

  public List<ObjectReferenceRepresentationModel> getSecondaryObjectReferences() {
    return secondaryObjectReferences;
  }

  public void setSecondaryObjectReferences(
      List<ObjectReferenceRepresentationModel> secondaryObjectReferences) {
    this.secondaryObjectReferences = secondaryObjectReferences;
  }

  public void setCustomAttributes(List<CustomAttribute> customAttributes) {
    this.customAttributes = customAttributes;
  }

  public List<CustomAttribute> getCallbackInfo() {
    return callbackInfo;
  }

  public void setCallbackInfo(List<CustomAttribute> callbackInfo) {
    this.callbackInfo = callbackInfo;
  }
}
