package io.kadai.task.internal.models;

import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.task.api.CallbackState;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskCustomIntField;
import io.kadai.task.api.TaskState;
import io.kadai.task.api.models.Attachment;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.api.models.TaskPatch;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Internal model for Task patch operations. All fields are nullable to support partial updates. */
public class TaskPatchImpl implements TaskPatch {

  private Instant received;
  private Instant created;
  private Instant claimed;
  private Instant modified;
  private Instant planned;
  private Instant due;
  private Instant completed;
  private String name;
  private String creator;
  private String note;
  private String description;
  private TaskState state;
  private ClassificationSummary classificationSummary;
  private Integer groupByCount;
  private WorkbasketSummary workbasketSummary;
  private String businessProcessId;
  private String parentBusinessProcessId;
  private String owner;
  private String ownerLongName;
  private ObjectReference primaryObjRef;
  
  private Integer priority;
  private Integer manualPriority;
  private Integer numberOfComments;
  private Boolean isRead;
  private Boolean isTransferred;
  private Boolean isReopened;
  private List<ObjectReference> secondaryObjectReferences;

  // Custom fields - all nullable
  private String custom1;
  private String custom2;
  private String custom3;
  private String custom4;
  private String custom5;
  private String custom6;
  private String custom7;
  private String custom8;
  private String custom9;
  private String custom10;
  private String custom11;
  private String custom12;
  private String custom13;
  private String custom14;
  private String custom15;
  private String custom16;
  private Integer customInt1;
  private Integer customInt2;
  private Integer customInt3;
  private Integer customInt4;
  private Integer customInt5;
  private Integer customInt6;
  private Integer customInt7;
  private Integer customInt8;

  private Map<String, String> customAttributes;
  private Map<String, String> callbackInfo;
  private CallbackState callbackState;
  private List<Attachment> attachments;

  public Instant getPlanned() {
    return planned;
  }

  public void setPlanned(Instant planned) {
    this.planned = planned;
  }

  public Instant getDue() {
    return due;
  }

