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

package io.kadai.workbasket.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.rest.RestEndpoints;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import io.kadai.workbasket.api.WorkbasketType;
import io.kadai.workbasket.rest.models.DistributionTargetsCollectionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemCollectionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketSummaryPagedRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketSummaryRepresentationModel;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/** Test WorkbasketController. */
@KadaiSpringBootTest
class WorkbasketControllerIntTest {

  private final RestHelper restHelper;
  private final RestClient restClient;
  private final JsonMapper jsonMapper;
  private String createdWorkbasketId;

  @Autowired
  WorkbasketControllerIntTest(
      RestHelper restHelper, RestClient restClient, JsonMapper jsonMapper) {
    this.restHelper = restHelper;
    this.restClient = restClient;
    this.jsonMapper = jsonMapper;
  }

  @AfterEach
  void cleanUpCreatedWorkbasket() {
    if (createdWorkbasketId != null) {
      restClient
          .delete()
          .uri(restHelper.toUrl(RestEndpoints.URL_WORKBASKET_ID, createdWorkbasketId))
          .headers(
              headers -> headers.addAll(RestHelper.generateHeadersForUser("businessadmin")))
          .retrieve()
          .toBodilessEntity();
    }
  }

  @Test
  void testGetWorkbasket() {
    final String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID, "WBI%3A100000000000000000000000000000000006");

