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

package io.kadai.monitor.api.reports;

import io.kadai.common.api.TimeInterval;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.monitor.api.TaskTimestamp;
import io.kadai.monitor.api.reports.header.ColumnHeader;
import io.kadai.monitor.api.reports.header.TimeIntervalColumnHeader;
import io.kadai.monitor.api.reports.item.MonitorQueryItem;
import io.kadai.monitor.api.reports.row.Row;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.models.Task;
import java.util.List;

/**
 * A TaskCustomFieldValueReport aggregates {@linkplain Task} related data.
 *
 * <p>Each {@linkplain Row} represents a value of the requested {@linkplain TaskCustomField}.
 *
 * <p>Each {@linkplain ColumnHeader} represents a {@linkplain TimeInterval}.
 */
public class TaskCustomFieldValueReport extends Report<MonitorQueryItem, TimeIntervalColumnHeader> {

  public TaskCustomFieldValueReport(List<TimeIntervalColumnHeader> timeIntervalColumnHeaders) {
    super(timeIntervalColumnHeaders, new String[] {"TASK CUSTOM FIELDS"});
  }

  /** Builder for {@linkplain TaskCustomFieldValueReport}. */
  public interface Builder
      extends TimeIntervalReportBuilder<Builder, MonitorQueryItem, TimeIntervalColumnHeader> {

    @Override
    TaskCustomFieldValueReport buildReport()
        throws InvalidArgumentException, NotAuthorizedException;

    @Override
    TaskCustomFieldValueReport buildReport(TaskTimestamp timestamp)
        throws InvalidArgumentException, NotAuthorizedException;
  }
}
