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

package io.kadai.task.rest.assembler;

import io.kadai.common.api.BulkOperationResults;
import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.task.rest.models.BulkOperationResultsRepresentationModel;
import java.util.HashMap;
import java.util.Map;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BulkOperationResultsRepresentationModelAssembler
    implements RepresentationModelAssembler<
        BulkOperationResults<String, KadaiException>, BulkOperationResultsRepresentationModel> {

  @NonNull
  @Override
  public BulkOperationResultsRepresentationModel toModel(
      BulkOperationResults<String, KadaiException> entity) {
    BulkOperationResultsRepresentationModel repModel =
        new BulkOperationResultsRepresentationModel();
    Map<String, ErrorCode> newErrorMap = new HashMap<>();
    for (Map.Entry<String, KadaiException> entry : entity.getErrorMap().entrySet()) {
      newErrorMap.put(entry.getKey(), entry.getValue().getErrorCode());
    }
    repModel.setTasksWithErrors(newErrorMap);
    return repModel;
  }
}
