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

import static java.util.function.Predicate.not;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import io.kadai.classification.rest.assembler.ClassificationSummaryRepresentationModelAssembler;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskCustomIntField;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.models.Task;
import io.kadai.task.internal.models.TaskImpl;
import io.kadai.task.internal.models.TaskPatchImpl;
import io.kadai.task.rest.TaskController;
import io.kadai.task.rest.models.TaskPatchRepresentationModel;
import io.kadai.task.rest.models.TaskRepresentationModel;
import io.kadai.task.rest.models.TaskRepresentationModel.CustomAttribute;
import io.kadai.workbasket.rest.assembler.WorkbasketSummaryRepresentationModelAssembler;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** EntityModel assembler for {@link TaskRepresentationModel}. */
@Component
public class TaskRepresentationModelAssembler
    implements RepresentationModelAssembler<Task, TaskRepresentationModel> {

  private final TaskService taskService;
  private final ClassificationSummaryRepresentationModelAssembler classificationAssembler;
  private final WorkbasketSummaryRepresentationModelAssembler workbasketAssembler;
  private final AttachmentRepresentationModelAssembler attachmentAssembler;
  private final ObjectReferenceRepresentationModelAssembler objectReferenceAssembler;

  @Autowired
  public TaskRepresentationModelAssembler(
      TaskService taskService,
      ClassificationSummaryRepresentationModelAssembler classificationAssembler,
      WorkbasketSummaryRepresentationModelAssembler workbasketAssembler,
      AttachmentRepresentationModelAssembler attachmentAssembler,
      ObjectReferenceRepresentationModelAssembler objectReferenceAssembler) {
    this.taskService = taskService;
    this.classificationAssembler = classificationAssembler;
    this.workbasketAssembler = workbasketAssembler;
    this.attachmentAssembler = attachmentAssembler;
    this.objectReferenceAssembler = objectReferenceAssembler;
  }

  @NonNull
  @Override
  public TaskRepresentationModel toModel(Task task) {
    TaskRepresentationModel repModel = new TaskRepresentationModel();
    repModel.setTaskId(task.getId());
    repModel.setExternalId(task.getExternalId());
    repModel.setCreated(task.getCreated());
    repModel.setClaimed(task.getClaimed());
    repModel.setCompleted(task.getCompleted());
    repModel.setModified(task.getModified());
    repModel.setPlanned(task.getPlanned());
    repModel.setReceived(task.getReceived());
    repModel.setDue(task.getDue());
    repModel.setName(task.getName());
    repModel.setCreator(task.getCreator());
    repModel.setNote(task.getNote());
    repModel.setDescription(task.getDescription());
    repModel.setPriority(task.getPriority());
    repModel.setManualPriority(task.getManualPriority());
    repModel.setState(task.getState());
    repModel.setNumberOfComments(task.getNumberOfComments());
    repModel.setClassificationSummary(
        classificationAssembler.toModel(task.getClassificationSummary()));
    repModel.setWorkbasketSummary(workbasketAssembler.toModel(task.getWorkbasketSummary()));
    repModel.setBusinessProcessId(task.getBusinessProcessId());
    repModel.setParentBusinessProcessId(task.getParentBusinessProcessId());
    repModel.setOwner(task.getOwner());
    repModel.setOwnerLongName(task.getOwnerLongName());
    repModel.setPrimaryObjRef(objectReferenceAssembler.toModel(task.getPrimaryObjRef()));
    repModel.setSecondaryObjectReferences(
        task.getSecondaryObjectReferences().stream()
            .map(objectReferenceAssembler::toModel)
            .toList());
    repModel.setRead(task.isRead());
    repModel.setTransferred(task.isTransferred());
    repModel.setReopened(task.isReopened());
    repModel.setGroupByCount(task.getGroupByCount());
    repModel.setAttachments(
        task.getAttachments().stream().map(attachmentAssembler::toModel).toList());
    repModel.setCustomAttributes(
        task.getCustomAttributeMap().entrySet().stream().map(CustomAttribute::of).toList());
    repModel.setCallbackInfo(
        task.getCallbackInfo().entrySet().stream().map(CustomAttribute::of).toList());
    repModel.setCustom1(task.getCustomField(TaskCustomField.CUSTOM_1));
    repModel.setCustom2(task.getCustomField(TaskCustomField.CUSTOM_2));
    repModel.setCustom3(task.getCustomField(TaskCustomField.CUSTOM_3));
    repModel.setCustom4(task.getCustomField(TaskCustomField.CUSTOM_4));
    repModel.setCustom5(task.getCustomField(TaskCustomField.CUSTOM_5));
    repModel.setCustom6(task.getCustomField(TaskCustomField.CUSTOM_6));
    repModel.setCustom7(task.getCustomField(TaskCustomField.CUSTOM_7));
    repModel.setCustom8(task.getCustomField(TaskCustomField.CUSTOM_8));
    repModel.setCustom9(task.getCustomField(TaskCustomField.CUSTOM_9));
    repModel.setCustom10(task.getCustomField(TaskCustomField.CUSTOM_10));
    repModel.setCustom11(task.getCustomField(TaskCustomField.CUSTOM_11));
    repModel.setCustom12(task.getCustomField(TaskCustomField.CUSTOM_12));
    repModel.setCustom13(task.getCustomField(TaskCustomField.CUSTOM_13));
    repModel.setCustom14(task.getCustomField(TaskCustomField.CUSTOM_14));
    repModel.setCustom15(task.getCustomField(TaskCustomField.CUSTOM_15));
    repModel.setCustom16(task.getCustomField(TaskCustomField.CUSTOM_16));
    repModel.setCustomInt1(task.getCustomIntField(TaskCustomIntField.CUSTOM_INT_1));
    repModel.setCustomInt2(task.getCustomIntField(TaskCustomIntField.CUSTOM_INT_2));
    repModel.setCustomInt3(task.getCustomIntField(TaskCustomIntField.CUSTOM_INT_3));
    repModel.setCustomInt4(task.getCustomIntField(TaskCustomIntField.CUSTOM_INT_4));
    repModel.setCustomInt5(task.getCustomIntField(TaskCustomIntField.CUSTOM_INT_5));
    repModel.setCustomInt6(task.getCustomIntField(TaskCustomIntField.CUSTOM_INT_6));
    repModel.setCustomInt7(task.getCustomIntField(TaskCustomIntField.CUSTOM_INT_7));
    repModel.setCustomInt8(task.getCustomIntField(TaskCustomIntField.CUSTOM_INT_8));
    try {
      repModel.add(linkTo(methodOn(TaskController.class).getTask(task.getId())).withSelfRel());
    } catch (Exception e) {
      throw new SystemException("caught unexpected Exception.", e.getCause());
    }
    return repModel;
  }

  public Task toEntityModel(TaskRepresentationModel repModel) throws InvalidArgumentException {
    verifyCorrectCustomAttributesFormat(repModel.getCustomAttributes());
    TaskImpl task = (TaskImpl) taskService.newTask();
    task.setId(repModel.getTaskId());
    task.setExternalId(repModel.getExternalId());
    task.setCreated(repModel.getCreated());
    task.setClaimed(repModel.getClaimed());
    task.setCompleted(repModel.getCompleted());
    task.setModified(repModel.getModified());
    task.setPlanned(repModel.getPlanned());
    task.setReceived(repModel.getReceived());
    task.setDue(repModel.getDue());
    task.setName(repModel.getName());
    task.setCreator(repModel.getCreator());
    task.setNote(repModel.getNote());
    task.setDescription(repModel.getDescription());
    task.setPriority(repModel.getPriority());
    task.setManualPriority(repModel.getManualPriority());
    task.setState(repModel.getState());
    task.setNumberOfComments(repModel.getNumberOfComments());
    if (repModel.getClassificationSummary() != null) {
      task.setClassificationSummary(
          classificationAssembler.toEntityModel(repModel.getClassificationSummary()));
    }
    if (repModel.getWorkbasketSummary() != null) {
      task.setWorkbasketSummary(workbasketAssembler.toEntityModel(repModel.getWorkbasketSummary()));
    }
    task.setBusinessProcessId(repModel.getBusinessProcessId());
    task.setParentBusinessProcessId(repModel.getParentBusinessProcessId());
    task.setOwner(repModel.getOwner());
    task.setOwnerLongName(repModel.getOwnerLongName());
    task.setPrimaryObjRef(objectReferenceAssembler.toEntity(repModel.getPrimaryObjRef()));
    task.setRead(repModel.isRead());
    task.setTransferred(repModel.isTransferred());
    task.setReopened(repModel.isReopened());
    task.setGroupByCount(repModel.getGroupByCount());
    task.setCustomField(TaskCustomField.CUSTOM_1, repModel.getCustom1());
    task.setCustomField(TaskCustomField.CUSTOM_2, repModel.getCustom2());
    task.setCustomField(TaskCustomField.CUSTOM_3, repModel.getCustom3());
    task.setCustomField(TaskCustomField.CUSTOM_4, repModel.getCustom4());
    task.setCustomField(TaskCustomField.CUSTOM_5, repModel.getCustom5());
    task.setCustomField(TaskCustomField.CUSTOM_6, repModel.getCustom6());
    task.setCustomField(TaskCustomField.CUSTOM_7, repModel.getCustom7());
    task.setCustomField(TaskCustomField.CUSTOM_8, repModel.getCustom8());
    task.setCustomField(TaskCustomField.CUSTOM_9, repModel.getCustom9());
    task.setCustomField(TaskCustomField.CUSTOM_10, repModel.getCustom10());
    task.setCustomField(TaskCustomField.CUSTOM_11, repModel.getCustom11());
    task.setCustomField(TaskCustomField.CUSTOM_12, repModel.getCustom12());
    task.setCustomField(TaskCustomField.CUSTOM_13, repModel.getCustom13());
    task.setCustomField(TaskCustomField.CUSTOM_14, repModel.getCustom14());
    task.setCustomField(TaskCustomField.CUSTOM_15, repModel.getCustom15());
    task.setCustomField(TaskCustomField.CUSTOM_16, repModel.getCustom16());
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_1, repModel.getCustomInt1());
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_2, repModel.getCustomInt2());
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_3, repModel.getCustomInt3());
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_4, repModel.getCustomInt4());
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_5, repModel.getCustomInt5());
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_6, repModel.getCustomInt6());
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_7, repModel.getCustomInt7());
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_8, repModel.getCustomInt8());
    task.setAttachments(
        repModel.getAttachments().stream().map(attachmentAssembler::toEntityModel).toList());
    task.setSecondaryObjectReferences(
        repModel.getSecondaryObjectReferences().stream()
            .map(objectReferenceAssembler::toEntity)
            .toList());
    task.setCustomAttributeMap(
        repModel.getCustomAttributes().stream()
            .collect(Collectors.toMap(CustomAttribute::getKey, CustomAttribute::getValue)));
    task.setCallbackInfo(
        repModel.getCallbackInfo().stream()
            .filter(e -> Objects.nonNull(e.getKey()))
            .filter(not(e -> e.getKey().isEmpty()))
            .collect(Collectors.toMap(CustomAttribute::getKey, CustomAttribute::getValue)));
    return task;
  }

  private void verifyCorrectCustomAttributesFormat(List<CustomAttribute> customAttributes)
      throws InvalidArgumentException {

    if (customAttributes.stream()
        .anyMatch(
            customAttribute ->
                customAttribute.getKey() == null
                    || customAttribute.getKey().isEmpty()
                    || customAttribute.getValue() == null)) {
      throw new InvalidArgumentException(
          "Format of custom attributes is not valid. Please provide the following format: "
              + "\"customAttributes\": [{\"key\": \"someKey\",\"value\": \"someValue\"},{...}])");
    }
  }

  public TaskPatchImpl toPatchModel(TaskPatchRepresentationModel repModel) {
    if (repModel.getCustomAttributes() != null) {
      verifyCorrectCustomAttributesFormat(repModel.getCustomAttributes());
    }
    TaskPatchImpl taskPatchImpl = new TaskPatchImpl();

    // Base fields - only set if not null
    if (repModel.getTaskId() != null) {
      taskPatchImpl.setId(repModel.getTaskId());
    }
    if (repModel.getExternalId() != null) {
      taskPatchImpl.setExternalId(repModel.getExternalId());
    }
    if (repModel.getReceived() != null) {
      taskPatchImpl.setReceived(repModel.getReceived());
    }
    if (repModel.getCreated() != null) {
      taskPatchImpl.setCreated(repModel.getCreated());
    }
    if (repModel.getClaimed() != null) {
      taskPatchImpl.setClaimed(repModel.getClaimed());
    }
    if (repModel.getModified() != null) {
      taskPatchImpl.setModified(repModel.getModified());
    }
    if (repModel.getPlanned() != null) {
      taskPatchImpl.setPlanned(repModel.getPlanned());
    }
    if (repModel.getDue() != null) {
      taskPatchImpl.setDue(repModel.getDue());
    }
    if (repModel.getCompleted() != null) {
      taskPatchImpl.setCompleted(repModel.getCompleted());
    }
    if (repModel.getName() != null) {
      taskPatchImpl.setName(repModel.getName());
    }
    if (repModel.getCreator() != null) {
      taskPatchImpl.setCreator(repModel.getCreator());
    }
    if (repModel.getNote() != null) {
      taskPatchImpl.setNote(repModel.getNote());
    }
    if (repModel.getDescription() != null) {
      taskPatchImpl.setDescription(repModel.getDescription());
    }
    if (repModel.getState() != null) {
      taskPatchImpl.setState(repModel.getState());
    }
    if (repModel.getClassificationSummary() != null) {
      taskPatchImpl.setClassificationSummary(
          classificationAssembler.toEntityModel(repModel.getClassificationSummary()));
    }
    if (repModel.getGroupByCount() != null) {
      taskPatchImpl.setGroupByCount(repModel.getGroupByCount());
    }
    if (repModel.getWorkbasketSummary() != null) {
      taskPatchImpl.setWorkbasketSummary(
          workbasketAssembler.toEntityModel(repModel.getWorkbasketSummary()));
    }
    if (repModel.getBusinessProcessId() != null) {
      taskPatchImpl.setBusinessProcessId(repModel.getBusinessProcessId());
    }
    if (repModel.getParentBusinessProcessId() != null) {
      taskPatchImpl.setParentBusinessProcessId(repModel.getParentBusinessProcessId());
    }
    if (repModel.getOwner() != null) {
      taskPatchImpl.setOwner(repModel.getOwner());
    }
    if (repModel.getOwnerLongName() != null) {
      taskPatchImpl.setOwnerLongName(repModel.getOwnerLongName());
    }
    if (repModel.getPrimaryObjRef() != null) {
      taskPatchImpl.setPrimaryObjRef(
          objectReferenceAssembler.toEntity(repModel.getPrimaryObjRef()));
    }

    if (repModel.getPriority() != null) {
      taskPatchImpl.setPriority(repModel.getPriority());
    }
    if (repModel.getManualPriority() != null) {
      taskPatchImpl.setManualPriority(repModel.getManualPriority());
    }
    if (repModel.getNumberOfComments() != null) {
      taskPatchImpl.setNumberOfComments(repModel.getNumberOfComments());
    }
    if (repModel.getIsRead() != null) {
      taskPatchImpl.setIsRead(repModel.getIsRead());
    }
    if (repModel.getIsTransferred() != null) {
      taskPatchImpl.setIsTransferred(repModel.getIsTransferred());
    }
    if (repModel.getIsReopened() != null) {
      taskPatchImpl.setIsReopened(repModel.getIsReopened());
    }
    if (repModel.getSecondaryObjectReferences() != null) {
      taskPatchImpl.setSecondaryObjectReferences(
          repModel.getSecondaryObjectReferences().stream()
              .map(objectReferenceAssembler::toEntity)
              .toList());
    }

    // Custom fields - only set if not null
    if (repModel.getCustom1() != null) {
      taskPatchImpl.setCustom1(repModel.getCustom1());
    }
    if (repModel.getCustom2() != null) {
      taskPatchImpl.setCustom2(repModel.getCustom2());
    }
    if (repModel.getCustom3() != null) {
      taskPatchImpl.setCustom3(repModel.getCustom3());
    }
    if (repModel.getCustom4() != null) {
      taskPatchImpl.setCustom4(repModel.getCustom4());
    }
    if (repModel.getCustom5() != null) {
      taskPatchImpl.setCustom5(repModel.getCustom5());
    }
    if (repModel.getCustom6() != null) {
      taskPatchImpl.setCustom6(repModel.getCustom6());
    }
    if (repModel.getCustom7() != null) {
      taskPatchImpl.setCustom7(repModel.getCustom7());
    }
    if (repModel.getCustom8() != null) {
      taskPatchImpl.setCustom8(repModel.getCustom8());
    }
    if (repModel.getCustom9() != null) {
      taskPatchImpl.setCustom9(repModel.getCustom9());
    }
    if (repModel.getCustom10() != null) {
      taskPatchImpl.setCustom10(repModel.getCustom10());
    }
    if (repModel.getCustom11() != null) {
      taskPatchImpl.setCustom11(repModel.getCustom11());
    }
    if (repModel.getCustom12() != null) {
      taskPatchImpl.setCustom12(repModel.getCustom12());
    }
    if (repModel.getCustom13() != null) {
      taskPatchImpl.setCustom13(repModel.getCustom13());
    }
    if (repModel.getCustom14() != null) {
      taskPatchImpl.setCustom14(repModel.getCustom14());
    }
    if (repModel.getCustom15() != null) {
      taskPatchImpl.setCustom15(repModel.getCustom15());
    }
    if (repModel.getCustom16() != null) {
      taskPatchImpl.setCustom16(repModel.getCustom16());
    }

    // Custom int fields - only set if not null
    if (repModel.getCustomInt1() != null) {
      taskPatchImpl.setCustomInt1(repModel.getCustomInt1());
    }
    if (repModel.getCustomInt2() != null) {
      taskPatchImpl.setCustomInt2(repModel.getCustomInt2());
    }
    if (repModel.getCustomInt3() != null) {
      taskPatchImpl.setCustomInt3(repModel.getCustomInt3());
    }
    if (repModel.getCustomInt4() != null) {
      taskPatchImpl.setCustomInt4(repModel.getCustomInt4());
    }
    if (repModel.getCustomInt5() != null) {
      taskPatchImpl.setCustomInt5(repModel.getCustomInt5());
    }
    if (repModel.getCustomInt6() != null) {
      taskPatchImpl.setCustomInt6(repModel.getCustomInt6());
    }
    if (repModel.getCustomInt7() != null) {
      taskPatchImpl.setCustomInt7(repModel.getCustomInt7());
    }
    if (repModel.getCustomInt8() != null) {
      taskPatchImpl.setCustomInt8(repModel.getCustomInt8());
    }
    if (repModel.getAttachments() != null) {
      taskPatchImpl.setAttachments(
          repModel.getAttachments().stream().map(attachmentAssembler::toEntityModel).toList());
    }

    if (repModel.getCustomAttributes() != null) {
      taskPatchImpl.setCustomAttributes(
          repModel.getCustomAttributes().stream()
              .collect(Collectors.toMap(CustomAttribute::getKey, CustomAttribute::getValue)));
    }
    if (repModel.getCallbackInfo() != null) {
      taskPatchImpl.setCallbackInfo(
          repModel.getCallbackInfo().stream()
              .filter(e -> Objects.nonNull(e.getKey()))
              .filter(not(e -> e.getKey().isEmpty()))
              .collect(Collectors.toMap(CustomAttribute::getKey, CustomAttribute::getValue)));
    }
    return taskPatchImpl;
  }
}
