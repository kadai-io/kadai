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

package io.kadai.monitor.api.reports.item;

import io.kadai.monitor.api.reports.Report;

/**
 * A QueryItem is en entity on which a {@linkplain Report} is based on. It represents the content of
 * a cell in the {@linkplain Report}.
 */
public interface QueryItem {

  /**
   * The key of a QueryItem determines its {@linkplain io.kadai.monitor.api.reports.row.Row row}
   * within a {@linkplain Report}.
   *
   * @return the key of this QueryItem.
   */
  String getKey();

  /**
   * Its value will be added to the existing cell value during the insertion into a {@linkplain
   * Report}.
   *
   * @return the value
   */
  int getValue();
}
