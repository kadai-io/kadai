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

package io.kadai.task.rest;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.rest.QueryParameterValidation;
import io.kadai.task.api.TaskQuery;
import java.util.Arrays;
import java.util.Collections;

public class TaskQueryFilterCustomIntFieldsValidation
    implements QueryParameterValidation<TaskQuery, Void, TaskQueryFilterCustomIntFields> {

  @Override
  public void validate(TaskQueryFilterCustomIntFields query) throws InvalidArgumentException {
    validateParameter(query.getCustomInt1Within(), "custom-int-1-within");
    validateParameter(query.getCustomInt2Within(), "custom-int-2-within");
    validateParameter(query.getCustomInt3Within(), "custom-int-3-within");
    validateParameter(query.getCustomInt4Within(), "custom-int-4-within");
    validateParameter(query.getCustomInt5Within(), "custom-int-5-within");
    validateParameter(query.getCustomInt6Within(), "custom-int-6-within");
    validateParameter(query.getCustomInt7Within(), "custom-int-7-within");
    validateParameter(query.getCustomInt8Within(), "custom-int-8-within");
    validateParameter(query.getCustomInt1NotWithin(), "custom-int-1-not-within");
    validateParameter(query.getCustomInt2NotWithin(), "custom-int-2-not-within");
    validateParameter(query.getCustomInt3NotWithin(), "custom-int-3-not-within");
    validateParameter(query.getCustomInt4NotWithin(), "custom-int-4-not-within");
    validateParameter(query.getCustomInt5NotWithin(), "custom-int-5-not-within");
    validateParameter(query.getCustomInt6NotWithin(), "custom-int-6-not-within");
    validateParameter(query.getCustomInt7NotWithin(), "custom-int-7-not-within");
    validateParameter(query.getCustomInt8NotWithin(), "custom-int-8-not-within");

    validateParameterCombination(
        query.getCustomInt1Within(),
        query.getCustomInt1From(),
        query.getCustomInt1To(),
        "custom-int-1-within",
        "custom-int-1-from",
        "custom-int-1-to");
    validateParameterCombination(
        query.getCustomInt1NotWithin(),
        query.getCustomInt1FromNot(),
        query.getCustomInt1ToNot(),
        "custom-int-1-not-within",
        "custom-int-1-from-not",
        "custom-int-1-to-not");
    validateParameterCombination(
        query.getCustomInt2Within(),
        query.getCustomInt2From(),
        query.getCustomInt2To(),
        "custom-int-2-within",
        "custom-int-2-from",
        "custom-int-2-to");
    validateParameterCombination(
        query.getCustomInt2NotWithin(),
        query.getCustomInt2FromNot(),
        query.getCustomInt2ToNot(),
        "custom-int-2-not-within",
        "custom-int-2-from-not",
        "custom-int-2-to-not");
    validateParameterCombination(
        query.getCustomInt3Within(),
        query.getCustomInt3From(),
        query.getCustomInt3To(),
        "custom-int-3-within",
        "custom-int-3-from",
        "custom-int-3-to");
    validateParameterCombination(
        query.getCustomInt3NotWithin(),
        query.getCustomInt3FromNot(),
        query.getCustomInt3ToNot(),
        "custom-int-3-not-within",
        "custom-int-3-from-not",
        "custom-int-3-to-not");
    validateParameterCombination(
        query.getCustomInt4Within(),
        query.getCustomInt4From(),
        query.getCustomInt4To(),
        "custom-int-4-within",
        "custom-int-4-from",
        "custom-int-4-to");
    validateParameterCombination(
        query.getCustomInt4NotWithin(),
        query.getCustomInt4FromNot(),
        query.getCustomInt4ToNot(),
        "custom-int-4-not-within",
        "custom-int-4-from-not",
        "custom-int-4-to-not");
    validateParameterCombination(
        query.getCustomInt5Within(),
        query.getCustomInt5From(),
        query.getCustomInt5To(),
        "custom-int-5-within",
        "custom-int-5-from",
        "custom-int-5-to");
    validateParameterCombination(
        query.getCustomInt5NotWithin(),
        query.getCustomInt5FromNot(),
        query.getCustomInt5ToNot(),
        "custom-int-5-not-within",
        "custom-int-5-from-not",
        "custom-int-5-to-not");
    validateParameterCombination(
        query.getCustomInt6Within(),
        query.getCustomInt6From(),
        query.getCustomInt6To(),
        "custom-int-6-within",
        "custom-int-6-from",
        "custom-int-6-to");
    validateParameterCombination(
        query.getCustomInt6NotWithin(),
        query.getCustomInt6FromNot(),
        query.getCustomInt6ToNot(),
        "custom-int-6-not-within",
        "custom-int-6-from-not",
        "custom-int-6-to-not");
    validateParameterCombination(
        query.getCustomInt7Within(),
        query.getCustomInt7From(),
        query.getCustomInt7To(),
        "custom-int-7-within",
        "custom-int-7-from",
        "custom-int-7-to");
    validateParameterCombination(
        query.getCustomInt7NotWithin(),
        query.getCustomInt7FromNot(),
        query.getCustomInt7ToNot(),
        "custom-int-7-not-within",
        "custom-int-7-from-not",
        "custom-int-7-to-not");
    validateParameterCombination(
        query.getCustomInt8Within(),
        query.getCustomInt8From(),
        query.getCustomInt8To(),
        "custom-int-8-within",
        "custom-int-8-from",
        "custom-int-8-to");
    validateParameterCombination(
        query.getCustomInt8NotWithin(),
        query.getCustomInt8FromNot(),
        query.getCustomInt8ToNot(),
        "custom-int-8-not-within",
        "custom-int-8-from-not",
        "custom-int-8-to-not");
  }

  private void validateParameterCombination(
      Integer[] paramWithin,
      Integer paramFrom,
      Integer paramTo,
      String paramWithinName,
      String paramFromName,
      String paramToName) {
    if (paramWithin != null && (paramFrom != null || paramTo != null)) {
      throw new InvalidArgumentException(
          "It is prohibited to use the param '"
              + paramWithinName
              + "' in combination "
              + "with the params '"
              + paramFromName
              + "'  and / or '"
              + paramToName
              + "'");
    }
  }

  private void validateParameter(Integer[] parameter, String parameterName) {
    validateLengthDividableByTwo(parameter, parameterName);
    validateIntervals(parameter, parameterName);
  }

  private void validateLengthDividableByTwo(Integer[] parameter, String parameterName) {
    if (parameter != null && parameter.length % 2 != 0) {
      throw new InvalidArgumentException(
          "provided length of the property '" + parameterName + "' is not dividable by 2");
    }
  }

  private void validateIntervals(Integer[] parameter, String parameterName) {
    if (parameter != null
        && (Collections.indexOfSubList(
                    Arrays.asList(parameter), Collections.nCopies(2, (Integer) null))
                % 2
            == 0)) {
      throw new InvalidArgumentException(
          "Each interval in '" + parameterName + "' shouldn't consist of two 'null' values");
    }
  }
}
