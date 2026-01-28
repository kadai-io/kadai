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

package io.kadai.monitor.api.reports;

import io.kadai.common.api.TimeInterval;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.monitor.api.CombinedClassificationFilter;
import io.kadai.monitor.api.TaskTimestamp;
import io.kadai.monitor.api.reports.header.ColumnHeader;
import io.kadai.monitor.api.reports.header.TimeIntervalColumnHeader;
import io.kadai.monitor.api.reports.item.MonitorQueryItem;
import io.kadai.monitor.api.reports.row.Row;
import io.kadai.task.api.models.Task;
import io.kadai.workbasket.api.models.Workbasket;
import java.util.List;

/**
 * A WorkbasketReport aggregates {@linkplain Task} related data.
 *
 * <p>Each {@linkplain Row} represents a {@linkplain Workbasket}.
 *
 * <p>Each {@linkplain ColumnHeader} represents a {@linkplain TimeInterval}.
 */
public class WorkbasketReport extends Report<MonitorQueryItem, TimeIntervalColumnHeader> {

  public WorkbasketReport(List<TimeIntervalColumnHeader> timeIntervalColumnHeaders) {
    super(timeIntervalColumnHeaders, new String[] {"WORKBASKET"});
  }

  /** Builder for {@linkplain WorkbasketReport}. */
  public interface Builder
      extends TimeIntervalReportBuilder<Builder, MonitorQueryItem, TimeIntervalColumnHeader> {

    @Override
    WorkbasketReport buildReport() throws InvalidArgumentException, NotAuthorizedException;

    @Override
    WorkbasketReport buildReport(TaskTimestamp timestamp)
        throws InvalidArgumentException, NotAuthorizedException;

    /**
     * Adds a list of {@linkplain CombinedClassificationFilter} to the builder. The created report
     * contains only tasks with a pair of a classificationId for a task and a classificationId for
     * the corresponding attachment in this list.
     *
     * @param combinedClassificationFilter a list of combinedClassificationFilter
     * @return the WorkbasketReportBuilder
     */
    WorkbasketReport.Builder combinedClassificationFilterIn(
        List<CombinedClassificationFilter> combinedClassificationFilter);
  }
}
