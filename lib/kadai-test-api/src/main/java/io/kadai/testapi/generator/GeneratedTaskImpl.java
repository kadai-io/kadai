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

package io.kadai.testapi.generator;

import io.kadai.task.api.TaskState;
import io.kadai.task.internal.models.TaskImpl;
import java.time.Instant;
import java.util.Objects;

final class GeneratedTaskImpl extends TaskImpl {
  private boolean freezeState;
  private boolean freezeCreated;
  private boolean freezeModified;
  private boolean freezeRead;
  private boolean freezeTransferred;
  private boolean freezeReopened;
  private boolean freezePriority;

  @Override
  public void setState(TaskState state) {
    if (!freezeState) {
      super.setState(state);
    }
  }

  @Override
  public void setCreated(Instant created) {
    if (!freezeCreated) {
      super.setCreated(created);
    }
  }

  @Override
  public void setModified(Instant modified) {
    if (!freezeModified) {
      super.setModified(modified);
    }
  }

  @Override
  public void setRead(boolean isRead) {
    if (!freezeRead) {
      super.setRead(isRead);
    }
  }

  @Override
  public void setTransferred(boolean isTransferred) {
    if (!freezeTransferred) {
      super.setTransferred(isTransferred);
    }
  }

  @Override
  public void setReopened(boolean isReopened) {
    if (!freezeReopened) {
      super.setReopened(isReopened);
    }
  }

  @Override
  public void setPriority(int priority) {
    if (!freezePriority) {
      super.setPriority(priority);
    }
  }

  void setStateIgnoreFreeze(TaskState state) {
    super.setState(state);
  }

  void setCreatedIgnoreFreeze(Instant created) {
    super.setCreated(created);
  }

  void setModifiedIgnoreFreeze(Instant modified) {
    super.setModified(modified);
  }

  void setReadIgnoreFreeze(boolean isRead) {
    super.setRead(isRead);
  }

  void setTransferredIgnoreFreeze(boolean isTransferred) {
    super.setTransferred(isTransferred);
  }

  void setReopenedIgnoreFreeze(boolean isReopened) {
    super.setReopened(isReopened);
  }

  void setPriorityIgnoreFreeze(int priority) {
    super.setPriority(priority);
  }

  void freezeState() {
    freezeState = true;
  }

  void freezeCreated() {
    freezeCreated = true;
  }

  void freezeModified() {
    freezeModified = true;
  }

  void freezeRead() {
    freezeRead = true;
  }

  void freezeTransferred() {
    freezeTransferred = true;
  }

  void freezeReopened() {
    freezeReopened = true;
  }

  void freezePriority() {
    freezePriority = true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        freezeState,
        freezeCreated,
        freezeModified,
        freezeRead,
        freezeTransferred,
        freezeReopened,
        freezePriority);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    if (!super.equals(obj)) return false;
    GeneratedTaskImpl other = (GeneratedTaskImpl) obj;
    return freezeState == other.freezeState
        && freezeCreated == other.freezeCreated
        && freezeModified == other.freezeModified
        && freezeRead == other.freezeRead
        && freezeTransferred == other.freezeTransferred
        && freezeReopened == other.freezeReopened
        && freezePriority == other.freezePriority;
  }
}
