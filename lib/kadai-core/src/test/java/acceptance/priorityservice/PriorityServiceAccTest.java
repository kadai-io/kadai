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

package acceptance.priorityservice;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.Classification;
import io.kadai.common.api.ScheduledJob;
import io.kadai.common.internal.util.Pair;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.Task;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingConsumer;

/** Acceptance test for all priority computation scenarios. */
@Disabled("Until we enable the use of Test-SPI's in only specific tests")
@ExtendWith(JaasExtension.class)
class PriorityServiceAccTest extends AbstractAccTest {

  @WithAccessId(user = "user-1-1")
  @TestFactory
  Stream<DynamicTest> should_SetThePriorityAccordingToTestProvider_When_CreatingTask() {
    List<Pair<String, Integer>> testCases = List.of(Pair.of("false", 1), Pair.of("true", 10));

    ThrowingConsumer<Pair<String, Integer>> test =
        x -> {
          Task task = taskService.newTask("USER-1-1", "DOMAIN_A");
          task.setCustomField(TaskCustomField.CUSTOM_6, x.getLeft());
          task.setClassificationKey("T2100");
          ObjectReference objectReference =
              createObjectReference("COMPANY_A", "SYSTEM_A", "INSTANCE_A", "VNR", "1234567");
          task.setPrimaryObjRef(objectReference);

          Task createdTask = taskService.createTask(task);
          assertThat(createdTask.getPriority()).isEqualTo(x.getRight());
        };

    return DynamicTest.stream(testCases.iterator(), x -> "entry in custom6: " + x.getLeft(), test);
  }

  @WithAccessId(user = "user-1-1")
  @TestFactory
  Stream<DynamicTest> should_SetThePriorityAccordingToTestProvider_When_UpdatingTask()
      throws Exception {
    List<Pair<String, Integer>> testCases = List.of(Pair.of("false", 1), Pair.of("true", 10));
    Task task = taskService.getTask("TKI:000000000000000000000000000000000000");
    int daysSinceCreated =
        Math.toIntExact(
            TimeUnit.DAYS.convert(
                Date.from(Instant.now()).getTime() - Date.from(task.getCreated()).getTime(),
                TimeUnit.MILLISECONDS));

    ThrowingConsumer<Pair<String, Integer>> test =
        x -> {
          task.setCustomField(TaskCustomField.CUSTOM_6, x.getLeft());

          Task updatedTask = taskService.updateTask(task);
          assertThat(updatedTask.getPriority()).isEqualTo(daysSinceCreated * x.getRight());
        };

    return DynamicTest.stream(testCases.iterator(), x -> "entry in custom6: " + x.getLeft(), test);
  }

  @WithAccessId(user = "admin")
  @Test
  void should_NotCreateClassificationChangedJob_When_PriorityProviderExisting() throws Exception {
    ClassificationService classificationService = kadaiEngine.getClassificationService();
    Classification classification =
        classificationService.getClassification("CLI:000000000000000000000000000000000001");
    classification.setPriority(10);

    classificationService.updateClassification(classification);
    List<ScheduledJob> jobsToRun = getJobMapper(kadaiEngine).findJobsToRun(Instant.now());
    assertThat(jobsToRun).isEmpty();

    classification.setServiceLevel("P4D");
    classificationService.updateClassification(classification);
    jobsToRun = getJobMapper(kadaiEngine).findJobsToRun(Instant.now());
    assertThat(jobsToRun).isEmpty();
  }
}
