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

package io.kadai.classification.api.exceptions;

import io.kadai.classification.api.models.Classification;
import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.KadaiException;
import io.kadai.task.api.models.Attachment;
import io.kadai.task.api.models.Task;
import java.util.Map;

/**
 * This exception is thrown when a specific {@linkplain Classification} was tried to be deleted
 * while still being in use. <br>
 * This could mean that there are either {@linkplain Task Tasks} or {@linkplain Attachment
 * Attachments} associated with it.
 */
public class ClassificationInUseException extends KadaiException {

  public static final String ERROR_KEY = "CLASSIFICATION_IN_USE";
  private final String classificationKey;
  private final String domain;

  public ClassificationInUseException(Classification classification, Throwable cause) {
    super(
        String.format(
            "The Classification with id = '%s' and key = '%s' in domain = '%s' "
                + "is in use and cannot be deleted. There are either Tasks or "
                + "Attachments associated with the Classification.",
            classification.getId(), classification.getKey(), classification.getDomain()),
        ErrorCode.of(
            ERROR_KEY,
            Map.ofEntries(
                Map.entry("classificationKey", ensureNullIsHandled(classification.getKey())),
                Map.entry("domain", ensureNullIsHandled(classification.getDomain())))),
        cause);
    classificationKey = classification.getKey();
    domain = classification.getDomain();
  }

  public String getClassificationKey() {
    return classificationKey;
  }

  public String getDomain() {
    return domain;
  }
}
