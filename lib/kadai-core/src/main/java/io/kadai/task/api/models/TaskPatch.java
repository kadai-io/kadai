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

package io.kadai.task.api.models;

import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.task.api.CallbackState;
import io.kadai.task.api.TaskState;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface TaskPatch {

  /**
   * Returns the received TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the received Instant
   */
  Instant getReceived();

  /**
   * Returns the created TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the created Instant
   */
  Instant getCreated();

  /**
   * Returns the claimed TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the claimed Instant
   */
  Instant getClaimed();

  /**
   * Returns the modified TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the last modified Instant
   */
  Instant getModified();

  /**
   * Returns the planned TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the planned Instant
   */
  Instant getPlanned();

  /**
   * Returns the Due TimeStamp of the {@linkplain TaskPatch}.
   *
   * <p>This instant denotes the last point in the allowed work time has ended or in short it is
   * inclusive.
   *
   * @return the due Instant
   */
  Instant getDue();

  /**
   * Returns the completed TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the completed Instant
   */
  Instant getCompleted();

  /**
   * Returns the name of the {@linkplain TaskPatch}.
   *
   * @return name
   */
  String getName();

  /**
   * Returns the name of the creator of the {@linkplain TaskPatch}.
   *
   * @return creator
   */
  String getCreator();

  /**
   * Returns the note attached to the {@linkplain TaskPatch}.
   *
   * @return note
   */
  String getNote();

  /**
   * Returns the description of the {@linkplain TaskPatch}.
   *
   * @return description
   */
  String getDescription();

  /**
   * Returns the state of the {@linkplain TaskPatch}.
   *
   * @return state
   */
  TaskState getState();

  /**
   * Returns the {@linkplain ClassificationSummary} of the {@linkplain TaskPatch}.
   *
   * @return {@linkplain ClassificationSummary}
   */
  ClassificationSummary getClassificationSummary();

  /**
   * Returns the number of {@linkplain Task Tasks} that are grouped together with the {@linkplain
   * Task} by a {@linkplain io.kadai.task.api.TaskQuery}. It's only not NULL when using {@linkplain
   * io.kadai.task.api.TaskQuery#groupByPor()} or {@linkplain
   * io.kadai.task.api.TaskQuery#groupBySor(String)}.
   *
   * @return the number of {@linkplain Task Tasks} grouped together with the {@linkplain Task}
   */
  Integer getGroupByCount();

  /**
   * Returns the {@linkplain WorkbasketSummary} of the {@linkplain TaskPatch}.
   *
   * @return {@linkplain WorkbasketSummary}
   */
  WorkbasketSummary getWorkbasketSummary();

  /**
   * Returns the businessProcessId of the {@linkplain TaskPatch}.
   *
   * @return businessProcessId
   */
  String getBusinessProcessId();

  /**
   * Returns the parentBusinessProcessId of the {@linkplain TaskPatch}.
   *
   * @return parentBusinessProcessId
   */
  String getParentBusinessProcessId();

  /**
   * Returns the primary {@linkplain ObjectReference} of the {@linkplain TaskPatch}.
   *
   * @return the Tasks primary {@linkplain ObjectReference}
   */
  ObjectReference getPrimaryObjRef();

  /**
   * Gets the manualPriority of the {@linkplain TaskPatch}. If the value of manualPriority is zero
   * or greater, the priority is automatically set to manualPriority. In this case, all computations
   * of priority are disabled. If the value of manualPriority is negative, Tasks are not prioritized
   * manually.
   *
   * @return the manualPriority of the TaskPatch
   */
  Integer getManualPriority();

  /**
   * Returns the isRead flag of the {@linkplain TaskPatch}.
   *
   * @return the Tasks isRead flag
   */
  Boolean isRead();

  /**
   * Returns the {@linkplain ObjectReference secondaryObjectReferences} of the {@linkplain
   * TaskPatch}.
   *
   * @return {@linkplain ObjectReference secondaryObjectReferences}
   */
  List<ObjectReference> getSecondaryObjectReferences();

  /**
   * Gets the value of the user-defined custom field 1.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom1();

  /**
   * Gets the value of the user-defined custom field 2.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom2();

  /**
   * Gets the value of the user-defined custom field 3.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom3();

  /**
   * Gets the value of the user-defined custom field 4.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom4();

  /**
   * Gets the value of the user-defined custom field 5.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom5();

  /**
   * Gets the value of the user-defined custom field 6.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom6();

  /**
   * Gets the value of the user-defined custom field 7.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom7();

  /**
   * Gets the value of the user-defined custom field 8.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom8();

  /**
   * Gets the value of the user-defined custom field 9.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom9();

  /**
   * Gets the value of the user-defined custom field 10.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom10();

  /**
   * Gets the value of the user-defined custom field 11.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom11();

  /**
   * Gets the value of the user-defined custom field 12.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom12();

  /**
   * Gets the value of the user-defined custom field 13.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom13();

  /**
   * Gets the value of the user-defined custom field 14.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom14();

  /**
   * Gets the value of the user-defined custom field 15.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom15();

  /**
   * Gets the value of the user-defined custom field 16.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom16();

  /**
   * Gets the value of the user-defined integer custom field 1.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt1();

  /**
   * Gets the value of the user-defined integer custom field 2.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt2();

  /**
   * Gets the value of the user-defined integer custom field 3.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt3();

  /**
   * Gets the value of the user-defined integer custom field 4.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt4();

  /**
   * Gets the value of the user-defined integer custom field 5.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt5();

  /**
   * Gets the value of the user-defined integer custom field 6.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt6();

  /**
   * Gets the value of the user-defined integer custom field 7.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt7();

  /**
   * Gets the value of the user-defined integer custom field 8.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt8();

  /**
   * Returns a Map of customAttributes.
   *
   * @return customAttributes as Map
   */
  Map<String, String> getCustomAttributes();

  /**
   * Returns the callbackInfo.
   *
   * @return callbackInfo as Map
   */
  Map<String, String> getCallbackInfo();

  /**
   * Returns the callbackState.
   *
   * @return callbackState
   */
  CallbackState getCallbackState();
}
