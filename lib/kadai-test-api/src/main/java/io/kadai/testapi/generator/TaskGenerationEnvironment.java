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

package io.kadai.testapi.generator;

import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.util.List;

/** Immutable description of the support data the generator can use. */
public record TaskGenerationEnvironment(
    List<String> domains,
    List<String> candidateUsers,
    String taskClassificationType,
    List<String> taskCategories,
    List<ClassificationSummary> classifications,
    List<WorkbasketSummary> workbaskets) {}
