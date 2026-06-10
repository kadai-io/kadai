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
import io.kadai.workbasket.api.WorkbasketType;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.util.List;
import java.util.Map;

record DomainFixtures(
    List<ClassificationSummary> classifications,
    List<WorkbasketSummary> workbaskets,
    Map<WorkbasketType, List<WorkbasketSummary>> workbasketsByType) {}
