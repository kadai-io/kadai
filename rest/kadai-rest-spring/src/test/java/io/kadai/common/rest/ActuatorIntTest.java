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

import io.kadai.common.rest.config.ActuatorSecurityConfiguration;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Integration tests for the Spring Boot Actuator management endpoints exposed by {@link
 * ActuatorSecurityConfiguration}.
 */
@KadaiSpringBootTest
class ActuatorIntTest {

  private static final String ACTUATOR_HEALTH = "/actuator/health";
  private static final String ACTUATOR_INFO = "/actuator/info";

  private final RestHelper restHelper;
  private final RestClient restClient;

  @Autowired
  ActuatorIntTest(RestHelper restHelper, RestClient restClient) {
    this.restHelper = restHelper;
    this.restClient = restClient;
  }

  @Test
  void should_ReturnStatusUp_When_HealthEndpointIsCalledWithoutAuthentication() {
    ResponseEntity<Map<String, Object>> response =
        restClient
            .get()
            .uri(restHelper.toUrl(ACTUATOR_HEALTH))
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).containsEntry("status", "UP");
  }

  @Test
  void should_ReturnHealthDetailsWithComponents_When_HealthEndpointIsCalledWithAuthentication() {
    ResponseEntity<Map<String, Object>> response =
        restClient
            .get()
            .uri(restHelper.toUrl(ACTUATOR_HEALTH))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .header("Accept", "application/vnd.spring-boot.actuator.v3+json")
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).containsEntry("status", "UP");
    assertThat(response.getBody()).containsKey("components");
  }

  @Test
  void should_ReturnStatusUp_When_HealthStatusSubEndpointIsCalledWithoutAuthentication() {
    ResponseEntity<Map<String, Object>> response =
        restClient
            .get()
            .uri(restHelper.toUrl(ACTUATOR_HEALTH + "/db"))
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).containsEntry("status", "UP");
  }

  @Test
  void should_ReturnOk_When_InfoEndpointIsCalledWithAuthentication() {
    ResponseEntity<Map<String, Object>> response =
        restClient
            .get()
            .uri(restHelper.toUrl(ACTUATOR_INFO))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .header("Accept", "application/vnd.spring-boot.actuator.v3+json")
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void should_Return401_When_InfoEndpointIsCalledWithoutAuthentication() {
    assertThatThrownBy(
            () ->
                restClient
                    .get()
                    .uri(restHelper.toUrl(ACTUATOR_INFO))
                    .retrieve()
                    .toEntity(String.class))
        .isInstanceOf(HttpClientErrorException.Unauthorized.class);
  }

  @Test
  void should_ReturnActuatorLinks_When_ActuatorRootIsCalledWithAuthentication() {
    ResponseEntity<Map<String, Object>> response =
        restClient
            .get()
            .uri(restHelper.toUrl("/actuator"))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .header("Accept", "application/vnd.spring-boot.actuator.v3+json")
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).containsKey("_links");
  }

  @Test
  void should_Return401_When_ActuatorRootIsCalledWithoutAuthentication() {
    assertThatThrownBy(
            () ->
                restClient
                    .get()
                    .uri(restHelper.toUrl("/actuator"))
                    .retrieve()
                    .toEntity(String.class))
        .isInstanceOf(HttpClientErrorException.Unauthorized.class);
  }
}
