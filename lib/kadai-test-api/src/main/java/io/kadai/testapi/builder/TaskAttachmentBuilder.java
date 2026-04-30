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

package io.kadai.testapi.builder;

import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.task.api.models.Attachment;
import io.kadai.task.api.models.ObjectReference;
import io.kadai.task.internal.models.AttachmentImpl;
import java.time.Instant;
import java.util.Map;

public class TaskAttachmentBuilder implements Builder<Attachment> {

  private final AttachmentImpl attachment = new AttachmentImpl();

  public static TaskAttachmentBuilder newAttachment() {
    return new TaskAttachmentBuilder();
  }

  public TaskAttachmentBuilder created(Instant created) {
    attachment.setCreated(created);
    return this;
  }

  public TaskAttachmentBuilder modified(Instant modified) {
    attachment.setModified(modified);
    return this;
  }

  public TaskAttachmentBuilder received(Instant received) {
    attachment.setReceived(received);
    return this;
  }

  public TaskAttachmentBuilder classificationSummary(ClassificationSummary classificationSummary) {
    attachment.setClassificationSummary(classificationSummary);
    return this;
  }

  public TaskAttachmentBuilder objectReference(ObjectReference objectReference) {
    attachment.setObjectReference(objectReference);
    return this;
  }

  public TaskAttachmentBuilder channel(String channel) {
    attachment.setChannel(channel);
    return this;
  }

  public TaskAttachmentBuilder customAttributes(Map<String, String> customAttributes) {
    attachment.setCustomAttributes(customAttributes);
    return this;
  }

  @Override
  public Attachment build() {
    AttachmentImpl a = attachment.copy();
    a.setTaskId(attachment.getTaskId());
    return a;
  }
}