  public void setDue(Instant due) {
    this.due = due;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TaskState getState() {
    return state;
  }

  public void setState(TaskState state) {
    this.state = state;
  }

  public ClassificationSummary getClassificationSummary() {
    return classificationSummary;
  }

  public void setClassificationSummary(ClassificationSummary classificationSummary) {
    this.classificationSummary = classificationSummary;
  }

  public WorkbasketSummary getWorkbasketSummary() {
    return workbasketSummary;
  }

  public void setWorkbasketSummary(WorkbasketSummary workbasketSummary) {
    this.workbasketSummary = workbasketSummary;
  }

  public ObjectReference getPrimaryObjRef() {
    return primaryObjRef;
  }

  public void setPrimaryObjRef(ObjectReference primaryObjRef) {
    this.primaryObjRef = primaryObjRef;
  }

  public String getBusinessProcessId() {
    return businessProcessId;
  }

  public void setBusinessProcessId(String businessProcessId) {
    this.businessProcessId = businessProcessId;
  }

  public String getParentBusinessProcessId() {
    return parentBusinessProcessId;
  }

  public void setParentBusinessProcessId(String parentBusinessProcessId) {
    this.parentBusinessProcessId = parentBusinessProcessId;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public Instant getModified() {
    return modified;
  }

  public void setModified(Instant modified) {
    this.modified = modified;
  }

  // Summary field getters and setters
  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Integer getManualPriority() {
    return manualPriority;
  }

  public void setManualPriority(Integer manualPriority) {
    this.manualPriority = manualPriority;
  }

  public Integer getNumberOfComments() {
    return numberOfComments;
  }

  public void setNumberOfComments(Integer numberOfComments) {
    this.numberOfComments = numberOfComments;
  }

  public Boolean isRead() {
    return isRead;
  }

  public void setIsRead(Boolean isRead) {
    this.isRead = isRead;
  }

  public Boolean isTransferred() {
    return isTransferred;
  }

  public void setIsTransferred(Boolean isTransferred) {
    this.isTransferred = isTransferred;
  }

  public Boolean isReopened() {
    return isReopened;
  }

  public void setIsReopened(Boolean isReopened) {
    this.isReopened = isReopened;
  }

  public List<ObjectReference> getSecondaryObjectReferences() {
    return secondaryObjectReferences;
  }

  public void setSecondaryObjectReferences(List<ObjectReference> secondaryObjectReferences) {
    this.secondaryObjectReferences = secondaryObjectReferences;
  }

  // Custom field getters and setters
  public String getCustom1() {
    return custom1;
  }

  public void setCustom1(String custom1) {
    this.custom1 = custom1;
  }

  public String getCustom2() {
    return custom2;
  }

  public void setCustom2(String custom2) {
    this.custom2 = custom2;
  }

  public String getCustom3() {
    return custom3;
  }

  public void setCustom3(String custom3) {
    this.custom3 = custom3;
  }

  public String getCustom4() {
    return custom4;
  }

  public void setCustom4(String custom4) {
    this.custom4 = custom4;
  }

  public String getCustom5() {
    return custom5;
  }

  public void setCustom5(String custom5) {
    this.custom5 = custom5;
  }

  public String getCustom6() {
    return custom6;
  }

  public void setCustom6(String custom6) {
    this.custom6 = custom6;
  }

  public String getCustom7() {
    return custom7;
  }

  public void setCustom7(String custom7) {
    this.custom7 = custom7;
  }

  public String getCustom8() {
    return custom8;
  }

  public void setCustom8(String custom8) {
    this.custom8 = custom8;
  }

  public String getCustom9() {
    return custom9;
  }

  public void setCustom9(String custom9) {
    this.custom9 = custom9;
  }

  public String getCustom10() {
    return custom10;
  }

  public void setCustom10(String custom10) {
    this.custom10 = custom10;
  }

  public String getCustom11() {
    return custom11;
  }

  public void setCustom11(String custom11) {
    this.custom11 = custom11;
  }

  public String getCustom12() {
    return custom12;
  }

  public void setCustom12(String custom12) {
    this.custom12 = custom12;
  }

  public String getCustom13() {
    return custom13;
  }

  public void setCustom13(String custom13) {
    this.custom13 = custom13;
  }

  public String getCustom14() {
    return custom14;
  }

  public void setCustom14(String custom14) {
    this.custom14 = custom14;
  }

  public String getCustom15() {
    return custom15;
  }

  public void setCustom15(String custom15) {
    this.custom15 = custom15;
  }

  public String getCustom16() {
    return custom16;
  }

  public void setCustom16(String custom16) {
    this.custom16 = custom16;
  }

  public Integer getCustomInt1() {
    return customInt1;
  }

  public void setCustomInt1(Integer customInt1) {
    this.customInt1 = customInt1;
  }

  public Integer getCustomInt2() {
    return customInt2;
  }

  public void setCustomInt2(Integer customInt2) {
    this.customInt2 = customInt2;
  }

  public Integer getCustomInt3() {
    return customInt3;
  }

  public void setCustomInt3(Integer customInt3) {
    this.customInt3 = customInt3;
  }

  public Integer getCustomInt4() {
    return customInt4;
  }

  public void setCustomInt4(Integer customInt4) {
    this.customInt4 = customInt4;
  }

  public Integer getCustomInt5() {
    return customInt5;
  }

  public void setCustomInt5(Integer customInt5) {
    this.customInt5 = customInt5;
  }

  public Integer getCustomInt6() {
    return customInt6;
  }

  public void setCustomInt6(Integer customInt6) {
    this.customInt6 = customInt6;
  }

  public Integer getCustomInt7() {
    return customInt7;
  }

  public void setCustomInt7(Integer customInt7) {
    this.customInt7 = customInt7;
  }

  public Integer getCustomInt8() {
    return customInt8;
  }

  public void setCustomInt8(Integer customInt8) {
    this.customInt8 = customInt8;
  }

  public Instant getReceived() {
    return received;
  }

  public void setReceived(Instant received) {
    this.received = received;
  }

  public Instant getCreated() {
    return created;
  }

  public void setCreated(Instant created) {
    this.created = created;
  }

  public Instant getClaimed() {
    return claimed;
  }

  public void setClaimed(Instant claimed) {
    this.claimed = claimed;
  }

  public Instant getCompleted() {
    return completed;
  }

  public void setCompleted(Instant completed) {
    this.completed = completed;
  }

  public Integer getGroupByCount() {
    return groupByCount;
  }

  public void setGroupByCount(Integer groupByCount) {
    this.groupByCount = groupByCount;
  }

  public String getOwnerLongName() {
    return ownerLongName;
  }

  public void setOwnerLongName(String ownerLongName) {
    this.ownerLongName = ownerLongName;
  }

  public Boolean getRead() {
    return isRead;
  }

  public Boolean getTransferred() {
    return isTransferred;
  }

  public Boolean getReopened() {
    return isReopened;
  }

  public Map<String, String> getCustomAttributes() {
    return customAttributes;
  }

  public void setCustomAttributes(Map<String, String> customAttribute) {
    this.customAttributes = customAttribute;
  }

  public Map<String, String> getCallbackInfo() {
    return callbackInfo;
  }

  public void setCallbackInfo(Map<String, String> callbackInfo) {
    this.callbackInfo = callbackInfo;
  }

  public CallbackState getCallbackState() {
    return callbackState;
  }

  public void setCallbackState(CallbackState callbackState) {
    this.callbackState = callbackState;
  }

  public List<Attachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<Attachment> attachments) {
    this.attachments = attachments;
  }

  /**
   * Applies all non-null fields from this TaskPatch to the provided TaskImpl. Only fields that are
   * not null will be written, preserving existing values otherwise.
   *
   * @param task the {@link TaskImpl} to update, may be {@code null}
   * @return the updated {@link TaskImpl}, or {@code null} if the input was {@code null}
   */
  public TaskImpl toTaskImpl(TaskImpl task) {
    if (task == null) {
      return null;
    }

    if (received != null) {
      task.setReceived(received);
    }
    if (created != null) {
      task.setCreated(created);
    }
    if (claimed != null) {
      task.setClaimed(claimed);
    }
    if (modified != null) {
      task.setModified(modified);
    }
    if (planned != null) {
      task.setPlanned(planned);
    }
    if (due != null) {
      task.setDue(due);
    }
    if (completed != null) {
      task.setCompleted(completed);
    }
    if (name != null) {
      task.setName(name);
    }
    if (creator != null) {
      task.setCreator(creator);
    }
    if (note != null) {
      task.setNote(note);
    }
    if (description != null) {
      task.setDescription(description);
    }
    if (state != null) {
      task.setState(state);
    }
    if (classificationSummary != null) {
      task.setClassificationSummary(classificationSummary);
    }
    if (groupByCount != null) {
      task.setGroupByCount(groupByCount);
    }
    if (workbasketSummary != null) {
      task.setWorkbasketSummary(workbasketSummary);
    }
    if (businessProcessId != null) {
      task.setBusinessProcessId(businessProcessId);
    }
    if (parentBusinessProcessId != null) {
      task.setParentBusinessProcessId(parentBusinessProcessId);
    }
    if (owner != null) {
      task.setOwner(owner);
    }
    if (ownerLongName != null) {
      task.setOwnerLongName(ownerLongName);
    }
    if (primaryObjRef != null) {
      task.setPrimaryObjRef(primaryObjRef);
    }

    if (priority != null) {
      task.setPriority(priority);
    }
    if (manualPriority != null) {
      task.setManualPriority(manualPriority);
    }
    if (numberOfComments != null) {
      task.setNumberOfComments(numberOfComments);
    }
    if (isRead != null) {
      task.setRead(isRead);
    }
    if (isTransferred != null) {
      task.setTransferred(isTransferred);
    }
    if (isReopened != null) {
      task.setReopened(isReopened);
    }
    if (secondaryObjectReferences != null) {
      // Copy to avoid reference issues
      task.setSecondaryObjectReferences(
          secondaryObjectReferences.stream()
              .map(ObjectReference::copy)
              .collect(Collectors.toList()));
    }

    // Custom string fields via API
    if (custom1 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_1, custom1);
    }
    if (custom2 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_2, custom2);
    }
    if (custom3 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_3, custom3);
    }
    if (custom4 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_4, custom4);
    }
    if (custom5 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_5, custom5);
    }
    if (custom6 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_6, custom6);
    }
    if (custom7 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_7, custom7);
    }
    if (custom8 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_8, custom8);
    }
    if (custom9 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_9, custom9);
    }
    if (custom10 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_10, custom10);
    }
    if (custom11 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_11, custom11);
    }
    if (custom12 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_12, custom12);
    }
    if (custom13 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_13, custom13);
    }
    if (custom14 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_14, custom14);
    }
    if (custom15 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_15, custom15);
    }
    if (custom16 != null) {
      task.setCustomField(TaskCustomField.CUSTOM_16, custom16);
    }

    // Custom int fields via API
    if (customInt1 != null) {
      task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_1, customInt1);
    }
    if (customInt2 != null) {
      task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_2, customInt2);
    }
    if (customInt3 != null) {
      task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_3, customInt3);
    }
    if (customInt4 != null) {
      task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_4, customInt4);
    }
    if (customInt5 != null) {
      task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_5, customInt5);
    }
    if (customInt6 != null) {
      task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_6, customInt6);
    }
    if (customInt7 != null) {
      task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_7, customInt7);
    }
    if (customInt8 != null) {
      task.setCustomIntField(TaskCustomIntField.CUSTOM_INT_8, customInt8);
    }

    if (customAttributes != null) {
      task.setCustomAttributeMap(customAttributes);
    }
    if (callbackInfo != null) {
      task.setCallbackInfo(callbackInfo);
    }
    if (callbackState != null) {
      task.setCallbackState(callbackState);
    }
    if (attachments != null) {
      // Copy to avoid reference issues
      task.setAttachments(attachments.stream().map(Attachment::copy).collect(Collectors.toList()));
    }

    return task;
  }
}
