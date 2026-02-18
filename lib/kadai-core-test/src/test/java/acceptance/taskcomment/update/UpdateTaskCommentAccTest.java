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

package acceptance.taskcomment.update;

import static io.kadai.testapi.DefaultTestEntities.defaultTestClassification;
import static io.kadai.testapi.DefaultTestEntities.defaultTestObjectReference;
import static io.kadai.testapi.DefaultTestEntities.defaultTestWorkbasket;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.exceptions.NotAuthorizedOnTaskCommentException;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.Task;
import io.kadai.task.api.models.TaskComment;
import io.kadai.task.internal.models.TaskCommentImpl;
import io.kadai.testapi.KadaiInject;
import io.kadai.testapi.KadaiIntegrationTest;
import io.kadai.testapi.builder.TaskBuilder;
import io.kadai.testapi.builder.TaskCommentBuilder;
import io.kadai.testapi.builder.WorkbasketAccessItemBuilder;
import io.kadai.testapi.security.WithAccessId;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@KadaiIntegrationTest
class UpdateTaskCommentAccTest {

  @KadaiInject TaskService taskService;
  @KadaiInject WorkbasketService workbasketService;
  @KadaiInject ClassificationService classificationService;

  ClassificationSummary defaultClassificationSummary;
  WorkbasketSummary defaultWorkbasketSummary;
  ObjectReference defaultObjectReference;

  @WithAccessId(user = "businessadmin")
  @BeforeAll
  void setup() throws Exception {
    defaultClassificationSummary =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    defaultWorkbasketSummary = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(defaultWorkbasketSummary.getId())
        .accessId("user-1-1")
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.READTASKS)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService);
    defaultObjectReference = defaultTestObjectReference().build();
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_UpdateTaskComment_For_TaskComment() throws Exception {
    Task task = createDefaultTask().buildAndStore(taskService);
    TaskComment taskComment =
        TaskCommentBuilder.newTaskComment()
            .taskId(task.getId())
            .textField("some text in textfield")
            .buildAndStore(taskService);

    taskComment.setTextField("updated textfield");
    taskService.updateTaskComment(taskComment);

    List<TaskComment> taskCommentsAfterUpdate = taskService.getTaskComments(task.getId());
    assertThat(taskCommentsAfterUpdate)
        .extracting(TaskComment::getTextField)
        .containsExactly("updated textfield");
  }

  @WithAccessId(user = "user-1-2")
  @Test
  void should_FailToUpdateTaskComment_When_UserHasNoAuthorization() throws Exception {
    Task task = createDefaultTask().buildAndStore(taskService, "user-1-1");
    TaskComment taskComment =
        TaskCommentBuilder.newTaskComment()
            .taskId(task.getId())
            .textField("some text in textfield")
            .buildAndStore(taskService, "user-1-1");
    taskComment.setTextField("updated textfield");

    assertThatThrownBy(() -> taskService.updateTaskComment(taskComment))
        .isInstanceOf(NotAuthorizedOnWorkbasketException.class);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_FailToUpdateTaskComment_When_ChangingCreator() throws Exception {
    Task task = createDefaultTask().buildAndStore(taskService);
    TaskCommentImpl taskComment =
        (TaskCommentImpl)
            TaskCommentBuilder.newTaskComment()
                .taskId(task.getId())
                .textField("some text in textfield")
                .buildAndStore(taskService);

    taskComment.setCreator("user-1-2");

    ThrowingCallable updateTaskCommentCall = () -> taskService.updateTaskComment(taskComment);
    assertThatThrownBy(updateTaskCommentCall)
        .isInstanceOf(NotAuthorizedOnTaskCommentException.class);
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_FailToUpdateTaskComment_When_TaskCommentWasModifiedConcurrently() throws Exception {
    Task task = createDefaultTask().buildAndStore(taskService);
    TaskComment taskCommentToUpdate =
        TaskCommentBuilder.newTaskComment()
            .taskId(task.getId())
            .textField("some text in textfield")
            .buildAndStore(taskService);

    taskCommentToUpdate.setTextField("updated textfield");
    TaskComment concurrentTaskCommentToUpdate =
        taskService.getTaskComment(taskCommentToUpdate.getId());
    concurrentTaskCommentToUpdate.setTextField("concurrently updated textfield");

    taskService.updateTaskComment(taskCommentToUpdate);

    assertThatThrownBy(() -> taskService.updateTaskComment(concurrentTaskCommentToUpdate))
        .isInstanceOf(ConcurrencyException.class);
  }

  private TaskBuilder createDefaultTask() {
    return (TaskBuilder.newTask()
        .classificationSummary(defaultClassificationSummary)
        .workbasketSummary(defaultWorkbasketSummary)
        .primaryObjRef(defaultObjectReference));
  }
}
