package io.kadai.monitor.api.reports.row;

import io.kadai.monitor.api.reports.WorkbasketPriorityReport;
import io.kadai.monitor.api.reports.item.DetailedPriorityQueryItem;

/**
 * Represents a single Row inside {@linkplain
 * WorkbasketPriorityReport.DetailedWorkbasketPriorityReport}. The collapsing criteria is the key of
 * each {@linkplain DetailedPriorityQueryItem}.
 */
public class DetailedWorkbasketPriorityRow extends FoldableRow<DetailedPriorityQueryItem> {
  public DetailedWorkbasketPriorityRow(String key, int columnSize) {
    super(
        key,
        columnSize,
        item -> item.getClassificationKey() != null ? item.getClassificationKey() : "N/A");
  }

  @Override
  public SingleRow<DetailedPriorityQueryItem> getFoldableRow(String key) {
    return (SingleRow<DetailedPriorityQueryItem>) super.getFoldableRow(key);
  }

  @Override
  protected Row<DetailedPriorityQueryItem> buildRow(String key, int columnSize) {
    return new SingleRow<>(key, columnSize);
  }
}
