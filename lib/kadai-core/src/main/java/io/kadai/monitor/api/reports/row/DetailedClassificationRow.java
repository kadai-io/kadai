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

package io.kadai.monitor.api.reports.row;

import io.kadai.monitor.api.reports.ClassificationReport;
import io.kadai.monitor.api.reports.item.DetailedMonitorQueryItem;

/**
 * Represents a single Row inside {@linkplain ClassificationReport.DetailedClassificationReport}.
 * The collapsing criteria is the attachement key of each {@linkplain DetailedMonitorQueryItem}.
 */
public class DetailedClassificationRow extends FoldableRow<DetailedMonitorQueryItem> {

  public DetailedClassificationRow(String key, int columnSize) {
    super(
        key, columnSize, item -> item.getAttachmentKey() != null ? item.getAttachmentKey() : "N/A");
  }

  @Override
  public SingleRow<DetailedMonitorQueryItem> getFoldableRow(String key) {
    return (SingleRow<DetailedMonitorQueryItem>) super.getFoldableRow(key);
  }

  @Override
  protected Row<DetailedMonitorQueryItem> buildRow(String key, int columnSize) {
    return new SingleRow<>(key, columnSize);
  }
}
