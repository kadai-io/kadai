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

package io.kadai.task.api;

import io.kadai.common.api.BaseQuery;
import io.kadai.task.api.models.ObjectReference;

/** ObjectReferenceQuery for generating dynamic sql. */
public interface ObjectReferenceQuery
    extends BaseQuery<ObjectReference, ObjectReferenceQueryColumnName> {

  /**
   * Add your company to your query.
   *
   * @param companies as Strings
   * @return the query
   */
  ObjectReferenceQuery companyIn(String... companies);

  /**
   * Add your system to your query.
   *
   * @param systems as Strings
   * @return the query
   */
  ObjectReferenceQuery systemIn(String... systems);

  /**
   * Add your systemInstance to your query.
   *
   * @param systemInstances as Strings
   * @return the query
   */
  ObjectReferenceQuery systemInstanceIn(String... systemInstances);

  /**
   * Add your type to your query.
   *
   * @param types as Strings
   * @return the query
   */
  ObjectReferenceQuery typeIn(String... types);

  /**
   * Add your value to your query.
   *
   * @param values as Strings
   * @return the query
   */
  ObjectReferenceQuery valueIn(String... values);
}
