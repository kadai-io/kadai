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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.rest.RestEndpoints;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import io.kadai.sampledata.SampleDataGenerator;
import io.kadai.workbasket.rest.models.WorkbasketAccessItemRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketDefinitionCollectionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketDefinitionRepresentationModel;
import io.kadai.workbasket.rest.models.WorkbasketRepresentationModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/** Integration tests for WorkbasketDefinitionController. */
@KadaiSpringBootTest
class WorkbasketDefinitionControllerIntTest {

  private final JsonMapper jsonMapper;
  private final RestHelper restHelper;
  private final RestClient restClient;
  private final DataSource dataSource;
  private String createdWorkbasketId;

  @Value("${kadai.schemaName:KADAI}")
  String schemaName;

  @Autowired
  WorkbasketDefinitionControllerIntTest(
      JsonMapper jsonMapper, RestHelper restHelper, RestClient restClient, DataSource dataSource) {
    this.jsonMapper = jsonMapper;
    this.restHelper = restHelper;
    this.restClient = restClient;
    this.dataSource = dataSource;
  }

  @BeforeEach
  void resetDb() {
    SampleDataGenerator sampleDataGenerator = new SampleDataGenerator(dataSource, schemaName);
    sampleDataGenerator.generateSampleData();
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
  void testExportWorkbasketFromDomain() {
    ResponseEntity<WorkbasketDefinitionCollectionRepresentationModel> response =
        executeExportRequestForDomain("DOMAIN_A");

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getContent())
        .hasOnlyElementsOfType(WorkbasketDefinitionRepresentationModel.class);

    boolean allAuthorizationsAreEmpty = true;
    boolean allDistributionTargetsAreEmpty = true;
    for (WorkbasketDefinitionRepresentationModel workbasketDefinition :
        response.getBody().getContent()) {
      if (allAuthorizationsAreEmpty && !workbasketDefinition.getAuthorizations().isEmpty()) {
        allAuthorizationsAreEmpty = false;
      }
      if (allDistributionTargetsAreEmpty
          && !workbasketDefinition.getDistributionTargets().isEmpty()) {
        allDistributionTargetsAreEmpty = false;
      }
      if (!allAuthorizationsAreEmpty && !allDistributionTargetsAreEmpty) {
        break;
      }
    }
    assertThat(allDistributionTargetsAreEmpty).isFalse();
    assertThat(allAuthorizationsAreEmpty).isFalse();
  }

  @Test
  void should_NotContainAnyLinks_When_ExportIsRequested() {
    ResponseEntity<WorkbasketDefinitionCollectionRepresentationModel> response =
        executeExportRequestForDomain("DOMAIN_A");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isPresent();
    assertThat(response.getBody().getContent())
        .extracting(WorkbasketDefinitionRepresentationModel::getWorkbasket)
        .extracting(WorkbasketRepresentationModel::getLinks)
        .extracting(Links::isEmpty)
        .containsOnly(true);
  }

