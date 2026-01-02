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

package io.kadai.monitor.api.reports.header;

import io.kadai.monitor.api.reports.Report;
import io.kadai.monitor.api.reports.item.QueryItem;

/**
 * A ColumnHeader is an element of a {@linkplain Report}. It determines weather a given &lt;Item&gt;
 * belongs into the representing column.
 *
 * @param <I> {@linkplain QueryItem} on which the {@linkplain Report} is based on.
 */
public interface ColumnHeader<I extends QueryItem> {

  /**
   * The display name is the string representation of this column. Used to give this column a name
   * during presentation.
   *
   * @return String representation of this column.
   */
  String getDisplayName();

  /**
   * Determines if a specific item is meant part of this column.
   *
   * @param item the given item to check.
   * @return True, if the item is supposed to be part of this column. Otherwise false.
   */
  boolean fits(I item);
}
