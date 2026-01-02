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

package acceptance.taskcomment.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.Classification;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskComment;
import io.kadai.testapi.DefaultTestEntities;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.builder.TaskCommentBuilder;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.models.Workbasket;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@KadaiIntegrationTest
class CreateTaskCommentAccTest {
  @KadaiInject TaskService taskService;
  @KadaiInject ClassificationService classificationService;
  @KadaiInject WorkbasketService workbasketService;

  Classification defaultClassification;
  Workbasket defaultWorkbasket;
  Task taskWithComments;
  TaskComment comment1;
  TaskComment comment2;

  @WithAccessId(user = "admin")
  @BeforeAll
  void setup() throws Exception {
    defaultClassification =
        DefaultTestEntities.defaultTestClassification().buildAndStore(classificationService);
    defaultWorkbasket =
        DefaultTestEntities.defaultTestWorkbasket().buildAndStore(workbasketService);
    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(defaultWorkbasket.getId())
        .accessId("user-1-1")
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService);

    taskWithComments =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassification.asSummary())
            .workbasketSummary(defaultWorkbasket.asSummary())
            .primaryObjRef(DefaultTestEntities.defaultTestObjectReference().build())
            .buildAndStore(taskService);
    comment1 =
        TaskCommentBuilder.newTaskComment()
            .taskId(taskWithComments.getId())
            .textField("Text1")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);
    comment2 =
        TaskCommentBuilder.newTaskComment()
            .taskId(taskWithComments.getId())
            .textField("Text1")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_CreateTaskCommentAndUpdateNumberOfComments_For_TaskThatAlreadyHasComments()
      throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassification.asSummary())
            .workbasketSummary(defaultWorkbasket.asSummary())
            .primaryObjRef(DefaultTestEntities.defaultTestObjectReference().build())
            .buildAndStore(taskService);
    comment1 =
        TaskCommentBuilder.newTaskComment()
            .taskId(task.getId())
            .textField("Text1")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);
    comment2 =
        TaskCommentBuilder.newTaskComment()
            .taskId(task.getId())
            .textField("Text1")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);
    TaskComment taskCommentToCreate = taskService.newTaskComment(task.getId());
    taskCommentToCreate.setTextField("Some text");
    taskService.createTaskComment(taskCommentToCreate);

    List<TaskComment> taskCommentsAfterInsert = taskService.getTaskComments(task.getId());
    assertThat(taskCommentsAfterInsert)
        .containsExactlyInAnyOrder(comment1, comment2, taskCommentToCreate);
    assertThat(taskService.getTask(task.getId()).getNumberOfComments()).isEqualTo(3);
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_FailToCreateTaskComment_When_UserHasNoWorkbasketPermission() {
    TaskComment taskCommentToCreate = taskService.newTaskComment(taskWithComments.getId());
    taskCommentToCreate.setTextField("Some text");

    ThrowingCallable call = () -> taskService.createTaskComment(taskCommentToCreate);

    NotAuthorizedOnWorkbasketException e =
        catchThrowableOfType(NotAuthorizedOnWorkbasketException.class, call);
    assertThat(e.getCurrentUserId()).isEqualTo("user-1-2");
    assertThat(e.getWorkbasketId()).isEqualTo(defaultWorkbasket.getId());
    assertThat(e.getRequiredPermissions())
        .containsExactly(WorkbasketPermission.READ, WorkbasketPermission.READTASKS);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_CreateTaskCommentAndUpdateNumberOfComments_When_CopyingAnotherComment()
      throws Exception {
    Task task =
        TaskBuilder.newTask()
            .classificationSummary(defaultClassification.asSummary())
            .workbasketSummary(defaultWorkbasket.asSummary())
            .primaryObjRef(DefaultTestEntities.defaultTestObjectReference().build())
            .buildAndStore(taskService);
    comment1 =
        TaskCommentBuilder.newTaskComment()
            .taskId(task.getId())
            .textField("Text1")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);
    comment2 =
        TaskCommentBuilder.newTaskComment()
            .taskId(task.getId())
            .textField("Text1")
            .created(Instant.now())
            .modified(Instant.now())
            .buildAndStore(taskService);
    TaskComment taskCommentToCreate = comment1.copy();

    taskService.createTaskComment(taskCommentToCreate);

    List<TaskComment> taskCommentsAfterInsert = taskService.getTaskComments(task.getId());
    assertThat(taskCommentsAfterInsert)
        .containsExactlyInAnyOrder(comment1, comment2, taskCommentToCreate);
    assertThat(taskService.getTask(task.getId()).getNumberOfComments()).isEqualTo(3);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_FailToCreateTaskCommentAndNotUpdateNumberOfComments_When_TaskIdIsNullOrNonExisting()
      throws Exception {
    TaskComment newTaskCommentForNonExistingTask =
        taskService.newTaskComment("Definitely non existing ID");
    newTaskCommentForNonExistingTask.setTextField("a newly created taskComment");
    TaskComment newTaskCommentForTaskIdNull = taskService.newTaskComment(null);
    newTaskCommentForTaskIdNull.setTextField("a newly created taskComment");

    ThrowingCallable call = () -> taskService.createTaskComment(newTaskCommentForNonExistingTask);
    TaskNotFoundException e = catchThrowableOfType(TaskNotFoundException.class, call);
    assertThat(e.getTaskId()).isEqualTo("Definitely non existing ID");

    call = () -> taskService.createTaskComment(newTaskCommentForTaskIdNull);
    e = catchThrowableOfType(TaskNotFoundException.class, call);
    assertThat(e.getTaskId()).isNull();
    assertThat(taskService.getTask(taskWithComments.getId()).getNumberOfComments()).isEqualTo(2);
  }
}