  @Test
  void testExportWorkbasketsFromWrongDomain() {
    ResponseEntity<WorkbasketDefinitionCollectionRepresentationModel> response =
        executeExportRequestForDomain("wrongDomain");
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getContent()).isEmpty();
  }

  @Test
  void testImportEveryWorkbasketFromDomainA() throws Exception {
    WorkbasketDefinitionCollectionRepresentationModel wbList =
        executeExportRequestForDomain("DOMAIN_A").getBody();
    assertThat(wbList).isNotNull();
    for (WorkbasketDefinitionRepresentationModel w : wbList.getContent()) {
      expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, w);
    }
  }

  @Test
  void should_PreserveCreatedForExistingImport() throws Exception {
    WorkbasketDefinitionCollectionRepresentationModel export =
        executeExportRequestForDomain("DOMAIN_A").getBody();
    assertThat(export).isNotNull();
    WorkbasketDefinitionRepresentationModel definition =
        export.getContent().stream()
            .filter(candidate -> !candidate.getAuthorizations().isEmpty())
            .findFirst()
            .orElseThrow();
    String existingId = definition.getWorkbasket().getWorkbasketId();
    Instant existingCreated = definition.getWorkbasket().getCreated();

    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, definition);

    WorkbasketDefinitionRepresentationModel existingAfterImport =
        executeExportRequestForDomain("DOMAIN_A").getBody().getContent().stream()
            .filter(
                candidate ->
                    candidate.getWorkbasket().getWorkbasketId().equals(existingId))
            .findFirst()
            .orElseThrow();
    assertThat(existingAfterImport.getWorkbasket().getCreated()).isEqualTo(existingCreated);

    definition.getWorkbasket().setCreated(Instant.EPOCH);
    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, definition);
    WorkbasketDefinitionRepresentationModel existingAfterExternalTimestampImport =
        executeExportRequestForDomain("DOMAIN_A").getBody().getContent().stream()
            .filter(
                candidate ->
                    candidate.getWorkbasket().getWorkbasketId().equals(existingId))
            .findFirst()
            .orElseThrow();
    assertThat(existingAfterExternalTimestampImport.getWorkbasket().getCreated())
        .isEqualTo(existingCreated);
  }

  @Test
  void should_GenerateNewIdentityAndRemapRelationsForNewImport() throws Exception {
    WorkbasketDefinitionCollectionRepresentationModel export =
        executeExportRequestForDomain("DOMAIN_A").getBody();
    assertThat(export).isNotNull();
    WorkbasketDefinitionRepresentationModel definition =
        export.getContent().stream()
            .filter(candidate -> !candidate.getAuthorizations().isEmpty())
            .findFirst()
            .orElseThrow();
    final String existingId = definition.getWorkbasket().getWorkbasketId();
    Set<String> exportedAccessItemIds = new HashSet<>();
    definition.getAuthorizations().stream()
        .map(WorkbasketAccessItemRepresentationModel::getAccessItemId)
        .forEach(exportedAccessItemIds::add);

    String importedOldId = "WBI:" + java.util.UUID.randomUUID();
    String newKey = "imported-workbasket-" + java.util.UUID.randomUUID();
    changeWorkbasketIdOrKey(definition, importedOldId, newKey);
    definition.getWorkbasket().setCreated(Instant.EPOCH);
    definition.setDistributionTargets(Set.of(existingId));
    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, definition);

    WorkbasketDefinitionRepresentationModel newAfterImport =
        executeExportRequestForDomain("DOMAIN_A").getBody().getContent().stream()
            .filter(candidate -> candidate.getWorkbasket().getKey().equals(newKey))
            .findFirst()
            .orElseThrow();
    createdWorkbasketId = newAfterImport.getWorkbasket().getWorkbasketId();
    assertThat(newAfterImport.getWorkbasket().getWorkbasketId())
        .isNotEqualTo(importedOldId)
        .isNotEqualTo(existingId);
    assertThat(newAfterImport.getWorkbasket().getCreated())
        .isNotNull()
        .isNotEqualTo(Instant.EPOCH);
    assertThat(newAfterImport.getWorkbasket().getModified())
        .isNotNull()
        .isNotEqualTo(Instant.EPOCH);
    assertThat(newAfterImport.getDistributionTargets()).containsExactly(existingId);
    assertThat(newAfterImport.getAuthorizations())
        .hasSameSizeAs(definition.getAuthorizations());
    for (WorkbasketAccessItemRepresentationModel authorization :
        newAfterImport.getAuthorizations()) {
      assertThat(exportedAccessItemIds).doesNotContain(authorization.getAccessItemId());
      assertThat(authorization.getWorkbasketId()).isEqualTo(createdWorkbasketId);
      assertThat(authorization.getWorkbasketKey()).isEqualTo(newKey);
    }
  }

  @Test
  void testImportWorkbasketWithoutDistributionTargets() throws Exception {
    WorkbasketDefinitionCollectionRepresentationModel pagedModel =
        executeExportRequestForDomain("DOMAIN_A").getBody();
    assertThat(pagedModel).isNotNull();
    WorkbasketDefinitionRepresentationModel w = pagedModel.getContent().iterator().next();
    w.setDistributionTargets(new HashSet<>());
    this.expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, w);
    w.getWorkbasket().setKey("newKey");
    w.getAuthorizations().forEach(authorization -> authorization.setWorkbasketKey("newKey"));
    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, w);
  }

  @Test
  void testImportWorkbasketWithDistributionTargetsInImportFile() throws Exception {
    WorkbasketDefinitionCollectionRepresentationModel wbList =
        executeExportRequestForDomain("DOMAIN_A").getBody();
    assertThat(wbList).isNotNull();
    Iterator<WorkbasketDefinitionRepresentationModel> iterator = wbList.getContent().iterator();

    WorkbasketDefinitionRepresentationModel w = iterator.next();
    w.setDistributionTargets(new HashSet<>());
    String letMeBeYourDistributionTarget = w.getWorkbasket().getWorkbasketId();
    WorkbasketDefinitionRepresentationModel w2 = iterator.next();
    w2.setDistributionTargets(Set.of(letMeBeYourDistributionTarget));
    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, w, w2);

    this.changeWorkbasketIdOrKey(w, "fancyNewId", null);
    w2.setDistributionTargets(Set.of("fancyNewId"));
    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, w, w2);

    this.changeWorkbasketIdOrKey(w, null, "nowImANewWB");
    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, w, w2);

    this.changeWorkbasketIdOrKey(w2, null, "nowImAlsoANewWB");
    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, w, w2);
  }

  @Test
  void testImportWorkbasketWithDistributionTargetsInSystem() throws Exception {
    WorkbasketDefinitionCollectionRepresentationModel wbList =
        executeExportRequestForDomain("DOMAIN_A").getBody();

    assertThat(wbList).isNotNull();
    List<WorkbasketDefinitionRepresentationModel> content = new ArrayList<>(wbList.getContent());

    content.removeIf(definition -> definition.getDistributionTargets().isEmpty());
    WorkbasketDefinitionRepresentationModel w = content.iterator().next();
    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, w);

    changeWorkbasketIdOrKey(w, null, "new");
    expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.NO_CONTENT, w);
  }

  @Test
  void testImportWorkbasketWithDistributionTargetsNotInSystem() {
    WorkbasketDefinitionCollectionRepresentationModel wbList =
        executeExportRequestForDomain("DOMAIN_A").getBody();

    assertThat(wbList).isNotNull();
    WorkbasketDefinitionRepresentationModel w = wbList.getContent().iterator().next();
    w.setDistributionTargets(Set.of("invalidWorkbasketId"));
    ThrowingCallable httpCall =
        () -> expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.BAD_REQUEST, w);
    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);

    w.getWorkbasket().setKey("anotherNewKey");

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testFailOnImportDuplicates() {
    WorkbasketDefinitionCollectionRepresentationModel pagedModel =
        executeExportRequestForDomain("DOMAIN_A").getBody();

    assertThat(pagedModel).isNotNull();
    WorkbasketDefinitionRepresentationModel w = pagedModel.getContent().iterator().next();
    ThrowingCallable httpCall =
        () -> expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.CONFLICT, w, w);
    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testNoErrorWhenImportWithSameIdButDifferentKeyAndDomain() throws Exception {
    WorkbasketDefinitionCollectionRepresentationModel wbList =
        executeExportRequestForDomain("DOMAIN_A").getBody();

    assertThat(wbList).isNotNull();
    Iterator<WorkbasketDefinitionRepresentationModel> iterator = wbList.getContent().iterator();
    WorkbasketDefinitionRepresentationModel w = iterator.next();
    WorkbasketDefinitionRepresentationModel differentLogicalId = iterator.next();
    this.changeWorkbasketIdOrKey(differentLogicalId, w.getWorkbasket().getWorkbasketId(), null);

    // breaks the logic but not the script- should we really allow this case?
    WorkbasketDefinitionRepresentationModel theDestroyer = iterator.next();
    theDestroyer.setDistributionTargets(
        Set.of(differentLogicalId.getWorkbasket().getWorkbasketId()));

    expectStatusWhenExecutingImportRequestOfWorkbaskets(
        HttpStatus.NO_CONTENT, w, differentLogicalId, theDestroyer);
  }

  @Test
  void testErrorWhenImportWithSameAccessIdAndWorkbasket() {
    WorkbasketDefinitionCollectionRepresentationModel pagedModel =
        executeExportRequestForDomain("DOMAIN_A").getBody();

    assertThat(pagedModel).isNotNull();
    WorkbasketDefinitionRepresentationModel w = pagedModel.getContent().iterator().next();

    ThrowingCallable httpCall =
        () -> expectStatusWhenExecutingImportRequestOfWorkbaskets(HttpStatus.CONFLICT, w, w);
    assertThatThrownBy(httpCall).isInstanceOf(HttpStatusCodeException.class);
  }

  private void changeWorkbasketIdOrKey(
      WorkbasketDefinitionRepresentationModel w, String newId, String newKey) {
    if (newId != null && !newId.isEmpty()) {
      w.getWorkbasket().setWorkbasketId(newId);
      w.getAuthorizations().forEach(auth -> auth.setWorkbasketId(newId));
    }
    if (newKey != null && !newKey.isEmpty()) {
      w.getWorkbasket().setKey(newKey);
      w.getAuthorizations().forEach(auth -> auth.setWorkbasketKey(newKey));
    }
  }

  private ResponseEntity<WorkbasketDefinitionCollectionRepresentationModel>
      executeExportRequestForDomain(String domain) {
    String url = restHelper.toUrl(RestEndpoints.URL_WORKBASKET_DEFINITIONS) + "?domain=" + domain;

    return restClient
        .get()
        .uri(url)
        .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
        .retrieve()
        .toEntity(WorkbasketDefinitionCollectionRepresentationModel.class);
  }

  private void expectStatusWhenExecutingImportRequestOfWorkbaskets(
      HttpStatus expectedStatus, WorkbasketDefinitionRepresentationModel... workbaskets)
      throws Exception {
    WorkbasketDefinitionCollectionRepresentationModel pagedModel =
        new WorkbasketDefinitionCollectionRepresentationModel(List.of(workbaskets));
    expectStatusWhenExecutingImportRequestOfWorkbaskets(expectedStatus, pagedModel);
  }

  private void expectStatusWhenExecutingImportRequestOfWorkbaskets(
      HttpStatus expectedStatus, WorkbasketDefinitionCollectionRepresentationModel pageModel)
      throws Exception {
    File tmpFile = File.createTempFile("test", ".tmp");
    try (FileOutputStream out = new FileOutputStream(tmpFile);
        OutputStreamWriter writer = new OutputStreamWriter(out, UTF_8)) {
      jsonMapper.writeValue(writer, pageModel);
    }

    MultiValueMap<String, FileSystemResource> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(tmpFile));

    HttpHeaders headers = RestHelper.generateHeadersForUser("businessadmin");
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    String serverUrl = restHelper.toUrl(RestEndpoints.URL_WORKBASKET_DEFINITIONS);

    ResponseEntity<Void> responseImport =
        restClient
            .post()
            .uri(serverUrl)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(body)
            .retrieve()
            .toEntity(Void.class);
    assertThat(responseImport.getStatusCode()).isEqualTo(expectedStatus);
  }
}
