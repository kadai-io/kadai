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

import io.kadai.classification.internal.models.ClassificationImpl;
import java.time.Instant;

class ClassificationTestImpl extends ClassificationImpl {

  private boolean freezeCreated = false;
  private boolean freezeModified = false;

  @Override
  public void setCreated(Instant created) {
    if (!freezeCreated) {
      super.setCreated(created);
    }
  }

  public void setCreatedIgnoreFreeze(Instant created) {
    super.setCreated(created);
  }

  @Override
  public void setModified(Instant modified) {
    if (!freezeModified) {
      super.setModified(modified);
    }
  }

  public void setModifiedIgnoreFreeze(Instant modified) {
    super.setModified(modified);
  }

  public void freezeCreated() {
    freezeCreated = true;
  }

  public void unfreezeCreated() {
    freezeCreated = false;
  }

  public void freezeModified() {
    freezeModified = true;
  }

  public void unfreezeModified() {
    freezeModified = false;
  }
}
