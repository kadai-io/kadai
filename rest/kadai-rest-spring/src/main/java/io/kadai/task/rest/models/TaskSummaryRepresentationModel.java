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

import static io.kadai.task.api.models.TaskSummary.DEFAULT_MANUAL_PRIORITY;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

public class TaskSummaryRepresentationModel extends BaseTaskRepresentationModel {

  @Schema(
      name = "owner",
      description = "The owner of the task. The owner is set upon claiming of the task.")
  protected String owner;

  @Schema(name = "ownerLongName", description = "The long name of the task owner.")
  protected String ownerLongName;

  @Schema(name = "priority", description = "The priority of the task.")
  protected int priority;

  @Schema(
      name = "manualPriority",
      description =
          "The manual priority of the task. If the value of manualPriority is zero or greater, the "
              + "priority is automatically set to manualPriority. In this case, all computations of"
              + " priority are disabled. If the value of manualPriority is negative, Tasks are not"
              + " prioritized manually.")
  protected int manualPriority = DEFAULT_MANUAL_PRIORITY;

  /** The current count of the comments. */
  protected int numberOfComments;

  @Schema(name = "isRead", description = "Indicator if the task has been read.")
  protected boolean isRead;

  @Schema(name = "isTransferred", description = "Indicator if the task has been transferred.")
  protected boolean isTransferred;

  @Schema(name = "isReopened", description = "Indicator if the task has been reopened.")
  protected boolean isReopened;

  @Schema(
      name = "secondaryObjectReferences",
      description = "Secondary object references of the task.")
  protected List<ObjectReferenceRepresentationModel> secondaryObjectReferences = new ArrayList<>();

  // Getters and setters for the unique fields only
  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getOwnerLongName() {
    return ownerLongName;
  }

  public void setOwnerLongName(String ownerLongName) {
    this.ownerLongName = ownerLongName;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int getManualPriority() {
    return manualPriority;
  }

  public void setManualPriority(int manualPriority) {
    this.manualPriority = manualPriority;
  }

  public int getNumberOfComments() {
    return numberOfComments;
  }

  public void setNumberOfComments(int numberOfComments) {
    this.numberOfComments = numberOfComments;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean isRead) {
    this.isRead = isRead;
  }

  public boolean isTransferred() {
    return isTransferred;
  }

  public void setTransferred(boolean isTransferred) {
    this.isTransferred = isTransferred;
  }

  public boolean isReopened() {
    return isReopened;
  }

  public void setReopened(boolean reopened) {
    isReopened = reopened;
  }

  public List<ObjectReferenceRepresentationModel> getSecondaryObjectReferences() {
    return secondaryObjectReferences;
  }

  public void setSecondaryObjectReferences(
      List<ObjectReferenceRepresentationModel> secondaryObjectReferences) {
    this.secondaryObjectReferences = secondaryObjectReferences;
  }
}
