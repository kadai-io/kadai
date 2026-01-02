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

package io.kadai.workbasket.rest.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "EntityModel class for Workbasket")
public class WorkbasketRepresentationModel extends WorkbasketSummaryRepresentationModel {

  @Schema(
      name = "created",
      description =
          "The creation timestamp of the workbasket in the system. The format is ISO-8601.")
  private Instant created;

  @Schema(
      name = "modified",
      description = "The timestamp of the last modification. The format is ISO-8601.")
  private Instant modified;

  public Instant getCreated() {
    return created;
  }

  public void setCreated(Instant created) {
    this.created = created;
  }

  public Instant getModified() {
    return modified;
  }

  public void setModified(Instant modified) {
    this.modified = modified;
  }
}
