/*
 * Copyright [2026] [envite consulting GmbH]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package acceptance.taskpostprocessing;

import static acceptance.taskpostprocessing.CreateTaskPostProcessingAccTest.TestCreateTaskPostProcessorProvider.SPI_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

import acceptance.taskpostprocessing.CreateTaskPostProcessingAccTest.TestCreateTaskPostProcessorProvider;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.spi.task.api.CreateTaskPostprocessor;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Task;
import io.kadai.testapi.DefaultTestEntities;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Acceptance test for "task post-processing" scenario. */
@KadaiIntegrationTest
@WithServiceProvider(
        serviceProviderInterface = CreateTaskPostprocessor.class,
        serviceProviders = TestCreateTaskPostProcessorProvider.class)
class CreateTaskPostProcessingAccTest {

  @KadaiInject TaskService taskService;

  WorkbasketSummary workbasketSummary;
  ClassificationSummary classificationSummary;

  @WithAccessId(user = "businessadmin")
  @BeforeAll
  void setup(ClassificationService classificationService, WorkbasketService workbasketService)
          throws Exception {
    classificationSummary =
            DefaultTestEntities.defaultTestClassification()
                    .buildAndStoreAsSummary(classificationService);

    workbasketSummary =
            DefaultTestEntities.defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
            .accessId("user-1-1")
            .workbasketId(workbasketSummary.getId())
            .permission(WorkbasketPermission.READ)
            .permission(WorkbasketPermission.OPEN)
            .permission(WorkbasketPermission.APPEND)
            .buildAndStore(workbasketService);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_ProcessTaskAfterCreation_When_CreateTaskPostProcessorEnabled() throws Exception {
    Task newTaskToCreate = taskService.newTask(workbasketSummary.getId());
    newTaskToCreate.setClassificationKey(classificationSummary.getKey());
    newTaskToCreate.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

    Task createdTask = taskService.createTask(newTaskToCreate);

    assertThat(createdTask.getCustomField(TaskCustomField.CUSTOM_2)).isEqualTo(SPI_VALUE);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_ReturnTaskWithPersistedId_When_PostProcessorReceivesTask() throws Exception {
    Task newTaskToCreate = taskService.newTask(workbasketSummary.getId());
    newTaskToCreate.setClassificationKey(classificationSummary.getKey());
    newTaskToCreate.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

    Task createdTask = taskService.createTask(newTaskToCreate);

    // The post-processor runs after persistCreatedTask(), so the task ID must already be set
    assertThat(createdTask.getId()).isNotNull().isNotEmpty();
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_ReturnTaskInReadyState_When_PostProcessorDoesNotChangeState() throws Exception {
    Task newTaskToCreate = taskService.newTask(workbasketSummary.getId());
    newTaskToCreate.setClassificationKey(classificationSummary.getKey());
    newTaskToCreate.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

    Task createdTask = taskService.createTask(newTaskToCreate);

    assertThat(createdTask.getState()).isEqualTo(TaskState.READY);
  }

  static class TestCreateTaskPostProcessorProvider implements CreateTaskPostprocessor {

    static final String SPI_VALUE = "postProcessedCustomField";

    @Override
    public Task processTaskAfterCreation(Task taskToProcess) {
      taskToProcess.setCustomField(TaskCustomField.CUSTOM_2, SPI_VALUE);
      return taskToProcess;
    }
  }
}
