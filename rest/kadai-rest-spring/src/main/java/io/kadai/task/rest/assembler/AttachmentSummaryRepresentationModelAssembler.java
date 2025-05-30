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

import io.kadai.classification.rest.assembler.ClassificationSummaryRepresentationModelAssembler;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.AttachmentSummary;
import io.kadai.task.internal.models.AttachmentSummaryImpl;
import io.kadai.task.rest.models.AttachmentSummaryRepresentationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** EntityModel assembler for {@link AttachmentSummaryRepresentationModel}. */
@Component
public class AttachmentSummaryRepresentationModelAssembler
    implements RepresentationModelAssembler<
        AttachmentSummary, AttachmentSummaryRepresentationModel> {

  private final ClassificationSummaryRepresentationModelAssembler classificationSummaryAssembler;
  private final ObjectReferenceRepresentationModelAssembler objectReferenceAssembler;
  private final TaskService taskService;

  @Autowired
  public AttachmentSummaryRepresentationModelAssembler(
      TaskService taskService,
      ClassificationSummaryRepresentationModelAssembler classificationSummaryAssembler,
      ObjectReferenceRepresentationModelAssembler objectReferenceAssembler) {
    this.taskService = taskService;
    this.classificationSummaryAssembler = classificationSummaryAssembler;
    this.objectReferenceAssembler = objectReferenceAssembler;
  }

  @NonNull
  @Override
  public AttachmentSummaryRepresentationModel toModel(@NonNull AttachmentSummary summary) {
    AttachmentSummaryRepresentationModel repModel = new AttachmentSummaryRepresentationModel();
    repModel.setAttachmentId(summary.getId());
    repModel.setTaskId(summary.getTaskId());
    repModel.setCreated(summary.getCreated());
    repModel.setModified(summary.getModified());
    repModel.setReceived(summary.getReceived());
    repModel.setClassificationSummary(
        classificationSummaryAssembler.toModel(summary.getClassificationSummary()));
    repModel.setObjectReference(objectReferenceAssembler.toModel(summary.getObjectReference()));
    repModel.setChannel(summary.getChannel());
    return repModel;
  }

  public AttachmentSummary toEntityModel(AttachmentSummaryRepresentationModel repModel) {
    AttachmentSummaryImpl attachment =
        (AttachmentSummaryImpl) taskService.newAttachment().asSummary();
    attachment.setId(repModel.getAttachmentId());
    attachment.setTaskId(repModel.getTaskId());
    attachment.setCreated(repModel.getCreated());
    attachment.setModified(repModel.getModified());
    attachment.setReceived(repModel.getReceived());
    attachment.setClassificationSummary(
        classificationSummaryAssembler.toEntityModel(repModel.getClassificationSummary()));
    attachment.setObjectReference(objectReferenceAssembler.toEntity(repModel.getObjectReference()));
    attachment.setChannel(repModel.getChannel());
    return attachment;
  }
}
