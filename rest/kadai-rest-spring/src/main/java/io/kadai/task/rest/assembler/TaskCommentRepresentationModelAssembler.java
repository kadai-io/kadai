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

package io.kadai.task.rest.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.rest.assembler.CollectionRepresentationModelAssembler;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.TaskComment;
import io.kadai.task.internal.models.TaskCommentImpl;
import io.kadai.task.rest.TaskCommentController;
import io.kadai.task.rest.models.TaskCommentCollectionRepresentationModel;
import io.kadai.task.rest.models.TaskCommentRepresentationModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** EntityModel assembler for {@link TaskCommentRepresentationModel}. */
@Component
public class TaskCommentRepresentationModelAssembler
    implements CollectionRepresentationModelAssembler<
        TaskComment, TaskCommentRepresentationModel, TaskCommentCollectionRepresentationModel> {

  private final TaskService taskService;

  @Autowired
  public TaskCommentRepresentationModelAssembler(TaskService taskService) {
    this.taskService = taskService;
  }

  @NonNull
  @Override
  public TaskCommentRepresentationModel toModel(@NonNull TaskComment taskComment) {
    TaskCommentRepresentationModel repModel = new TaskCommentRepresentationModel();
    repModel.setTaskCommentId(taskComment.getId());
    repModel.setTaskId(taskComment.getTaskId());
    repModel.setTextField(taskComment.getTextField());
    repModel.setCreator(taskComment.getCreator());
    repModel.setCreatorFullName(taskComment.getCreatorFullName());
    repModel.setCreated(taskComment.getCreated());
    repModel.setModified(taskComment.getModified());
    try {
      repModel.add(
          linkTo(methodOn(TaskCommentController.class).getTaskComment(taskComment.getId()))
              .withSelfRel());
    } catch (Exception e) {
      throw new SystemException("caught unexpected Exception.", e.getCause());
    }
    return repModel;
  }

  @Override
  public TaskCommentCollectionRepresentationModel buildCollectionEntity(
      List<TaskCommentRepresentationModel> content) {
    return new TaskCommentCollectionRepresentationModel(content);
  }

  public TaskComment toEntityModel(TaskCommentRepresentationModel repModel) {
    TaskCommentImpl taskComment =
        (TaskCommentImpl) taskService.newTaskComment(repModel.getTaskId());
    taskComment.setId(repModel.getTaskCommentId());
    taskComment.setTextField(repModel.getTextField());
    taskComment.setCreator(repModel.getCreator());
    taskComment.setCreatorFullName(repModel.getCreatorFullName());
    taskComment.setCreated(repModel.getCreated());
    taskComment.setModified(repModel.getModified());
    return taskComment;
  }
}
