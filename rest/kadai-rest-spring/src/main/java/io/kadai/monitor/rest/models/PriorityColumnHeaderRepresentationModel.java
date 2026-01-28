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

package io.kadai.monitor.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.ConstructorProperties;
import org.springframework.hateoas.RepresentationModel;

@JsonIgnoreProperties("links")
public class PriorityColumnHeaderRepresentationModel
    extends RepresentationModel<PriorityColumnHeaderRepresentationModel> {

  @Schema(
      name = "lowerBound",
      description = "Determine the lower priority for this column header. This value is inclusive.")
  private final int lowerBound;

  @Schema(
      name = "upperBound",
      description = "Determine the upper priority for this column header. This value is inclusive.")
  private final int upperBound;

  @ConstructorProperties({"lowerBound", "upperBound"})
  public PriorityColumnHeaderRepresentationModel(int lowerBound, int upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  public int getLowerBound() {
    return lowerBound;
  }

  public int getUpperBound() {
    return upperBound;
  }
}
