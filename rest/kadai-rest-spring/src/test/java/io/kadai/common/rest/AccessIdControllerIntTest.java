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

package io.kadai.common.rest;

import static io.kadai.rest.test.RestHelper.CLIENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.internal.util.Pair;
import io.kadai.common.rest.models.AccessIdRepresentationModel;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

@KadaiSpringBootTest
class AccessIdControllerIntTest {

  private static final ParameterizedTypeReference<List<AccessIdRepresentationModel>>
      ACCESS_ID_LIST_TYPE = new ParameterizedTypeReference<>() {};

  private final RestHelper restHelper;

  @Autowired
  AccessIdControllerIntTest(RestHelper restHelper) {
    this.restHelper = restHelper;
  }

  @TestFactory
  Stream<DynamicTest> should_ResolveAccessId_When_SearchingForDnOrCn() {
    List<Pair<String, String>> list =
        List.of(
            Pair.of(
                "cn=ksc-users,cn=groups,OU=Test,O=KADAI", "cn=ksc-users,cn=groups,ou=test,o=kadai"),
            Pair.of("uid=teamlead-1,cn=users,OU=Test,O=KADAI", "teamlead-1"),
            Pair.of("ksc-use", "cn=ksc-users,cn=groups,ou=test,o=kadai"),
            Pair.of("user-b-2", "user-b-2"),
            Pair.of("User-b-2", "user-b-2"),
            Pair.of("cn=g01,cn=groups,OU=Test,O=KADAI", "kadai:callcenter:ab:ab/a:callcenter"),
            Pair.of("cn=g02,cn=groups,OU=Test,O=KADAI", "kadai:callcenter:ab:ab/a:callcenter-vip"));

    ThrowingConsumer<Pair<String, String>> test =
        pair -> {
          String url =
              restHelper.toUrl(RestEndpoints.URL_ACCESS_ID) + "?search-for=" + pair.getLeft();

          ResponseEntity<List<AccessIdRepresentationModel>> response =
              CLIENT
                  .get()
                  .uri(url)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .retrieve()
                  .toEntity(ACCESS_ID_LIST_TYPE);

          assertThat(response.getBody())
              .isNotNull()
              .extracting(AccessIdRepresentationModel::getAccessId)
              .containsExactly(pair.getRight());
        };

    return DynamicTest.stream(list.iterator(), pair -> "search for: " + pair.getLeft(), test);
  }

