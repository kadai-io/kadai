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

import static io.kadai.rest.test.RestHelper.CLIENT;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kadai.classification.rest.models.ClassificationSummaryPagedRepresentationModel;
import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.rest.KadaiRestExceptionHandler.MalformedQueryParameter;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import io.kadai.workbasket.api.WorkbasketPermission;
import io.kadai.workbasket.api.WorkbasketType;
import io.kadai.workbasket.rest.models.WorkbasketSummaryPagedRepresentationModel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpStatusCodeException;

/** Test general Exception Handling. */
@KadaiSpringBootTest
class GeneralExceptionHandlingTest {

  private final RestHelper restHelper;
  private final ObjectMapper objectMapper;

  @Autowired
  GeneralExceptionHandlingTest(RestHelper restHelper, ObjectMapper objectMapper) {
    this.restHelper = restHelper;
    this.objectMapper = objectMapper;
  }

  @Test
  void testDeleteNonExisitingClassificationExceptionIsLogged() {
    String url = restHelper.toUrl(RestEndpoints.URL_CLASSIFICATIONS_ID, "non-existing-id");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .delete()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                .retrieve()
                .toEntity(ClassificationSummaryPagedRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .hasMessageContaining("non-existing-id");
  }

  @Test
  void should_ThrowExpressiveError_When_AQueryParameterIsInvalid() throws Exception {
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET) + "?required-permission=GROU";

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                .retrieve()
                .toEntity(WorkbasketSummaryPagedRepresentationModel.class);

    List<String> expectedValues =
        Arrays.stream(WorkbasketPermission.values()).map(Object::toString).toList();
    ErrorCode errorCode =
        ErrorCode.of(
            "QUERY_PARAMETER_MALFORMED",
            Map.of(
                "malformedQueryParameters",
                List.of(new MalformedQueryParameter("required-permission", "GROU", expectedValues))
                    .toArray(new MalformedQueryParameter[0])));

    assertThatThrownBy(httpCall)
        .isInstanceOf(BadRequest.class)
        .extracting(BadRequest.class::cast)
        .extracting(BadRequest::getResponseBodyAsString)
        .asString()
        .contains(objectMapper.writeValueAsString(errorCode));
  }

  @Test
  void should_CombineErrors_When_SameQueryParameterDeclarationsAreInvalidMultipleTimes()
      throws Exception {
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET) + "?type=GROU&type=invalid";

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                .retrieve()
                .toEntity(WorkbasketSummaryPagedRepresentationModel.class);

    List<String> expectedValuesForQueryParameterType =
        Arrays.stream(WorkbasketType.values()).map(Object::toString).toList();
    ErrorCode errorCode =
        ErrorCode.of(
            "QUERY_PARAMETER_MALFORMED",
            Map.of(
                "malformedQueryParameters",
                List.of(
                        new MalformedQueryParameter(
                            "type", "GROU", expectedValuesForQueryParameterType),
                        new MalformedQueryParameter(
                            "type", "invalid", expectedValuesForQueryParameterType))
                    .toArray(new MalformedQueryParameter[0])));

    assertThatThrownBy(httpCall)
        .isInstanceOf(BadRequest.class)
        .extracting(BadRequest.class::cast)
        .extracting(BadRequest::getResponseBodyAsString)
        .asString()
        .contains(objectMapper.writeValueAsString(errorCode));
  }

  @Test
  void should_FilterOutValidQueryParameters_When_OnlySomeQueryParametersDeclarationsAreInvalid()
      throws Exception {
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET) + "?type=GROUP&type=invalid";

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                .retrieve()
                .toEntity(WorkbasketSummaryPagedRepresentationModel.class);

    List<String> expectedValuesForQueryParameterType =
        Arrays.stream(WorkbasketType.values()).map(Object::toString).toList();
    ErrorCode errorCode =
        ErrorCode.of(
            "QUERY_PARAMETER_MALFORMED",
            Map.of(
                "malformedQueryParameters",
                List.of(
                        new MalformedQueryParameter(
                            "type", "invalid", expectedValuesForQueryParameterType))
                    .toArray(new MalformedQueryParameter[0])));

    assertThatThrownBy(httpCall)
        .isInstanceOf(BadRequest.class)
        .extracting(BadRequest.class::cast)
        .extracting(BadRequest::getResponseBodyAsString)
        .asString()
        .contains(objectMapper.writeValueAsString(errorCode));
  }

  @Test
  void should_CombineErrors_When_DifferentQueryParametersAreInvalid() throws Exception {
    String url =
        restHelper.toUrl(RestEndpoints.URL_WORKBASKET) + "?type=GROU&required-permission=invalid";

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                .retrieve()
                .toEntity(WorkbasketSummaryPagedRepresentationModel.class);

    List<String> expectedValuesForQueryParameterType =
        Arrays.stream(WorkbasketType.values()).map(Object::toString).toList();
    List<String> expectedValuesForQueryParameterRequiredPermission =
        Arrays.stream(WorkbasketPermission.values()).map(Object::toString).toList();
    ErrorCode errorCode =
        ErrorCode.of(
            "QUERY_PARAMETER_MALFORMED",
            Map.of(
                "malformedQueryParameters",
                List.of(
                        new MalformedQueryParameter(
                            "type", "GROU", expectedValuesForQueryParameterType),
                        new MalformedQueryParameter(
                            "required-permission",
                            "invalid",
                            expectedValuesForQueryParameterRequiredPermission))
                    .toArray(new MalformedQueryParameter[0])));

    assertThatThrownBy(httpCall)
        .isInstanceOf(BadRequest.class)
        .extracting(BadRequest.class::cast)
        .extracting(BadRequest::getResponseBodyAsString)
        .asString()
        .contains(objectMapper.writeValueAsString(errorCode));
  }
}
