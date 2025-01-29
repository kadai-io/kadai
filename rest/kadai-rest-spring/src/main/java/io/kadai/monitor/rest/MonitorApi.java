package io.kadai.monitor.rest;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.monitor.api.TaskTimestamp;
import io.kadai.monitor.rest.models.PriorityColumnHeaderRepresentationModel;
import io.kadai.monitor.rest.models.ReportRepresentationModel;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.TaskState;
import io.kadai.workbasket.api.WorkbasketType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface MonitorApi {

  /**
   * This endpoint generates a Workbasket Report.
   *
   * <p>Each Row represents a Workbasket.
   *
   * <p>Each Column Header represents a Time Interval.
   *
   * @title Compute a Workbasket Report
   * @param filterParameter the filter parameters
   * @param taskTimestamp determine which Task Timestamp should be used for comparison
   * @return the computed Report
   * @throws NotAuthorizedException if the current user is not authorized to compute the Report
   * @throws InvalidArgumentException TODO: this is never thrown ...
   */
  @Operation(
      summary = "Compute a Workbasket Report",
      description =
          "This endpoint generates a Workbasket Report.<p>Each Row represents a Workbasket.<p>Each "
              + "Column Header represents a Time Interval.",
      parameters = {
        @Parameter(
            name = "task-timestamp",
            description = "Determine which Task Timestamp should be used for comparison"),
        @Parameter(
            name = "state",
            examples = {
              @ExampleObject(value = "READY"),
              @ExampleObject(value = "CLAIMED"),
              @ExampleObject(value = "COMPLETED")
            })
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The computed Report",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = ReportRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))})
      })
  @GetMapping(path = RestEndpoints.URL_MONITOR_WORKBASKET_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<ReportRepresentationModel> computeWorkbasketReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws NotAuthorizedException, InvalidArgumentException;

  @GetMapping(path = RestEndpoints.URL_MONITOR_WORKBASKET_PRIORITY_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<ReportRepresentationModel> computePriorityWorkbasketReport(
      @ParameterObject PriorityReportFilterParameter filterParameter,
      @RequestParam(name = "workbasket-type", required = false) WorkbasketType[] workbasketTypes,
      @RequestParam(name = "columnHeader", required = false)
          PriorityColumnHeaderRepresentationModel[] columnHeaders)
      throws NotAuthorizedException, InvalidArgumentException;

  /**
   * This endpoint generates a Classification Category Report
   *
   * <p>Each Row represents a Classification category.
   *
   * <p>Each Column Header represents a Time Interval.
   *
   * @title Compute a Classification Category Report
   * @param filterParameter the filter parameters
   * @param taskTimestamp determine which Task Timestamp should be used for comparison
   * @return the computed Report
   * @throws NotAuthorizedException if the current user is not authorized to compute the Report
   * @throws InvalidArgumentException TODO: this is never thrown ...
   */
  @Operation(
      summary = "Compute a Classification Category Report",
      description =
          "This endpoint generates a Classification Category Report.<p>Each Row represents a "
              + "Classification category.<p>Each Column Header represents a Time Interval.",
      parameters = {
        @Parameter(
            name = "task-timestamp",
            description = "Determine which Task Timestamp should be used for comparison")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The computed Report",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = ReportRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))})
      })
  @GetMapping(path = RestEndpoints.URL_MONITOR_CLASSIFICATION_CATEGORY_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<ReportRepresentationModel> computeClassificationCategoryReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws InvalidArgumentException, NotAuthorizedException;

  /**
   * This endpoint generates a Classification Report.
   *
   * <p>Each Row represents a Classification.
   *
   * <p>Each Column Header represents a Time Interval.
   *
   * @title Compute a Classification Report
   * @param filterParameter the filter parameters
   * @param taskTimestamp determine which Task Timestamp should be used for comparison
   * @return the computed Report
   * @throws NotAuthorizedException if the current user is not authorized to compute the Report
   * @throws InvalidArgumentException TODO: this is never thrown
   */
  @Operation(
      summary = "Compute a Classification Report",
      description =
          "This endpoint generates a Classification Report.<p>Each Row represents a Classification."
              + "<p>Each Column Header represents a Time Interval.",
      parameters = {
        @Parameter(
            name = "task-timestamp",
            description = "Determine which Task Timestamp should be used for comparison")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The computed Report",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = ReportRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))})
      })
  @GetMapping(path = RestEndpoints.URL_MONITOR_CLASSIFICATION_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<ReportRepresentationModel> computeClassificationReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws NotAuthorizedException, InvalidArgumentException;

  /**
   * This endpoint generates a Detailed Classification Report.
   *
   * <p>Each Foldable Row represents a Classification and can be expanded to show the Classification
   * of Attachments.
   *
   * <p>Each Column Header represents a Time Interval.
   *
   * @title Compute a Detailed Classification Report
   * @param filterParameter the filter parameters
   * @param taskTimestamp determine which Task Timestamp should be used for comparison
   * @return the computed Report
   * @throws NotAuthorizedException if the current user is not authorized to compute the Report
   * @throws InvalidArgumentException TODO: this is never thrown
   */
  @Operation(
      summary = "Compute a Detailed Classification Report",
      description =
          "This endpoint generates a Detailed Classification Report.<p>Each Foldable Row represents"
              + " a Classification and can be expanded to show the Classification of Attachments."
              + "<p>Each Column Header represents a Time Interval.",
      parameters = {
        @Parameter(
            name = "task-timestamp",
            description = "Determine which Task Timestamp should be used for comparison")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The computed Report",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = ReportRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))})
      })
  @GetMapping(path = RestEndpoints.URL_MONITOR_DETAILED_CLASSIFICATION_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<ReportRepresentationModel> computeDetailedClassificationReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws NotAuthorizedException, InvalidArgumentException;

  /**
   * This endpoint generates a Task Custom Field Value Report.
   *
   * <p>Each Row represents a value of the requested Task Custom Field.
   *
   * <p>Each Column Header represents a Time Interval
   *
   * @title Compute a Detailed Classification Report
   * @param customField the Task Custom Field whose values are of interest
   * @param filterParameter the filter parameters
   * @param taskTimestamp determine which Task Timestamp should be used for comparison
   * @return the computed Report
   * @throws NotAuthorizedException if the current user is not authorized to compute the Report
   * @throws InvalidArgumentException TODO: this is never thrown
   */
  @Operation(
      summary = "Compute a Detailed Classification Report",
      description =
          "This endpoint generates a Task Custom Field Value Report.<p>Each Row represents a value "
              + "of the requested Task Custom Field.<p>Each Column Header represents a Time "
              + "Interval.",
      parameters = {
        @Parameter(
            name = "custom-field",
            description = "The Task Custom Field whose values are of interest",
            example = "CUSTOM_14",
            required = true),
        @Parameter(
            name = "task-timestamp",
            description = "Determine which Task Timestamp should be used for comparison")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The computed Report",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = ReportRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))})
      })
  @GetMapping(path = RestEndpoints.URL_MONITOR_TASK_CUSTOM_FIELD_VALUE_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<ReportRepresentationModel> computeTaskCustomFieldValueReport(
      @RequestParam(name = "custom-field") TaskCustomField customField,
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp taskTimestamp)
      throws NotAuthorizedException, InvalidArgumentException;

  /**
   * This endpoint generates a Task Status Report.
   *
   * <p>Each Row represents a Workbasket.
   *
   * <p>Each Column Header represents a Task State
   *
   * @title Compute a Task Status Report
   * @param domains Filter the report values by domains.
   * @param states Filter the report values by Task states.
   * @param workbasketIds Filter the report values by Workbasket Ids.
   * @param priorityMinimum Filter the report values by a minimum priority.
   * @return the computed Report
   * @throws NotAuthorizedException if the current user is not authorized to compute the Report
   */
  @Operation(
      summary = "Compute a Task Status Report",
      description =
          "This endpoint generates a Task Status Report.<p>Each Row represents a Workbasket.<p>"
              + "Each Column Header represents a Task State.",
      parameters = {
        @Parameter(
            name = "domain",
            description = "Filter the report values by domains",
            required = false),
        @Parameter(
            name = "state",
            description = "Filter the report values by Task states",
            required = false),
        @Parameter(
            name = "workbasket-id",
            description = "Filter the report values by Workbasket Ids",
            required = false),
        @Parameter(
            name = "priority-minimum",
            description = "Filter the report values by a minimum priority",
            required = false)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The computed Report",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = ReportRepresentationModel.class))),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))})
      })
  @GetMapping(path = RestEndpoints.URL_MONITOR_TASK_STATUS_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<ReportRepresentationModel> computeTaskStatusReport(
      @RequestParam(name = "domain", required = false) List<String> domains,
      @RequestParam(name = "state", required = false) List<TaskState> states,
      @RequestParam(name = "workbasket-id", required = false) List<String> workbasketIds,
      @RequestParam(name = "priority-minimum", required = false) Integer priorityMinimum)
      throws NotAuthorizedException;

  /**
   * This endpoint generates a Timestamp Report.
   *
   * <p>Each Foldable Row represents a TaskTimestamp and can be expanded to display the four
   * organization levels of the corresponding Workbasket.
   *
   * <p>Each Column Header represents a TimeInterval.
   *
   * @title Compute a Timestamp Report
   * @param filterParameter the filter parameter
   * @param timestamps Filter by the Task Timestamp of the task
   * @return the computed report
   * @throws NotAuthorizedException if the current user is not authorized to compute the report
   * @throws InvalidArgumentException TODO: this is never thrown
   */
  @Operation(
      summary = "Compute a Timestamp Report",
      description =
          "This endpoint generates a Timestamp Report.<p>Each Foldable Row represents a "
              + "TaskTimestamp and can be expanded to display the four organization levels of the "
              + "corresponding Workbasket.<p>Each Column Header represents a TimeInterval.",
      parameters = {
        @Parameter(
            name = "task-timestamp",
            description = "Filter by the Task Timestamp of the task")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The computed Report",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema = @Schema(implementation = ReportRepresentationModel.class))),
        @ApiResponse(
            responseCode = "400",
            description = "INVALID_ARGUMENT",
            content = {
              @Content(schema = @Schema(implementation = InvalidArgumentException.class))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))})
      })
  @GetMapping(path = RestEndpoints.URL_MONITOR_TIMESTAMP_REPORT)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ReportRepresentationModel> computeTimestampReport(
      @ParameterObject TimeIntervalReportFilterParameter filterParameter,
      @RequestParam(name = "task-timestamp", required = false) TaskTimestamp[] timestamps)
      throws NotAuthorizedException, InvalidArgumentException;
}
