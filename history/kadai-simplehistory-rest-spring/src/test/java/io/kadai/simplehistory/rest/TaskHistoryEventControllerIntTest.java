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

package io.kadai.simplehistory.rest;

import static io.kadai.rest.test.RestHelper.CLIENT;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.rest.models.PageMetadata;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import io.kadai.simplehistory.rest.models.TaskHistoryEventPagedRepresentationModel;
import io.kadai.simplehistory.rest.models.TaskHistoryEventRepresentationModel;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

/** Controller for integration test. */
@KadaiSpringBootTest
class TaskHistoryEventControllerIntTest {

  private final RestHelper restHelper;

  @Autowired
  TaskHistoryEventControllerIntTest(RestHelper restHelper) {
    this.restHelper = restHelper;
  }

  // region Get Task History Events

  @Test
  void should_GetAllHistoryEvents_When_UrlIsVisited() {
    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(45);
  }

  @Test
  void should_GenerateSelfLink_When_TaskHistoryEventsAreRequested() {
    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF))
        .isPresent()
        .get()
        .extracting(Link::getHref)
        .asString()
        .endsWith(HistoryRestEndpoints.URL_HISTORY_EVENTS);
  }

  @Test
  void should_ContainQueryParametersInComputedSelfLink_When_TaskHistoryEventsAreRequested() {
    String parameters = "?domain=DOMAIN_A&domain=DOMAIN_B";
    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS + parameters))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF))
        .isPresent()
        .get()
        .extracting(Link::getHref)
        .asString()
        .endsWith(HistoryRestEndpoints.URL_HISTORY_EVENTS + parameters);
  }

  @Test
  void should_SortEventsByBusinessProcessIdDesc_When_SortByAndOrderQueryParametersAreDeclared() {
    String parameters = "?sort-by=BUSINESS_PROCESS_ID&order=DESCENDING";
    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS + parameters))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent())
        .extracting(TaskHistoryEventRepresentationModel::getBusinessProcessId)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER.reversed());
  }

  @Test
  void should_ApplyBusinessProcessIdFilter_When_QueryParameterIsProvided() {
    String parameters = "?business-process-id=BPI:01";
    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS + parameters))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent())
        .extracting(TaskHistoryEventRepresentationModel::getTaskHistoryId)
        .containsExactly("THI:000000000000000000000000000000000000");
  }

  @Test
  void should_SortEventsByProxyAccessId_When_SortByAndOrderQueryParametersAreDeclared() {
    String parameters = "?sort-by=PROXY_ACCESS_ID&order=ASCENDING";
    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS + parameters))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent())
        .extracting(TaskHistoryEventRepresentationModel::getProxyAccessId)
        .filteredOn(Objects::nonNull)
        .isSortedAccordingTo(CASE_INSENSITIVE_ORDER);
  }

  @Test
  void should_ApplyProxyAccessIdFilter_When_QueryParameterIsProvided() {
    String parameters = "?proxy-access-id=monitor";
    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS + parameters))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent())
        .extracting(TaskHistoryEventRepresentationModel::getTaskHistoryId)
        .containsExactlyInAnyOrder(
            "THI:000000000000000000000000000000000027", "THI:000000000000000000000000000000000026");
  }

  @Test
  void should_ReturnBadStatusErrorCode_When_CreatedQueryParameterIsWrongFormatted() {
    String currentTime = "wrong format";
    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(
                    restHelper.toUrl(
                        HistoryRestEndpoints.URL_HISTORY_EVENTS + "?created=" + currentTime))
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                .retrieve()
                .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .hasMessageContaining(currentTime)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_ApplyCreatedFilter_When_QueryParametersAreProvided() {
    Instant now = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(
                restHelper.toUrl(
                    HistoryRestEndpoints.URL_HISTORY_EVENTS + "?created=" + now + "&created="))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(23);
  }

  @Test
  void should_ApplyPaging_When_PagingIsRequested() {
    String expectedFirstPageParameters = "?sort-by=TASK_HISTORY_EVENT_ID&page-size=3&page=1";
    String expectedLastPageParameters = "?sort-by=TASK_HISTORY_EVENT_ID&page-size=3&page=15";
    String expectedPrevPageParameters = "?sort-by=TASK_HISTORY_EVENT_ID&page-size=3&page=2";
    String expectedNextPageParameters = "?sort-by=TASK_HISTORY_EVENT_ID&page-size=3&page=4";

    String parameters = "?sort-by=TASK_HISTORY_EVENT_ID&page-size=3&page=3";

    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS + parameters))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent())
        .extracting(TaskHistoryEventRepresentationModel::getTaskHistoryId)
        .containsExactly(
            "THI:000000000000000000000000000000000006",
            "THI:000000000000000000000000000000000007",
            "THI:000000000000000000000000000000000008");

    assertThat(response.getBody().getPageMetadata()).isEqualTo(new PageMetadata(3, 45, 15, 3));

    assertThat(response.getBody().getLink(IanaLinkRelations.FIRST))
        .isPresent()
        .get()
        .extracting(Link::getHref)
        .asString()
        .endsWith(HistoryRestEndpoints.URL_HISTORY_EVENTS + expectedFirstPageParameters);
    assertThat(response.getBody().getLink(IanaLinkRelations.LAST))
        .isPresent()
        .get()
        .extracting(Link::getHref)
        .asString()
        .endsWith(HistoryRestEndpoints.URL_HISTORY_EVENTS + expectedLastPageParameters);
    assertThat(response.getBody().getLink(IanaLinkRelations.PREV))
        .isPresent()
        .get()
        .extracting(Link::getHref)
        .asString()
        .endsWith(HistoryRestEndpoints.URL_HISTORY_EVENTS + expectedPrevPageParameters);
    assertThat(response.getBody().getLink(IanaLinkRelations.NEXT))
        .isPresent()
        .get()
        .extracting(Link::getHref)
        .asString()
        .endsWith(HistoryRestEndpoints.URL_HISTORY_EVENTS + expectedNextPageParameters);
  }

  // endregion

  // region Get Specific Task History Event

  @Test
  void should_GenerateSelfLink_When_SpecificTaskHistoryEventIsRequested() {
    String id = "THI:000000000000000000000000000000000000";
    String expectedUrl =
        UriComponentsBuilder.fromPath(HistoryRestEndpoints.URL_HISTORY_EVENTS_ID)
            .buildAndExpand(URLEncoder.encode(id, StandardCharsets.UTF_8))
            .toUriString();

    ResponseEntity<TaskHistoryEventPagedRepresentationModel> response =
        CLIENT
            .get()
            .uri(restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS_ID, id))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(TaskHistoryEventPagedRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF))
        .isPresent()
        .get()
        .extracting(Link::getHref)
        .asString()
        .endsWith(expectedUrl);
  }

  @Test
  void should_GetSpecificTaskHistoryEventWithDetails_When_SingleEventIsQueried() {
    ResponseEntity<TaskHistoryEventRepresentationModel> response =
        CLIENT
            .get()
            .uri(
                restHelper.toUrl(
                    HistoryRestEndpoints.URL_HISTORY_EVENTS_ID,
                    "THI:000000000000000000000000000000000000"))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(TaskHistoryEventRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDetails()).isNotNull();
  }

  @Test
  void should_ThrowException_When_ProvidingInvalidFilterParams() {

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(
                    restHelper.toUrl(HistoryRestEndpoints.URL_HISTORY_EVENTS)
                        + "?domain=DOMAIN_A"
                        + "&illegalParam=illegal"
                        + "&anotherIllegalParam=stillIllegal"
                        + "&sort-by=TASK_ID&order=DESCENDING&page-size=5&page=2")
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                .retrieve()
                .toEntity(TaskHistoryEventPagedRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .hasMessageContaining(
            "Unknown request parameters found: [anotherIllegalParam, illegalParam]")
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  // endregion
}
