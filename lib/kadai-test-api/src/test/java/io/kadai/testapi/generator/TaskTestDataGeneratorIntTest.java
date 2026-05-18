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

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Task;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.security.WithAccessId;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

@KadaiIntegrationTest
class TaskTestDataGeneratorIntTest {

  @KadaiInject KadaiEngine kadaiEngine;
  @KadaiInject TaskService taskService;

  @WithAccessId(user = "admin")
  @Test
  void should_GenerateConfigurationCompatibleTasksFromLiveEnvironment() {
    TaskTestDataGenerator generator = TaskTestDataGenerator.from(kadaiEngine);
    KadaiConfiguration configuration = kadaiEngine.getConfiguration();
    List<String> configuredDomains =
        configuration.getDomains().stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(domain -> !domain.isEmpty())
            .toList();

    List<Task> tasks = generator.stream(25).toList();

    assertThat(tasks).hasSize(25);
    assertThat(generator.getEnvironment().domains()).containsExactlyElementsOf(configuredDomains);
    assertThat(tasks)
        .allSatisfy(
            task -> {
              assertThat(configuredDomains).contains(task.getDomain());
              assertThat(task.getClassificationSummary()).isNotNull();
              assertThat(task.getWorkbasketSummary()).isNotNull();
              assertThat(task.getPrimaryObjRef()).isNotNull();
              assertThat(task.getWorkbasketSummary().getDomain()).isEqualTo(task.getDomain());
              assertThat(task.getClassificationSummary().getDomain()).isEqualTo(task.getDomain());
              assertThat(configuration.getClassificationCategoriesByType("TASK"))
                  .anyMatch(
                      category ->
                          category.equalsIgnoreCase(task.getClassificationSummary().getCategory()));
            });
  }

  @WithAccessId(user = "admin")
  @Test
  void should_PersistRequestedNumberOfTasksInBatches() {
    long before = taskService.createTaskQuery().count();
    TaskTestDataGenerator generator = TaskTestDataGenerator.from(kadaiEngine);

    GenerationSummary summary = generator.persist(15, 4);
    long after = taskService.createTaskQuery().count();

    assertThat(summary.requestedTaskCount()).isEqualTo(15);
    assertThat(summary.processedTaskCount()).isEqualTo(15);
    assertThat(summary.batchCount()).isEqualTo(4);
    assertThat(after - before).isEqualTo(15);
  }
}