    ResponseEntity<WorkbasketRepresentationModel> response =
        restClient
            .get()
            .uri(URLDecoder.decode(url, StandardCharsets.UTF_8))
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).contains(Link.of(url));
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaTypes.HAL_JSON);
  }

  @Test
  void testGetAllWorkbaskets() {
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET);

    ResponseEntity<WorkbasketSummaryPagedRepresentationModel> response =
        restClient
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(WorkbasketSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
  }

  @Test
  void testGetAllWorkbasketsBusinessAdminHasOpenPermission() {
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET) + "?required-permission=OPEN";

    ResponseEntity<WorkbasketSummaryPagedRepresentationModel> response =
        restClient
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(WorkbasketSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getRequiredLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(6);
  }

  @Test
  void testGetAllWorkbasketsKeepingFilters() {
    String parameters = "?type=PERSONAL&sort-by=KEY&order=DESCENDING";
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET) + parameters;

    ResponseEntity<WorkbasketSummaryPagedRepresentationModel> response =
        restClient
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(WorkbasketSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getBody().getRequiredLink(IanaLinkRelations.SELF).getHref())
        .endsWith(parameters);
  }

  @Test
  void testUpdateWorkbasketWithConcurrentModificationShouldThrowException() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID, "WBI:100000000000000000000000000000000001");

    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("teamlead-1");

    ResponseEntity<WorkbasketRepresentationModel> initialWorkbasketResourceRequestResponse =
        restClient
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class);
    WorkbasketRepresentationModel workbasketRepresentationModel =
        initialWorkbasketResourceRequestResponse.getBody();
    assertThat(workbasketRepresentationModel).isNotNull();

    workbasketRepresentationModel.setKey("GPK_KSC");
    workbasketRepresentationModel.setDomain("DOMAIN_A");
    workbasketRepresentationModel.setType(WorkbasketType.PERSONAL);
    workbasketRepresentationModel.setName("was auch immer");
    workbasketRepresentationModel.setOwner("Joerg");
    workbasketRepresentationModel.setModified(Instant.now());

    ThrowingCallable httpCall =
        () ->
            restClient
                .put()
                .uri(url)
                .headers(headers -> headers.addAll(httpHeaders))
                .body(workbasketRepresentationModel)
                .retrieve()
                .toEntity(WorkbasketRepresentationModel.class);
    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void testUpdateWorkbasketOfNonExistingWorkbasketShouldThrowException() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID, "WBI:100004857400039500000999999999999999");

    ThrowingCallable httpCall =
        () ->
            restClient
                .get()
                .uri(url)
                .headers(
                    headers -> headers.addAll(RestHelper.generateHeadersForUser("businessadmin")))
                .retrieve()
                .toEntity(WorkbasketRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void should_UpdateWorkbasket() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID, "WBI:100000000000000000000000000000000001");

    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("admin");

    ResponseEntity<WorkbasketRepresentationModel> response =
        restClient
            .get()
            .uri(URLDecoder.decode(url, StandardCharsets.UTF_8))
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    WorkbasketRepresentationModel workbasketToUpdate = response.getBody();
    workbasketToUpdate.setName("new name");

    ResponseEntity<WorkbasketRepresentationModel> responseUpdate =
        restClient
            .put()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .body(workbasketToUpdate)
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class);

    assertThat(responseUpdate.getBody()).isNotNull();
    assertThat(responseUpdate.getBody().getName()).isEqualTo("new name");
  }

  @Test
  void should_PreserveCallerSelectedWorkbasketIdAndGenerateTimestamps() {
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET);
    HttpHeaders headers = RestHelper.generateHeadersForUser("businessadmin");
    String chosenId = "caller-" + UUID.randomUUID().toString().substring(0, 30);
    WorkbasketRepresentationModel request =
        newWorkbasketRepresentation("chosen-" + UUID.randomUUID());
    request.setWorkbasketId(chosenId);

    ResponseEntity<WorkbasketRepresentationModel> createResponse =
        restClient
            .post()
            .uri(url)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(request)
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class);
    WorkbasketRepresentationModel created = createResponse.getBody();
    if (created != null) {
      createdWorkbasketId = created.getWorkbasketId();
    }
    assertThat(created).isNotNull();
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(created.getWorkbasketId()).isEqualTo(chosenId);
    assertThat(created.getCreated()).isNotNull().isNotEqualTo(Instant.EPOCH);
    assertThat(created.getModified()).isNotNull().isNotEqualTo(Instant.EPOCH);
    assertThat(created.getName()).isEqualTo(request.getName());

    WorkbasketRepresentationModel persisted = getWorkbasket(chosenId, headers);
    assertThat(persisted.getWorkbasketId()).isEqualTo(chosenId);
    assertThat(persisted.getCreated()).isEqualTo(created.getCreated());
    assertThat(persisted.getModified()).isEqualTo(created.getModified());
  }

  @Test
  void should_GenerateWorkbasketIdWhenOmitted() {
    HttpHeaders headers = RestHelper.generateHeadersForUser("businessadmin");
    WorkbasketRepresentationModel request =
        newWorkbasketRepresentation("generated-" + UUID.randomUUID());

    WorkbasketRepresentationModel created =
        restClient
            .post()
            .uri(restHelper.toUrl(RestEndpoints.URL_WORKBASKET))
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(request)
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class)
            .getBody();
    assertThat(created).isNotNull();
    createdWorkbasketId = created.getWorkbasketId();
    assertThat(createdWorkbasketId).startsWith("WBI:");
    assertThat(created.getCreated()).isNotNull().isNotEqualTo(Instant.EPOCH);
    assertThat(created.getModified()).isNotNull().isNotEqualTo(Instant.EPOCH);

    WorkbasketRepresentationModel persisted = getWorkbasket(createdWorkbasketId, headers);
    assertThat(persisted.getWorkbasketId()).isEqualTo(createdWorkbasketId);
    assertThat(persisted.getCreated()).isEqualTo(created.getCreated());
    assertThat(persisted.getModified()).isEqualTo(created.getModified());
  }

  @Test
  void should_PreserveCreatedWhenOmittedAndRejectChangedOrExplicitlyClearedCreated()
      throws Exception {
    HttpHeaders headers = RestHelper.generateHeadersForUser("businessadmin");
    WorkbasketRepresentationModel createRequest =
        newWorkbasketRepresentation("created-time-" + UUID.randomUUID());
    WorkbasketRepresentationModel created =
        restClient
            .post()
            .uri(restHelper.toUrl(RestEndpoints.URL_WORKBASKET))
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(createRequest)
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class)
            .getBody();
    if (created != null) {
      createdWorkbasketId = created.getWorkbasketId();
    }
    assertThat(created).isNotNull();
    String workbasketId = created.getWorkbasketId();
    Instant originalCreated = created.getCreated();
    Instant originalModified = created.getModified();
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET_ID, workbasketId);

    assertBadRequest(
        () -> putWorkbasket(url, updateBody(created, originalCreated.plusSeconds(1)), headers));
    WorkbasketRepresentationModel afterChangedCreated = getWorkbasket(workbasketId, headers);
    assertThat(afterChangedCreated.getCreated()).isEqualTo(originalCreated);
    assertThat(afterChangedCreated.getModified()).isEqualTo(originalModified);

    assertBadRequest(
        () -> putWorkbasket(url, updateBodyWithExplicitNullCreated(created), headers));
    WorkbasketRepresentationModel afterClearCreated = getWorkbasket(workbasketId, headers);
    assertThat(afterClearCreated.getCreated()).isEqualTo(originalCreated);
    assertThat(afterClearCreated.getModified()).isEqualTo(originalModified);

    created.setName("Updated name");
    created.setDescription("Updated description");
    created.setType(WorkbasketType.TOPIC);
    created.setOwner("updated-owner");
    created.setCustom1("updated custom value");
    created.setOrgLevel1("updated organization");
    created.setMarkedForDeletion(true);
    ResponseEntity<WorkbasketRepresentationModel> omittedCreatedResponse =
        putWorkbasket(url, updateBodyWithoutCreated(created), headers);
    assertThat(omittedCreatedResponse.getBody()).isNotNull();
    assertThat(omittedCreatedResponse.getBody().getCreated()).isEqualTo(originalCreated);
    WorkbasketRepresentationModel persisted = getWorkbasket(workbasketId, headers);
    assertThat(persisted.getCreated()).isEqualTo(originalCreated);
    assertThat(persisted.getName()).isEqualTo("Updated name");
    assertThat(persisted.getDescription()).isEqualTo("Updated description");
    assertThat(persisted.getType()).isEqualTo(WorkbasketType.TOPIC);
    assertThat(persisted.getOwner()).isEqualTo("updated-owner");
    assertThat(persisted.getCustom1()).isEqualTo("updated custom value");
    assertThat(persisted.getOrgLevel1()).isEqualTo("updated organization");
    assertThat(persisted.getMarkedForDeletion()).isTrue();
  }

  @Test
  void should_RejectChangesToWorkbasketKeyAndDomain() {
    HttpHeaders headers = RestHelper.generateHeadersForUser("businessadmin");
    WorkbasketRepresentationModel created =
        restClient
            .post()
            .uri(restHelper.toUrl(RestEndpoints.URL_WORKBASKET))
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(newWorkbasketRepresentation("immutable-" + UUID.randomUUID()))
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class)
            .getBody();
    assertThat(created).isNotNull();
    createdWorkbasketId = created.getWorkbasketId();
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET_ID, createdWorkbasketId);
    String originalKey = created.getKey();
    final String originalDomain = created.getDomain();

    created.setKey("different-" + UUID.randomUUID());
    assertNotFound(() -> putWorkbasket(url, updateBody(created, created.getCreated()), headers));
    created.setKey(originalKey);

    created.setDomain("DOMAIN_B");
    assertNotFound(() -> putWorkbasket(url, updateBody(created, created.getCreated()), headers));

    WorkbasketRepresentationModel persisted = getWorkbasket(createdWorkbasketId, headers);
    assertThat(persisted.getKey()).isEqualTo(originalKey);
    assertThat(persisted.getDomain()).isEqualTo(originalDomain);
  }

  @Test
  void should_KeepAccessItemIdentityOnUpdateAndRejectUnknownIdWithoutReplacingCollection() {
    HttpHeaders headers = RestHelper.generateHeadersForUser("businessadmin");
    WorkbasketRepresentationModel createRequest =
        newWorkbasketRepresentation("access-item-identity-" + UUID.randomUUID());
    WorkbasketRepresentationModel created =
        restClient
            .post()
            .uri(restHelper.toUrl(RestEndpoints.URL_WORKBASKET))
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(createRequest)
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class)
            .getBody();
    if (created != null) {
      createdWorkbasketId = created.getWorkbasketId();
    }
    assertThat(created).isNotNull();
    String workbasketId = created.getWorkbasketId();
    String accessItemsUrl =
        restHelper.toUrl(RestEndpoints.URL_WORKBASKET_ID_ACCESS_ITEMS, workbasketId);

    WorkbasketAccessItemRepresentationModel newItem =
        new WorkbasketAccessItemRepresentationModel();
    newItem.setAccessId("rest-" + UUID.randomUUID());
    newItem.setWorkbasketKey("caller-supplied-key");
    WorkbasketAccessItemCollectionRepresentationModel firstResponse =
        putAccessItems(accessItemsUrl, List.of(newItem), headers);
    WorkbasketAccessItemRepresentationModel persistedItem =
        firstResponse.getContent().iterator().next();
    String accessItemId = persistedItem.getAccessItemId();
    assertThat(accessItemId).isNotBlank();
    assertThat(persistedItem.getWorkbasketId()).isEqualTo(workbasketId);
    assertThat(persistedItem.getWorkbasketKey()).isEqualTo(created.getKey());

    WorkbasketAccessItemRepresentationModel permissionUpdate =
        new WorkbasketAccessItemRepresentationModel();
    permissionUpdate.setAccessItemId(accessItemId);
    permissionUpdate.setAccessId(persistedItem.getAccessId());
    permissionUpdate.setWorkbasketId(workbasketId);
    permissionUpdate.setWorkbasketKey("another-fake-key");
    permissionUpdate.setPermRead(true);
    permissionUpdate.setPermOpen(true);
    WorkbasketAccessItemRepresentationModel updatedItem =
        putAccessItems(accessItemsUrl, List.of(permissionUpdate), headers)
            .getContent()
            .iterator()
            .next();
    assertThat(updatedItem.getAccessItemId()).isEqualTo(accessItemId);
    assertThat(updatedItem.isPermOpen()).isTrue();
    assertThat(updatedItem.getWorkbasketKey()).isEqualTo(created.getKey());

    WorkbasketAccessItemRepresentationModel unknownId =
        new WorkbasketAccessItemRepresentationModel();
    unknownId.setAccessItemId("unknown-" + UUID.randomUUID());
    unknownId.setWorkbasketId(workbasketId);
    unknownId.setAccessId(persistedItem.getAccessId());
    assertBadRequest(() -> putAccessItems(accessItemsUrl, List.of(unknownId), headers));

    WorkbasketAccessItemRepresentationModel stillPersisted =
        getAccessItems(accessItemsUrl, headers).getContent().iterator().next();
    assertThat(stillPersisted.getAccessItemId()).isEqualTo(accessItemId);
    assertThat(stillPersisted.isPermOpen()).isTrue();
  }

  @Test
  void testGetSecondPageSortedByKey() {
    String parameters = "?sort-by=KEY&order=DESCENDING&page-size=5&page=2";
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET) + parameters;

    ResponseEntity<WorkbasketSummaryPagedRepresentationModel> response =
        restClient
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(WorkbasketSummaryPagedRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).hasSize(5);
    assertThat(response.getBody().getContent().iterator().next().getKey()).isEqualTo("USER-1-1");
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.FIRST)).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.LAST)).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.NEXT)).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.PREV)).isNotNull();
    assertThat(response.getBody().getRequiredLink(IanaLinkRelations.SELF).getHref())
        .endsWith(parameters);
  }

  @Test
  void testMarkWorkbasketForDeletionAsBusinessAdminWithoutExplicitReadPermission() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID, "WBI:100000000000000000000000000000000005");

    ResponseEntity<?> response =
        restClient
            .delete()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("businessadmin")))
            .retrieve()
            .toEntity(Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  void statusCode423ShouldBeReturnedIfWorkbasketContainsNonCompletedTasks() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID, "WBI:100000000000000000000000000000000004");

    ThrowingCallable call =
        () ->
            restClient
                .delete()
                .uri(url)
                .headers(
                    headers -> headers.addAll(RestHelper.generateHeadersForUser("businessadmin")))
                .retrieve()
                .toEntity(Void.class);

    assertThatThrownBy(call)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.LOCKED);
  }

  @Test
  void testRemoveWorkbasketAsDistributionTarget() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION,
            "WBI:100000000000000000000000000000000007");
    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("teamlead-1");

    ResponseEntity<?> response =
        restClient
            .delete()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    String url2 =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION,
            "WBI:100000000000000000000000000000000002");
    ResponseEntity<DistributionTargetsCollectionRepresentationModel> response2 =
        restClient
            .get()
            .uri(url2)
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(DistributionTargetsCollectionRepresentationModel.class);

    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response2.getBody()).isNotNull();
    assertThat(response2.getBody().getContent())
        .extracting(WorkbasketSummaryRepresentationModel::getWorkbasketId)
        .doesNotContain("WBI:100000000000000000000000000000000007");
  }

  @Test
  void testGetWorkbasketAccessItems() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID_ACCESS_ITEMS,
            "WBI:100000000000000000000000000000000005");

    ResponseEntity<WorkbasketAccessItemCollectionRepresentationModel> response =
        restClient
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(WorkbasketAccessItemCollectionRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaTypes.HAL_JSON);
    assertThat(response.getBody().getContent()).hasSize(4);
  }

  @Test
  void should_SetWorkbasketAccessItemsForAWorkbasket() {
    String workbasketId = "WBI:000000000000000000000000000000000900";
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET_ID_ACCESS_ITEMS, workbasketId);
    WorkbasketAccessItemRepresentationModel wbAccessItem =
        new WorkbasketAccessItemRepresentationModel();
    wbAccessItem.setWorkbasketId(workbasketId);
    wbAccessItem.setAccessId("new-access-id");
    wbAccessItem.setAccessName("new-access-name");
    wbAccessItem.setPermOpen(true);

    WorkbasketAccessItemCollectionRepresentationModel repModel =
        new WorkbasketAccessItemCollectionRepresentationModel(List.of(wbAccessItem));

    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("admin");

    ResponseEntity<WorkbasketAccessItemCollectionRepresentationModel> response =
        restClient
            .put()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .body(repModel)
            .retrieve()
            .toEntity(WorkbasketAccessItemCollectionRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();

    ResponseEntity<WorkbasketAccessItemCollectionRepresentationModel> responseGetAccessItems =
        restClient
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .retrieve()
            .toEntity(WorkbasketAccessItemCollectionRepresentationModel.class);

    assertThat(responseGetAccessItems.getBody()).isNotNull();
    assertThat(responseGetAccessItems.getBody().getContent()).hasSize(1);
    Collection<WorkbasketAccessItemRepresentationModel> collection =
        responseGetAccessItems.getBody().getContent();
    Iterator<WorkbasketAccessItemRepresentationModel> iterator = collection.iterator();
    WorkbasketAccessItemRepresentationModel returnedWbAccessItem = iterator.next();
    assertThat(returnedWbAccessItem.getWorkbasketId()).isEqualTo(workbasketId);
    assertThat(returnedWbAccessItem.getAccessId()).isEqualTo("new-access-id");
    assertThat(returnedWbAccessItem.getAccessName()).isEqualTo("new-access-name");
    assertThat(returnedWbAccessItem.isPermOpen()).isTrue();
  }

  @Test
  void should_ThrowExceptionForSetWorkbasketAccessItems_When_PayloadContainsDuplicateAccessId() {
    String workbasketId = "WBI:000000000000000000000000000000000900";
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET_ID_ACCESS_ITEMS, workbasketId);
    WorkbasketAccessItemRepresentationModel wbAccessItem =
        new WorkbasketAccessItemRepresentationModel();
    wbAccessItem.setWorkbasketId(workbasketId);
    wbAccessItem.setAccessId("new-access-id");
    wbAccessItem.setAccessName("new-access-name");
    wbAccessItem.setPermOpen(true);

    WorkbasketAccessItemCollectionRepresentationModel repModel =
        new WorkbasketAccessItemCollectionRepresentationModel(List.of(wbAccessItem, wbAccessItem));

    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("admin");

    final ThrowingCallable call =
        () ->
            restClient
                .put()
                .uri(url)
                .headers(headers -> headers.addAll(httpHeaders))
                .body(repModel)
                .retrieve()
                .toEntity(WorkbasketAccessItemCollectionRepresentationModel.class);

    assertThatThrownBy(call)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testGetWorkbasketDistributionTargets() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION,
            "WBI:100000000000000000000000000000000001");

    ResponseEntity<DistributionTargetsCollectionRepresentationModel> response =
        restClient
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
            .retrieve()
            .toEntity(DistributionTargetsCollectionRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaTypes.HAL_JSON);
    assertThat(response.getBody().getContent()).hasSize(4);
  }

  @Test
  void should_SetAllDistributionTargets() {
    List<String> distributionTargets =
        List.of(
            "WBI:100000000000000000000000000000000003", "WBI:100000000000000000000000000000000004");
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_WORKBASKET_ID_DISTRIBUTION,
            "WBI:100000000000000000000000000000000002");

    HttpHeaders httpHeaders = RestHelper.generateHeadersForUser("admin");

    ResponseEntity<DistributionTargetsCollectionRepresentationModel> response =
        restClient
            .put()
            .uri(url)
            .headers(headers -> headers.addAll(httpHeaders))
            .body(distributionTargets)
            .retrieve()
            .toEntity(DistributionTargetsCollectionRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();

    ResponseEntity<DistributionTargetsCollectionRepresentationModel>
        responseGetDistributionTargets =
            restClient
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(httpHeaders))
                .retrieve()
                .toEntity(DistributionTargetsCollectionRepresentationModel.class);
    assertThat(responseGetDistributionTargets.getBody()).isNotNull();
    assertThat(responseGetDistributionTargets.getBody().getContent()).hasSize(2);
    assertThat(
            responseGetDistributionTargets.getBody().getContent().stream()
                .map(WorkbasketSummaryRepresentationModel::getWorkbasketId)
                .toList())
        .containsExactlyInAnyOrder(
            "WBI:100000000000000000000000000000000003", "WBI:100000000000000000000000000000000004");
  }

  @Test
  void should_ThrowException_When_ProvidingInvalidFilterParams() {
    String url =
        restHelper.toUrl(RestEndpoints.URL_WORKBASKET)
            + "?type=PERSONAL"
            + "&illegalParam=illegal"
            + "&anotherIllegalParam=stillIllegal"
            + "&sort-by=KEY&order=DESCENDING&page-size=5&page=2";

    ThrowingCallable httpCall =
        () ->
            restClient
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                .retrieve()
                .toEntity(WorkbasketSummaryPagedRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .hasMessageContaining(
            "Unknown request parameters found: [anotherIllegalParam, illegalParam]")
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_CreateWorkbasket() {
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET);

    WorkbasketRepresentationModel workbasketToCreate = new WorkbasketRepresentationModel();
    workbasketToCreate.setKey("newKey");
    workbasketToCreate.setDomain("DOMAIN_A");
    workbasketToCreate.setType(WorkbasketType.GROUP);
    workbasketToCreate.setName("this is a wonderful workbasket name");

    ResponseEntity<WorkbasketRepresentationModel> responseCreate =
        restClient
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .body(workbasketToCreate)
            .retrieve()
            .toEntity(WorkbasketRepresentationModel.class);

    assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(responseCreate.getBody()).isNotNull();

    String wbIdOfCreatedWb = responseCreate.getBody().getWorkbasketId();
    assertThat(wbIdOfCreatedWb).startsWith("WBI:");
  }

  private WorkbasketRepresentationModel newWorkbasketRepresentation(String key) {
    WorkbasketRepresentationModel workbasket = new WorkbasketRepresentationModel();
    workbasket.setKey(key);
    workbasket.setDomain("DOMAIN_A");
    workbasket.setType(WorkbasketType.GROUP);
    workbasket.setName("Test Workbasket " + key);
    workbasket.setDescription("REST owned identity test");
    workbasket.setCustom1("custom value");
    workbasket.setOrgLevel1("organization");
    workbasket.setCreated(Instant.EPOCH);
    workbasket.setModified(Instant.EPOCH);
    return workbasket;
  }

  private WorkbasketRepresentationModel getWorkbasket(String workbasketId, HttpHeaders headers) {
    return restClient
        .get()
        .uri(restHelper.toUrl(RestEndpoints.URL_WORKBASKET_ID, workbasketId))
        .headers(httpHeaders -> httpHeaders.addAll(headers))
        .retrieve()
        .toEntity(WorkbasketRepresentationModel.class)
        .getBody();
  }

  private String updateBody(WorkbasketRepresentationModel workbasket, Instant created)
      throws Exception {
    return updateBody(workbasket, true, created);
  }

  private String updateBody(
      WorkbasketRepresentationModel workbasket, boolean includeCreated, Instant created)
      throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("workbasketId", workbasket.getWorkbasketId());
    body.put("key", workbasket.getKey());
    body.put("domain", workbasket.getDomain());
    body.put("name", workbasket.getName());
    body.put("type", workbasket.getType());
    body.put("description", workbasket.getDescription());
    body.put("owner", workbasket.getOwner());
    body.put("custom1", workbasket.getCustom1());
    body.put("orgLevel1", workbasket.getOrgLevel1());
    body.put("markedForDeletion", workbasket.getMarkedForDeletion());
    body.put("modified", workbasket.getModified());
    if (includeCreated) {
      body.put("created", created);
    }
    return jsonMapper.writeValueAsString(body);
  }

  private String updateBodyWithoutCreated(WorkbasketRepresentationModel workbasket)
      throws Exception {
    return updateBody(workbasket, false, null);
  }

  private String updateBodyWithExplicitNullCreated(WorkbasketRepresentationModel workbasket)
      throws Exception {
    String bodyWithoutCreated = updateBodyWithoutCreated(workbasket);
    return bodyWithoutCreated.substring(0, bodyWithoutCreated.length() - 1)
        + ",\"created\":null}";
  }

  private ResponseEntity<WorkbasketRepresentationModel> putWorkbasket(
      String url, String body, HttpHeaders headers) {
    return restClient
        .put()
        .uri(url)
        .headers(httpHeaders -> httpHeaders.addAll(headers))
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .toEntity(WorkbasketRepresentationModel.class);
  }

  private WorkbasketAccessItemCollectionRepresentationModel putAccessItems(
      String url,
      Collection<WorkbasketAccessItemRepresentationModel> content,
      HttpHeaders headers) {
    return restClient
        .put()
        .uri(url)
        .headers(httpHeaders -> httpHeaders.addAll(headers))
        .body(new WorkbasketAccessItemCollectionRepresentationModel(content))
        .retrieve()
        .toEntity(WorkbasketAccessItemCollectionRepresentationModel.class)
        .getBody();
  }

  private WorkbasketAccessItemCollectionRepresentationModel getAccessItems(
      String url, HttpHeaders headers) {
    return restClient
        .get()
        .uri(url)
        .headers(httpHeaders -> httpHeaders.addAll(headers))
        .retrieve()
        .toEntity(WorkbasketAccessItemCollectionRepresentationModel.class)
        .getBody();
  }

  private void assertBadRequest(ThrowingCallable httpCall) {
    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  private void assertNotFound(ThrowingCallable httpCall) {
    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }
}
