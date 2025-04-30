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

package io.kadai.common.rest.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.beans.ConstructorProperties;
import java.util.Map;
import org.springframework.hateoas.RepresentationModel;

public class CustomAttributesRepresentationModel
    extends RepresentationModel<CustomAttributesRepresentationModel> {

  @Schema(name = "customAttributes", description = "The custom configuration attributes.")
  private final Map<String, Object> customAttributes;

  @ConstructorProperties({"customAttributes"})
  public CustomAttributesRepresentationModel(Map<String, Object> customAttributes) {
    this.customAttributes = customAttributes;
  }

  public Map<String, Object> getCustomAttributes() {
    return customAttributes;
  }
}
