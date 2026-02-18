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

package io.kadai.monitor.rest;

import static io.kadai.rest.test.RestHelper.CLIENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.monitor.rest.models.PriorityColumnHeaderRepresentationModel;
import io.kadai.monitor.rest.models.ReportRepresentationModel;
import io.kadai.monitor.rest.models.ReportRepresentationModel.RowRepresentationModel;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

/** Test MonitorController. */
@KadaiSpringBootTest
class MonitorControllerIntTest {

  private final RestHelper restHelper;
  private final ObjectMapper objectMapper;

  @Autowired
  MonitorControllerIntTest(RestHelper restHelper, ObjectMapper objectMapper) {
    this.restHelper = restHelper;
    this.objectMapper = objectMapper;
  }

  @Test
  void should_ReturnAllOpenTasksByState_When_QueryingForAWorkbasketAndReadyAndClaimedState() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_TASK_STATUS_REPORT)
            + "?workbasket-id=WBI:100000000000000000000000000000000007"
            + "&state=READY&state=CLAIMED";

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
    int totalOpenTasks = response.getBody().getSumRow().get(0).getTotal();
    assertThat(totalOpenTasks).isEqualTo(15);
    int[] tasksPerState = response.getBody().getSumRow().get(0).getCells();
    // should be 2 READY, 13 CLAIMED
    int[] expectedTasksPerState = new int[] {2, 13};
    assertThat(tasksPerState).isEqualTo(expectedTasksPerState);
  }

  @Test
  void should_ReturnAllOpenTasksByState_When_QueryingForSpecificWbAndStateReadyAndMinimumPrio() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_TASK_STATUS_REPORT)
            + "?workbasket-id=WBI:"
            + "100000000000000000000000000000000007&state=READY&priority-minimum=1";

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
    int[] tasksInStateReady = response.getBody().getSumRow().get(0).getCells();
    // should be 2 READY
    assertThat(tasksInStateReady).isEqualTo(new int[] {2});
  }

  @Test
  void should_ApplyAllFiltersAndComputeReport_When_QueryingForAWorkbasketReport() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_WORKBASKET_REPORT)
            + "?workbasket-id=WBI:100000000000000000000000000000000015"
            + "&custom-3=abcd"
            + "&custom-3=abbd"
            + "&custom-3-not-in=abbb"
            + "&custom-4=defg"
            + "&custom-5=important";

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    ReportRepresentationModel report = response.getBody();

    assertThat(report).isNotNull();

    assertThat(report.getSumRow())
        .extracting(RowRepresentationModel::getCells)
        .containsExactly(new int[] {0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void should_ApplyStateFilterAndComputeReport_When_QueryingForAWorkbasketReport() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_WORKBASKET_REPORT)
            + "?workbasket-id=WBI:100000000000000000000000000000000008"
            + "&state=READY"
            + "&state=CLAIMED";

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    ReportRepresentationModel report = response.getBody();
    assertThat(report).isNotNull();
    assertThat(report.getSumRow())
        .extracting(RowRepresentationModel::getCells)
        // expecting 4 tasks due tomorrow (RELATIVE_DATE(1))
        .containsExactly(new int[] {0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0});
  }

  @Test
  void should_ComputeWorkbasketPriorityReport_When_QueryingForAWorkbasketPriorityReport() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_WORKBASKET_PRIORITY_REPORT)
            + "?workbasket-type=TOPIC&workbasket-type=GROUP";

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    ReportRepresentationModel report = response.getBody();

    assertThat(report).isNotNull();

    assertThat(report.getSumRow()).extracting(RowRepresentationModel::getTotal).containsExactly(32);
  }

  @Test
  void should_ApplyComplexFilterCombination_When_QueryingForAWorkbasketPriorityReport() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_WORKBASKET_PRIORITY_REPORT)
            + "?workbasket-type=TOPIC&workbasket-type=GROUP"
            + "&state=READY&state=CLAIMED"
            + "&custom-6=074&custom-6=075"
            + "&custom-7-not-in=20";

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    ReportRepresentationModel report = response.getBody();
    assertThat(report).isNotNull();
    assertThat(report.getSumRow())
        .extracting(RowRepresentationModel::getCells)
        .containsExactly(new int[] {2, 0, 1});
  }

  @Test
  void should_DetectPriorityColumnHeader_When_HeaderIsPassedAsQueryParameter() throws Exception {
    PriorityColumnHeaderRepresentationModel columnHeader =
        new PriorityColumnHeaderRepresentationModel(10, 20);

    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_WORKBASKET_PRIORITY_REPORT)
            + "?columnHeader="
            + URLEncoder.encode(
                objectMapper.writeValueAsString(columnHeader), StandardCharsets.UTF_8);

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    ReportRepresentationModel report = response.getBody();
    assertThat(report).isNotNull();
    assertThat(report.getMeta().getHeader()).containsExactly("10 - 20");
  }

  @Test
  void should_ReturnBadRequest_When_PriorityColumnHeaderIsNotAValidJson() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_WORKBASKET_PRIORITY_REPORT)
            + "?columnHeader=invalidJson";

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
                .retrieve()
                .toEntity(ReportRepresentationModel.class);

    assertThatThrownBy(httpCall).isInstanceOf(BadRequest.class);
  }

  @Test
  void should_ComputeWorkbasketPriorityReport_When_QueryingForADetailedWorkbasketPriorityReport() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_DETAILED_WORKBASKET_PRIORITY_REPORT)
            + "?workbasket-type=TOPIC&workbasket-type=GROUP";

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    ReportRepresentationModel report = response.getBody();

    assertThat(report).isNotNull();

    assertThat(report.getSumRow())
        .extracting(RowRepresentationModel::getTotal)
        .containsExactly(32, 24, 2, 3, 3);
  }

  @Test
  void should_ApplyComplexFilterCombination_When_QueryingForADetailedWorkbasketPriorityReport() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_DETAILED_WORKBASKET_PRIORITY_REPORT)
            + "?workbasket-type=TOPIC&workbasket-type=GROUP"
            + "&state=READY&state=CLAIMED"
            + "&custom-6=074&custom-6=075"
            + "&custom-7-not-in=20";

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    ReportRepresentationModel report = response.getBody();
    assertThat(report).isNotNull();
    assertThat(report.getSumRow())
        .extracting(RowRepresentationModel::getCells)
        .containsExactlyInAnyOrder(
            new int[] {2, 0, 1},
            new int[] {2, 0, 1}
        );
  }

  @Test
  void should_DetectPriorityColumnHeader_When_HeaderIsPassedAsQueryParameterForDetailedWorkbasket()
      throws Exception {
    PriorityColumnHeaderRepresentationModel columnHeader =
        new PriorityColumnHeaderRepresentationModel(10, 20);

    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_DETAILED_WORKBASKET_PRIORITY_REPORT)
            + "?columnHeader="
            + URLEncoder.encode(
                objectMapper.writeValueAsString(columnHeader), StandardCharsets.UTF_8);

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    ReportRepresentationModel report = response.getBody();
    assertThat(report).isNotNull();
    assertThat(report.getMeta().getHeader()).containsExactly("10 - 20");
  }

  @Test
  void should_ReturnBadRequest_When_PriorityColumnHeaderIsNotAValidJsonForDetailedWorkbasket() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_MONITOR_DETAILED_WORKBASKET_PRIORITY_REPORT)
            + "?columnHeader=invalidJson";

    ThrowingCallable httpCall =
        () ->
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("monitor")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    assertThatThrownBy(httpCall).isInstanceOf(BadRequest.class);
  }

  @Test
  void should_ComputeClassificationCategoryReport() {
    String url = restHelper.toUrl(RestEndpoints.URL_MONITOR_CLASSIFICATION_CATEGORY_REPORT);

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    String[][] descArray = {{"AUTOMATIC"}, {"EXTERN"}, {"MANUAL"}};
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getRows().stream().map(RowRepresentationModel::getDesc).toList())
        .hasSize(3)
        .containsExactlyInAnyOrder(descArray);
  }

  @Test
  void should_ComputeClassificationReport() {
    String url = restHelper.toUrl(RestEndpoints.URL_MONITOR_CLASSIFICATION_REPORT);

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getRows().stream().map(RowRepresentationModel::getDesc).toList())
        .hasSize(6);
  }

  @Test
  void should_ComputeDetailedClassificationReport() {
    String url = restHelper.toUrl(RestEndpoints.URL_MONITOR_DETAILED_CLASSIFICATION_REPORT);

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void should_ComputeTaskStatusReport() {
    String url = restHelper.toUrl(RestEndpoints.URL_MONITOR_TASK_STATUS_REPORT);

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void should_ComputeTimestampReport() {
    String url = restHelper.toUrl(RestEndpoints.URL_MONITOR_TIMESTAMP_REPORT);

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void should_ComputeTaskCustomFieldValueReport() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_MONITOR_TASK_CUSTOM_FIELD_VALUE_REPORT + "?custom-field=CUSTOM_14");

    ResponseEntity<ReportRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ReportRepresentationModel.class);

    String[][] descArray = {{"abc"}, {"dde"}, {"ert"}};

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getRows().stream().map(RowRepresentationModel::getDesc).toList())
        .hasSize(3)
        .containsExactlyInAnyOrder(descArray);
  }
}
