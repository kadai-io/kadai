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
import io.kadai.classification.rest.models.ClassificationSummaryRepresentationModel;
import io.kadai.task.rest.models.TaskRepresentationModel.CustomAttribute;
import io.kadai.workbasket.rest.models.WorkbasketSummaryRepresentationModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(
    description =
        "Patch model for Task with all fields nullable - null values are ignored during updates.")
@JsonIgnoreProperties("attachmentSummaries")
public class TaskPatchRepresentationModel {

  @Schema(
      name = "received",
      description =
          "Timestamp when the task has been received. It notes when the surrounding process started"
              + " and not just when the actual task was created.")
  private Instant received;

  @Schema(
      name = "planned",
      description =
          "Planned start of the task. The actual completion of the task should be between PLANNED"
              + " and DUE.")
  private Instant planned;

  @Schema(
      name = "due",
      description =
          "Timestamp when the task is due. The actual completion of the task should be between "
              + "PLANNED and DUE.")
  private Instant due;

  @Schema(name = "name", description = "The name of the task.")
  private String name;

  @Schema(name = "note", description = "note.")
  private String note;

  @Schema(name = "description", description = "The description of the task.")
  private String description;

  @Schema(name = "classificationSummary", description = "The classification of this task.")
  private ClassificationSummaryRepresentationModel classificationSummary;

  @Schema(name = "workbasketSummary", description = "The workbasket this task resides in.")
  private WorkbasketSummaryRepresentationModel workbasketSummary;

  @Schema(name = "businessProcessId", description = "The classification of this task.")
  private String businessProcessId;

  @Schema(name = "parentBusinessProcessId", description = "the parent business process id.")
  private String parentBusinessProcessId;

  @Schema(name = "primaryObjRef", description = "The Objects primary ObjectReference.")
  private ObjectReferenceRepresentationModel primaryObjRef;

  @Schema(
      name = "manualPriority",
      description =
          "The manual priority of the task. If the value of manualPriority is zero or greater, the "
              + "priority is automatically set to manualPriority. In this case, all computations of"
              + " priority are disabled. If the value of manualPriority is negative, Tasks are not"
              + " prioritized manually.")
  private Integer manualPriority;

  @Schema(name = "isRead", description = "Indicator if the task has been read.")
  private Boolean isRead;

  @Schema(
      name = "secondaryObjectReferences",
      description = "Secondary object references of the task.")
  private List<ObjectReferenceRepresentationModel> secondaryObjectReferences;

  @Schema(name = "custom1", description = "A custom property with name \"1\".")
  protected String custom1;

  @Schema(name = "custom2", description = "A custom property with name \"2\".")
  protected String custom2;

  @Schema(name = "custom3", description = "A custom property with name \"3\".")
  protected String custom3;

  @Schema(name = "custom4", description = "A custom property with name \"4\".")
  protected String custom4;

  @Schema(name = "custom5", description = "A custom property with name \"5\".")
  protected String custom5;

  @Schema(name = "custom6", description = "A custom property with name \"6\".")
  protected String custom6;

  @Schema(name = "custom7", description = "A custom property with name \"7\".")
  protected String custom7;

  @Schema(name = "custom8", description = "A custom property with name \"8\".")
  protected String custom8;

  @Schema(name = "custom9", description = "A custom property with name \"9\".")
  protected String custom9;

  @Schema(name = "custom10", description = "A custom property with name \"10\".")
  protected String custom10;

  @Schema(name = "custom11", description = "A custom property with name \"11\".")
  protected String custom11;

  @Schema(name = "custom12", description = "A custom property with name \"12\".")
  protected String custom12;

  @Schema(name = "custom13", description = "A custom property with name \"13\".")
  protected String custom13;

  @Schema(name = "custom14", description = "A custom property with name \"14\".")
  protected String custom14;

  @Schema(name = "custom15", description = "A custom property with name \"15\".")
  protected String custom15;

  @Schema(name = "custom16", description = "A custom property with name \"16\".")
  protected String custom16;

