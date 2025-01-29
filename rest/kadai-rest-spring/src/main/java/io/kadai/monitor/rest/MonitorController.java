/*
 * Copyright [2024] [envite consulting GmbH]
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

package io.kadai.monitor.rest;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.monitor.api.MonitorService;
import io.kadai.monitor.api.TaskTimestamp;
import io.kadai.monitor.api.reports.ClassificationCategoryReport;
import io.kadai.monitor.api.reports.ClassificationReport;
import io.kadai.monitor.api.reports.TaskCustomFieldValueReport;
import io.kadai.monitor.api.reports.TaskStatusReport;
import io.kadai.monitor.api.reports.TimestampReport;
import io.kadai.monitor.api.reports.WorkbasketPriorityReport;
import io.kadai.monitor.api.reports.WorkbasketReport;
import io.kadai.monitor.api.reports.header.PriorityColumnHeader;
import io.kadai.monitor.rest.assembler.PriorityColumnHeaderRepresentationModelAssembler;
import io.kadai.monitor.rest.assembler.ReportRepresentationModelAssembler;
import io.kadai.monitor.rest.models.PriorityColumnHeaderRepresentationModel;
import io.kadai.monitor.rest.models.ReportRepresentationModel;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskState;
import io.kadai.workbasket.api.WorkbasketType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all monitoring endpoints. */
@RestController
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class MonitorController implements MonitorApi {

  private final MonitorService monitorService;

  private final ReportRepresentationModelAssembler reportRepresentationModelAssembler;
  private final PriorityColumnHeaderRepresentationModelAssembler
      priorityColumnHeaderRepresentationModelAssembler;

  @Autowired
  MonitorController(
      MonitorService monitorService,
      ReportRepresentationModelAssembler reportRepresentationModelAssembler,
      PriorityColumnHeaderRepresentationModelAssembler
          priorityColumnHeaderRepresentationModelAssembler) {
    this.monitorService = monitorService;
    this.reportRepresentationModelAssembler = reportRepresentationModelAssembler;
    this.priorityColumnHeaderRepresentationModelAssembler =
        priorityColumnHeaderRepresentationModelAssembler;
  }

  @GetMapping(path = RestEndpoints.URL_MONITOR_WORKBASKET_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ReportRepresentationModel> computeWorkbasketReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws NotAuthorizedException, InvalidArgumentException {
    if (taskTimestamp == null) {
      taskTimestamp = TaskTimestamp.DUE;
    }

    WorkbasketReport.Builder builder = monitorService.createWorkbasketReportBuilder();
    filterParameter.apply(builder);

    ReportRepresentationModel report =
        reportRepresentationModelAssembler.toModel(
            builder.buildReport(taskTimestamp), filterParameter, taskTimestamp);

    return ResponseEntity.status(HttpStatus.OK).body(report);
  }

  @GetMapping(path = RestEndpoints.URL_MONITOR_WORKBASKET_PRIORITY_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ReportRepresentationModel> computePriorityWorkbasketReport(
      @ParameterObject PriorityReportFilterParameter filterParameter,
      @RequestParam(name = "workbasket-type", required = false) WorkbasketType[] workbasketTypes,
      @RequestParam(name = "columnHeader", required = false)
          PriorityColumnHeaderRepresentationModel[] columnHeaders)
      throws NotAuthorizedException, InvalidArgumentException {

    WorkbasketPriorityReport.Builder builder =
        monitorService.createWorkbasketPriorityReportBuilder().workbasketTypeIn(workbasketTypes);
    filterParameter.apply(builder);

    if (columnHeaders != null) {
      List<PriorityColumnHeader> priorityColumnHeaders =
          Arrays.stream(columnHeaders)
              .map(priorityColumnHeaderRepresentationModelAssembler::toEntityModel)
              .toList();
      builder.withColumnHeaders(priorityColumnHeaders);
    }

    ReportRepresentationModel report =
        reportRepresentationModelAssembler.toModel(
            builder.buildReport(), filterParameter, workbasketTypes, columnHeaders);

    return ResponseEntity.status(HttpStatus.OK).body(report);
  }

  @GetMapping(path = RestEndpoints.URL_MONITOR_CLASSIFICATION_CATEGORY_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ReportRepresentationModel> computeClassificationCategoryReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws InvalidArgumentException, NotAuthorizedException {
    if (taskTimestamp == null) {
      taskTimestamp = TaskTimestamp.DUE;
    }

    ClassificationCategoryReport.Builder builder =
        monitorService.createClassificationCategoryReportBuilder();
    filterParameter.apply(builder);

    ReportRepresentationModel report =
        reportRepresentationModelAssembler.toModel(
            builder.buildReport(taskTimestamp), filterParameter, taskTimestamp);

    return ResponseEntity.status(HttpStatus.OK).body(report);
  }

  @GetMapping(path = RestEndpoints.URL_MONITOR_CLASSIFICATION_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ReportRepresentationModel> computeClassificationReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws NotAuthorizedException, InvalidArgumentException {
    if (taskTimestamp == null) {
      taskTimestamp = TaskTimestamp.DUE;
    }

    ClassificationReport.Builder builder = monitorService.createClassificationReportBuilder();
    filterParameter.apply(builder);

    ReportRepresentationModel report =
        reportRepresentationModelAssembler.toModel(
            builder.buildReport(taskTimestamp), filterParameter, taskTimestamp);

    return ResponseEntity.status(HttpStatus.OK).body(report);
  }

  @GetMapping(path = RestEndpoints.URL_MONITOR_DETAILED_CLASSIFICATION_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ReportRepresentationModel> computeDetailedClassificationReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws NotAuthorizedException, InvalidArgumentException {
    if (taskTimestamp == null) {
      taskTimestamp = TaskTimestamp.DUE;
    }

    ClassificationReport.Builder builder = monitorService.createClassificationReportBuilder();
    filterParameter.apply(builder);

    ReportRepresentationModel report =
        reportRepresentationModelAssembler.toModel(
            builder.buildDetailedReport(taskTimestamp), filterParameter, taskTimestamp);

    return ResponseEntity.status(HttpStatus.OK).body(report);
  }

  @GetMapping(path = RestEndpoints.URL_MONITOR_TASK_CUSTOM_FIELD_VALUE_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ReportRepresentationModel> computeTaskCustomFieldValueReport(
      @RequestParam(name = "custom-field") TaskCustomField customField,
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws NotAuthorizedException, InvalidArgumentException {
    if (taskTimestamp == null) {
      taskTimestamp = TaskTimestamp.DUE;
    }

    TaskCustomFieldValueReport.Builder builder =
        monitorService.createTaskCustomFieldValueReportBuilder(customField);
    filterParameter.apply(builder);

    ReportRepresentationModel report =
        reportRepresentationModelAssembler.toModel(
            builder.buildReport(taskTimestamp), customField, filterParameter, taskTimestamp);

    return ResponseEntity.status(HttpStatus.OK).body(report);
  }

  @GetMapping(path = RestEndpoints.URL_MONITOR_TASK_STATUS_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ReportRepresentationModel> computeTaskStatusReport(
      @RequestParam(name = "domain", required = false) List<String> domains,
      @RequestParam(name = "state", required = false) List<TaskState> states,
      @RequestParam(name = "workbasket-id", required = false) List<String> workbasketIds,
      @RequestParam(name = "priority-minimum", required = false) Integer priorityMinimum)
      throws NotAuthorizedException {

    TaskStatusReport.Builder builder = monitorService.createTaskStatusReportBuilder();
    if (states != null && !states.isEmpty()) {
      builder = builder.stateIn(states);
    }
    if (domains != null && !domains.isEmpty()) {
      builder.domainIn(domains);
    }
    if (workbasketIds != null && !workbasketIds.isEmpty()) {
      builder.workbasketIdsIn(workbasketIds);
    }
    if (priorityMinimum != null) {
      builder.priorityMinimum(priorityMinimum);
    }

    return ResponseEntity.ok(
        reportRepresentationModelAssembler.toModel(
            builder.buildReport(), domains, states, workbasketIds, priorityMinimum));
  }

  @GetMapping(path = RestEndpoints.URL_MONITOR_TIMESTAMP_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ReportRepresentationModel> computeTimestampReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp[] timestamps)
      throws NotAuthorizedException, InvalidArgumentException {

    TimestampReport.Builder builder = monitorService.createTimestampReportBuilder();
    filterParameter.apply(builder);
    Optional.ofNullable(timestamps).map(Arrays::asList).ifPresent(builder::withTimestamps);

    ReportRepresentationModel report =
        reportRepresentationModelAssembler.toModel(
            builder.buildReport(), filterParameter, timestamps);

    return ResponseEntity.status(HttpStatus.OK).body(report);
  }
}
