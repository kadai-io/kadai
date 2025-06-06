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

package acceptance.task.distribute;

import static io.kadai.testapi.DefaultTestEntities.defaultTestClassification;
import static io.kadai.testapi.DefaultTestEntities.defaultTestObjectReference;
import static io.kadai.testapi.DefaultTestEntities.defaultTestWorkbasket;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import acceptance.task.distribute.DistributeTaskAccTest.TaskDistributionProviderForTest;
import acceptance.task.distribute.DistributeTaskAccTest.TaskDistributionProviderForTest2;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.spi.task.api.TaskDistributionProvider;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.TaskSummary;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.WithServiceProvider;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketAccessItemAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@KadaiIntegrationTest
@WithServiceProvider(
    serviceProviderInterface = TaskDistributionProvider.class,
    serviceProviders = {
      TaskDistributionProviderForTest.class,
      TaskDistributionProviderForTest2.class
    })
class DistributeTaskAccTest {
  @KadaiInject TaskService taskService;
  @KadaiInject WorkbasketService workbasketService;
  @KadaiInject ClassificationService classificationService;

  ClassificationSummary classificationSummary;
  ObjectReference objectReference;

  WorkbasketSummary workbasketSummary1;
  WorkbasketSummary workbasketSummary2;
  WorkbasketSummary workbasketSummary3;
  WorkbasketSummary workbasketSummary4;
  WorkbasketSummary workbasketSummary5;
  WorkbasketSummary workbasketSummary6;
  List<TaskSummary> taskSummaries;
  TaskSummary taskSummaryForWb5;
  TaskSummary taskSummaryForWb6;

  @WithAccessId(user = "admin")
  @BeforeAll
  void setup() throws Exception {

    classificationSummary =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);

    objectReference = defaultTestObjectReference().build();

