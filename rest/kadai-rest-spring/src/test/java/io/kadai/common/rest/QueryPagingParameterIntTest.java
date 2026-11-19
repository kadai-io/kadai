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

package io.kadai.common.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Verifies that every endpoint taking a {@link QueryPagingParameter} validates its values. */
@KadaiSpringBootTest
class QueryPagingParameterIntTest {

  private static final String TASK_ID = "TKI:000000000000000000000000000000000000";

  private final RestHelper restHelper;
  private final RestClient restClient;
  private final JsonMapper jsonMapper;

  @Autowired
  QueryPagingParameterIntTest(RestHelper restHelper, RestClient restClient, JsonMapper jsonMapper) {
    this.restHelper = restHelper;
    this.restClient = restClient;
    this.jsonMapper = jsonMapper;
  }

  static Stream<String> pagedEndpoints() {
    return Stream.of(
        RestEndpoints.URL_TASKS,
        RestEndpoints.URL_TASKS_IDS,
        RestEndpoints.URL_TASK_COMMENTS,
        RestEndpoints.URL_WORKBASKET,
        RestEndpoints.URL_WORKBASKET_ACCESS_ITEMS,
        RestEndpoints.URL_CLASSIFICATIONS,
        RestEndpoints.URL_USERS);
  }

  static Stream<Arguments> invalidPagingParameters() {
    return pagedEndpoints()
        .flatMap(
            endpoint ->
                Stream.of(
                    Arguments.of(endpoint, "page=0&page-size=5", "page"),
                    Arguments.of(endpoint, "page=-5&page-size=5", "page"),
                    Arguments.of(endpoint, "page=1&page-size=0", "page-size"),
                    Arguments.of(endpoint, "page=1&page-size=-5", "page-size"),
                    Arguments.of(endpoint, "page-size=0", "page-size")));
  }

  @ParameterizedTest(name = "{0}?{1}")
  @MethodSource("invalidPagingParameters")
  void should_ReturnBadRequest_When_PagingParameterIsLessThanOne(
      String endpoint, String queryParameters, String invalidParameter) {
    String url = restHelper.toUrl(endpoint, TASK_ID) + "?" + queryParameters;

    ThrowingCallable httpCall =
        () ->
            restClient
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                .retrieve()
                .toEntity(String.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(BadRequest.class)
        .extracting(BadRequest.class::cast)
        .satisfies(
            e -> {
              JsonNode body = jsonMapper.readTree(e.getResponseBodyAsString());
              assertThat(body.path("error").path("key").asString())
                  .isEqualTo(InvalidArgumentException.ERROR_KEY);
              assertThat(body.path("message").asString()).contains("'" + invalidParameter + "'");
            });
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pagedEndpoints")
  void should_AcceptPagingParameters_When_PageAndPageSizeAreOne(String endpoint) {
    String url = restHelper.toUrl(endpoint, TASK_ID) + "?page=1&page-size=1";

    ResponseEntity<String> response =
        restClient
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    if (RestEndpoints.URL_TASK_COMMENTS.equals(endpoint)) {
      // the task comment endpoint applies paging but does not return page metadata
      return;
    }
    JsonNode page = jsonMapper.readTree(response.getBody()).path("page");
    assertThat(page.path("size").asLong()).isOne();
    assertThat(page.path("number").asLong()).isOne();
  }
}
