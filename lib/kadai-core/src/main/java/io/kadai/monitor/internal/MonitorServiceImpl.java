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

package io.kadai.monitor.internal;

import io.kadai.common.internal.InternalKadaiEngine;
import io.kadai.monitor.api.MonitorService;
import io.kadai.monitor.api.reports.ClassificationCategoryReport;
import io.kadai.monitor.api.reports.ClassificationReport;
import io.kadai.monitor.api.reports.TaskCustomFieldValueReport;
import io.kadai.monitor.api.reports.TaskStatusReport;
import io.kadai.monitor.api.reports.TimestampReport;
import io.kadai.monitor.api.reports.WorkbasketPriorityReport;
import io.kadai.monitor.api.reports.WorkbasketReport;
import io.kadai.monitor.internal.reports.ClassificationCategoryReportBuilderImpl;
import io.kadai.monitor.internal.reports.ClassificationReportBuilderImpl;
import io.kadai.monitor.internal.reports.TaskCustomFieldValueReportBuilderImpl;
import io.kadai.monitor.internal.reports.TaskStatusReportBuilderImpl;
import io.kadai.monitor.internal.reports.TimestampReportBuilderImpl;
import io.kadai.monitor.internal.reports.WorkbasketPriorityReportBuilderImpl;
import io.kadai.monitor.internal.reports.WorkbasketReportBuilderImpl;
import io.kadai.task.api.TaskCustomField;

/** This is the implementation of MonitorService. */
public class MonitorServiceImpl implements MonitorService {

  private final InternalKadaiEngine kadaiEngine;
  private final MonitorMapper monitorMapper;

  public MonitorServiceImpl(InternalKadaiEngine kadaiEngine, MonitorMapper monitorMapper) {
    super();
    this.kadaiEngine = kadaiEngine;
    this.monitorMapper = monitorMapper;
  }

  @Override
  public WorkbasketReport.Builder createWorkbasketReportBuilder() {
    return new WorkbasketReportBuilderImpl(kadaiEngine, monitorMapper);
  }

  @Override
  public WorkbasketPriorityReport.Builder createWorkbasketPriorityReportBuilder() {
    return new WorkbasketPriorityReportBuilderImpl(kadaiEngine, monitorMapper);
  }

  @Override
  public ClassificationCategoryReport.Builder createClassificationCategoryReportBuilder() {
    return new ClassificationCategoryReportBuilderImpl(kadaiEngine, monitorMapper);
  }

  @Override
  public ClassificationReport.Builder createClassificationReportBuilder() {
    return new ClassificationReportBuilderImpl(kadaiEngine, monitorMapper);
  }

  @Override
  public TaskCustomFieldValueReport.Builder createTaskCustomFieldValueReportBuilder(
      TaskCustomField taskCustomField) {
    return new TaskCustomFieldValueReportBuilderImpl(kadaiEngine, monitorMapper, taskCustomField);
  }

  @Override
  public TaskStatusReport.Builder createTaskStatusReportBuilder() {
    return new TaskStatusReportBuilderImpl(kadaiEngine, monitorMapper);
  }

  @Override
  public TimestampReport.Builder createTimestampReportBuilder() {
    return new TimestampReportBuilderImpl(kadaiEngine, monitorMapper);
  }
}