    workbasketSummary1 = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    workbasketSummary2 = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    workbasketSummary3 = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    workbasketSummary4 = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    workbasketSummary5 = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);
    workbasketSummary6 = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

    workbasketService.setDistributionTargets(
        workbasketSummary1.getId(),
        List.of(
            workbasketSummary2.getId(), workbasketSummary3.getId(), workbasketSummary4.getId()));
    workbasketService.setDistributionTargets(
        workbasketSummary5.getId(),
        List.of(
            workbasketSummary2.getId(), workbasketSummary3.getId(), workbasketSummary4.getId()));

    taskSummaries = new ArrayList<>();
    for (int i = 1; i <= 6; i++) {
      taskSummaries.add(
          new TaskBuilder()
              .classificationSummary(classificationSummary)
              .workbasketSummary(workbasketSummary1)
              .primaryObjRef(objectReference)
              .buildAndStoreAsSummary(taskService));
    }

    taskSummaryForWb5 =
        new TaskBuilder()
            .classificationSummary(classificationSummary)
            .workbasketSummary(workbasketSummary5)
            .primaryObjRef(objectReference)
            .buildAndStoreAsSummary(taskService);

    taskSummaryForWb6 =
        new TaskBuilder()
            .classificationSummary(classificationSummary)
            .workbasketSummary(workbasketSummary6)
            .primaryObjRef(objectReference)
            .buildAndStoreAsSummary(taskService);

    createAccessItemForUser(workbasketSummary1.getId());
    createAccessItemForUser(workbasketSummary2.getId());
    createAccessItemForUser(workbasketSummary3.getId());
    createAccessItemForUser(workbasketSummary4.getId());
    createAccessItemForUser(workbasketSummary5.getId());
    createAccessItemForUser(workbasketSummary6.getId());
  }

  @WithAccessId(user = "admin")
  @AfterEach
  void cleanWorkbaskets() throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {
    transferAllTasksBackToWorkbasket1();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_DistributeTasksCorrectly_When_WorkbasketIdOnly()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {

    taskService.distribute(workbasketSummary1.getId());

    List<TaskSummary> tasksInWb1 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary1.getId()).list();
    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();
    List<TaskSummary> tasksInWb3 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary3.getId()).list();
    List<TaskSummary> tasksInWb4 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary4.getId()).list();

    assertThat(tasksInWb1).isEmpty();
    assertThat(tasksInWb2).hasSize(2);
    assertThat(tasksInWb3).hasSize(2);
    assertThat(tasksInWb4).hasSize(2);
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_DistributeTasksCorrectly_When_TaskListOnly()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {
    String sourceWorkbasketId = workbasketSummary1.getId();
    List<String> taskIds = taskSummaries.subList(0, 3).stream().map(TaskSummary::getId).toList();

    taskService.distribute(sourceWorkbasketId, taskIds);

    List<TaskSummary> tasksInWb1 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary1.getId()).list();
    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();
    List<TaskSummary> tasksInWb3 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary3.getId()).list();
    List<TaskSummary> tasksInWb4 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary4.getId()).list();

    assertThat(tasksInWb1).hasSize(3);
    assertThat(tasksInWb2).hasSize(1);
    assertThat(tasksInWb3).hasSize(1);
    assertThat(tasksInWb4).hasSize(1);
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_DistributeTasksCorrectly_When_SourceWorkbasketAndDestinationWorkbaskets()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {
    String sourceWorkbasketId = workbasketSummary1.getId();
    List<String> destinationWorkbasketIds =
        List.of(workbasketSummary2.getId(), workbasketSummary3.getId());

    taskService.distributeWithDestinations(sourceWorkbasketId, destinationWorkbasketIds);

    List<TaskSummary> tasksInWb1 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary1.getId()).list();
    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();
    List<TaskSummary> tasksInWb3 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary3.getId()).list();
    List<TaskSummary> tasksInWb4 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary4.getId()).list();

    assertThat(tasksInWb1).isEmpty();
    assertThat(tasksInWb2).hasSize(3);
    assertThat(tasksInWb3).hasSize(3);
    assertThat(tasksInWb4).isEmpty();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_DistributeTasksCorrectly_When_TaskListAndDestinationWorkbaskets()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {
    String sourceWorkbasketId = workbasketSummary1.getId();
    List<String> taskIds = taskSummaries.subList(0, 2).stream().map(TaskSummary::getId).toList();
    List<String> destinationWorkbasketIds =
        List.of(workbasketSummary2.getId(), workbasketSummary3.getId());

    taskService.distributeWithDestinations(sourceWorkbasketId, taskIds, destinationWorkbasketIds);

    List<TaskSummary> tasksInWb1 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary1.getId()).list();
    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();
    List<TaskSummary> tasksInWb3 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary3.getId()).list();
    List<TaskSummary> tasksInWb4 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary4.getId()).list();

    assertThat(tasksInWb1).hasSize(4);
    assertThat(tasksInWb2).hasSize(1);
    assertThat(tasksInWb3).hasSize(1);
    assertThat(tasksInWb4).isEmpty();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_DistributeTasksCorrectly_When_SourceWorkbasketAndDestinationWorkbasketsAndStrategy()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {
    String sourceWorkbasketId = workbasketSummary1.getId();
    List<String> destinationWorkbasketIds =
        List.of(workbasketSummary2.getId(), workbasketSummary3.getId());

    taskService.distribute(
        sourceWorkbasketId, destinationWorkbasketIds, "TaskDistributionProviderForTest", null);

    List<TaskSummary> tasksInWb1 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary1.getId()).list();
    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();
    List<TaskSummary> tasksInWb3 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary3.getId()).list();
    List<TaskSummary> tasksInWb4 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary4.getId()).list();

    assertThat(tasksInWb1).hasSize(1);
    assertThat(tasksInWb2).hasSize(3);
    assertThat(tasksInWb3).hasSize(2);
    assertThat(tasksInWb4).isEmpty();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_DistributeTasksCorrectly_When_TaskListAndDestinationWorkbasketsAndStrategy()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {
    String sourceWorkbasketId = workbasketSummary1.getId();
    List<String> taskIds = taskSummaries.stream().map(TaskSummary::getId).toList();
    List<String> destinationWorkbasketIds =
        List.of(workbasketSummary2.getId(), workbasketSummary3.getId());

    taskService.distribute(
        sourceWorkbasketId,
        taskIds,
        destinationWorkbasketIds,
        "TaskDistributionProviderForTest",
        null);

    List<TaskSummary> tasksInWb1 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary1.getId()).list();
    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();
    List<TaskSummary> tasksInWb3 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary3.getId()).list();
    List<TaskSummary> tasksInWb4 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary4.getId()).list();

    assertThat(tasksInWb1).hasSize(1);

    List<Integer> actualDistribution =
        List.of(tasksInWb2.size(), tasksInWb3.size(), tasksInWb4.size());
    List<Integer> expectedValues = List.of(0, 2, 3);
    assertThat(actualDistribution).containsAll(expectedValues);
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_DistributeTasksCorrectly_When_TaskListAndAndStrategy()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {
    String sourceWorkbasketId = workbasketSummary1.getId();
    List<String> taskIds = taskSummaries.stream().map(TaskSummary::getId).toList();

    taskService.distributeWithStrategy(
        sourceWorkbasketId, taskIds, "TaskDistributionProviderForTest", null);

    List<TaskSummary> tasksInWb1 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary1.getId()).list();
    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();
    List<TaskSummary> tasksInWb3 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary3.getId()).list();
    List<TaskSummary> tasksInWb4 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary4.getId()).list();

    List<Integer> actualDistribution =
        List.of(tasksInWb2.size(), tasksInWb3.size(), tasksInWb4.size());
    List<Integer> expectedValues = List.of(1, 2, 3);
    assertThat(actualDistribution).containsAll(expectedValues);
    assertThat(tasksInWb1).isEmpty();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_DistributeTasksCorrectly_When_SourceWorkbasketAndAndStrategy()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {

    taskService.distributeWithStrategy(
        workbasketSummary1.getId(), "TaskDistributionProviderForTest", null);

    List<TaskSummary> tasksInWb1 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary1.getId()).list();
    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();
    List<TaskSummary> tasksInWb3 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary3.getId()).list();
    List<TaskSummary> tasksInWb4 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary4.getId()).list();

    List<Integer> actualDistribution =
        List.of(tasksInWb2.size(), tasksInWb3.size(), tasksInWb4.size());

    List<Integer> expectedValues = List.of(1, 2, 3);

    assertThat(actualDistribution).containsAll(expectedValues);
    assertThat(tasksInWb1).isEmpty();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_ResetOwner_When_TasksDistributed()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {

    taskService.distribute(workbasketSummary1.getId());

    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();

    assertThat(tasksInWb2.get(0).getOwner()).isNull();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_NotSetTransferFlag_When_TasksDistributed()
      throws NotAuthorizedOnWorkbasketException,
          WorkbasketNotFoundException,
          InvalidTaskStateException,
          TaskNotFoundException {

    taskService.distributeWithDestinations(
        workbasketSummary1.getId(), List.of(workbasketSummary2.getId()));

    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();

    assertThat(tasksInWb2.get(0).isTransferred()).isTrue();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_ThrowInvalidArgumentException_When_TasksFromDifferentSourceWorkbaskets() {
    String sourceWorkbasketId = workbasketSummary1.getId();
    List<String> taskIds =
        Stream.concat(
                taskSummaries.subList(0, 2).stream().map(TaskSummary::getId),
                Stream.of(taskSummaryForWb5.getId()))
            .toList();

    assertThatThrownBy(() -> taskService.distribute(sourceWorkbasketId, taskIds))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("Not all tasks are in the same workbasket.");
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_ThrowException_When_WorkbasketHasNoDistributionTargets() {
    String workbasketSummary6Id = workbasketSummary6.getId();
    assertThatThrownBy(() -> taskService.distributeWithDestinations(workbasketSummary6Id, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Ids of destinationWorkbaskets cannot be null or empty.");
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_ThrowNotAuthorizedOnWorkbasketException_When_NotAuthorized() {

    String expectedWorkbasketId = workbasketSummary1.getId();

    assertThatThrownBy(() -> taskService.distribute(expectedWorkbasketId, null))
        .isInstanceOf(NotAuthorizedOnWorkbasketException.class)
        .hasMessageContaining(
            String.format(
                "Not authorized. The current user 'user-1-1' has no '[DISTRIBUTE]' permission(s) "
                    + "for Workbasket '%s'.",
                expectedWorkbasketId));
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_ThrowInvalidArgumentException_When_DistributionStrategyDoesNotExist() {

    String nonExistingStrategy = "NoExistingStrategy";
    String workbasketSummary1Id = workbasketSummary1.getId();

    assertThatThrownBy(
            () ->
                taskService.distributeWithStrategy(workbasketSummary1Id, nonExistingStrategy, null))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining(
            "The distribution strategy '%s' does not exist.", nonExistingStrategy);
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_DoNothing_When_TaskIdsAreEmptyViaSourceWorkbasketId() throws Exception {

    BulkOperationResults<String, KadaiException> result =
        taskService.distribute(workbasketSummary2.getId());

    assertThat(result.getFailedIds()).isEmpty();

    List<TaskSummary> tasksInWb2 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary2.getId()).list();
    List<TaskSummary> tasksInWb3 =
        taskService.createTaskQuery().workbasketIdIn(workbasketSummary3.getId()).list();

    assertThat(tasksInWb2).isEmpty();
    assertThat(tasksInWb3).isEmpty();
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_ThrowWorkbasketNotFoundException_When_WorkbasketDoesNotExist() {

    String nonExistentWorkbasketId = "NonExistentWorkbasket";

    assertThatThrownBy(() -> taskService.distribute(nonExistentWorkbasketId, null))
        .isInstanceOf(WorkbasketNotFoundException.class)
        .hasMessageContaining(
            String.format("Workbasket with id '%s' was not found.", nonExistentWorkbasketId));
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_ReturnErrorInBulkResult_When_TaskDoesNotExist() throws Exception {
    String sourceWorkbasketId = workbasketSummary1.getId();
    String nonExistingTask = "NonExistingTaskId";
    String existingTaskId = taskSummaries.get(0).getId();
    List<String> taskIds = List.of(nonExistingTask, existingTaskId);

    BulkOperationResults<String, KadaiException> result =
        taskService.distribute(sourceWorkbasketId, taskIds);


    assertThat(result.getErrorMap()).containsKey(nonExistingTask);
    assertThat(result.getErrorMap().get(nonExistingTask))
        .isInstanceOf(TaskNotFoundException.class)
        .hasMessageContaining("Task with id 'NonExistingTaskId' was not found.");

    assertThat(result.getErrorMap()).doesNotContainKey(existingTaskId);
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_ThrowWorkbasketNotFoundExceptionn_When_DestinationWorkbasketDoesNotExist() {
    String sourceWorkbasketId = workbasketSummary1.getId();
    List<String> nonExistingDestinationWorkbasket = List.of("NonExistingDestinationWorkbasket");
    List<String> taskIds = taskSummaries.subList(0, 2).stream().map(TaskSummary::getId).toList();
    assertThatThrownBy(
            () ->
                taskService.distributeWithDestinations(
                    sourceWorkbasketId, taskIds, nonExistingDestinationWorkbasket))
        .isInstanceOf(WorkbasketNotFoundException.class)
        .hasMessageContaining(
            String.format(
                "Workbasket with id '%s' was not found.", nonExistingDestinationWorkbasket.get(0)));
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_ThrowInvalidArgumentException_When_TasksFromDifferentWorkbasketsProvided() {
    String sourceWorkbasketId = workbasketSummary1.getId();
    List<String> taskIds = List.of(taskSummaries.get(0).getId(), taskSummaryForWb5.getId());

    assertThatThrownBy(() -> taskService.distribute(sourceWorkbasketId, taskIds))
        .isInstanceOf(InvalidArgumentException.class)
        .hasMessageContaining("Not all tasks are in the same workbasket.");
  }

  void createAccessItemForUser(String workbasketSummaryId)
      throws WorkbasketAccessItemAlreadyExistException,
          WorkbasketNotFoundException,
          NotAuthorizedException {
    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(workbasketSummaryId)
        .accessId("user-1-2")
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.DISTRIBUTE)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .permission(WorkbasketPermission.TRANSFER)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService);
  }

  void transferAllTasksBackToWorkbasket1()
      throws NotAuthorizedOnWorkbasketException, WorkbasketNotFoundException {
    taskService.transferTasks(
        workbasketSummary1.getId(), taskSummaries.stream().map(TaskSummary::getId).toList());
  }

  public static class TaskDistributionProviderForTest implements TaskDistributionProvider {

    @Override
    public void initialize(KadaiEngine kadaiEngine) {
      // NOOP
    }

    public Map<String, List<String>> distributeTasks(
        List<String> taskIds,
        List<String> destinationWorkbasketIds,
        Map<String, Object> additionalInformation) {

      if (taskIds == null || taskIds.isEmpty()) {
        throw new IllegalArgumentException("Task IDs list cannot be null or empty.");
      }
      if (destinationWorkbasketIds == null || destinationWorkbasketIds.isEmpty()) {
        throw new IllegalArgumentException("Workbasket IDs list cannot be null or empty.");
      }

      Map<String, List<String>> distributedTaskIds = new HashMap<>();
      for (String workbasketId : destinationWorkbasketIds) {
        distributedTaskIds.put(workbasketId, new ArrayList<>());
      }

      distributedTaskIds.get(destinationWorkbasketIds.get(0)).addAll(taskIds.subList(0, 3));
      distributedTaskIds.get(destinationWorkbasketIds.get(1)).addAll(taskIds.subList(3, 5));

      if (destinationWorkbasketIds.size() == 3) {
        distributedTaskIds.get(destinationWorkbasketIds.get(2)).add(taskIds.get(5));
      }

      return distributedTaskIds;
    }
  }

  public static class TaskDistributionProviderForTest2 implements TaskDistributionProvider {

    @Override
    public void initialize(KadaiEngine kadaiEngine) {
      // NOOP
    }

    @Override
    public Map<String, List<String>> distributeTasks(
        List<String> taskIds,
        List<String> destinationWorkbasketIds,
        Map<String, Object> additionalInformation) {
      return Map.of();
    }
  }
}
