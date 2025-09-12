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

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.classification.rest.models.ClassificationSummaryRepresentationModel;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskCustomIntField;
import io.kadai.task.api.TaskService;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Attachment;
import io.kadai.task.api.models.Task;
import io.kadai.task.internal.models.AttachmentImpl;
import io.kadai.task.internal.models.ObjectReferenceImpl;
import io.kadai.task.internal.models.TaskImpl;
import io.kadai.task.internal.models.TaskPatchImpl;
import io.kadai.task.rest.models.AttachmentRepresentationModel;
import io.kadai.task.rest.models.ObjectReferenceRepresentationModel;
import io.kadai.task.rest.models.TaskPatchRepresentationModel;
import io.kadai.task.rest.models.TaskRepresentationModel;
import io.kadai.workbasket.api.WorkbasketService;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import io.kadai.workbasket.rest.models.WorkbasketSummaryRepresentationModel;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@KadaiSpringBootTest
class TaskRepresentationModelAssemblerTest {

  TaskService taskService;
  WorkbasketService workbasketService;
  ClassificationService classificationService;
  TaskRepresentationModelAssembler assembler;

  @Autowired
  TaskRepresentationModelAssemblerTest(
      TaskService taskService,
      WorkbasketService workbasketService,
      ClassificationService classificationService,
      TaskRepresentationModelAssembler assembler) {
    this.taskService = taskService;
    this.workbasketService = workbasketService;
    this.classificationService = classificationService;
    this.assembler = assembler;
  }

