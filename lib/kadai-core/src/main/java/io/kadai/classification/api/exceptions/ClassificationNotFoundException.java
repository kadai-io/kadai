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

package io.kadai.classification.api.exceptions;

import io.kadai.classification.api.models.Classification;
import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import java.util.Map;

/** Thrown if a specific {@linkplain Classification} is not in the database. */
public class ClassificationNotFoundException extends KadaiException {

  public static final String ERROR_KEY_ID = "CLASSIFICATION_WITH_ID_NOT_FOUND";
  public static final String ERROR_KEY_KEY_DOMAIN = "CLASSIFICATION_WITH_KEY_NOT_FOUND";
  private final String classificationId;
  private final String classificationKey;
  private final String domain;

  public ClassificationNotFoundException(String classificationId) {
    super(
        String.format("Classification with id '%s' wasn't found", classificationId),
        ErrorCode.of(
            ERROR_KEY_ID, Map.of("classificationId", ensureNullIsHandled(classificationId))));
    this.classificationId = classificationId;
    classificationKey = null;
    domain = null;
  }

  public ClassificationNotFoundException(String key, String domain) {
    super(
        String.format(
            "Classification with key '%s' and domain '%s' could not be found", key, domain),
        ErrorCode.of(
            ERROR_KEY_KEY_DOMAIN,
            Map.ofEntries(
                Map.entry("classificationKey", ensureNullIsHandled(key)),
                Map.entry("domain", ensureNullIsHandled(domain)))));
    this.classificationKey = key;
    this.domain = domain;
    classificationId = null;
  }

  public String getClassificationKey() {
    return classificationKey;
  }

  public String getDomain() {
    return domain;
  }

  public String getClassificationId() {
    return classificationId;
  }
}
