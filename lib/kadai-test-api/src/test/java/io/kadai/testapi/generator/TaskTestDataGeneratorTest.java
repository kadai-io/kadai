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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kadai.KadaiConfiguration;
import io.kadai.classification.api.ClassificationQuery;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.Classification;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.classification.internal.models.ClassificationImpl;
import io.kadai.classification.internal.models.ClassificationSummaryImpl;
import io.kadai.common.api.KadaiRole;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Task;
import io.kadai.workbasket.api.WorkbasketQuery;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.WorkbasketType;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.internal.models.WorkbasketImpl;
import io.kadai.workbasket.internal.models.WorkbasketSummaryImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TaskTestDataGeneratorTest {

  @Test
  void should_GenerateRequestedNumberOfTasksInConfiguredBatchSizes() {
    TaskTestDataGenerator generator =
        new TaskTestDataGenerator(baseEnvironment(), fixedClock(), 17L);

    List<List<Task>> batches = new ArrayList<>();
    GenerationSummary summary = generator.generate(11, 4, batches::add);

    assertThat(summary.requestedTaskCount()).isEqualTo(11);
    assertThat(summary.processedTaskCount()).isEqualTo(11);
    assertThat(summary.batchCount()).isEqualTo(3);
    assertThat(batches).hasSize(3);
    assertThat(batches.get(0)).hasSize(4);
    assertThat(batches.get(1)).hasSize(4);
    assertThat(batches.get(2)).hasSize(3);
    assertThat(batches.stream().flatMap(List::stream).map(Task::getExternalId))
        .doesNotHaveDuplicates();
  }

  @Test
  void should_GenerateRealisticAndCompatibleTaskData() {
    TaskGenerationEnvironment environment = baseEnvironment();
    TaskTestDataGenerator generator = new TaskTestDataGenerator(environment, fixedClock(), 99L);

    List<Task> tasks = generator.stream(250).toList();

    assertThat(tasks).hasSize(250);
    assertThat(tasks)
        .allSatisfy(
            task -> {
              assertThat(environment.domains()).contains(task.getDomain());
              assertThat(task.getWorkbasketSummary()).isNotNull();
              assertThat(task.getClassificationSummary()).isNotNull();
              assertThat(task.getWorkbasketSummary().getDomain()).isEqualTo(task.getDomain());
              assertThat(task.getClassificationSummary().getDomain()).isEqualTo(task.getDomain());
              assertThat(environment.taskCategories())
                  .anyMatch(
                      category ->
                          category.equalsIgnoreCase(task.getClassificationSummary().getCategory()));
              assertThat(task.getPrimaryObjRef()).isNotNull();
              assertThat(task.getPrimaryObjRef().getCompany()).isNotBlank();
              assertThat(task.getPrimaryObjRef().getType()).isNotBlank();
              assertThat(task.getPrimaryObjRef().getValue()).matches("\\d{8}");
              assertThat(task.getCreated()).isNotNull();
              assertThat(task.getModified()).isNotNull();
              assertThat(task.getModified()).isAfterOrEqualTo(task.getCreated());
              assertThat(task.getPlanned()).isNotNull();
              assertThat(task.getDue()).isAfter(task.getPlanned());
              assertThat(task.getExternalId()).startsWith("ETI:");
              assertThat(task.getBusinessProcessId()).startsWith("BPI:");
              assertThat(task.getParentBusinessProcessId()).startsWith("PBPI:");
              assertThat(task.getCustomField(io.kadai.task.api.TaskCustomField.CUSTOM_1))
                  .isNotBlank();
              assertThat(task.getCustomField(io.kadai.task.api.TaskCustomField.CUSTOM_2))
                  .isNotBlank();
              assertThat(task.getCustomField(io.kadai.task.api.TaskCustomField.CUSTOM_14))
                  .isEqualTo("abc");
            });

    assertThat(tasks.stream().map(Task::getState).collect(Collectors.toSet()))
        .contains(TaskState.READY, TaskState.CLAIMED, TaskState.COMPLETED);

    assertThat(tasks)
        .filteredOn(task -> task.getState() == TaskState.READY)
        .allSatisfy(task -> assertThat(task.getOwner()).isNull());

    assertThat(tasks)
        .filteredOn(
            task -> task.getState().isClaimedState() || task.getState() == TaskState.COMPLETED)
        .allSatisfy(task -> assertThat(task.getOwner()).isNotBlank());

    assertThat(tasks.stream().map(Task::getDomain).collect(Collectors.toSet()))
        .containsExactlyInAnyOrder("DOMAIN_A", "DOMAIN_B");
  }

  @Test
  void should_ResolveExistingCompatibleEnvironmentWithoutCreatingAdditionalSupportData() {
    ClassificationService classificationService = Mockito.mock(ClassificationService.class);
    ClassificationQuery classificationQuery = Mockito.mock(ClassificationQuery.class);
    when(classificationService.createClassificationQuery()).thenReturn(classificationQuery);
    when(classificationQuery.typeIn(any(String[].class))).thenReturn(classificationQuery);
    when(classificationQuery.domainIn(any(String[].class))).thenReturn(classificationQuery);
    when(classificationQuery.list())
        .thenReturn(
            List.of(
                classificationSummary("CLI:A1", "A_EXT", "DOMAIN_A", "EXTERNAL", 1, "P1D"),
                classificationSummary("CLI:A2", "A_EXT_2", "DOMAIN_A", "EXTERNAL", 2, "P2D"),
                classificationSummary("CLI:A3", "A_MAN", "DOMAIN_A", "MANUAL", 2, "P2D"),
                classificationSummary("CLI:A4", "A_MAN_2", "DOMAIN_A", "MANUAL", 3, "P3D"),
                classificationSummary("CLI:B1", "B_EXT", "DOMAIN_B", "EXTERNAL", 1, "P1D"),
                classificationSummary("CLI:B2", "B_EXT_2", "DOMAIN_B", "EXTERNAL", 2, "P2D"),
                classificationSummary("CLI:B3", "B_MAN", "DOMAIN_B", "MANUAL", 2, "P2D"),
                classificationSummary("CLI:B4", "B_MAN_2", "DOMAIN_B", "MANUAL", 3, "P3D")));

    WorkbasketService workbasketService = Mockito.mock(WorkbasketService.class);
    WorkbasketQuery workbasketQuery = Mockito.mock(WorkbasketQuery.class);
    when(workbasketService.createWorkbasketQuery()).thenReturn(workbasketQuery);
    when(workbasketQuery.domainIn(any(String[].class))).thenReturn(workbasketQuery);
    when(workbasketQuery.list())
        .thenReturn(
            List.of(
                workbasketSummary(
                    "WBI:A1", "A_USER", "DOMAIN_A", WorkbasketType.PERSONAL, "user-1-1"),
                workbasketSummary(
                    "WBI:A1B", "A_USER_2", "DOMAIN_A", WorkbasketType.PERSONAL, "user-1-2"),
                workbasketSummary(
                    "WBI:A2", "A_GROUP", "DOMAIN_A", WorkbasketType.GROUP, "teamlead-1"),
                workbasketSummary("WBI:A3", "A_TOPIC", "DOMAIN_A", WorkbasketType.TOPIC, ""),
                workbasketSummary(
                    "WBI:B1", "B_USER", "DOMAIN_B", WorkbasketType.PERSONAL, "user-b-1"),
                workbasketSummary(
                    "WBI:B1B", "B_USER_2", "DOMAIN_B", WorkbasketType.PERSONAL, "user-b-2"),
                workbasketSummary(
                    "WBI:B2", "B_GROUP", "DOMAIN_B", WorkbasketType.GROUP, "teamlead-2"),
                workbasketSummary("WBI:B3", "B_TOPIC", "DOMAIN_B", WorkbasketType.TOPIC, "")));

    TaskGenerationEnvironment environment =
        TaskTestDataGenerator.resolveEnvironment(
            mockConfiguration(), classificationService, workbasketService);

    assertThat(environment.domains()).containsExactly("DOMAIN_A", "DOMAIN_B");
    assertThat(environment.taskClassificationType()).isEqualTo("TASK");
    assertThat(environment.taskCategories()).containsExactly("EXTERNAL", "MANUAL");
    assertThat(environment.classifications()).hasSize(8);
    assertThat(environment.workbaskets()).hasSize(8);
    assertThat(environment.candidateUsers())
        .contains("admin", "user-1-1", "teamlead-1", "user-b-1", "teamlead-2")
        .doesNotContain("cn=ksc-users,cn=groups,OU=Test,O=KADAI");

    verify(classificationService, never()).newClassification(anyString(), anyString(), anyString());
    verify(workbasketService, never()).newWorkbasket(anyString(), anyString());
  }

  @Test
  void should_BootstrapCompatibleEnvironmentWhenNoSupportDataExists() throws Exception {
    ClassificationService classificationService = Mockito.mock(ClassificationService.class);
    ClassificationQuery classificationQuery = Mockito.mock(ClassificationQuery.class);
    when(classificationService.createClassificationQuery()).thenReturn(classificationQuery);
    when(classificationQuery.typeIn(any(String[].class))).thenReturn(classificationQuery);
    when(classificationQuery.domainIn(any(String[].class))).thenReturn(classificationQuery);
    when(classificationQuery.list()).thenReturn(List.of());

    AtomicInteger classificationCounter = new AtomicInteger();
    when(classificationService.newClassification(anyString(), anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              ClassificationImpl classification = new ClassificationImpl();
              classification.setKey(invocation.getArgument(0));
              classification.setDomain(invocation.getArgument(1));
              classification.setType(invocation.getArgument(2));
              return classification;
            });
    when(classificationService.createClassification(any(Classification.class)))
        .thenAnswer(
            invocation -> {
              Classification classification = invocation.getArgument(0);
              ((ClassificationImpl) classification)
                  .setId("CLI:GEN:" + classificationCounter.incrementAndGet());
              return classification;
            });

    WorkbasketService workbasketService = Mockito.mock(WorkbasketService.class);
    WorkbasketQuery workbasketQuery = Mockito.mock(WorkbasketQuery.class);
    when(workbasketService.createWorkbasketQuery()).thenReturn(workbasketQuery);
    when(workbasketQuery.domainIn(any(String[].class))).thenReturn(workbasketQuery);
    when(workbasketQuery.list()).thenReturn(List.of());

    AtomicInteger workbasketCounter = new AtomicInteger();
    when(workbasketService.newWorkbasket(anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              WorkbasketImpl workbasket = new WorkbasketImpl();
              workbasket.setKey(invocation.getArgument(0));
              workbasket.setDomain(invocation.getArgument(1));
              return workbasket;
            });
    when(workbasketService.createWorkbasket(any(Workbasket.class)))
        .thenAnswer(
            invocation -> {
              Workbasket workbasket = invocation.getArgument(0);
              ((WorkbasketImpl) workbasket).setId("WBI:GEN:" + workbasketCounter.incrementAndGet());
              return workbasket;
            });

    TaskGenerationEnvironment environment =
        TaskTestDataGenerator.resolveEnvironment(
            mockConfiguration(), classificationService, workbasketService);

    assertThat(environment.classifications()).hasSize(8);
    assertThat(environment.workbaskets()).hasSize(8);
    assertThat(environment.candidateUsers()).contains("admin", "user-1-1", "user-b-1");
    verify(classificationService, Mockito.times(8)).createClassification(any());
    verify(workbasketService, Mockito.times(8)).createWorkbasket(any());
  }

  @Test
  void should_RejectInvalidArgumentsAndInvalidEnvironments() {
    TaskTestDataGenerator generator =
        new TaskTestDataGenerator(baseEnvironment(), fixedClock(), 5L);

    assertThatThrownBy(() -> generator.stream(-1)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> generator.generate(1, 0, tasks -> {}))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> generator.generate(1, 1, null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () ->
                new TaskTestDataGenerator(
                    new TaskGenerationEnvironment(
                        List.of("DOMAIN_A"),
                        List.of("admin"),
                        "TASK",
                        List.of("EXTERNAL"),
                        List.of(),
                        List.of())))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static TaskGenerationEnvironment baseEnvironment() {
    return new TaskGenerationEnvironment(
        List.of("DOMAIN_A", "DOMAIN_B"),
        List.of("user-1-1", "user-1-2", "user-b-1", "teamlead-1"),
        "TASK",
        List.of("EXTERNAL", "MANUAL"),
        List.of(
            classificationSummary("CLI:1", "L1050", "DOMAIN_A", "EXTERNAL", 1, "P1D"),
            classificationSummary("CLI:2", "T2000", "DOMAIN_A", "MANUAL", 3, "P2D"),
            classificationSummary("CLI:3", "L1060", "DOMAIN_B", "EXTERNAL", 2, "P1D"),
            classificationSummary("CLI:4", "A12", "DOMAIN_B", "MANUAL", 4, "P3D")),
        List.of(
            workbasketSummary("WBI:1", "USER-1-1", "DOMAIN_A", WorkbasketType.PERSONAL, "user-1-1"),
            workbasketSummary("WBI:2", "GPK_KSC", "DOMAIN_A", WorkbasketType.GROUP, "teamlead-1"),
            workbasketSummary("WBI:3", "TPK_VIP", "DOMAIN_A", WorkbasketType.TOPIC, ""),
            workbasketSummary("WBI:4", "USER-B-1", "DOMAIN_B", WorkbasketType.PERSONAL, "user-b-1"),
            workbasketSummary("WBI:5", "GPK_B_KSC", "DOMAIN_B", WorkbasketType.GROUP, "teamlead-1"),
            workbasketSummary("WBI:6", "TPK_B", "DOMAIN_B", WorkbasketType.TOPIC, "")));
  }

  private static KadaiConfiguration mockConfiguration() {
    KadaiConfiguration configuration = Mockito.mock(KadaiConfiguration.class);
    when(configuration.getDomains()).thenReturn(List.of("DOMAIN_A", "DOMAIN_B"));
    when(configuration.getClassificationTypes()).thenReturn(List.of("TASK", "DOCUMENT"));
    when(configuration.getClassificationCategoriesByType("TASK"))
        .thenReturn(List.of("EXTERNAL", "MANUAL"));
    when(configuration.getAllClassificationCategories()).thenReturn(List.of("EXTERNAL", "MANUAL"));
    when(configuration.getRoleMap())
        .thenReturn(
            Map.of(
                KadaiRole.ADMIN,
                Set.of("admin"),
                KadaiRole.USER,
                Set.of("user-1-1", "user-b-1", "cn=ksc-users,cn=groups,OU=Test,O=KADAI")));
    return configuration;
  }

  private static ClassificationSummary classificationSummary(
      String id, String key, String domain, String category, int priority, String serviceLevel) {
    ClassificationSummaryImpl classification = new ClassificationSummaryImpl();
    classification.setId(id);
    classification.setKey(key);
    classification.setDomain(domain);
    classification.setType("TASK");
    classification.setCategory(category);
    classification.setPriority(priority);
    classification.setServiceLevel(serviceLevel);
    classification.setName(key + "-name");
    return classification;
  }

  private static WorkbasketSummary workbasketSummary(
      String id, String key, String domain, WorkbasketType type, String owner) {
    WorkbasketSummaryImpl workbasket = new WorkbasketSummaryImpl();
    workbasket.setId(id);
    workbasket.setKey(key);
    workbasket.setDomain(domain);
    workbasket.setType(type);
    workbasket.setOwner(owner);
    workbasket.setName(key + "-name");
    workbasket.setDescription(key + "-description");
    return workbasket;
  }

  private static Clock fixedClock() {
    return Clock.fixed(Instant.parse("2026-05-18T10:15:30Z"), ZoneOffset.UTC);
  }
}
