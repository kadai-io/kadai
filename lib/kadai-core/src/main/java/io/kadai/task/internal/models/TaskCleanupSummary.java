/*
 * Copyright [2024] [envite consulting GmbH]
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

package io.kadai.task.internal.models;

import java.util.Objects;

/**
 * A lightweight summary used by the TaskCleanupJob. Contains only the fields needed for cleanup
 * logic: task id and parent business process id.
 */
public class TaskCleanupSummary {

  private String taskId;
  private String parentBusinessProcessId;

  public TaskCleanupSummary() {}

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getParentBusinessProcessId() {
    return parentBusinessProcessId;
  }

  public void setParentBusinessProcessId(String parentBusinessProcessId) {
    this.parentBusinessProcessId = parentBusinessProcessId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(taskId, parentBusinessProcessId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TaskCleanupSummary)) {
      return false;
    }
    TaskCleanupSummary other = (TaskCleanupSummary) obj;
    return Objects.equals(taskId, other.taskId)
        && Objects.equals(parentBusinessProcessId, other.parentBusinessProcessId);
  }

  @Override
  public String toString() {
    return "TaskCleanupSummary [taskId="
        + taskId
        + ", parentBusinessProcessId="
        + parentBusinessProcessId
        + "]";
  }
}