  @Test
  void should_ReturnEntity_When_ConvertingRepresentationModelToEntity() throws Exception {
    // given
    ObjectReferenceRepresentationModel primaryObjRef = new ObjectReferenceRepresentationModel();
    primaryObjRef.setId("abc");
    WorkbasketSummaryRepresentationModel workbasketResource =
        new WorkbasketSummaryRepresentationModel();
    workbasketResource.setWorkbasketId("workbasketId");
    ClassificationSummaryRepresentationModel classificationSummary =
        new ClassificationSummaryRepresentationModel();
    classificationSummary.setKey("keyabc");
    classificationSummary.setDomain("DOMAIN_A");
    classificationSummary.setType("MANUAL");
    AttachmentRepresentationModel attachment = new AttachmentRepresentationModel();
    attachment.setClassificationSummary(classificationSummary);
    attachment.setAttachmentId("attachmentId");
    attachment.setObjectReference(primaryObjRef);
    TaskRepresentationModel repModel = new TaskRepresentationModel();
    repModel.setTaskId("taskId");
    repModel.setExternalId("externalId");
    repModel.setCreated(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setClaimed(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setCompleted(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setModified(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setPlanned(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setReceived(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setDue(Instant.parse("2019-09-13T08:44:17.588Z"));
    repModel.setName("name");
    repModel.setCreator("creator");
    repModel.setDescription("desc");
    repModel.setNote("note");
    repModel.setManualPriority(123);
    repModel.setPriority(123);
    repModel.setState(TaskState.READY);
    repModel.setNumberOfComments(2);
    repModel.setClassificationSummary(classificationSummary);
    repModel.setWorkbasketSummary(workbasketResource);
    repModel.setBusinessProcessId("businessProcessId");
    repModel.setParentBusinessProcessId("parentBusinessProcessId");
    repModel.setOwner("owner");
    repModel.setOwnerLongName("ownerLongName");
    repModel.setPrimaryObjRef(primaryObjRef);
    repModel.setRead(true);
    repModel.setTransferred(true);
    repModel.setReopened(true);
    repModel.setCustomAttributes(List.of(TaskRepresentationModel.CustomAttribute.of("abc", "def")));
    repModel.setCallbackInfo(List.of(TaskRepresentationModel.CustomAttribute.of("ghi", "jkl")));
    repModel.setAttachments(List.of(attachment));
    repModel.setGroupByCount(0);
    repModel.setCustom1("custom1");
    repModel.setCustom2("custom2");
    repModel.setCustom3("custom3");
    repModel.setCustom4("custom4");
    repModel.setCustom5("custom5");
    repModel.setCustom6("custom6");
    repModel.setCustom7("custom7");
    repModel.setCustom8("custom8");
    repModel.setCustom9("custom9");
    repModel.setCustom10("custom10");
    repModel.setCustom11("custom11");
    repModel.setCustom12("custom12");
    repModel.setCustom13("custom13");
    repModel.setCustom14("custom14");
    repModel.setCustom15("custom15");
    repModel.setCustom16("custom16");
    repModel.setCustomInt1(1);
    repModel.setCustomInt2(2);
    repModel.setCustomInt3(3);
    repModel.setCustomInt4(4);
    repModel.setCustomInt5(5);
    repModel.setCustomInt6(6);
    repModel.setCustomInt7(7);
    repModel.setCustomInt8(8);
    // when
    Task task = assembler.toEntityModel(repModel);
    // then
    testEquality(task, repModel);
  }

  @Test
  void should_ReturnEntity_When_ConvertingRepresentationModelWithoutWorkbasketSummaryToEntity()
      throws Exception {
    // given
    ObjectReferenceRepresentationModel primaryObjRef = new ObjectReferenceRepresentationModel();
    primaryObjRef.setId("abc");
    ClassificationSummaryRepresentationModel classificationSummary =
        new ClassificationSummaryRepresentationModel();
    classificationSummary.setKey("keyabc");
    classificationSummary.setDomain("DOMAIN_A");
    classificationSummary.setType("MANUAL");
    AttachmentRepresentationModel attachment = new AttachmentRepresentationModel();
    attachment.setClassificationSummary(classificationSummary);
    attachment.setAttachmentId("attachmentId");
    attachment.setObjectReference(primaryObjRef);
    TaskRepresentationModel repModel = new TaskRepresentationModel();
    repModel.setTaskId("taskId");
    repModel.setExternalId("externalId");
    repModel.setClassificationSummary(classificationSummary);
    repModel.setPrimaryObjRef(primaryObjRef);
    // when
    Task task = assembler.toEntityModel(repModel);
    // then
    assertThat(repModel.getWorkbasketSummary()).isNull();
    assertThat(task.getWorkbasketSummary())
        .isNotNull()
        .hasAllNullFieldsOrPropertiesExcept(
            "markedForDeletion", "custom1", "custom2", "custom3", "custom4");
  }

  @Test
  void should_ReturnRepresentationModel_When_ConvertingEntityToRepresentationModel()
      throws Exception {
    // given
    ObjectReferenceImpl primaryObjRef = new ObjectReferenceImpl();
    primaryObjRef.setId("abc");
    final Workbasket workbasket = workbasketService.newWorkbasket("key", "domain");
    ClassificationSummary classification =
        classificationService.newClassification("ckey", "cdomain", "MANUAL").asSummary();
    AttachmentImpl attachment = (AttachmentImpl) taskService.newAttachment();
    attachment.setClassificationSummary(classification);
    attachment.setId("attachmentId");
    attachment.setObjectReference(primaryObjRef);
    TaskImpl task = (TaskImpl) taskService.newTask();
    task.setId("taskId");
    task.setExternalId("externalId");
    task.setCreated(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setClaimed(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setCompleted(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setModified(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setPlanned(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setReceived(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setDue(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setName("name");
    task.setCreator("creator");
    task.setDescription("desc");
    task.setNote("note");
    task.setPriority(123);
    task.setManualPriority(-5);
    task.setState(TaskState.READY);
    task.setNumberOfComments(2);
    task.setClassificationSummary(classification);
    task.setWorkbasketSummary(workbasket.asSummary());
    task.setBusinessProcessId("businessProcessId");
    task.setParentBusinessProcessId("parentBusinessProcessId");
    task.setOwner("owner");
    task.setOwnerLongName("ownerLongName");
    task.setPrimaryObjRef(primaryObjRef);
    task.setRead(true);
    task.setTransferred(true);
    task.setReopened(true);
    task.setGroupByCount(0);
    task.setCustomAttributeMap(Map.of("abc", "def"));
    task.setCallbackInfo(Map.of("ghi", "jkl"));
    task.setAttachments(List.of(attachment));
    task.setCustomField(TaskCustomField.CUSTOM_1, "custom1");
    task.setCustomField(TaskCustomField.CUSTOM_2, "custom2");
    task.setCustomField(TaskCustomField.CUSTOM_3, "custom3");
    task.setCustomField(TaskCustomField.CUSTOM_4, "custom4");
    task.setCustomField(TaskCustomField.CUSTOM_5, "custom5");
    task.setCustomField(TaskCustomField.CUSTOM_6, "custom6");
    task.setCustomField(TaskCustomField.CUSTOM_7, "custom7");
    task.setCustomField(TaskCustomField.CUSTOM_8, "custom8");
    task.setCustomField(TaskCustomField.CUSTOM_9, "custom9");
    task.setCustomField(TaskCustomField.CUSTOM_10, "custom10");
    task.setCustomField(TaskCustomField.CUSTOM_11, "custom11");
    task.setCustomField(TaskCustomField.CUSTOM_12, "custom12");
    task.setCustomField(TaskCustomField.CUSTOM_13, "custom13");
    task.setCustomField(TaskCustomField.CUSTOM_14, "custom14");
    task.setCustomField(TaskCustomField.CUSTOM_15, "custom15");
    task.setCustomField(TaskCustomField.CUSTOM_16, "custom16");
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_1, 1);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_2, 2);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_3, 3);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_4, 4);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_5, 5);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_6, 6);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_7, 7);
    task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_8, 8);
    // when
    TaskRepresentationModel repModel = assembler.toModel(task);
    // then
    testEquality(task, repModel);
    testLinks(repModel);
  }

  @Test
  void should_Equal_When_ComparingEntityWithConvertedEntity() throws InvalidArgumentException {
    // given
    ObjectReferenceImpl primaryObjRef = new ObjectReferenceImpl();
    primaryObjRef.setId("abc");
    final WorkbasketSummary workbasket =
        workbasketService.newWorkbasket("key", "domain").asSummary();
    ClassificationSummary classification =
        classificationService.newClassification("ckey", "cdomain", "MANUAL").asSummary();
    AttachmentImpl attachment = (AttachmentImpl) taskService.newAttachment();
    attachment.setClassificationSummary(classification);
    attachment.setId("attachmentId");
    attachment.setObjectReference(primaryObjRef);
    TaskImpl task = (TaskImpl) taskService.newTask();
    task.setId("taskId");
    task.setExternalId("externalId");
    task.setCreated(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setClaimed(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setCompleted(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setModified(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setPlanned(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setReceived(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setDue(Instant.parse("2019-09-13T08:44:17.588Z"));
    task.setName("name");
    task.setCreator("creator");
    task.setDescription("desc");
    task.setNote("note");
    task.setPriority(123);
    task.setManualPriority(123);
    task.setState(TaskState.READY);
    task.setNumberOfComments(2);
    task.setClassificationSummary(classification);
    task.setWorkbasketSummary(workbasket);
    task.setBusinessProcessId("businessProcessId");
    task.setParentBusinessProcessId("parentBusinessProcessId");
    task.setOwner("owner");
    task.setOwnerLongName("ownerLongName");
    task.setPrimaryObjRef(primaryObjRef);
    task.setRead(true);
    task.setTransferred(true);
    task.setReopened(true);
    task.setGroupByCount(0);
    task.setCustomAttributeMap(Map.of("abc", "def"));
    task.setCallbackInfo(Map.of("ghi", "jkl"));
    task.setAttachments(List.of(attachment));
    task.setCustom1("custom1");
    task.setCustom2("custom2");
    task.setCustom3("custom3");
    task.setCustom4("custom4");
    task.setCustom5("custom5");
    task.setCustom6("custom6");
    task.setCustom7("custom7");
    task.setCustom8("custom8");
    task.setCustom9("custom9");
    task.setCustom10("custom10");
    task.setCustom11("custom11");
    task.setCustom12("custom12");
    task.setCustom13("custom13");
    task.setCustom14("custom14");
    task.setCustom15("custom15");
    task.setCustom16("custom16");
    task.setCustomInt1(1);
    task.setCustomInt2(2);
    task.setCustomInt3(3);
    task.setCustomInt4(4);
    task.setCustomInt5(5);
    task.setCustomInt6(6);
    task.setCustomInt7(7);
    task.setCustomInt8(8);
    // when
    TaskRepresentationModel repModel = assembler.toModel(task);
    Task task2 = assembler.toEntityModel(repModel);
    // then
    assertThat(task).hasNoNullFieldsOrProperties().isNotSameAs(task2).isEqualTo(task2);
  }

  @Test
  void should_ReturnTaskPatch_When_ConvertingTaskPatchRepresentationModelWithAllFields() {
    // given
    TaskPatchRepresentationModel repModel = new TaskPatchRepresentationModel();
    repModel.setReceived(Instant.parse("2024-01-01T10:00:00.000Z"));
    repModel.setCreated(Instant.parse("2024-01-01T11:00:00.000Z"));
    repModel.setClaimed(Instant.parse("2024-01-01T12:00:00.000Z"));
    repModel.setModified(Instant.parse("2024-01-01T13:00:00.000Z"));
    repModel.setPlanned(Instant.parse("2024-01-01T14:00:00.000Z"));
    repModel.setDue(Instant.parse("2024-01-01T15:00:00.000Z"));
    repModel.setCompleted(Instant.parse("2024-01-01T16:00:00.000Z"));
    repModel.setName("Test Task");
    repModel.setCreator("test-creator");
    repModel.setNote("Test note");
    repModel.setDescription("Test description");
    repModel.setState(TaskState.READY);
    repModel.setPriority(50);
    repModel.setManualPriority(25);
    repModel.setNumberOfComments(5);
    repModel.setIsRead(true);
    repModel.setIsTransferred(false);
    repModel.setIsReopened(true);
    repModel.setGroupByCount(3);
    repModel.setBusinessProcessId("BPI-001");
    repModel.setParentBusinessProcessId("PBPI-001");
    repModel.setOwner("test-owner");
    repModel.setOwnerLongName("Test Owner Long Name");
    repModel.setCustom1("custom1-value");
    repModel.setCustom2("custom2-value");
    repModel.setCustom3("custom3-value");
    repModel.setCustom4("custom4-value");
    repModel.setCustom5("custom5-value");
    repModel.setCustom6("custom6-value");
    repModel.setCustom7("custom7-value");
    repModel.setCustom8("custom8-value");
    repModel.setCustom9("custom9-value");
    repModel.setCustom10("custom10-value");
    repModel.setCustom11("custom11-value");
    repModel.setCustom12("custom12-value");
    repModel.setCustom13("custom13-value");
    repModel.setCustom14("custom14-value");
    repModel.setCustom15("custom15-value");
    repModel.setCustom16("custom16-value");
    repModel.setCustomInt1(1001);
    repModel.setCustomInt2(1002);
    repModel.setCustomInt3(1003);
    repModel.setCustomInt4(1004);
    repModel.setCustomInt5(1005);
    repModel.setCustomInt6(1006);
    repModel.setCustomInt7(1007);
    repModel.setCustomInt8(1008);

    ClassificationSummaryRepresentationModel classificationSummary =
        new ClassificationSummaryRepresentationModel();
    classificationSummary.setClassificationId("CLI:123");
    classificationSummary.setKey("TEST-KEY");
    classificationSummary.setDomain("DOMAIN_A");
    classificationSummary.setType("TASK");
    repModel.setClassificationSummary(classificationSummary);

    WorkbasketSummaryRepresentationModel workbasketSummary =
        new WorkbasketSummaryRepresentationModel();
    workbasketSummary.setWorkbasketId("WBI:123");
    workbasketSummary.setKey("TEST-WB");
    workbasketSummary.setDomain("DOMAIN_A");
    repModel.setWorkbasketSummary(workbasketSummary);

    ObjectReferenceRepresentationModel primaryObjRef = new ObjectReferenceRepresentationModel();
    primaryObjRef.setCompany("TestCompany");
    primaryObjRef.setSystem("TestSystem");
    primaryObjRef.setSystemInstance("TestInstance");
    primaryObjRef.setType("TestType");
    primaryObjRef.setValue("test-value");
    repModel.setPrimaryObjRef(primaryObjRef);

    // Secondary object references
    ObjectReferenceRepresentationModel secondaryObjRef1 = new ObjectReferenceRepresentationModel();
    secondaryObjRef1.setCompany("SecCompany1");
    secondaryObjRef1.setSystem("SecSystem1");
    secondaryObjRef1.setSystemInstance("SecInstance1");
    secondaryObjRef1.setType("SecType1");
    secondaryObjRef1.setValue("sec-value1");

    ObjectReferenceRepresentationModel secondaryObjRef2 = new ObjectReferenceRepresentationModel();
    secondaryObjRef2.setCompany("SecCompany2");
    secondaryObjRef2.setSystem("SecSystem2");
    secondaryObjRef2.setSystemInstance("SecInstance2");
    secondaryObjRef2.setType("SecType2");
    secondaryObjRef2.setValue("sec-value2");

    repModel.setSecondaryObjectReferences(List.of(secondaryObjRef1, secondaryObjRef2));

    // Custom attributes
    repModel.setCustomAttributes(
        List.of(
            TaskRepresentationModel.CustomAttribute.of("attr1", "value1"),
            TaskRepresentationModel.CustomAttribute.of("attr2", "value2")));

    // Callback info
    repModel.setCallbackInfo(
        List.of(
            TaskRepresentationModel.CustomAttribute.of("callback1", "callback-value1"),
            TaskRepresentationModel.CustomAttribute.of("callback2", "callback-value2")));

    // Attachments
    AttachmentRepresentationModel attachment = new AttachmentRepresentationModel();
    attachment.setAttachmentId("ATT:123");
    attachment.setClassificationSummary(classificationSummary);
    repModel.setAttachments(List.of(attachment));

    // when
    TaskPatchImpl taskPatchImpl = assembler.toPatchImpl(repModel);

    // then
    assertThat(taskPatchImpl).isNotNull();
    assertThat(taskPatchImpl.getReceived()).isEqualTo(Instant.parse("2024-01-01T10:00:00.000Z"));
    assertThat(taskPatchImpl.getCreated()).isEqualTo(Instant.parse("2024-01-01T11:00:00.000Z"));
    assertThat(taskPatchImpl.getClaimed()).isEqualTo(Instant.parse("2024-01-01T12:00:00.000Z"));
    assertThat(taskPatchImpl.getModified()).isEqualTo(Instant.parse("2024-01-01T13:00:00.000Z"));
    assertThat(taskPatchImpl.getPlanned()).isEqualTo(Instant.parse("2024-01-01T14:00:00.000Z"));
    assertThat(taskPatchImpl.getDue()).isEqualTo(Instant.parse("2024-01-01T15:00:00.000Z"));
    assertThat(taskPatchImpl.getCompleted()).isEqualTo(Instant.parse("2024-01-01T16:00:00.000Z"));
    assertThat(taskPatchImpl.getName()).isEqualTo("Test Task");
    assertThat(taskPatchImpl.getCreator()).isEqualTo("test-creator");
    assertThat(taskPatchImpl.getNote()).isEqualTo("Test note");
    assertThat(taskPatchImpl.getDescription()).isEqualTo("Test description");
    assertThat(taskPatchImpl.getState()).isEqualTo(TaskState.READY);
    assertThat(taskPatchImpl.getPriority()).isEqualTo(50);
    assertThat(taskPatchImpl.getManualPriority()).isEqualTo(25);
    assertThat(taskPatchImpl.getNumberOfComments()).isEqualTo(5);
    assertThat(taskPatchImpl.isRead()).isTrue();
    assertThat(taskPatchImpl.isTransferred()).isFalse();
    assertThat(taskPatchImpl.isReopened()).isTrue();
    assertThat(taskPatchImpl.getGroupByCount()).isEqualTo(3);
    assertThat(taskPatchImpl.getBusinessProcessId()).isEqualTo("BPI-001");
    assertThat(taskPatchImpl.getParentBusinessProcessId()).isEqualTo("PBPI-001");
    assertThat(taskPatchImpl.getOwner()).isEqualTo("test-owner");
    assertThat(taskPatchImpl.getOwnerLongName()).isEqualTo("Test Owner Long Name");
    assertThat(taskPatchImpl.getCustom1()).isEqualTo("custom1-value");
    assertThat(taskPatchImpl.getCustom2()).isEqualTo("custom2-value");
    assertThat(taskPatchImpl.getCustom3()).isEqualTo("custom3-value");
    assertThat(taskPatchImpl.getCustom4()).isEqualTo("custom4-value");
    assertThat(taskPatchImpl.getCustom5()).isEqualTo("custom5-value");
    assertThat(taskPatchImpl.getCustom6()).isEqualTo("custom6-value");
    assertThat(taskPatchImpl.getCustom7()).isEqualTo("custom7-value");
    assertThat(taskPatchImpl.getCustom8()).isEqualTo("custom8-value");
    assertThat(taskPatchImpl.getCustom9()).isEqualTo("custom9-value");
    assertThat(taskPatchImpl.getCustom10()).isEqualTo("custom10-value");
    assertThat(taskPatchImpl.getCustom11()).isEqualTo("custom11-value");
    assertThat(taskPatchImpl.getCustom12()).isEqualTo("custom12-value");
    assertThat(taskPatchImpl.getCustom13()).isEqualTo("custom13-value");
    assertThat(taskPatchImpl.getCustom14()).isEqualTo("custom14-value");
    assertThat(taskPatchImpl.getCustom15()).isEqualTo("custom15-value");
    assertThat(taskPatchImpl.getCustom16()).isEqualTo("custom16-value");
    assertThat(taskPatchImpl.getCustomInt1()).isEqualTo(1001);
    assertThat(taskPatchImpl.getCustomInt2()).isEqualTo(1002);
    assertThat(taskPatchImpl.getCustomInt3()).isEqualTo(1003);
    assertThat(taskPatchImpl.getCustomInt4()).isEqualTo(1004);
    assertThat(taskPatchImpl.getCustomInt5()).isEqualTo(1005);
    assertThat(taskPatchImpl.getCustomInt6()).isEqualTo(1006);
    assertThat(taskPatchImpl.getCustomInt7()).isEqualTo(1007);
    assertThat(taskPatchImpl.getCustomInt8()).isEqualTo(1008);

    assertThat(taskPatchImpl.getClassificationSummary()).isNotNull();
    assertThat(taskPatchImpl.getClassificationSummary().getId()).isEqualTo("CLI:123");
    assertThat(taskPatchImpl.getClassificationSummary().getKey()).isEqualTo("TEST-KEY");
    assertThat(taskPatchImpl.getClassificationSummary().getDomain()).isEqualTo("DOMAIN_A");
    assertThat(taskPatchImpl.getClassificationSummary().getType()).isEqualTo("TASK");

    assertThat(taskPatchImpl.getWorkbasketSummary()).isNotNull();
    assertThat(taskPatchImpl.getWorkbasketSummary().getId()).isEqualTo("WBI:123");
    assertThat(taskPatchImpl.getWorkbasketSummary().getKey()).isEqualTo("TEST-WB");
    assertThat(taskPatchImpl.getWorkbasketSummary().getDomain()).isEqualTo("DOMAIN_A");

    assertThat(taskPatchImpl.getPrimaryObjRef()).isNotNull();
    assertThat(taskPatchImpl.getPrimaryObjRef().getCompany()).isEqualTo("TestCompany");
    assertThat(taskPatchImpl.getPrimaryObjRef().getSystem()).isEqualTo("TestSystem");
    assertThat(taskPatchImpl.getPrimaryObjRef().getSystemInstance()).isEqualTo("TestInstance");
    assertThat(taskPatchImpl.getPrimaryObjRef().getType()).isEqualTo("TestType");
    assertThat(taskPatchImpl.getPrimaryObjRef().getValue()).isEqualTo("test-value");

    assertThat(taskPatchImpl.getSecondaryObjectReferences()).hasSize(2);
    assertThat(taskPatchImpl.getSecondaryObjectReferences().get(0).getCompany())
        .isEqualTo("SecCompany1");
    assertThat(taskPatchImpl.getSecondaryObjectReferences().get(1).getCompany())
        .isEqualTo("SecCompany2");

    assertThat(taskPatchImpl.getCustomAttributes()).hasSize(2);
    assertThat(taskPatchImpl.getCustomAttributes().get("attr1")).isEqualTo("value1");
    assertThat(taskPatchImpl.getCustomAttributes().get("attr2")).isEqualTo("value2");

    assertThat(taskPatchImpl.getCallbackInfo()).hasSize(2);
    assertThat(taskPatchImpl.getCallbackInfo().get("callback1")).isEqualTo("callback-value1");
    assertThat(taskPatchImpl.getCallbackInfo().get("callback2")).isEqualTo("callback-value2");

    assertThat(taskPatchImpl.getAttachments()).hasSize(1);
    assertThat(taskPatchImpl.getAttachments().get(0).getId()).isEqualTo("ATT:123");
  }

  @Test
  void should_ReturnTaskPatchWithNullFields_When_ConvertingRepresentationModelWithNullValues() {
    // given
    TaskPatchRepresentationModel repModel = new TaskPatchRepresentationModel();
    // Only set a few fields, leave others null
    repModel.setName("Only Name Set");
    repModel.setPriority(100);

    // when
    TaskPatchImpl taskPatchImpl = assembler.toPatchImpl(repModel);

    // then
    assertThat(taskPatchImpl).isNotNull();
    assertThat(taskPatchImpl.getName()).isEqualTo("Only Name Set");
    assertThat(taskPatchImpl.getPriority()).isEqualTo(100);

    // Verify null fields are not set
    assertThat(taskPatchImpl.getReceived()).isNull();
    assertThat(taskPatchImpl.getCreated()).isNull();
    assertThat(taskPatchImpl.getClaimed()).isNull();
    assertThat(taskPatchImpl.getModified()).isNull();
    assertThat(taskPatchImpl.getPlanned()).isNull();
    assertThat(taskPatchImpl.getDue()).isNull();
    assertThat(taskPatchImpl.getCompleted()).isNull();
    assertThat(taskPatchImpl.getCreator()).isNull();
    assertThat(taskPatchImpl.getNote()).isNull();
    assertThat(taskPatchImpl.getDescription()).isNull();
    assertThat(taskPatchImpl.getState()).isNull();
    assertThat(taskPatchImpl.getManualPriority()).isNull();
    assertThat(taskPatchImpl.getNumberOfComments()).isNull();
    assertThat(taskPatchImpl.isRead()).isNull();
    assertThat(taskPatchImpl.isTransferred()).isNull();
    assertThat(taskPatchImpl.isReopened()).isNull();
    assertThat(taskPatchImpl.getGroupByCount()).isNull();
    assertThat(taskPatchImpl.getBusinessProcessId()).isNull();
    assertThat(taskPatchImpl.getParentBusinessProcessId()).isNull();
    assertThat(taskPatchImpl.getOwner()).isNull();
    assertThat(taskPatchImpl.getOwnerLongName()).isNull();
    assertThat(taskPatchImpl.getClassificationSummary()).isNull();
    assertThat(taskPatchImpl.getWorkbasketSummary()).isNull();
    assertThat(taskPatchImpl.getPrimaryObjRef()).isNull();
    assertThat(taskPatchImpl.getSecondaryObjectReferences()).isNull();
    assertThat(taskPatchImpl.getCustomAttributes()).isNull();
    assertThat(taskPatchImpl.getCallbackInfo()).isNull();
    assertThat(taskPatchImpl.getAttachments()).isNull();

    assertThat(taskPatchImpl.getCustom1()).isNull();
    assertThat(taskPatchImpl.getCustom2()).isNull();
    assertThat(taskPatchImpl.getCustom3()).isNull();
    assertThat(taskPatchImpl.getCustom4()).isNull();
    assertThat(taskPatchImpl.getCustom5()).isNull();
    assertThat(taskPatchImpl.getCustom6()).isNull();
    assertThat(taskPatchImpl.getCustom7()).isNull();
    assertThat(taskPatchImpl.getCustom8()).isNull();
    assertThat(taskPatchImpl.getCustom9()).isNull();
    assertThat(taskPatchImpl.getCustom10()).isNull();
    assertThat(taskPatchImpl.getCustom11()).isNull();
    assertThat(taskPatchImpl.getCustom12()).isNull();
    assertThat(taskPatchImpl.getCustom13()).isNull();
    assertThat(taskPatchImpl.getCustom14()).isNull();
    assertThat(taskPatchImpl.getCustom15()).isNull();
    assertThat(taskPatchImpl.getCustom16()).isNull();
    assertThat(taskPatchImpl.getCustomInt1()).isNull();
    assertThat(taskPatchImpl.getCustomInt2()).isNull();
    assertThat(taskPatchImpl.getCustomInt3()).isNull();
    assertThat(taskPatchImpl.getCustomInt4()).isNull();
    assertThat(taskPatchImpl.getCustomInt5()).isNull();
    assertThat(taskPatchImpl.getCustomInt6()).isNull();
    assertThat(taskPatchImpl.getCustomInt7()).isNull();
    assertThat(taskPatchImpl.getCustomInt8()).isNull();
  }

  private void testEquality(Task task, TaskRepresentationModel repModel) throws Exception {
    TaskSummaryRepresentationModelAssemblerTest.testEquality(task, repModel);

    testEqualityCustomAttributes(task.getCustomAttributeMap(), repModel.getCustomAttributes());
    testEqualityCustomAttributes(task.getCallbackInfo(), repModel.getCallbackInfo());
    testEqualityAttachments(task.getAttachments(), repModel.getAttachments());
  }

  private void testEqualityCustomAttributes(
      Map<String, String> customAttributes,
      List<TaskRepresentationModel.CustomAttribute> repModelAttributes) {
    assertThat(repModelAttributes).hasSize(customAttributes.size());
    repModelAttributes.forEach(
        attribute ->
            assertThat(attribute.getValue()).isEqualTo(customAttributes.get(attribute.getKey())));
  }

  private void testEqualityAttachments(
      List<Attachment> attachments, List<AttachmentRepresentationModel> repModels) {
    String[] objects = attachments.stream().map(Attachment::getId).toArray(String[]::new);

    // Anything else should be be tested in AttachmentResourceAssemblerTest
    assertThat(repModels)
        .hasSize(attachments.size())
        .extracting(AttachmentRepresentationModel::getAttachmentId)
        .containsExactlyInAnyOrder(objects);
  }

  private void testLinks(TaskRepresentationModel repModel) {
    assertThat(repModel.getLinks()).hasSize(1);
    assertThat(repModel.getRequiredLink("self").getHref())
        .isEqualTo(RestEndpoints.URL_TASKS_ID.replaceAll("\\{.*}", repModel.getTaskId()));
  }
}