  @Schema(name = "customInt1", description = "A custom int property with name \"1\".")
  protected Integer customInt1;

  @Schema(name = "customInt2", description = "A custom int property with name \"2\".")
  protected Integer customInt2;

  @Schema(name = "customInt3", description = "A custom int property with name \"3\".")
  protected Integer customInt3;

  @Schema(name = "customInt4", description = "A custom int property with name \"4\".")
  protected Integer customInt4;

  @Schema(name = "customInt5", description = "A custom int property with name \"5\".")
  protected Integer customInt5;

  @Schema(name = "customInt6", description = "A custom int property with name \"6\".")
  protected Integer customInt6;

  @Schema(name = "customInt7", description = "A custom int property with name \"7\".")
  protected Integer customInt7;

  @Schema(name = "customInt8", description = "A custom int property with name \"8\".")
  protected Integer customInt8;

  @Schema(name = "customAttributes", description = "Additional information of the task.")
  private List<CustomAttribute> customAttributes;

  @Schema(name = "callbackInfo", description = "Callback Information of the task.")
  private List<CustomAttribute> callbackInfo;

  public Boolean getIsRead() {
    return isRead;
  }

  public void setRead(Boolean read) {
    isRead = read;
  }

  public Instant getReceived() {
    return received;
  }

  public void setReceived(Instant received) {
    this.received = received;
  }

  public Instant getPlanned() {
    return planned;
  }

  public void setPlanned(Instant planned) {
    this.planned = planned;
  }

  public Instant getDue() {
    return due;
  }

