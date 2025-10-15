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

package io.kadai.task.api;

import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Record representing a patch for a Task. All fields are nullable to allow partial updates. */
public record TaskPatch(
    Instant received,
    Instant planned,
    Instant due,
    String name,
    String note,
    String description,
    ClassificationSummary classificationSummary,
    WorkbasketSummary workbasketSummary,
    String businessProcessId,
    String parentBusinessProcessId,
    ObjectReference primaryObjRef,
    Integer manualPriority,
    Boolean isRead,
    List<ObjectReference> secondaryObjectReferences,

    // Custom fields
    String custom1,
    String custom2,
    String custom3,
    String custom4,
    String custom5,
    String custom6,
    String custom7,
    String custom8,
    String custom9,
    String custom10,
    String custom11,
    String custom12,
    String custom13,
    String custom14,
    String custom15,
    String custom16,
    Integer customInt1,
    Integer customInt2,
    Integer customInt3,
    Integer customInt4,
    Integer customInt5,
    Integer customInt6,
    Integer customInt7,
    Integer customInt8,
    Map<String, String> customAttributes,
    Map<String, String> callbackInfo) {}
