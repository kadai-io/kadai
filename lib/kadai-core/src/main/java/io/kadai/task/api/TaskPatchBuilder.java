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

package io.kadai.task.api;

import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class TaskPatchBuilder {

  private Instant received;
  private Instant planned;
  private Instant due;

  private String name;
  private String note;
  private String description;
  private ClassificationSummary classificationSummary;
  private WorkbasketSummary workbasketSummary;
  private String businessProcessId;
  private String parentBusinessProcessId;
  private ObjectReference primaryObjRef;
  private Integer manualPriority;
  private Boolean isRead;
  private List<ObjectReference> secondaryObjectReferences;

  // Custom string fields
  private String custom1;
  private String custom2;
  private String custom3;
  private String custom4;
  private String custom5;
  private String custom6;
  private String custom7;
  private String custom8;
  private String custom9;
  private String custom10;
  private String custom11;
  private String custom12;
  private String custom13;
  private String custom14;
  private String custom15;
  private String custom16;

  // Custom int fields
  private Integer customInt1;
  private Integer customInt2;
  private Integer customInt3;
  private Integer customInt4;
  private Integer customInt5;
  private Integer customInt6;
  private Integer customInt7;
  private Integer customInt8;

  private Map<String, String> customAttributes;
  private Map<String, String> callbackInfo;
  private CallbackState callbackState;

  // --- Builder methods for all fields ---
  public TaskPatchBuilder received(Instant received) {
    this.received = received;
    return this;
  }

  public TaskPatchBuilder planned(Instant planned) {
    this.planned = planned;
    return this;
  }

  public TaskPatchBuilder due(Instant due) {
    this.due = due;
    return this;
  }

  public TaskPatchBuilder name(String name) {
    this.name = name;
    return this;
  }

  public TaskPatchBuilder note(String note) {
    this.note = note;
    return this;
  }

  public TaskPatchBuilder description(String description) {
    this.description = description;
    return this;
  }

  public TaskPatchBuilder classificationSummary(ClassificationSummary classificationSummary) {
    this.classificationSummary = classificationSummary;
    return this;
  }

  public TaskPatchBuilder workbasketSummary(WorkbasketSummary workbasketSummary) {
    this.workbasketSummary = workbasketSummary;
    return this;
  }

  public TaskPatchBuilder businessProcessId(String businessProcessId) {
    this.businessProcessId = businessProcessId;
    return this;
  }

  public TaskPatchBuilder parentBusinessProcessId(String parentBusinessProcessId) {
    this.parentBusinessProcessId = parentBusinessProcessId;
    return this;
  }

  public TaskPatchBuilder primaryObjRef(ObjectReference primaryObjRef) {
    this.primaryObjRef = primaryObjRef;
    return this;
  }

  public TaskPatchBuilder manualPriority(Integer manualPriority) {
    this.manualPriority = manualPriority;
    return this;
  }

  public TaskPatchBuilder isRead(Boolean isRead) {
    this.isRead = isRead;
    return this;
  }

  public TaskPatchBuilder secondaryObjectReferences(
      List<ObjectReference> secondaryObjectReferences) {
    this.secondaryObjectReferences = secondaryObjectReferences;
    return this;
  }

  // Custom strings
  public TaskPatchBuilder custom1(String v) {
    this.custom1 = v;
    return this;
  }

  public TaskPatchBuilder custom2(String v) {
    this.custom2 = v;
    return this;
  }

  public TaskPatchBuilder custom3(String v) {
    this.custom3 = v;
    return this;
  }

  public TaskPatchBuilder custom4(String v) {
    this.custom4 = v;
    return this;
  }

  public TaskPatchBuilder custom5(String v) {
    this.custom5 = v;
    return this;
  }

  public TaskPatchBuilder custom6(String v) {
    this.custom6 = v;
    return this;
  }

  public TaskPatchBuilder custom7(String v) {
    this.custom7 = v;
    return this;
  }

  public TaskPatchBuilder custom8(String v) {
    this.custom8 = v;
    return this;
  }

  public TaskPatchBuilder custom9(String v) {
    this.custom9 = v;
    return this;
  }

  public TaskPatchBuilder custom10(String v) {
    this.custom10 = v;
    return this;
  }

  public TaskPatchBuilder custom11(String v) {
    this.custom11 = v;
    return this;
  }

  public TaskPatchBuilder custom12(String v) {
    this.custom12 = v;
    return this;
  }

  public TaskPatchBuilder custom13(String v) {
    this.custom13 = v;
    return this;
  }

  public TaskPatchBuilder custom14(String v) {
    this.custom14 = v;
    return this;
  }

  public TaskPatchBuilder custom15(String v) {
    this.custom15 = v;
    return this;
  }

  public TaskPatchBuilder custom16(String v) {
    this.custom16 = v;
    return this;
  }

  // Custom ints
  public TaskPatchBuilder customInt1(Integer v) {
    this.customInt1 = v;
    return this;
  }

  public TaskPatchBuilder customInt2(Integer v) {
    this.customInt2 = v;
    return this;
  }

  public TaskPatchBuilder customInt3(Integer v) {
    this.customInt3 = v;
    return this;
  }

  public TaskPatchBuilder customInt4(Integer v) {
    this.customInt4 = v;
    return this;
  }

  public TaskPatchBuilder customInt5(Integer v) {
    this.customInt5 = v;
    return this;
  }

  public TaskPatchBuilder customInt6(Integer v) {
    this.customInt6 = v;
    return this;
  }

  public TaskPatchBuilder customInt7(Integer v) {
    this.customInt7 = v;
    return this;
  }

  public TaskPatchBuilder customInt8(Integer v) {
    this.customInt8 = v;
    return this;
  }

  public TaskPatchBuilder customAttributes(Map<String, String> customAttributes) {
    this.customAttributes = customAttributes;
    return this;
  }

  public TaskPatchBuilder callbackInfo(Map<String, String> callbackInfo) {
    this.callbackInfo = callbackInfo;
    return this;
  }

  public TaskPatchBuilder callbackState(CallbackState callbackState) {
    this.callbackState = callbackState;
    return this;
  }

  // Build method
  public TaskPatch build() {
    return new TaskPatch(
        received,
        planned,
        due,
        name,
        note,
        description,
        classificationSummary,
        workbasketSummary,
        businessProcessId,
        parentBusinessProcessId,
        primaryObjRef,
        manualPriority,
        isRead,
        secondaryObjectReferences,
        custom1,
        custom2,
        custom3,
        custom4,
        custom5,
        custom6,
        custom7,
        custom8,
        custom9,
        custom10,
        custom11,
        custom12,
        custom13,
        custom14,
        custom15,
        custom16,
        customInt1,
        customInt2,
        customInt3,
        customInt4,
        customInt5,
        customInt6,
        customInt7,
        customInt8,
        customAttributes,
        callbackInfo);
  }
}
