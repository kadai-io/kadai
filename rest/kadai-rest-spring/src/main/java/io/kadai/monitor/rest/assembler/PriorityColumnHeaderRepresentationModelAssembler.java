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

package io.kadai.monitor.rest.assembler;

import io.kadai.monitor.api.reports.header.PriorityColumnHeader;
import io.kadai.monitor.rest.models.PriorityColumnHeaderRepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PriorityColumnHeaderRepresentationModelAssembler
    implements RepresentationModelAssembler<
        PriorityColumnHeader, PriorityColumnHeaderRepresentationModel> {

  @Override
  @NonNull
  public PriorityColumnHeaderRepresentationModel toModel(@NonNull PriorityColumnHeader entity) {
    return new PriorityColumnHeaderRepresentationModel(
        entity.getLowerBoundInc(), entity.getUpperBoundInc());
  }

  public PriorityColumnHeader toEntityModel(PriorityColumnHeaderRepresentationModel repModel) {
    return new PriorityColumnHeader(repModel.getLowerBound(), repModel.getUpperBound());
  }
}