  public void setDue(Instant due) {
    this.due = due;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ClassificationSummaryRepresentationModel getClassificationSummary() {
    return classificationSummary;
  }

  public void setClassificationSummary(
      ClassificationSummaryRepresentationModel classificationSummary) {
    this.classificationSummary = classificationSummary;
  }

  public WorkbasketSummaryRepresentationModel getWorkbasketSummary() {
    return workbasketSummary;
  }

  public void setWorkbasketSummary(WorkbasketSummaryRepresentationModel workbasketSummary) {
    this.workbasketSummary = workbasketSummary;
  }

  public String getBusinessProcessId() {
    return businessProcessId;
  }

  public void setBusinessProcessId(String businessProcessId) {
    this.businessProcessId = businessProcessId;
  }

  public String getParentBusinessProcessId() {
    return parentBusinessProcessId;
  }

  public void setParentBusinessProcessId(String parentBusinessProcessId) {
    this.parentBusinessProcessId = parentBusinessProcessId;
  }

  public ObjectReferenceRepresentationModel getPrimaryObjRef() {
    return primaryObjRef;
  }

  public void setPrimaryObjRef(ObjectReferenceRepresentationModel primaryObjRef) {
    this.primaryObjRef = primaryObjRef;
  }

  public Integer getManualPriority() {
    return manualPriority;
  }

  public void setManualPriority(Integer manualPriority) {
    this.manualPriority = manualPriority;
  }

  public List<ObjectReferenceRepresentationModel> getSecondaryObjectReferences() {
    return secondaryObjectReferences;
  }

  public void setSecondaryObjectReferences(
      List<ObjectReferenceRepresentationModel> secondaryObjectReferences) {
    this.secondaryObjectReferences = secondaryObjectReferences;
  }

  public String getCustom1() {
    return custom1;
  }

  public void setCustom1(String custom1) {
    this.custom1 = custom1;
  }

  public String getCustom2() {
    return custom2;
  }

  public void setCustom2(String custom2) {
    this.custom2 = custom2;
  }

  public String getCustom3() {
    return custom3;
  }

  public void setCustom3(String custom3) {
    this.custom3 = custom3;
  }

  public String getCustom4() {
    return custom4;
  }

  public void setCustom4(String custom4) {
    this.custom4 = custom4;
  }

  public String getCustom5() {
    return custom5;
  }

  public void setCustom5(String custom5) {
    this.custom5 = custom5;
  }

  public String getCustom6() {
    return custom6;
  }

  public void setCustom6(String custom6) {
    this.custom6 = custom6;
  }

  public String getCustom7() {
    return custom7;
  }

  public void setCustom7(String custom7) {
    this.custom7 = custom7;
  }

  public String getCustom8() {
    return custom8;
  }

  public void setCustom8(String custom8) {
    this.custom8 = custom8;
  }

  public String getCustom9() {
    return custom9;
  }

  public void setCustom9(String custom9) {
    this.custom9 = custom9;
  }

  public String getCustom10() {
    return custom10;
  }

  public void setCustom10(String custom10) {
    this.custom10 = custom10;
  }

  public String getCustom11() {
    return custom11;
  }

  public void setCustom11(String custom11) {
    this.custom11 = custom11;
  }

  public String getCustom12() {
    return custom12;
  }

  public void setCustom12(String custom12) {
    this.custom12 = custom12;
  }

  public String getCustom13() {
    return custom13;
  }

  public void setCustom13(String custom13) {
    this.custom13 = custom13;
  }

  public String getCustom14() {
    return custom14;
  }

  public void setCustom14(String custom14) {
    this.custom14 = custom14;
  }

  public String getCustom15() {
    return custom15;
  }

  public void setCustom15(String custom15) {
    this.custom15 = custom15;
  }

  public String getCustom16() {
    return custom16;
  }

  public void setCustom16(String custom16) {
    this.custom16 = custom16;
  }

  public Integer getCustomInt1() {
    return customInt1;
  }

  public void setCustomInt1(Integer customInt1) {
    this.customInt1 = customInt1;
  }

  public Integer getCustomInt2() {
    return customInt2;
  }

  public void setCustomInt2(Integer customInt2) {
    this.customInt2 = customInt2;
  }

  public Integer getCustomInt3() {
    return customInt3;
  }

  public void setCustomInt3(Integer customInt3) {
    this.customInt3 = customInt3;
  }

  public Integer getCustomInt4() {
    return customInt4;
  }

  public void setCustomInt4(Integer customInt4) {
    this.customInt4 = customInt4;
  }

  public Integer getCustomInt5() {
    return customInt5;
  }

  public void setCustomInt5(Integer customInt5) {
    this.customInt5 = customInt5;
  }

  public Integer getCustomInt6() {
    return customInt6;
  }

  public void setCustomInt6(Integer customInt6) {
    this.customInt6 = customInt6;
  }

  public Integer getCustomInt7() {
    return customInt7;
  }

  public void setCustomInt7(Integer customInt7) {
    this.customInt7 = customInt7;
  }

  public Integer getCustomInt8() {
    return customInt8;
  }

  public void setCustomInt8(Integer customInt8) {
    this.customInt8 = customInt8;
  }

  public List<CustomAttribute> getCustomAttributes() {
    return customAttributes;
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

  public boolean isEmpty() {
    return received == null
        && planned == null
        && due == null
        && name == null
        && note == null
        && description == null
        && classificationSummary == null
        && workbasketSummary == null
        && businessProcessId == null
        && parentBusinessProcessId == null
        && primaryObjRef == null
        && manualPriority == null
        && isRead == null
        && secondaryObjectReferences == null
        && custom1 == null
        && custom2 == null
        && custom3 == null
        && custom4 == null
        && custom5 == null
        && custom6 == null
        && custom7 == null
        && custom8 == null
        && custom9 == null
        && custom10 == null
        && custom11 == null
        && custom12 == null
        && custom13 == null
        && custom14 == null
        && custom15 == null
        && custom16 == null
        && customInt1 == null
        && customInt2 == null
        && customInt3 == null
        && customInt4 == null
        && customInt5 == null
        && customInt6 == null
        && customInt7 == null
        && customInt8 == null
        && customAttributes == null
        && (callbackInfo == null || callbackInfo.isEmpty()); // should not be emptied during update
  }
}
