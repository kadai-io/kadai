package io.kadai.task.api.models;

import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.task.api.CallbackState;
import io.kadai.task.api.TaskState;
import io.kadai.workbasket.api.models.WorkbasketSummary;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface TaskPatch {
  /**
   * Returns the id of the {@linkplain TaskPatch}.
   *
   * @return taskId
   */
  String getId();

  /**
   * Sets the id of the {@linkplain TaskPatch}.
   *
   * @param id new id
   */
  void setId(String id);

  /**
   * Returns the externalId of the {@linkplain TaskPatch}.
   *
   * @return externalId
   */
  String getExternalId();

  /**
   * Sets the externalId of the {@linkplain TaskPatch}.
   *
   * @param externalId new externalId
   */
  void setExternalId(String externalId);

  /**
   * Returns the received TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the received Instant
   */
  Instant getReceived();

  /**
   * Sets the received TimeStamp of the {@linkplain TaskPatch}.
   *
   * @param received new received TimeStamp
   */
  void setReceived(Instant received);

  /**
   * Returns the created TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the created Instant
   */
  Instant getCreated();

  /**
   * Sets the created TimeStamp of the {@linkplain TaskPatch}.
   *
   * @param created new created TimeStamp
   */
  void setCreated(Instant created);

  /**
   * Returns the claimed TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the claimed Instant
   */
  Instant getClaimed();

  /**
   * Sets the claimed TimeStamp of the {@linkplain TaskPatch}.
   *
   * @param claimed new claimed TimeStamp
   */
  void setClaimed(Instant claimed);

  /**
   * Returns the modified TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the last modified Instant
   */
  Instant getModified();

  /**
   * Sets the modified TimeStamp of the {@linkplain TaskPatch}.
   *
   * @param modified new modified TimeStamp
   */
  void setModified(Instant modified);

  /**
   * Returns the planned TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the planned Instant
   */
  Instant getPlanned();

  /**
   * Sets the planned TimeStamp of the {@linkplain TaskPatch}.
   *
   * @param planned new planned TimeStamp
   */
  void setPlanned(Instant planned);

  /**
   * Returns the Due TimeStamp of the {@linkplain TaskPatch}.
   *
   * <p>This instant denotes the last point in the allowed work time has ended or in short it is
   * inclusive.
   *
   * @return the due Instant
   */
  Instant getDue();

  /**
   * Sets the due TimeStamp of the {@linkplain TaskPatch}.
   *
   * @param due new due TimeStamp
   */
  void setDue(Instant due);

  /**
   * Returns the completed TimeStamp of the {@linkplain TaskPatch}.
   *
   * @return the completed Instant
   */
  Instant getCompleted();

  /**
   * Sets the completed TimeStamp of the {@linkplain TaskPatch}.
   *
   * @param completed new completed TimeStamp
   */
  void setCompleted(Instant completed);

  /**
   * Returns the name of the {@linkplain TaskPatch}.
   *
   * @return name
   */
  String getName();

  /**
   * Sets the name of the current TaskPatch.
   *
   * @param name the name of the TaskPatch
   */
  void setName(String name);

  /**
   * Returns the name of the creator of the {@linkplain TaskPatch}.
   *
   * @return creator
   */
  String getCreator();

  /**
   * Sets the creator of the current TaskPatch.
   *
   * @param creator the creator of the TaskPatch
   */
  void setCreator(String creator);

  /**
   * Returns the note attached to the {@linkplain TaskPatch}.
   *
   * @return note
   */
  String getNote();

  /**
   * Sets/Changing the custom note for this TaskPatch.
   *
   * @param note the custom note for this TaskPatch.
   */
  void setNote(String note);

  /**
   * Returns the description of the {@linkplain TaskPatch}.
   *
   * @return description
   */
  String getDescription();

  /**
   * Sets the description of the TaskPatch.
   *
   * @param description the description of the TaskPatch
   */
  void setDescription(String description);

  /**
   * Returns the state of the {@linkplain TaskPatch}.
   *
   * @return state
   */
  TaskState getState();

  /**
   * Sets the state of the TaskPatch.
   *
   * @param state the state of the TaskPatch
   */
  void setState(TaskState state);

  /**
   * Returns the {@linkplain ClassificationSummary} of the {@linkplain TaskPatch}.
   *
   * @return {@linkplain ClassificationSummary}
   */
  ClassificationSummary getClassificationSummary();

  /**
   * Sets the classification summary of the TaskPatch.
   *
   * @param classificationSummary the classification summary of the TaskPatch
   */
  void setClassificationSummary(ClassificationSummary classificationSummary);

  /**
   * Returns the number of {@linkplain Task Tasks} that are grouped together with the {@linkplain
   * Task} by a {@linkplain io.kadai.task.api.TaskQuery}. It's only not NULL when using {@linkplain
   * io.kadai.task.api.TaskQuery#groupByPor()} or {@linkplain
   * io.kadai.task.api.TaskQuery#groupBySor(String)}.
   *
   * @return the number of {@linkplain Task Tasks} grouped together with the {@linkplain Task}
   */
  Integer getGroupByCount();

  /**
   * Sets the number of Tasks grouped together with the task.
   *
   * @param groupByCount the number of Tasks grouped together
   */
  void setGroupByCount(Integer groupByCount);

  /**
   * Returns the {@linkplain WorkbasketSummary} of the {@linkplain TaskPatch}.
   *
   * @return {@linkplain WorkbasketSummary}
   */
  WorkbasketSummary getWorkbasketSummary();

  /**
   * Sets the workbasket summary of the TaskPatch.
   *
   * @param workbasketSummary the workbasket summary of the TaskPatch
   */
  void setWorkbasketSummary(WorkbasketSummary workbasketSummary);

  /**
   * Returns the businessProcessId of the {@linkplain TaskPatch}.
   *
   * @return businessProcessId
   */
  String getBusinessProcessId();

  /**
   * Sets the associated businessProcessId.
   *
   * @param businessProcessId Sets the businessProcessId the Task belongs to.
   */
  void setBusinessProcessId(String businessProcessId);

  /**
   * Returns the parentBusinessProcessId of the {@linkplain TaskPatch}.
   *
   * @return parentBusinessProcessId
   */
  String getParentBusinessProcessId();

  /**
   * Sets the parentBusinessProcessId. ParentBusinessProcessId is needed to group associated
   * processes and to identify the main process.
   *
   * @param parentBusinessProcessId the business process id of the parent the Task belongs to
   */
  void setParentBusinessProcessId(String parentBusinessProcessId);

  /**
   * Returns the owner.
   *
   * @return owner
   */
  String getOwner();

  /**
   * Sets the owner of the Task.
   *
   * @param owner the owner of the Task
   */
  void setOwner(String owner);

  /**
   * Returns long name of the owner of the {@linkplain TaskPatch}.
   *
   * @return the long name of the owner
   */
  String getOwnerLongName();

  /**
   * Sets the long name of the owner of the TaskPatch.
   *
   * @param ownerLongName the long name of the owner of the TaskPatch
   */
  void setOwnerLongName(String ownerLongName);

  /**
   * Returns the primary {@linkplain ObjectReference} of the {@linkplain TaskPatch}.
   *
   * @return the Tasks primary {@linkplain ObjectReference}
   */
  ObjectReference getPrimaryObjRef();

  /**
   * Sets the {@linkplain ObjectReference primaryObjectReference} of the TaskPatch.
   *
   * @param primaryObjRef to Task main-subject
   */
  void setPrimaryObjRef(ObjectReference primaryObjRef);

  /**
   * Returns the priority of the {@linkplain Task}.
   *
   * @return priority
   */
  Integer getPriority();

  /**
   * Sets the priority of the TaskPatch.
   *
   * @param priority priority of TaskPatch
   */
  void setPriority(Integer priority);

  /**
   * Gets the manualPriority of the {@linkplain TaskPatch}. If the value of manualPriority is zero
   * or greater, the priority is automatically set to manualPriority. In this case, all computations
   * of priority are disabled. If the value of manualPriority is negative, Tasks are not prioritized
   * manually.
   *
   * @return the manualPriority of the TaskPatch
   */
  Integer getManualPriority();

  /**
   * Sets the manualPriority of the TaskPatch. If the value of manualPriority is zero or greater,
   * the priority is automatically set to manualPriority. In this case, all computations of priority
   * are disabled. If the value of manualPriority is negative, Tasks are not prioritized manually.
   *
   * @param manualPriority the value for manualPriority of the Task
   */
  void setManualPriority(Integer manualPriority);

  /**
   * Returns the count of the comments of the {@linkplain TaskPatch}.
   *
   * @return numberOfComments
   */
  Integer getNumberOfComments();

  /**
   * Sets the number of comments of the TaskPatch.
   *
   * @param numberOfComments the number of comments
   */
  void setNumberOfComments(Integer numberOfComments);

  /**
   * Returns the isRead flag of the {@linkplain TaskPatch}.
   *
   * @return the Tasks isRead flag
   */
  Boolean isRead();

  /**
   * Sets the isRead flag of the TaskPatch.
   *
   * @param isRead isRead flag of the TaskPatch
   */
  void setIsRead(Boolean isRead);

  /**
   * Returns the isTransferred flag of the {@linkplain TaskPatch}.
   *
   * @return the Tasks isTransferred flag
   */
  Boolean isTransferred();

  /**
   * Sets the isTransferred flag of the TaskPatch.
   *
   * @param isTransferred isTransferred flag of the TaskPatch
   */
  void setIsTransferred(Boolean isTransferred);

  /**
   * Returns whether the {@linkplain Task} has been previously reopened.
   *
   * @return true, if Tasks has been reopened before
   */
  Boolean isReopened();

  /**
   * Sets the isReopened flag of the TaskPatch.
   *
   * @param isReopened isReopened flag of the TaskPatch
   */
  void setIsReopened(Boolean isReopened);

  /**
   * Returns the {@linkplain ObjectReference secondaryObjectReferences} of the {@linkplain
   * TaskPatch}.
   *
   * @return {@linkplain ObjectReference secondaryObjectReferences}
   */
  List<ObjectReference> getSecondaryObjectReferences();

  /**
   * Sets the secondary object references of the TaskPatch.
   *
   * @param secondaryObjectReferences list of secondary object references of the TaskPatch
   */
  void setSecondaryObjectReferences(List<ObjectReference> secondaryObjectReferences);

  /**
   * Gets the value of the user-defined custom field 1.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom1();

  /**
   * Sets the value of the user-defined custom field 1.
   *
   * @param custom1 the value to set, may be {@code null}
   */
  void setCustom1(String custom1);

  /**
   * Gets the value of the user-defined custom field 2.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom2();

  /**
   * Sets the value of the user-defined custom field 2.
   *
   * @param custom2 the value to set, may be {@code null}
   */
  void setCustom2(String custom2);

  /**
   * Gets the value of the user-defined custom field 3.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom3();

  /**
   * Sets the value of the user-defined custom field 3.
   *
   * @param custom3 the value to set, may be {@code null}
   */
  void setCustom3(String custom3);

  /**
   * Gets the value of the user-defined custom field 4.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom4();

  /**
   * Sets the value of the user-defined custom field 4.
   *
   * @param custom4 the value to set, may be {@code null}
   */
  void setCustom4(String custom4);

  /**
   * Gets the value of the user-defined custom field 5.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom5();

  /**
   * Sets the value of the user-defined custom field 5.
   *
   * @param custom5 the value to set, may be {@code null}
   */
  void setCustom5(String custom5);

  /**
   * Gets the value of the user-defined custom field 6.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom6();

  /**
   * Sets the value of the user-defined custom field 6.
   *
   * @param custom6 the value to set, may be {@code null}
   */
  void setCustom6(String custom6);

  /**
   * Gets the value of the user-defined custom field 7.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom7();

  /**
   * Sets the value of the user-defined custom field 7.
   *
   * @param custom7 the value to set, may be {@code null}
   */
  void setCustom7(String custom7);

  /**
   * Gets the value of the user-defined custom field 8.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom8();

  /**
   * Sets the value of the user-defined custom field 8.
   *
   * @param custom8 the value to set, may be {@code null}
   */
  void setCustom8(String custom8);

  /**
   * Gets the value of the user-defined custom field 9.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom9();

  /**
   * Sets the value of the user-defined custom field 9.
   *
   * @param custom9 the value to set, may be {@code null}
   */
  void setCustom9(String custom9);

  /**
   * Gets the value of the user-defined custom field 10.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom10();

  /**
   * Sets the value of the user-defined custom field 10.
   *
   * @param custom10 the value to set, may be {@code null}
   */
  void setCustom10(String custom10);

  /**
   * Gets the value of the user-defined custom field 11.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom11();

  /**
   * Sets the value of the user-defined custom field 11.
   *
   * @param custom11 the value to set, may be {@code null}
   */
  void setCustom11(String custom11);

  /**
   * Gets the value of the user-defined custom field 12.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom12();

  /**
   * Sets the value of the user-defined custom field 12.
   *
   * @param custom12 the value to set, may be {@code null}
   */
  void setCustom12(String custom12);

  /**
   * Gets the value of the user-defined custom field 13.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom13();

  /**
   * Sets the value of the user-defined custom field 13.
   *
   * @param custom13 the value to set, may be {@code null}
   */
  void setCustom13(String custom13);

  /**
   * Gets the value of the user-defined custom field 14.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom14();

  /**
   * Sets the value of the user-defined custom field 14.
   *
   * @param custom14 the value to set, may be {@code null}
   */
  void setCustom14(String custom14);

  /**
   * Gets the value of the user-defined custom field 15.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom15();

  /**
   * Sets the value of the user-defined custom field 15.
   *
   * @param custom15 the value to set, may be {@code null}
   */
  void setCustom15(String custom15);

  /**
   * Gets the value of the user-defined custom field 16.
   *
   * @return the value of the custom field, or {@code null} if not set
   */
  String getCustom16();

  /**
   * Sets the value of the user-defined custom field 16.
   *
   * @param custom16 the value to set, may be {@code null}
   */
  void setCustom16(String custom16);

  /**
   * Gets the value of the user-defined integer custom field 1.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt1();

  /**
   * Sets the value of the user-defined integer custom field 1.
   *
   * @param customInt1 the integer value to set, may be {@code null}
   */
  void setCustomInt1(Integer customInt1);

  /**
   * Gets the value of the user-defined integer custom field 2.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt2();

  /**
   * Sets the value of the user-defined integer custom field 2.
   *
   * @param customInt2 the integer value to set, may be {@code null}
   */
  void setCustomInt2(Integer customInt2);

  /**
   * Gets the value of the user-defined integer custom field 3.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt3();

  /**
   * Sets the value of the user-defined integer custom field 3.
   *
   * @param customInt3 the integer value to set, may be {@code null}
   */
  void setCustomInt3(Integer customInt3);

  /**
   * Gets the value of the user-defined integer custom field 4.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt4();

  /**
   * Sets the value of the user-defined integer custom field 4.
   *
   * @param customInt4 the integer value to set, may be {@code null}
   */
  void setCustomInt4(Integer customInt4);

  /**
   * Gets the value of the user-defined integer custom field 5.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt5();

  /**
   * Sets the value of the user-defined integer custom field 5.
   *
   * @param customInt5 the integer value to set, may be {@code null}
   */
  void setCustomInt5(Integer customInt5);

  /**
   * Gets the value of the user-defined integer custom field 6.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt6();

  /**
   * Sets the value of the user-defined integer custom field 6.
   *
   * @param customInt6 the integer value to set, may be {@code null}
   */
  void setCustomInt6(Integer customInt6);

  /**
   * Gets the value of the user-defined integer custom field 7.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt7();

  /**
   * Sets the value of the user-defined integer custom field 7.
   *
   * @param customInt7 the integer value to set, may be {@code null}
   */
  void setCustomInt7(Integer customInt7);

  /**
   * Gets the value of the user-defined integer custom field 8.
   *
   * @return the integer value of the custom field, or {@code null} if not set
   */
  Integer getCustomInt8();

  /**
   * Sets the value of the user-defined integer custom field 8.
   *
   * @param customInt8 the integer value to set, may be {@code null}
   */
  void setCustomInt8(Integer customInt8);

  /**
   * Returns a Map of customAttributes.
   *
   * @return customAttributes as Map
   */
  Map<String, String> getCustomAttributes();

  /**
   * Sets the custom attribute map of the TaskPatch.
   *
   * @param customAttributes the map of custom attributes
   */
  void setCustomAttributes(Map<String, String> customAttributes);

  /**
   * Returns the callbackInfo.
   *
   * @return callbackInfo as Map
   */
  Map<String, String> getCallbackInfo();

  /**
   * Sets the callback info of the TaskPatch.
   *
   * @param callbackInfo the callbackInfo of the TaskPatch
   */
  void setCallbackInfo(Map<String, String> callbackInfo);

  /**
   * Returns the callbackState.
   *
   * @return callbackState
   */
  CallbackState getCallbackState();

  /**
   * Sets the callback state of the TaskPatch.
   *
   * @param callbackState the callbackState of the TaskPatch
   */
  void setCallbackState(CallbackState callbackState);

  /**
   * Return the {@linkplain Attachment attachment} for the TaskPatch. <br>
   * Do not use List.add()/addAll() for adding elements, because it can cause redundant data. Use
   * addAttachment(). Clear() and remove() can be used, because it's a controllable change.
   *
   * @return the List of {@linkplain Attachment attachments} for this TaskPatch
   */
  List<Attachment> getAttachments();

  /**
   * Sets the attachments of the TaskPatch.
   *
   * @param attachments the attachments of the TaskPatch
   */
  void setAttachments(List<Attachment> attachments);
}