  @Test
  void should_ReturnEmptyResults_ifInvalidCharacterIsUsedInCondition() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_ACCESS_ID) + "?search-for=ksc-teamleads,cn=groups";

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getBody()).isNotNull().isEmpty();
  }

  @Test
  void testGetMatches() {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID) + "?search-for=rig";

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getBody())
        .isNotNull()
        .extracting(AccessIdRepresentationModel::getName)
        .containsExactlyInAnyOrder("Schläfrig, Tim", "Eifrig, Elena");
  }

  @Test
  void should_ReturnAccessIdWithUmlauten_ifBased64EncodedUserIsLookedUp() {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID) + "?search-for=läf";

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getBody())
        .isNotNull()
        .extracting(AccessIdRepresentationModel::getName)
        .containsExactlyInAnyOrder("Schläfrig, Tim");
  }

  @Test
  void should_ThrowException_When_SearchForIsTooShort() {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID) + "?search-for=al";

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                .retrieve()
                .toEntity(ACCESS_ID_LIST_TYPE);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .hasMessageContaining("Minimum Length is")
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_ReturnAccessIdsOfGroupsTheAccessIdIsMemberOf_ifAccessIdOfUserIsGiven() {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID_GROUPS) + "?access-id=teamlead-2";

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getBody())
        .isNotNull()
        .extracting(AccessIdRepresentationModel::getAccessId)
        .usingElementComparator(String.CASE_INSENSITIVE_ORDER)
        .containsExactlyInAnyOrder(
            "cn=ksc-teamleads,cn=groups,OU=Test,O=KADAI",
            "cn=business-admins,cn=groups,OU=Test,O=KADAI",
            "cn=monitor-users,cn=groups,OU=Test,O=KADAI",
            "cn=Organisationseinheit KSC 2,"
                + "cn=Organisationseinheit KSC,cn=organisation,OU=Test,O=KADAI");
  }

  @Test
  void should_ReturnAccessIdsOfPermissionsTheAccessIdIsMemberOf_ifAccessIdOfUserIsGiven() {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID_PERMISSIONS) + "?access-id=user-1-2";

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getBody())
        .isNotNull()
        .extracting(AccessIdRepresentationModel::getAccessId)
        .usingElementComparator(String.CASE_INSENSITIVE_ORDER)
        .containsExactlyInAnyOrder(
            "kadai:callcenter:ab:ab/a:callcenter", "kadai:callcenter:ab:ab/a:callcenter-vip");
  }

  @Test
  void should_ValidateAccessIdWithEqualsFilterAndReturnAccessIdsOfGroupsTheAccessIdIsMemberOf() {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID_GROUPS) + "?access-id=user-2-1";

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getBody())
        .isNotNull()
        .extracting(AccessIdRepresentationModel::getAccessId)
        .usingElementComparator(String.CASE_INSENSITIVE_ORDER)
        .containsExactlyInAnyOrder(
            "cn=ksc-users,cn=groups,ou=Test,O=KADAI",
            "cn=Organisationseinheit KSC 2,cn=Organisationseinheit KSC,"
                + "cn=organisation,ou=Test,O=KADAI");
  }

  @Test
  void should_ValidateAccessIdWithEqualsFilterAndReturnAccessIdsOfPermissionsAccessIdIsMemberOf() {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID_PERMISSIONS) + "?access-id=user-2-1";

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getBody())
        .isNotNull()
        .extracting(AccessIdRepresentationModel::getAccessId)
        .usingElementComparator(String.CASE_INSENSITIVE_ORDER)
        .containsExactlyInAnyOrder("kadai:callcenter:ab:ab/a:callcenter");
  }

  @Test
  void should_ReturnBadRequest_ifAccessIdOfUserContainsInvalidCharacter() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_ACCESS_ID_GROUPS) + "?access-id=teamlead-2,cn=users";

    ThrowingCallable call =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                .retrieve()
                .toEntity(ACCESS_ID_LIST_TYPE);

    assertThatThrownBy(call)
        .isInstanceOf(HttpStatusCodeException.class)
        .hasMessageContaining("The AccessId is invalid")
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_ReturnAccessIdsOfGroupsTheAccessIdIsMemberOf_ifAccessIdOfGroupIsGiven() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_ACCESS_ID_GROUPS)
            + "?access-id=cn=Organisationseinheit KSC 1,"
            + "cn=Organisationseinheit KSC,cn=organisation,OU=Test,O=KADAI";

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getBody())
        .isNotNull()
        .extracting(AccessIdRepresentationModel::getAccessId)
        .usingElementComparator(String.CASE_INSENSITIVE_ORDER)
        .containsExactlyInAnyOrder("cn=Organisationseinheit KSC,cn=organisation,OU=Test,O=KADAI");
  }

  @Test
  void should_ThrowNotAuthorizedException_ifCallerOfGroupRetrievalIsNotAdminOrBusinessAdmin() {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID_GROUPS) + "?access-id=teamlead-2";

    ThrowingCallable call =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                .retrieve()
                .toEntity(ACCESS_ID_LIST_TYPE);

    assertThatThrownBy(call)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_ThrowNotAuthorizedException_ifCallerOfPermissionRetrievalIsNotAdminOrBusinessAdmin() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_ACCESS_ID_PERMISSIONS) + "?access-id=teamlead-2";

    ThrowingCallable call =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                .retrieve()
                .toEntity(ACCESS_ID_LIST_TYPE);

    assertThatThrownBy(call)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_ThrowNotAuthorizedException_ifCallerOfValidationIsNotAdminOrBusinessAdmin() {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID) + "?search-for=al";

    ThrowingCallable call =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                .retrieve()
                .toEntity(ACCESS_ID_LIST_TYPE);

    assertThatThrownBy(call)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_ReturnAccessIds_When_SearchingUsersByNameOrAccessId() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_ACCESS_ID_WITH_NAME) + "?search-for=user-1&role=user";

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getBody())
        .isNotNull()
        .extracting(AccessIdRepresentationModel::getAccessId)
        .containsExactlyInAnyOrder("user-1-1", "user-1-2");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "cn=invalid-cn,cn=groups,OU=Test,O=KADAI",
        "cn=invalid-cn,cn=boop,OU=Test,O=KADAI"
      })
  void should_ReturnEmptyList_When_SearchedForValueIsAValidDnButDoesNotExist(String validDn) {
    String url = restHelper.toUrl(RestEndpoints.URL_ACCESS_ID) + "?search-for=" + validDn;

    ResponseEntity<List<AccessIdRepresentationModel>> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(ACCESS_ID_LIST_TYPE);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }
}
