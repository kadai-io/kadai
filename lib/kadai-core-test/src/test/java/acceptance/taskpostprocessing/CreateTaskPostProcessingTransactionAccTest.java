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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.taskpostprocessing.CreateTaskPostProcessingTransactionAccTest.ThrowingCreateTaskPostProcessorProvider;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.spi.task.api.CreateTaskPostprocessor;
import io.kadai.task.api.TaskService;
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

/**
 * Acceptance test documenting the transactional boundary of {@code
 * TaskServiceImpl#createTask()}.
 *
 * <p>{@code postprocessTaskCreation()} runs inside the DB transaction, consistent with other SPI
 * hooks (e.g. {@code afterRequestReview}, {@code afterRequestChanges}). A failure in
 * post-processing therefore rolls back the entire transaction, including the persisted task, and
 * the exception is propagated to the caller.
 */
@KadaiIntegrationTest
@WithServiceProvider(
        serviceProviderInterface = CreateTaskPostprocessor.class,
        serviceProviders = ThrowingCreateTaskPostProcessorProvider.class)
class CreateTaskPostProcessingTransactionAccTest {

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
  void should_ThrowException_When_PostProcessorFails() {
    Task newTaskToCreate = taskService.newTask(workbasketSummary.getId());
    newTaskToCreate.setClassificationKey(classificationSummary.getKey());
    newTaskToCreate.setPrimaryObjRef(DefaultTestEntities.defaultTestObjectReference().build());

    assertThatThrownBy(() -> taskService.createTask(newTaskToCreate))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining(ThrowingCreateTaskPostProcessorProvider.ERROR_MESSAGE);
  }

  static class ThrowingCreateTaskPostProcessorProvider implements CreateTaskPostprocessor {

    static final String ERROR_MESSAGE = "Simulated post-processor failure";

    @Override
    public Task processTaskAfterCreation(Task taskToProcess) {
      throw new RuntimeException(ERROR_MESSAGE);
    }
  }
}
