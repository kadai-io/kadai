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

package io.kadai.routing.dmn.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

/** Model class for a routing upload result. */
public class RoutingUploadResultRepresentationModel
    extends RepresentationModel<RoutingUploadResultRepresentationModel> {

  @Schema(
      name = "amountOfImportedRows",
      description = "The total amount of imported rows from the provided excel sheet.")
  protected int amountOfImportedRows;

  @Schema(
      name = "result",
      description = "A human readable String that contains the amount of imported rows.")
  protected String result;

  public int getAmountOfImportedRows() {
    return amountOfImportedRows;
  }

  public void setAmountOfImportedRows(int amountOfImportedRows) {
    this.amountOfImportedRows = amountOfImportedRows;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }
}
