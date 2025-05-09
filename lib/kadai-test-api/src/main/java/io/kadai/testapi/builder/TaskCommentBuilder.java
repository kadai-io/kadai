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

package io.kadai.testapi.builder;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.exceptions.TaskCommentNotFoundException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.TaskComment;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import java.time.Instant;

public class TaskCommentBuilder implements EntityBuilder<TaskComment, TaskService> {

  private final TaskCommentTestImpl testTaskComment = new TaskCommentTestImpl();

  public static TaskCommentBuilder newTaskComment() {
    return new TaskCommentBuilder();
  }

  public TaskCommentBuilder taskId(String taskId) {
    testTaskComment.setTaskId(taskId);
    return this;
  }

  public TaskCommentBuilder textField(String textField) {
    testTaskComment.setTextField(textField);
    return this;
  }

  public TaskCommentBuilder created(Instant created) {
    testTaskComment.setCreatedIgnoringFreeze(created);
    if (created != null) {
      testTaskComment.freezeCreated();
    } else {
      testTaskComment.unfreezeCreated();
    }
    return this;
  }

  public TaskCommentBuilder modified(Instant modified) {
    testTaskComment.setModifiedIgnoringFreeze(modified);
    if (modified != null) {
      testTaskComment.freezeModified();
    } else {
      testTaskComment.unfreezeModified();
    }
    return this;
  }

  @Override
  public TaskComment buildAndStore(TaskService taskService)
      throws InvalidArgumentException,
          TaskNotFoundException,
          TaskCommentNotFoundException,
          NotAuthorizedOnWorkbasketException {
    try {
      TaskComment t = taskService.createTaskComment(testTaskComment);
      return taskService.getTaskComment(t.getId());
    } finally {
      testTaskComment.setId(null);
    }
  }

  @Override
  public TaskComment build() {
    return testTaskComment.copy();
  }
}
