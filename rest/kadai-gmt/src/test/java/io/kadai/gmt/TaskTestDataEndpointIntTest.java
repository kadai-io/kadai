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

package io.kadai.gmt;

import static org.assertj.core.api.Assertions.assertThat;

import io.kadai.classification.api.ClassificationService;
import io.kadai.common.api.KadaiEngine;
import io.kadai.task.api.TaskService;
import io.kadai.workbasket.api.WorkbasketService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Integration test for the synchronous Green Metrics Tool test-data endpoint. */
@Testcontainers
@SpringBootTest(
    classes = {GmtApplication.class, TaskTestDataEndpointIntTest.TestSecurityConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskTestDataEndpointIntTest {

  private static final long TASK_COUNT = 12;

  @Container
  static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:17");

  @LocalServerPort private int port;

  @Autowired private KadaiEngine kadaiEngine;
  @Autowired private TaskService taskService;
  @Autowired private WorkbasketService workbasketService;
  @Autowired private ClassificationService classificationService;

  @DynamicPropertySource
  static void configureDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    registry.add("spring.datasource.driverClassName", POSTGRESQL::getDriverClassName);
    registry.add("kadai.schemaName", () -> "kadai");
    registry.add("management.health.ldap.enabled", () -> false);
  }

  @Test
  void should_CreateTasksAndSupportingFixturesAfterTheApplicationIsHealthy() {
    RestClient restClient = RestClient.create();
    ResponseEntity<Map<String, Object>> healthResponse =
        restClient
            .get()
            .uri(url("/actuator/health"))
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {});

    assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(healthResponse.getBody()).containsEntry("status", "UP");

    TestDataCounts countsBefore = retrieveTestDataCounts();
    ResponseEntity<Void> response =
        restClient
            .post()
            .uri(url("/api/v1/gmt/tasks"))
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, basicAuthentication("admin", "admin"))
            .body("{\"taskCount\":" + TASK_COUNT + "}")
            .retrieve()
            .toBodilessEntity();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    TestDataCounts countsAfter = retrieveTestDataCounts();
    assertThat(countsAfter.taskCount() - countsBefore.taskCount()).isEqualTo(TASK_COUNT);
    assertThat(countsAfter.workbasketCount()).isGreaterThan(countsBefore.workbasketCount());
    assertThat(countsAfter.classificationCount()).isGreaterThan(countsBefore.classificationCount());
  }

  private TestDataCounts retrieveTestDataCounts() {
    return kadaiEngine.runAsAdmin(
        () ->
            new TestDataCounts(
                taskService.createTaskQuery().count(),
                workbasketService.createWorkbasketQuery().count(),
                classificationService.createClassificationQuery().count()));
  }

  private String url(String path) {
    return "http://localhost:" + port + "/kadai" + path;
  }

  private static String basicAuthentication(String user, String password) {
    return "Basic "
        + Base64.getEncoder()
            .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
  }

  private record TestDataCounts(long taskCount, long workbasketCount, long classificationCount) {}

  /** Authentication configuration independent of an external LDAP server. */
  @TestConfiguration
  static class TestSecurityConfiguration {

    @Bean("ldapAuthenticationManager")
    AuthenticationManager authenticationManager() {
      return authentication -> {
        if ("admin".equals(authentication.getName())
            && "admin".equals(authentication.getCredentials())) {
          return UsernamePasswordAuthenticationToken.authenticated(
              new User(
                  authentication.getName(),
                  authentication.getCredentials().toString(),
                  java.util.List.of(new SimpleGrantedAuthority("admin"))),
              authentication.getCredentials(),
              java.util.List.of(new SimpleGrantedAuthority("admin")));
        }
        throw new BadCredentialsException("Invalid test credentials");
      };
    }
  }
}
