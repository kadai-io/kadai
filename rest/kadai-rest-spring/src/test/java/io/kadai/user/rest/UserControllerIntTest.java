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

package io.kadai.user.rest;

import static io.kadai.rest.test.RestHelper.CLIENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.rest.RestEndpoints;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import io.kadai.user.rest.models.UserCollectionRepresentationModel;
import io.kadai.user.rest.models.UserRepresentationModel;
import java.util.Collection;
import java.util.Set;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.web.client.HttpStatusCodeException;

/** Tests the endpoints of the UserController. */
@KadaiSpringBootTest
class UserControllerIntTest {
  private final RestHelper restHelper;

  @Autowired
  UserControllerIntTest(RestHelper restHelper) {
    this.restHelper = restHelper;
  }

  @Test
  void should_ReturnExistingUser() {
    String url = restHelper.toUrl(RestEndpoints.URL_USERS_ID, "TEAMLEAD-1");

    ResponseEntity<UserRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserRepresentationModel.class);
    assertThat(responseEntity.getBody()).isNotNull();
  }

  @Test
  void should_ReturnExistingUsers() {
    String url = restHelper.toUrl(RestEndpoints.URL_USERS) + "?user-id=user-1-1&user-id=USER-1-2";

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);
    UserCollectionRepresentationModel response = responseEntity.getBody();
    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(2);
    assertThat(response.getContent())
        .extracting("firstName")
        .containsExactlyInAnyOrder("Max", "Elena");
  }

  @Test
  void should_ReturnCurrentUser() {
    String url = restHelper.toUrl(RestEndpoints.URL_USERS) + "?current-user";

    ResponseEntity<UserCollectionRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(1);
    assertThat(response.getBody().getContent()).extracting("userId").containsExactly("teamlead-1");
  }

  @Test
  void should_ReturnExceptionCurrentUserWithBadValue() {
    String url = restHelper.toUrl(RestEndpoints.URL_USERS) + "?current-user=asd";

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                .retrieve()
                .toEntity(UserCollectionRepresentationModel.class);
    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_ReturnExceptionCurrentUserWithEmptyValue() {
    String url = restHelper.toUrl(RestEndpoints.URL_USERS) + "?current-user=";

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                .retrieve()
                .toEntity(UserCollectionRepresentationModel.class);
    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_ReturnOnlyCurrentUserWhileUsingUserIds() {
    String url = restHelper.toUrl(RestEndpoints.URL_USERS) + "?current-user&user-id=teamlead-1";

    ResponseEntity<UserCollectionRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(1);
    assertThat(response.getBody().getContent()).extracting("userId").containsExactly("teamlead-1");
  }

  @Test
  void should_ReturnExistingUsersAndCurrentUser() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + "?user-id=user-1-1&user-id=USER-1-2&current-user";

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);
    UserCollectionRepresentationModel response = responseEntity.getBody();
    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(3);
    assertThat(response.getContent())
        .extracting("userId")
        .containsExactlyInAnyOrder("user-1-1", "user-1-2", "teamlead-1");
  }

  @Test
  void should_ReturnExistingUsers_When_ParameterContainsDuplicateAndInvalidIds() {
    // also testing different query parameter format
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + "?user-id=user-1-1"
            + "&user-id=user-1-1"
            + "&user-id=user-2-1"
            + "&user-id=user-2-1"
            + "&user-id=user-2-1"
            + "&user-id=NotExistingId"
            + "&user-id="
            + "&user-id=AnotherNonExistingId";

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);
    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getContent()).hasSize(2);
    assertThat(responseEntity.getBody().getContent())
        .extracting("firstName")
        .containsExactlyInAnyOrder("Max", "Simone");
  }

  @ParameterizedTest
  @CsvSource({"KADAI,1", "Human Workflow,2", "BPM,3", "Envite,4"})
  void should_ReturnExistingUsers_For_OrgLevel_When_OrgLevelExists(String orgLevel, int level) {
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + String.format("?orgLevel%d=%s", level, orgLevel);

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getContent()).isNotEmpty();
    responseEntity
        .getBody()
        .getContent()
        .forEach(
            user -> {
              if (level == 1) {
                assertThat(user.getOrgLevel1()).isEqualTo(orgLevel);
              } else if (level == 2) {
                assertThat(user.getOrgLevel2()).isEqualTo(orgLevel);
              } else if (level == 3) {
                assertThat(user.getOrgLevel3()).isEqualTo(orgLevel);
              } else if (level == 4) {
                assertThat(user.getOrgLevel4()).isEqualTo(orgLevel);
              }
            });
  }

  @ParameterizedTest
  @CsvSource({"Non-Existent,1", "Non-Existent,2", "Non-Existent,3", "Non-Existent,4"})
  void should_ReturnEmptyList_For_OrgLevel_When_OrgLevelNotExists(String orgLevel, int level) {
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + String.format("?orgLevel%d=%s", level, orgLevel);

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getContent()).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
    "KADAI,Non-existent,1",
    "Human Workflow,Non-existent,2",
    "BPM,Non-existent,3",
    "Envite,Non-existent,4"
  })
  void should_ReturnUnion_For_DifferentOrgLevelsWithSameLevel(
      String orgLevel1, String orgLevel2, int level) {
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + String.format("?orgLevel%d=%s", level, orgLevel1)
            + String.format("&orgLevel%d=%s", level, orgLevel2);

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getContent()).isNotEmpty();
    responseEntity
        .getBody()
        .getContent()
        .forEach(
            user -> {
              if (level == 1) {
                assertThat(user.getOrgLevel1()).isEqualTo(orgLevel1);
              } else if (level == 2) {
                assertThat(user.getOrgLevel2()).isEqualTo(orgLevel1);
              } else if (level == 3) {
                assertThat(user.getOrgLevel3()).isEqualTo(orgLevel1);
              } else if (level == 4) {
                assertThat(user.getOrgLevel4()).isEqualTo(orgLevel1);
              }
            });
  }

  @ParameterizedTest
  @CsvSource({
    "KADAI,1,Human Workflow,2",
    "Human Workflow,2,BPM,3",
    "BPM,3,Envite,4",
    "Envite,4,KADAI,1"
  })
  void should_ReturnIntersection_For_DifferentOrgLevelsWithDifferentLevels(
      String orgLevel1, int level1, String orgLevel2, int level2) {
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + String.format("?orgLevel%d=%s", level1, orgLevel1)
            + String.format("&orgLevel%d=%s", level2, orgLevel2);

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getContent()).isNotEmpty();
    final int allUsersCount = 14;
    assertThat(responseEntity.getBody().getContent()).hasSize(allUsersCount - 1);
  }

  @ParameterizedTest
  @CsvSource({
    "KADAI,1,Non-existent,2",
    "Human Workflow,2,Non-existent,3",
    "BPM,3,Non-existent,4",
    "Envite,4,Non-existent,1"
  })
  void should_ReturnEmptyList_For_ContradictoryOrgLevels(
      String orgLevel1, int level1, String orgLevel2, int level2) {
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + String.format("?orgLevel%d=%s", level1, orgLevel1)
            + String.format("&orgLevel%d=%s", level2, orgLevel2);

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getContent()).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({"KADAI,1", "Human Workflow,2", "BPM,3", "Envite,4"})
  void should_ReturnIntersection_When_OrgLevelAndUserIdsAreGiven(String orgLevel, int level) {
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + String.format("?orgLevel%d=%s", level, orgLevel)
            + "&user-id=user-1-1"
            + "&user-id=user-2-1";

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getContent())
        .haveExactly(
            1,
            new Condition<>(user -> user.getUserId().equals("user-1-1"), "user with id user-1-1"))
        .haveExactly(
            1,
            new Condition<>(user -> user.getUserId().equals("user-2-1"), "user with id user-2-1"));
    responseEntity.getBody().getContent().stream()
        .filter(user -> !user.getUserId().equals("user-1-1"))
        .filter(user -> !user.getUserId().equals("user-2-1"))
        .forEach(
            user -> {
              if (level == 1) {
                assertThat(user.getOrgLevel1()).isEqualTo(orgLevel);
              } else if (level == 2) {
                assertThat(user.getOrgLevel2()).isEqualTo(orgLevel);
              } else if (level == 3) {
                assertThat(user.getOrgLevel3()).isEqualTo(orgLevel);
              } else if (level == 4) {
                assertThat(user.getOrgLevel4()).isEqualTo(orgLevel);
              }
            });
  }

  @ParameterizedTest
  @CsvSource({"KADAI,1", "Human Workflow,2", "BPM,3", "Envite,4"})
  void should_ReturnIntersection_When_OrgLevelAndCurrentUserAreGiven(String orgLevel, int level) {
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + String.format("?orgLevel%d=%s", level, orgLevel)
            + "&current-user";

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getContent())
        .haveExactly(
            1,
            new Condition<>(
                user -> user.getUserId().equals("teamlead-1"), "user with id teamlead-1"));
    responseEntity.getBody().getContent().stream()
        .filter(user -> !user.getUserId().equals("teamlead-1"))
        .forEach(
            user -> {
              if (level == 1) {
                assertThat(user.getOrgLevel1()).isEqualTo(orgLevel);
              } else if (level == 2) {
                assertThat(user.getOrgLevel2()).isEqualTo(orgLevel);
              } else if (level == 3) {
                assertThat(user.getOrgLevel3()).isEqualTo(orgLevel);
              } else if (level == 4) {
                assertThat(user.getOrgLevel4()).isEqualTo(orgLevel);
              }
            });
  }

  @ParameterizedTest
  @CsvSource({"KADAI,1", "Human Workflow,2", "BPM,3", "Envite,4"})
  void should_ReturnIntersection_When_OrgLevelAndUserIdsAndCurrentUserAreGiven(
      String orgLevel, int level) {
    String url =
        restHelper.toUrl(RestEndpoints.URL_USERS)
            + String.format("?orgLevel%d=%s", level, orgLevel)
            + "&current-user"
            + "&user-id=user-1-1"
            + "&user-id=user-2-1"
            + "&user-id=teamlead-1";

    ResponseEntity<UserCollectionRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(UserCollectionRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();

    Collection<UserRepresentationModel> users = responseEntity.getBody().getContent();

    assertThat(users)
        .haveExactly(
            1,
            new Condition<>(
                user -> user.getUserId().equals("teamlead-1"), "user with id teamlead-1"));
    assertThat(users).hasSize(3);
    users.stream()
        .filter(user -> !user.getUserId().equals("teamlead-1"))
        .forEach(
            user -> {
              if (level == 1) {
                assertThat(user.getOrgLevel1()).isEqualTo(orgLevel);
              } else if (level == 2) {
                assertThat(user.getOrgLevel2()).isEqualTo(orgLevel);
              } else if (level == 3) {
                assertThat(user.getOrgLevel3()).isEqualTo(orgLevel);
              } else if (level == 4) {
                assertThat(user.getOrgLevel4()).isEqualTo(orgLevel);
              }
            });
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void should_CreateValidUser_When_CallingCreateEndpointWithAllAttributesExceptDomains() {
    UserRepresentationModel newUser = new UserRepresentationModel();
    newUser.setUserId("12345");
    newUser.setGroups(Set.of("group1", "group2"));
    newUser.setPermissions(Set.of("perm1", "perm2"));
    newUser.setFirstName("Hans");
    newUser.setLastName("Georg");
    newUser.setFullName("Georg, Hans");
    newUser.setLongName("Georg, Hans - (12345)");
    newUser.setEmail("hans.georg@web.com");
    newUser.setMobilePhone("017325862");
    newUser.setPhone("017325862");
    newUser.setData("data");
    newUser.setOrgLevel4("orgLevel4");
    newUser.setOrgLevel3("orgLevel3");
    newUser.setOrgLevel2("orgLevel2");
    newUser.setOrgLevel1("orgLevel1");

    String url = restHelper.toUrl(RestEndpoints.URL_USERS);

    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("teamlead-1");

    ResponseEntity<UserRepresentationModel> responseEntity =
        CLIENT
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .body(newUser)
            .retrieve()
            .toEntity(UserRepresentationModel.class);

    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    url = restHelper.toUrl(RestEndpoints.URL_USERS_ID, "12345");

    responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(UserRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull().isEqualTo(newUser);
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void should_CreateValidUser_When_CallingCreateEndpointWithoutGroupsPermissionsDomains() {
    UserRepresentationModel newUser = new UserRepresentationModel();
    newUser.setUserId("123456");
    newUser.setFirstName("Hans");
    newUser.setLastName("Georg");
    newUser.setFullName("Georg, Hans");
    newUser.setLongName("Georg, Hans - (12345)");
    newUser.setEmail("hans.georg@web.com");
    newUser.setMobilePhone("017325862");

    String url = restHelper.toUrl(RestEndpoints.URL_USERS);

    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("teamlead-1");

    ResponseEntity<UserRepresentationModel> responseEntity =
        CLIENT
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .body(newUser)
            .retrieve()
            .toEntity(UserRepresentationModel.class);

    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    url = restHelper.toUrl(RestEndpoints.URL_USERS_ID, "123456");

    responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(UserRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull().isEqualTo(newUser);
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void should_UpdateExistingUser_When_CallingUpdateEndpoint() {
    String url = restHelper.toUrl(RestEndpoints.URL_USERS_ID, "teamlead-1");

    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("teamlead-1");

    ResponseEntity<UserRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(UserRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();

    UserRepresentationModel model = responseEntity.getBody();
    model.setLastName("Mueller");

    responseEntity =
        CLIENT
            .put()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .body(model)
            .retrieve()
            .toEntity(UserRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getLastName()).isEqualTo("Mueller");
  }

  @Test
  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  void should_DeleteExistingUser_When_CallingDeleteEndpoint() {
    String url = restHelper.toUrl(RestEndpoints.URL_USERS_ID, "user-1-3");

    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("teamlead-1");

    ResponseEntity<UserRepresentationModel> responseEntity =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(UserRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getUserId()).isEqualTo("user-1-3");

    responseEntity =
        CLIENT
            .delete()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(UserRepresentationModel.class);

    assertThat(responseEntity.getBody()).isNull();

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(httpHeaders))
                .retrieve()
                .toEntity(UserRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }
}
