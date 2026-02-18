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

package io.kadai.monitor.api.reports.row;

import io.kadai.monitor.api.reports.Report;
import io.kadai.monitor.api.reports.header.ColumnHeader;
import io.kadai.monitor.api.reports.item.QueryItem;
import java.util.Map;

/**
 * A SingleRow represents a single row in a {@linkplain Report}. It contains an array of cells whose
 * index corresponds to the {@linkplain ColumnHeader} index in the {@linkplain Report}.
 *
 * @param <I> {@linkplain QueryItem} on which the {@linkplain Report} is based on.
 */
public class SingleRow<I extends QueryItem> implements Row<I> {

  private final int[] cells;
  private final String key;
  private int total = 0;
  private String displayName;

  public SingleRow(String key, int columnCount) {
    this.key = key;
    this.displayName = key;
    cells = new int[columnCount];
  }

  @Override
  public void addItem(I item, int index) throws IndexOutOfBoundsException {
    total += item.getValue();
    cells[index] += item.getValue();
  }

  @Override
  public void updateTotalValue(I item) {
    total += item.getValue();
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public void setDisplayName(Map<String, String> displayMap) {
    displayName = displayMap.getOrDefault(key, key);
  }

  @Override
  public final int getTotalValue() {
    return total;
  }

  @Override
  public final int[] getCells() {
    return cells.clone();
  }
}
