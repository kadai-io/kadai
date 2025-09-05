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

package io.kadai.task.rest;

import static io.kadai.rest.test.RestHelper.CLIENT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.KadaiConfiguration;
import io.kadai.classification.rest.models.ClassificationSummaryRepresentationModel;
import io.kadai.common.internal.util.Pair;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import io.kadai.task.api.TaskState;
import io.kadai.task.rest.models.AttachmentRepresentationModel;
import io.kadai.task.rest.models.BulkOperationResultsRepresentationModel;
import io.kadai.task.rest.models.DistributionTasksRepresentationModel;
import io.kadai.task.rest.models.IsReadRepresentationModel;
import io.kadai.task.rest.models.ObjectReferenceRepresentationModel;
import io.kadai.task.rest.models.TaskIdListRepresentationModel;
import io.kadai.task.rest.models.TaskRepresentationModel;
import io.kadai.task.rest.models.TaskRepresentationModel.CustomAttribute;
import io.kadai.task.rest.models.TaskSummaryCollectionRepresentationModel;
import io.kadai.task.rest.models.TaskSummaryPagedRepresentationModel;
import io.kadai.task.rest.models.TaskSummaryRepresentationModel;
import io.kadai.task.rest.models.TransferTaskRepresentationModel;
import io.kadai.task.rest.routing.IntegrationTestTaskRouter;
import io.kadai.workbasket.rest.models.WorkbasketSummaryRepresentationModel;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.HttpStatusCodeException;
import org.testcontainers.shaded.com.google.common.collect.Lists;

/** Test Task Controller. */
@KadaiSpringBootTest
@SuppressWarnings({"raw", "unchecked"})
class TaskControllerIntTest {

  private static final ParameterizedTypeReference<Map<String, Object>>
      BULK_RESULT_TASKS_MODEL_TYPE = new ParameterizedTypeReference<>() {};

  private final RestHelper restHelper;
  @Autowired KadaiConfiguration kadaiConfiguration;

  @Autowired
  TaskControllerIntTest(RestHelper restHelper) {
    this.restHelper = restHelper;
  }

  @Test
  void should_UpdateTaskOwnerOfReadyForReviewTask() {
    final String url = restHelper.toUrl("/api/v1/tasks/TKI:000000000000000000000000000000000104");

    ResponseEntity<TaskRepresentationModel> responseGet =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
            .retrieve()
            .toEntity(TaskRepresentationModel.class);

    assertThat(responseGet.getBody()).isNotNull();
    TaskRepresentationModel theTaskRepresentationModel = responseGet.getBody();
    assertThat(theTaskRepresentationModel.getState()).isEqualTo(TaskState.READY_FOR_REVIEW);
    assertThat(theTaskRepresentationModel.getOwner()).isNull();

    // set Owner and update Task
    theTaskRepresentationModel.setOwner("dummyUser");

    ResponseEntity<TaskRepresentationModel> responseUpdate =
        CLIENT
            .put()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
            .body(theTaskRepresentationModel)
            .retrieve()
            .toEntity(TaskRepresentationModel.class);

    assertThat(responseUpdate.getBody()).isNotNull();
    TaskRepresentationModel theUpdatedTaskRepresentationModel = responseUpdate.getBody();
    assertThat(theUpdatedTaskRepresentationModel.getState()).isEqualTo(TaskState.READY_FOR_REVIEW);
    assertThat(theUpdatedTaskRepresentationModel.getOwner()).isEqualTo("dummyUser");
  }

  private TaskRepresentationModel getTaskResourceSample() {
    ClassificationSummaryRepresentationModel classificationResource =
        new ClassificationSummaryRepresentationModel();
    classificationResource.setKey("L11010");
    WorkbasketSummaryRepresentationModel workbasketSummary =
        new WorkbasketSummaryRepresentationModel();
    workbasketSummary.setWorkbasketId("WBI:100000000000000000000000000000000004");

    ObjectReferenceRepresentationModel objectReference = new ObjectReferenceRepresentationModel();
    objectReference.setCompany("MyCompany1");
    objectReference.setSystem("MySystem1");
    objectReference.setSystemInstance("MyInstance1");
    objectReference.setType("MyType1");
    objectReference.setValue("00000001");

    TaskRepresentationModel taskRepresentationModel = new TaskRepresentationModel();
    taskRepresentationModel.setClassificationSummary(classificationResource);
    taskRepresentationModel.setWorkbasketSummary(workbasketSummary);
    taskRepresentationModel.setPrimaryObjRef(objectReference);
    return taskRepresentationModel;
  }

  private ObjectReferenceRepresentationModel getObjectReferenceResourceSample() {
    ObjectReferenceRepresentationModel objectReference = new ObjectReferenceRepresentationModel();
    objectReference.setCompany("MyCompany1");
    objectReference.setSystem("MySystem1");
    objectReference.setSystemInstance("MyInstance1");
    objectReference.setType("MyType1");
    objectReference.setValue("00000001");
    return objectReference;
  }

  private AttachmentRepresentationModel getAttachmentResourceSample() {
    AttachmentRepresentationModel attachmentRepresentationModel =
        new AttachmentRepresentationModel();
    attachmentRepresentationModel.setAttachmentId("A11010");
    attachmentRepresentationModel.setObjectReference(getObjectReferenceResourceSample());
    ClassificationSummaryRepresentationModel classificationSummaryRepresentationModel =
        new ClassificationSummaryRepresentationModel();
    classificationSummaryRepresentationModel.setClassificationId(
        "CLI:100000000000000000000000000000000004");
    classificationSummaryRepresentationModel.setKey("L11010");
    attachmentRepresentationModel.setClassificationSummary(
        classificationSummaryRepresentationModel);
    return attachmentRepresentationModel;
  }

  private ObjectReferenceRepresentationModel getSampleSecondaryObjectReference(String suffix) {
    ObjectReferenceRepresentationModel objectReference = new ObjectReferenceRepresentationModel();
    objectReference.setCompany("SecondaryCompany" + suffix);
    objectReference.setSystem("SecondarySystem" + suffix);
    objectReference.setSystemInstance("SecondaryInstance" + suffix);
    objectReference.setType("SecondaryType" + suffix);
    objectReference.setValue("0000000" + suffix);
    return objectReference;
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class GetTasks {

    @Test
    void should_GetAllTasks() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS);

      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(63);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketId() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?workbasket-id=WBI:100000000000000000000000000000000001";

      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(22);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketIdWithinMultiplePlannedTimeIntervals() {
      Instant firstInstant = Instant.now().minus(7, ChronoUnit.DAYS);
      Instant secondInstant = Instant.now().minus(10, ChronoUnit.DAYS);
      Instant thirdInstant = Instant.now().minus(10, ChronoUnit.DAYS);
      Instant fourthInstant = Instant.now().minus(11, ChronoUnit.DAYS);
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000001"
                      + "&planned=%s&planned="
                      + "&planned=%s&planned=%s"
                      + "&planned=&planned=%s"
                      + "&sort-by=PLANNED",
                  firstInstant, secondInstant, thirdInstant, fourthInstant);

      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(6);
    }

    @Test
    void should_GetCustomIntCorrectly_When_GettingTaskWithCustomIntValues() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000025");
      ResponseEntity<TaskRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      TaskRepresentationModel repModel = response.getBody();
      assertThat(repModel).isNotNull();
      assertThat(repModel.getCustomInt1()).isEqualTo(1);
      assertThat(repModel.getCustomInt2()).isEqualTo(2);
      assertThat(repModel.getCustomInt3()).isEqualTo(3);
      assertThat(repModel.getCustomInt4()).isEqualTo(4);
      assertThat(repModel.getCustomInt5()).isEqualTo(5);
      assertThat(repModel.getCustomInt6()).isEqualTo(6);
      assertThat(repModel.getCustomInt7()).isEqualTo(7);
      assertThat(repModel.getCustomInt8()).isEqualTo(8);
    }

    @TestFactory
    Stream<DynamicTest> should_GetAllTasks_For_SpecifiedWorkbasketIdAndCustomIntFieldIn() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s=%s",
                        i, i);
            ResponseEntity<TaskSummaryPagedRepresentationModel> response =
                CLIENT
                    .get()
                    .uri(url)
                    .headers(
                        headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                    .retrieve()
                    .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThat(response.getBody()).isNotNull();
            assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(22);
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @TestFactory
    Stream<DynamicTest> should_GetAllTasks_For_SpecifiedWorkbasketIdAndCustomIntFieldFrom() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s-from=%s",
                        i, i);
            ResponseEntity<TaskSummaryPagedRepresentationModel> response =
                CLIENT
                    .get()
                    .uri(url)
                    .headers(
                        headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                    .retrieve()
                    .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThat(response.getBody()).isNotNull();
            assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(22);
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @TestFactory
    Stream<DynamicTest> should_GetAllTasks_For_SpecifiedWorkbasketIdAndCustomIntFieldFromAndTo() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s-from=-1&custom-int-%s-to=123",
                        i, i);
            ResponseEntity<TaskSummaryPagedRepresentationModel> response =
                CLIENT
                    .get()
                    .uri(url)
                    .headers(
                        headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                    .retrieve()
                    .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThat(response.getBody()).isNotNull();
            assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(22);
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @TestFactory
    Stream<DynamicTest> should_GetAllTasks_For_SpecifiedWorkbasketIdAndCustomIntFieldTo() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s-to=%s",
                        i, i);
            ResponseEntity<TaskSummaryPagedRepresentationModel> response =
                CLIENT
                    .get()
                    .uri(url)
                    .headers(
                        headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                    .retrieve()
                    .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThat(response.getBody()).isNotNull();
            assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(22);
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @TestFactory
    Stream<DynamicTest> should_GetAllTasks_For_SpecifiedWorkbasketIdAndCustomIntFieldNotIn() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s-not=25",
                        i);
            ResponseEntity<TaskSummaryPagedRepresentationModel> response =
                CLIENT
                    .get()
                    .uri(url)
                    .headers(
                        headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                    .retrieve()
                    .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThat(response.getBody()).isNotNull();
            assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(22);
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @TestFactory
    Stream<DynamicTest>
        should_ThrowException_For_SpecifiedWorkbasketIdAndCustomIntFieldWithinIncorrectInterval() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s-within=%s"
                            + "&custom-int-%s-within=23"
                            + "&custom-int-%s-within=15",
                        i, i, i, i);
            ThrowingCallable httpCall =
                () ->
                    CLIENT
                        .get()
                        .uri(url)
                        .headers(
                            headers ->
                                headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                        .retrieve()
                        .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThatThrownBy(httpCall)
                .isInstanceOf(HttpStatusCodeException.class)
                .hasMessageContaining(
                    String.format(
                        "provided length of the property 'custom-int-%s-within' is not dividable by"
                            + " 2",
                        i))
                .extracting(HttpStatusCodeException.class::cast)
                .extracting(HttpStatusCodeException::getStatusCode)
                .isEqualTo(HttpStatus.BAD_REQUEST);
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @TestFactory
    Stream<DynamicTest>
        should_ThrowException_For_SpecifiedWorkbasketIdAndCustomIntFieldWithinNullInterval() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s-within="
                            + "&custom-int-%s-within=",
                        i, i, i);
            ThrowingCallable httpCall =
                () ->
                    CLIENT
                        .get()
                        .uri(url)
                        .headers(
                            headers ->
                                headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                        .retrieve()
                        .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThatThrownBy(httpCall)
                .isInstanceOf(HttpStatusCodeException.class)
                .hasMessageContaining(
                    String.format(
                        """
                            Each interval in 'custom-int-%s-within' \
                            shouldn't consist of two 'null' values""",
                        i))
                .extracting(HttpStatusCodeException.class::cast)
                .extracting(HttpStatusCodeException::getStatusCode)
                .isEqualTo(HttpStatus.BAD_REQUEST);
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @TestFactory
    Stream<DynamicTest> should_GetAllTasks_For_SpecifiedWorkbasketIdAndCustomIntFieldWithin() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s-within=%s"
                            + "&custom-int-%s-within=15",
                        i, i, i);
            ResponseEntity<TaskSummaryPagedRepresentationModel> response =
                CLIENT
                    .get()
                    .uri(url)
                    .headers(
                        headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                    .retrieve()
                    .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThat(response.getBody()).isNotNull();
            assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(22);
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @TestFactory
    Stream<DynamicTest>
        should_GetAllTasks_For_SpecifiedWorkbasketIdAndCustomIntFieldWithinOpenLowerBound() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s-within="
                            + "&custom-int-%s-within=%s",
                        i, i, i);
            ResponseEntity<TaskSummaryPagedRepresentationModel> response =
                CLIENT
                    .get()
                    .uri(url)
                    .headers(
                        headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                    .retrieve()
                    .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThat(response.getBody()).isNotNull();
            assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(22);
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @TestFactory
    Stream<DynamicTest>
        should_GetAllTasks_For_SpecifiedWorkbasketIdAndCustomIntFieldNotWithinOpenUpperBound() {
      List<Integer> customIntValues = List.of(1, 2, 3, 4, 5, 6, 7, 8);
      ThrowingConsumer<Integer> test =
          i -> {
            String url =
                restHelper.toUrl(RestEndpoints.URL_TASKS)
                    + String.format(
                        "?workbasket-id=WBI:100000000000000000000000000000000001"
                            + "&custom-int-%s-not-within=%s"
                            + "&custom-int-%s-not-within=",
                        i, i, i);
            ResponseEntity<TaskSummaryPagedRepresentationModel> response =
                CLIENT
                    .get()
                    .uri(url)
                    .headers(
                        headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                    .retrieve()
                    .toEntity(TaskSummaryPagedRepresentationModel.class);

            assertThat(response.getBody()).isNotNull();
            assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
            assertThat(response.getBody().getContent()).isEmpty();
          };

      return DynamicTest.stream(customIntValues.iterator(), c -> "customInt" + c, test);
    }

    @Test
    void should_GetAllTasks_For_SpecifiesWorkbasketIdWithinSinglePlannedTimeInterval() {
      Instant plannedFromInstant = Instant.now().minus(6, ChronoUnit.DAYS);
      Instant plannedToInstant = Instant.now().minus(3, ChronoUnit.DAYS);
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?workbasket-id=WBI:100000000000000000000000000000000001"
              + "&planned-from="
              + plannedFromInstant
              + "&planned-until="
              + plannedToInstant
              + "&sort-by=PLANNED";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(3);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketIdWithinSingleIndefinitePlannedTimeInterval() {
      Instant plannedFromInstant = Instant.now().minus(6, ChronoUnit.DAYS);
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?workbasket-id=WBI:100000000000000000000000000000000001"
              + "&planned-from="
              + plannedFromInstant
              + "&sort-by=PLANNED";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(4);
    }

    @Test
    void
        should_ThrowException_When_GettingTasksByWorkbasketIdWithInvalidPlannedParamsCombination() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?workbasket-id=WBI:100000000000000000000000000000000001"
              + "&planned=2020-01-22T09:44:47.453Z,,"
              + "2020-01-19T07:44:47.453Z,2020-01-19T19:44:47.453Z,"
              + ",2020-01-18T09:44:47.453Z"
              + "&planned-from=2020-01-19T07:44:47.453Z"
              + "&sort-by=planned";
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketIdWithinMultipleDueTimeIntervals() {
      Instant firstInstant = Instant.now().minus(7, ChronoUnit.DAYS);
      Instant secondInstant = Instant.now().minus(10, ChronoUnit.DAYS);
      Instant thirdInstant = Instant.now().minus(10, ChronoUnit.DAYS);
      Instant fourthInstant = Instant.now().minus(11, ChronoUnit.DAYS);
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000001"
                      + "&due=%s&due="
                      + "&due=%s&due=%s"
                      + "&due=&due=%s"
                      + "&sort-by=DUE",
                  firstInstant, secondInstant, thirdInstant, fourthInstant);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(22);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketIdAndPriorityFromAndUntil() {
      Integer priorityFrom = 2;
      Integer priorityTo = 3;
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&priority-from=%s&priority-until=%s",
                  priorityFrom, priorityTo);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(2);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketIdAndMultiplePriorityWithinIntervals() {
      Integer priorityFrom1 = 2;
      Integer priorityFrom2 = 0;
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&priority-within=%s&priority-within=&priority-within=%s&priority-within=",
                  priorityFrom1, priorityFrom2);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(3);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketIdAndPriorityNotFromAndNotUntil() {
      Integer priorityFrom = 2;
      Integer priorityTo = 3;
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&priority-not-from=%s&priority-not-until=%s",
                  priorityFrom, priorityTo);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketIdAndMultiplePriorityNotWithinIntervals() {
      Integer priorityFrom1 = 2;
      Integer priorityFrom2 = 1;
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&priority-not-within=%s&priority-not-within="
                      + "&priority-not-within=%s&priority-not-within=",
                  priorityFrom1, priorityFrom2);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void should_ThrowException_When_GettingTasksByWorkbasketIdWithPriorityNotWithinAndNotFrom() {
      Integer priorityFrom1 = 2;
      Integer priorityFrom2 = 1;
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&priority-not-within=%s&priority-not-within="
                      + "&priority-not-from=%s&priority-not-until=",
                  priorityFrom1, priorityFrom2);

      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_ThrowException_When_GettingTasksByWorkbasketIdWithPriorityWithinAndPriorityFrom() {
      Integer priorityFrom1 = 2;
      Integer priorityFrom2 = 1;
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&priority-within=%s&priority-within=&priority-from=%s&priority-until=",
                  priorityFrom1, priorityFrom2);

      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_ThrowException_When_GettingTasksByWorkbasketIdWithEvenNumberOfPriorityWithin() {
      Integer priorityFrom1 = 2;
      Integer priorityFrom2 = 1;
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&priority-within=%s&priority-within=&priority-within=%s",
                  priorityFrom1, priorityFrom2);

      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_ThrowException_When_GettingTasksByWorkbasketIdWithEvenNumberOfPriorityNotWithin() {
      Integer priorityFrom1 = 2;
      Integer priorityFrom2 = 1;
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&priority-not-within=%s&priority-not-within=&priority-not-within=%s",
                  priorityFrom1, priorityFrom2);

      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_ReturnAllTasks_For_SpecifiedWorkbasketIdAndClassificationParentKeyIn() {
      String parentKey = "L11010";
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&classification-parent-key=%s",
                  parentKey);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void should_ReturnAllTasks_For_SpecifiedWorkbasketIdAndClassificationParentKeyNotIn() {
      String parentKey = "L11010";
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format(
                  "?workbasket-id=WBI:100000000000000000000000000000000006"
                      + "&classification-parent-key-not=%s",
                  parentKey);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(2);
    }

    @Test
    void should_ReturnAllTasks_For_SpecifiedWorkbasketIdAndClassificationParentKeyLike() {
      String parentKey = "L%";
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format("?classification-parent-key-like=%s", parentKey);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(3);
    }

    @Test
    void should_ReturnAllTasks_For_SpecifiedWorkbasketIdAndClassificationParentKeyNotLike() {
      String parentKey = "L%";
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + String.format("?classification-parent-key-not-like=%s", parentKey);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(91);
    }

    @Test
    void should_ReturnAllTasks_For_ProvidedPrimaryObjectReference() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?por="
              + URLEncoder.encode(
                  "{\"systemInstance\":\"MyInstance1\",\"type\":\"MyType1\"}", UTF_8);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(5);
    }

    @Test
    void should_ReturnAllTasks_For_ProvidedSecondaryObjectReferenceByTypeAndValue() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?sor="
              + URLEncoder.encode("{\"type\":\"Type2\",\"value\":\"Value2\"}", UTF_8);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(2);
    }

    @Test
    void should_ReturnAllTasks_For_ProvidedSecondaryObjectReferenceByCompany() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?sor="
              + URLEncoder.encode("{\"company\":\"Company3\"}", UTF_8);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void should_ReturnNoTasks_For_ProvidedNonexistentSecondaryObjectReference() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?sor="
              + URLEncoder.encode("{\"type\":\"Type2\",\"value\":\"Quatsch\"}", UTF_8);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    void should_ReturnAllTasksByWildcardSearch_For_ProvidedSearchValue() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?wildcard-search-value=99"
              + "&wildcard-search-fields=NAME"
              + "&wildcard-search-fields=CUSTOM_3"
              + "&wildcard-search-fields=CUSTOM_4";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(4);
    }

    @TestFactory
    Stream<DynamicTest> should_ThrowException_When_ProvidingInvalidFormatForCustomAttributes() {
      Iterator<CustomAttribute> iterator =
          Arrays.asList(
                  CustomAttribute.of(null, "value"),
                  CustomAttribute.of("", "value"),
                  CustomAttribute.of("key", null))
              .iterator();

      ThrowingConsumer<CustomAttribute> test =
          customAttribute -> {
            TaskRepresentationModel taskRepresentationModel = getTaskResourceSample();
            taskRepresentationModel.setCustomAttributes(List.of(customAttribute));
            String url = restHelper.toUrl(RestEndpoints.URL_TASKS);
            ThrowingCallable httpCall =
                () ->
                    CLIENT
                        .post()
                        .uri(url)
                        .headers(
                            headers ->
                                headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                        .body(taskRepresentationModel)
                        .retrieve()
                        .toEntity(TaskRepresentationModel.class);
            assertThatThrownBy(httpCall)
                .isInstanceOf(HttpStatusCodeException.class)
                .hasMessageContaining("Format of custom attributes is not valid")
                .extracting(HttpStatusCodeException.class::cast)
                .extracting(HttpStatusCodeException::getStatusCode)
                .isEqualTo(HttpStatus.BAD_REQUEST);
          };

      return DynamicTest.stream(iterator, c -> "customAttribute: '" + c.getKey() + "'", test);
    }

    @Test
    void should_ThrowException_When_ProvidingInvalidFilterParams() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?workbasket-id=WBI:100000000000000000000000000000000001"
              + "&illegalParam=illegal"
              + "&anotherIllegalParam=stillIllegal"
              + "&sort-by=NAME&order=DESCENDING&page-size=5&page=2";
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .hasMessageContaining(
              "Unknown request parameters found: [anotherIllegalParam, illegalParam]")
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_ThrowException_When_ProvidingInvalidOrder() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?workbasket-id=WBI:100000000000000000000000000000000001"
              + "&sort-by=NAME&order=WRONG";
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .hasMessageContaining("\"expectedValues\":[\"ASCENDING\",\"DESCENDING\"]")
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_ThrowNotAuthorized_When_UserHasNoAuthorizationOnTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000000");
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-b-1")))
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void should_ThrowException_When_ProvidingInvalidWildcardSearchParameters() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?wildcard-search-value=%rt%";
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);

      String url2 =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?wildcard-search-fields=NAME,CUSTOM_3,CUSTOM_4";
      ThrowingCallable httpCall2 =
          () ->
              CLIENT
                  .get()
                  .uri(url2)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall2)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketIdWithinSingleDueTimeInterval() {
      Instant dueFromInstant = Instant.now().minus(8, ChronoUnit.DAYS);
      Instant dueToInstant = Instant.now().minus(3, ChronoUnit.DAYS);
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?workbasket-id=WBI:100000000000000000000000000000000001"
              + "&due-from="
              + dueFromInstant
              + "&due-until="
              + dueToInstant
              + "&sort-by=DUE";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketIdWithinSingleIndefiniteDueTimeInterval() {
      Instant dueToInstant = Instant.now().minus(1, ChronoUnit.DAYS);
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?workbasket-id=WBI:100000000000000000000000000000000001"
              + "&due-until="
              + dueToInstant
              + "&sort-by=DUE";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(6);
    }

    @Test
    void should_GetAllTasks_For_WorkbasketIdWithInvalidDueParamsCombination() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?workbasket-id=WBI:100000000000000000000000000000000001"
              + "&due=2020-01-22T09:44:47.453Z,,"
              + "2020-01-19T07:44:47.453Z,2020-01-19T19:44:47.453Z,"
              + ",2020-01-18T09:44:47.453Z"
              + "&due-from=2020-01-19T07:44:47.453Z"
              + "&sort-by=planned";
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedWorkbasketKeyAndDomain() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS) + "?workbasket-key=USER-1-2&domain=DOMAIN_A";

      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(23);
    }

    @Test
    void should_GetAllTasks_For_SpecifiedExternalId() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?external-id=ETI:000000000000000000000000000000000003"
              + "&external-id=ETI:000000000000000000000000000000000004";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(2);
    }

    @Test
    void should_ThrowException_When_KeyIsSetButDomainIsMissing() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?workbasket-key=USER-1-2";
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
                  .retrieve()
                  .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_GetAllTasksWithAdminRole() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS);
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(94);
    }

    @Test
    void should_KeepFiltersInTheLinkOfTheResponse_When_GettingTasks() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?por-type=VNR&por-value=22334455&sort-by=POR_VALUE&order=DESCENDING";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getRequiredLink(IanaLinkRelations.SELF).getHref())
          .endsWith(
              "/api/v1/tasks?por-type=VNR&por-value=22334455"
                  + "&sort-by=POR_VALUE&order=DESCENDING");
    }

    @ParameterizedTest
    @CsvSource({
      "owner=user-1-1, 10",
      "owner-is-null, 65",
      "owner-is-null=true, 65",
      "owner-is-null&owner=user-1-1, 75",
      "owner-is-null=TRUE&owner=user-1-1, 75",
      "state=READY&owner-is-null&owner=user-1-1, 56",
      "state=READY&owner-is-null=TrUe&owner=user-1-1, 56",
    })
    void should_ReturnTasksWithVariousOwnerParameters_When_GettingTasks(
        String queryParams, int expectedSize) {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?" + queryParams;
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat((response.getBody()).getContent()).hasSize(expectedSize);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"=true", "=TRUE", "="})
    void should_TreatOwnerIsNullTrue_For_Value(String value) {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?owner-is-null" + value;
      ResponseEntity<TaskSummaryCollectionRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryCollectionRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(65);
      assertThat(response.getBody().getContent())
          .allSatisfy(task -> assertThat(task).extracting("owner").isNull());
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo", "bar", "false"})
    void should_ReturnException_For_OwnerIsNullWithBadValue(String badValue) {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?owner-is-null=" + badValue;

      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .retrieve()
                  .toEntity(TaskSummaryCollectionRepresentationModel.class);
      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_GetAllTasks_For_GettingLastTaskSummaryPageSortedByPorValue() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?state=READY&state=CLAIMED"
              + "&sort-by=POR_VALUE&order=DESCENDING"
              + "&sort-by=TASK_ID&order=ASCENDING"
              + "&page-size=5&page=16";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getContent()).hasSize(5);
      assertThat(response.getBody().getRequiredLink(IanaLinkRelations.LAST).getHref())
          .contains("page=16");
      assertThat(response.getBody().getContent().iterator().next().getTaskId())
          .isEqualTo("TKI:000000000000000000000000000000000064");
      assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getRequiredLink(IanaLinkRelations.SELF).getHref())
          .endsWith(
              "/api/v1/tasks"
                  + "?state=READY&state=CLAIMED"
                  + "&sort-by=POR_VALUE&sort-by=TASK_ID"
                  + "&order=DESCENDING&order=ASCENDING"
                  + "&page-size=5&page=16");
      assertThat(response.getBody().getLink(IanaLinkRelations.FIRST)).isNotNull();
      assertThat(response.getBody().getLink(IanaLinkRelations.LAST)).isNotNull();
      assertThat(response.getBody().getLink(IanaLinkRelations.PREV)).isNotNull();
    }

    @Test
    void should_SortByOwnerLongName() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?sort-by=OWNER_LONG_NAME"
              + "&order=DESCENDING";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
    }

    @Test
    void should_GroupByPor() throws Exception {
      Field useSpecificDb2Taskquery =
          kadaiConfiguration.getClass().getDeclaredField("useSpecificDb2Taskquery");
      useSpecificDb2Taskquery.setAccessible(true);
      useSpecificDb2Taskquery.setBoolean(kadaiConfiguration, true);

      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?group-by=POR_VALUE";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(14);
      assertThat(
              response.getBody().getContent().stream()
                  .filter(task -> task.getPrimaryObjRef().getValue().equals("MyValue1"))
                  .map(TaskSummaryRepresentationModel::getGroupByCount)
                  .toArray())
          .containsExactly(6);

      useSpecificDb2Taskquery.setBoolean(kadaiConfiguration, false);
    }

    @Test
    void should_GroupBySor() throws Exception {
      Field useSpecificDb2Taskquery =
          kadaiConfiguration.getClass().getDeclaredField("useSpecificDb2Taskquery");
      useSpecificDb2Taskquery.setAccessible(true);
      useSpecificDb2Taskquery.setBoolean(kadaiConfiguration, true);

      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?group-by-sor=Type2";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(1);
      assertThat(
              response.getBody().getContent().stream()
                  .map(TaskSummaryRepresentationModel::getGroupByCount)
                  .toArray())
          .containsExactly(2);

      useSpecificDb2Taskquery.setBoolean(kadaiConfiguration, false);
    }

    @Test
    void testGetLastPageSortedByDueWithHiddenTasksRemovedFromResult() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?sort-by=DUE&sort-by=TASK_ID&"
              + "order=DESCENDING&order=ASCENDING";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getContent()).hasSize(63);

      String url2 =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?sort-by=DUE&sort-by=TASK_ID&"
              + "order=DESCENDING&order=ASCENDING&"
              + "page-size=5&page=5";
      response =
          CLIENT
              .get()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getContent()).hasSize(5);
      assertThat(response.getBody().getRequiredLink(IanaLinkRelations.LAST).getHref())
          .contains("page=13");
      assertThat(response.getBody().getContent().iterator().next().getTaskId())
          .isEqualTo("TKI:000000000000000000000000000000000071");
      assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getRequiredLink(IanaLinkRelations.SELF).getHref())
          .endsWith(
              "/api/v1/tasks?"
                  + "sort-by=DUE&sort-by=TASK_ID&"
                  + "order=DESCENDING&order=ASCENDING&"
                  + "page-size=5&page=5");
      assertThat(response.getBody().getLink(IanaLinkRelations.FIRST)).isNotNull();
      assertThat(response.getBody().getLink(IanaLinkRelations.LAST)).isNotNull();
      assertThat(response.getBody().getLink(IanaLinkRelations.PREV)).isNotNull();
    }

    @Test
    void should_GetAllTasks_For_GettingSecondPageFilteredByPorAttributesSortedByType() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?por-company=00&por-system=PASystem&por-instance=00&"
              + "por-type=VNR&por-value=22334455&"
              + "sort-by=POR_TYPE&sort-by=TASK_ID&"
              + "order=ASCENDING&order=ASCENDING&"
              + "page-size=5&page=2";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getContent())
          .extracting(TaskSummaryRepresentationModel::getTaskId)
          .containsExactlyInAnyOrder("TKI:000000000000000000000000000000000013");
      assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getRequiredLink(IanaLinkRelations.SELF).getHref())
          .endsWith(
              "/api/v1/tasks?por-company=00&por-system=PASystem&por-instance=00&"
                  + "por-type=VNR&por-value=22334455&"
                  + "sort-by=POR_TYPE&sort-by=TASK_ID&"
                  + "order=ASCENDING&order=ASCENDING&"
                  + "page-size=5&page=2");
      assertThat(response.getBody().getLink(IanaLinkRelations.FIRST)).isNotNull();
      assertThat(response.getBody().getLink(IanaLinkRelations.LAST)).isNotNull();
      assertThat(response.getBody().getLink(IanaLinkRelations.PREV)).isNotNull();
    }

    @Test
    void should_GetAllTasksWithComments_When_FilteringByHasCommentsIsSetToTrue() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?has-comments=true";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getContent())
          .extracting(TaskSummaryRepresentationModel::getTaskId)
          .containsExactlyInAnyOrder(
              "TKI:000000000000000000000000000000000000",
              "TKI:000000000000000000000000000000000001",
              "TKI:000000000000000000000000000000000002",
              "TKI:000000000000000000000000000000000004",
              "TKI:000000000000000000000000000000000025",
              "TKI:000000000000000000000000000000000026",
              "TKI:000000000000000000000000000000000027");
    }

    @Test
    void should_GetAllTasksWithoutComments_When_FilteringByHasCommentsIsSetToFalse() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?has-comments=false";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getContent())
          .extracting(TaskSummaryRepresentationModel::getTaskId)
          .doesNotContain(
              "TKI:000000000000000000000000000000000000",
              "TKI:000000000000000000000000000000000001",
              "TKI:000000000000000000000000000000000002",
              "TKI:000000000000000000000000000000000004",
              "TKI:000000000000000000000000000000000025",
              "TKI:000000000000000000000000000000000026",
              "TKI:000000000000000000000000000000000027")
          .hasSize(56);
    }

    @Test
    void should_NotGetEmptyAttachmentList_When_GettingTaskWithAttachment() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000002");
      ResponseEntity<TaskRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      TaskRepresentationModel repModel = response.getBody();
      assertThat(repModel).isNotNull();
      assertThat(repModel.getAttachments()).isNotEmpty();
    }

    @Test
    void should_ReturnFilteredTasks_When_GettingTaskWithoutAttachments() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?without-attachment=true";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(87);
    }

    @Test
    void should_ThrowException_When_WithoutAttachmentsIsSetToFalse() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?without-attachment=false";

      assertThatThrownBy(
              () ->
                  CLIENT
                      .get()
                      .uri(url)
                      .headers(
                          headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                      .retrieve()
                      .toEntity(TaskSummaryPagedRepresentationModel.class))
          .isInstanceOf(HttpStatusCodeException.class)
          .hasMessageContaining(
              "provided value of the property 'without-attachment' must be 'true'");
    }

    @Test
    void should_NotGetEmptyObjectReferencesList_When_GettingTaskWithObjectReferences() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000001");
      ResponseEntity<TaskRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      TaskRepresentationModel repModel = response.getBody();
      assertThat(repModel).isNotNull();
      assertThat(repModel.getSecondaryObjectReferences()).isNotEmpty();
    }

    @Test
    void should_ReturnFilteredTasks_When_GettingTasksBySecondaryObjectReferenceValue() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?sor-value=Value2";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(3);
    }

    @Test
    void should_ReturnFilteredTasks_When_GettingTasksBySecondaryObjectReferenceTypeLike() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?sor-type-like=Type";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(3);
    }

    @Test
    void should_ReturnFilteredTasks_When_GettingTasksBySecondaryObjectReferenceValueAndCompany() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS) + "?sor-value=Value2&sor-company=Company1";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void should_ReturnFilteredTasks_When_GettingTasksByIsReopenedFalse() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?is-reopened=false";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getContent())
          .allSatisfy(task -> assertThat(task.isReopened()).isFalse());
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(62);
    }

    @Test
    void should_ReturnFilteredTasks_When_GettingTasksByIsReopenedTrue() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS) + "?is-reopened=true";
      ResponseEntity<TaskSummaryPagedRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskSummaryPagedRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getContent())
          .allSatisfy(task -> assertThat(task.isReopened()).isTrue());
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void should_GetPriorityCorrectly_When_GettingTaskWithManualPriority() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000070000000000000079");
      ResponseEntity<TaskRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getPriority())
          .isEqualTo((response.getBody().getManualPriority()))
          .isEqualTo(56);
    }

    @Test
    void should_ReturnReceivedDate_When_GettingTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000024");
      ResponseEntity<TaskRepresentationModel> response =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(response.getBody())
          .isNotNull()
          .extracting(TaskSummaryRepresentationModel::getReceived)
          .isNotNull();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CreateTasks {

    @Test
    void should_CreateAndDeleteTask() {
      TaskRepresentationModel taskRepresentationModel = getTaskResourceSample();
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS);
      ResponseEntity<TaskRepresentationModel> responseCreate =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .body(taskRepresentationModel)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(responseCreate.getBody()).isNotNull();

      String taskIdOfCreatedTask = responseCreate.getBody().getTaskId();
      assertThat(taskIdOfCreatedTask).startsWith("TKI:");

      String url2 = restHelper.toUrl(RestEndpoints.URL_TASKS_ID_FORCE, taskIdOfCreatedTask);
      ResponseEntity<TaskRepresentationModel> responseDeleted =
          CLIENT
              .delete()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(ParameterizedTypeReference.forType(Void.class));
      assertThat(responseDeleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_CreateTaskWithError_When_SpecifyingAttachmentWrong() {
      TaskRepresentationModel taskRepresentationModel = getTaskResourceSample();
      AttachmentRepresentationModel attachmentRepresentationModel = getAttachmentResourceSample();
      attachmentRepresentationModel.setTaskId(taskRepresentationModel.getTaskId() + "wrongId");
      taskRepresentationModel.setAttachments(Lists.newArrayList(attachmentRepresentationModel));

      String url = restHelper.toUrl(RestEndpoints.URL_TASKS);
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .post()
                  .uri(url)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .body(taskRepresentationModel)
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_CreateAndDeleteTaskWithSecondaryObjectReferences_When_SpecifyingObjectReferences() {
      TaskRepresentationModel taskRepresentationModel = getTaskResourceSample();
      ObjectReferenceRepresentationModel obj0 = getSampleSecondaryObjectReference("0");
      obj0.setTaskId(taskRepresentationModel.getTaskId());
      ObjectReferenceRepresentationModel obj1 = getSampleSecondaryObjectReference("1");
      obj1.setTaskId(taskRepresentationModel.getTaskId());
      List<ObjectReferenceRepresentationModel> secondaryObjectReferences = List.of(obj0, obj1);
      taskRepresentationModel.setSecondaryObjectReferences(secondaryObjectReferences);

      String url = restHelper.toUrl(RestEndpoints.URL_TASKS);
      ResponseEntity<TaskRepresentationModel> responseCreate =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .body(taskRepresentationModel)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(responseCreate.getBody()).isNotNull();
      String taskIdOfCreatedTask = responseCreate.getBody().getTaskId();

      String url2 = restHelper.toUrl(RestEndpoints.URL_TASKS_ID_FORCE, taskIdOfCreatedTask);
      ResponseEntity<TaskRepresentationModel> responseDeleted =
          CLIENT
              .delete()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(ParameterizedTypeReference.forType(Void.class));
      assertThat(responseDeleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_CreateAndDeleteTaskWithManualPriority_When_SpecifyingManualPriority() {
      TaskRepresentationModel taskRepresentationModel = getTaskResourceSample();
      taskRepresentationModel.setManualPriority(7);

      String url = restHelper.toUrl(RestEndpoints.URL_TASKS);
      ResponseEntity<TaskRepresentationModel> responseCreate =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .body(taskRepresentationModel)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(responseCreate.getBody()).isNotNull();
      assertThat(responseCreate.getBody().getPriority())
          .isEqualTo((responseCreate.getBody().getManualPriority()))
          .isEqualTo(7);

      String taskIdOfCreatedTask = responseCreate.getBody().getTaskId();
      String url2 = restHelper.toUrl(RestEndpoints.URL_TASKS_ID_FORCE, taskIdOfCreatedTask);
      ResponseEntity<TaskRepresentationModel> responseDeleted =
          CLIENT
              .delete()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(ParameterizedTypeReference.forType(Void.class));
      assertThat(responseDeleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_CreateTaskWithCorrectPriorityAndThenDeleteIt_When_NotSpecifyingManualPriority() {
      TaskRepresentationModel taskRepresentationModel = getTaskResourceSample();

      String url = restHelper.toUrl(RestEndpoints.URL_TASKS);
      ResponseEntity<TaskRepresentationModel> responseCreate =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .body(taskRepresentationModel)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(responseCreate.getBody()).isNotNull();
      // The classification of taskRepresentationModel with the key "L11010" has priority=1
      assertThat(responseCreate.getBody().getPriority()).isEqualTo(1);
      assertThat(responseCreate.getBody().getManualPriority()).isEqualTo(-1);

      String taskIdOfCreatedTask = responseCreate.getBody().getTaskId();
      String url2 = restHelper.toUrl(RestEndpoints.URL_TASKS_ID_FORCE, taskIdOfCreatedTask);
      ResponseEntity<TaskRepresentationModel> responseDeleted =
          CLIENT
              .delete()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(ParameterizedTypeReference.forType(Void.class));
      assertThat(responseDeleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    /**
     * TSK-926: If Planned and Due Date is provided to create a task and not matching to service
     * level throw an exception One is calculated by other date +- service level.
     */
    @Test
    void should_ThrowException_When_CreatingTaskWithPlannedAndDueDateNotMatchingServiceLevel() {
      TaskRepresentationModel taskRepresentationModel = getTaskResourceSample();
      Instant plannedTime = Instant.parse("2019-09-13T08:44:17.588Z");
      taskRepresentationModel.setPlanned(plannedTime);
      taskRepresentationModel.setDue(plannedTime);
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS);
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .post()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                  .body(taskRepresentationModel)
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(httpCall).isInstanceOf(HttpStatusCodeException.class);
    }

    @Test
    void should_RouteCreatedTask_When_CreatingTaskWithoutWorkbasketInformation() {
      TaskRepresentationModel taskRepresentationModel = getTaskResourceSample();
      taskRepresentationModel.setWorkbasketSummary(null);

      String url = restHelper.toUrl(RestEndpoints.URL_TASKS);
      ResponseEntity<TaskRepresentationModel> responseCreate =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .body(taskRepresentationModel)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(responseCreate.getBody().getWorkbasketSummary().getWorkbasketId())
          .isEqualTo(IntegrationTestTaskRouter.DEFAULT_ROUTING_TARGET);

      String url2 =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID_FORCE, responseCreate.getBody().getTaskId());
      ResponseEntity<TaskRepresentationModel> responseDeleted =
          CLIENT
              .delete()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(ParameterizedTypeReference.forType(Void.class));
      assertThat(responseDeleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_ThrowException_When_CreatingTaskWithInvalidParameter() throws Exception {
      final String taskToCreateJson =
          "{\"classificationKey\":\"L11010\","
              + "\"workbasketSummaryResource\":"
              + "{\"workbasketId\":\"WBI:100000000000000000000000000000000004\"},"
              + "\"primaryObjRef\":{\"company\":\"MyCompany1\",\"system\":\"MySystem1\","
              + "\"systemInstance\":\"MyInstance1\",\"type\":\"MyType1\",\"value\":\"00000001\"}}";

      URL url = new URL(restHelper.toUrl(RestEndpoints.URL_TASKS));
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setDoOutput(true);
      con.setRequestProperty("Authorization", RestHelper.encodeUserAndPasswordAsBasicAuth("admin"));
      con.setRequestProperty("Content-Type", "application/json");
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), UTF_8));
      out.write(taskToCreateJson);
      out.flush();
      out.close();
      assertThat(con.getResponseCode()).isEqualTo(400);

      con.disconnect();
      final String taskToCreateJson2 =
          "{\"classificationSummaryResource\":"
              + "{\"classificationId\":\"CLI:100000000000000000000000000000000004\"},"
              + "\"workbasketSummaryResource\":{\"workbasketId\":\"\"},"
              + "\"primaryObjRef\":{\"company\":\"MyCompany1\",\"system\":\"MySystem1\","
              + "\"systemInstance\":\"MyInstance1\",\"type\":\"MyType1\",\"value\":\"00000001\"}}";

      url = new URL(restHelper.toUrl(RestEndpoints.URL_TASKS));
      con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setDoOutput(true);
      con.setRequestProperty("Authorization", RestHelper.encodeUserAndPasswordAsBasicAuth("admin"));
      con.setRequestProperty("Content-Type", "application/json");
      out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), UTF_8));
      out.write(taskToCreateJson2);
      out.flush();
      out.close();
      assertThat(con.getResponseCode()).isEqualTo(400);

      con.disconnect();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class UpdateTasks {

    @Test
    void should_ChangeValueOfReceived_When_UpdatingTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:100000000000000000000000000000000000");
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      final TaskRepresentationModel originalTask = responseGet.getBody();
      Instant expectedReceived = Instant.parse("2019-09-13T08:44:17.588Z");
      originalTask.setReceived(expectedReceived);
      ResponseEntity<TaskRepresentationModel> responseUpdate =
          CLIENT
              .put()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .body(originalTask)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      TaskRepresentationModel updatedTask = responseUpdate.getBody();
      assertThat(updatedTask).isNotNull();
      assertThat(updatedTask.getReceived()).isEqualTo(expectedReceived);
    }

    @Test
    void should_ChangeValueOfModified_When_UpdatingTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:100000000000000000000000000000000000");
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      final TaskRepresentationModel originalTask = responseGet.getBody();
      assertThat(originalTask).isNotNull();
      ResponseEntity<TaskRepresentationModel> responseUpdate =
          CLIENT
              .put()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .body(originalTask)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      TaskRepresentationModel updatedTask = responseUpdate.getBody();
      assertThat(updatedTask).isNotNull();
      assertThat(originalTask.getModified()).isBefore(updatedTask.getModified());
    }

    @Test
    void should_ThrowError_When_UpdatingTaskWithBadAttachment() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:100000000000000000000000000000000000");
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      final TaskRepresentationModel originalTask = responseGet.getBody();
      assertThat(originalTask).isNotNull();

      AttachmentRepresentationModel attachmentRepresentationModel = getAttachmentResourceSample();
      attachmentRepresentationModel.setTaskId(originalTask.getTaskId() + "wrongId");
      originalTask.setAttachments(Lists.newArrayList(attachmentRepresentationModel));

      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .put()
                  .uri(url)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .body(originalTask)
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class DeleteTasks {

    @Test
    void should_DeleteTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000039");
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(responseGet.getBody()).isNotNull();
      assertThat(responseGet.getBody().getState()).isEqualTo(TaskState.COMPLETED);

      ResponseEntity<TaskRepresentationModel> responseDelete =
          CLIENT
              .delete()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .hasMessageContaining(
              "Task with id 'TKI:000000000000000000000000000000000039' was not found.")
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_ForceDeleteTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000026");
      String urlForce =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_FORCE, "TKI:000000000000000000000000000000000026");
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(responseGet.getBody()).isNotNull();
      assertThat(responseGet.getBody().getState()).isEqualTo(TaskState.CLAIMED);

      ResponseEntity<TaskRepresentationModel> responseDelete =
          CLIENT
              .delete()
              .uri(urlForce)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .get()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .hasMessageContaining(
              "Task with id 'TKI:000000000000000000000000000000000026' was not found.")
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_DeleteAllTasks_For_ProvidedParams() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS)
              + "?task-id=TKI:000000000000000000000000000000000036"
              + "&task-id=TKI:000000000000000000000000000000000037"
              + "&task-id=TKI:000000000000000000000000000000000038"
              + "&custom14=abc";

      ResponseEntity<TaskSummaryCollectionRepresentationModel> response =
          CLIENT
              .delete()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskSummaryCollectionRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat((response.getBody()).getLink(IanaLinkRelations.SELF)).isNotNull();
      assertThat(response.getBody().getContent()).hasSize(3);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class UpdateTaskOwnerOfTasks {

    @Test
    void should_UpdateTaskOwnerOfReadyTask() {
      final String url = restHelper.toUrl("/api/v1/tasks/TKI:000000000000000000000000000000000025");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(responseGet.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = responseGet.getBody();
      assertThat(taskRepresentationModel.getState()).isEqualTo(TaskState.READY);
      assertThat(taskRepresentationModel.getOwner()).isNull();

      // set Owner and update Task
      taskRepresentationModel.setOwner("dummyUser");
      ResponseEntity<TaskRepresentationModel> responseUpdate =
          CLIENT
              .put()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .body(taskRepresentationModel)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(responseUpdate.getBody()).isNotNull();
      TaskRepresentationModel theUpdatedTaskRepresentationModel = responseUpdate.getBody();
      assertThat(theUpdatedTaskRepresentationModel.getState()).isEqualTo(TaskState.READY);
      assertThat(theUpdatedTaskRepresentationModel.getOwner()).isEqualTo("dummyUser");
    }

    @Test
    void should_ThrowException_When_UpdatingTaskOwnerOfClaimedTask() {
      final String url = restHelper.toUrl("/api/v1/tasks/TKI:000000000000000000000000000000000026");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(responseGet.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = responseGet.getBody();
      assertThat(taskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(taskRepresentationModel.getOwner()).isEqualTo("user-1-1");

      // set Owner and update Task
      taskRepresentationModel.setOwner("dummyuser");

      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .put()
                  .uri(url)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
                  .body(taskRepresentationModel)
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .isInstanceOf(HttpStatusCodeException.class)
          .hasMessageContaining("400");
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class TransferTasks {
    @TestFactory
    Stream<DynamicTest> should_SetTransferFlagAndOwnerDependentOnBody_When_TransferringTask() {
      Iterator<Pair<Boolean, String>> iterator =
          Arrays.asList(Pair.of(false, "user-1-1"), Pair.of(true, "user-1-1")).iterator();
      String url =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_TRANSFER_WORKBASKET_ID,
              "TKI:000000000000000000000000000000000003",
              "WBI:100000000000000000000000000000000006");

      ThrowingConsumer<Pair<Boolean, String>> test =
          pair -> {
            ResponseEntity<TaskRepresentationModel> response =
                CLIENT
                    .post()
                    .uri(url)
                    .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                    .body(
                        new TransferTaskRepresentationModel(pair.getLeft(), pair.getRight(), null))
                    .retrieve()
                    .toEntity(TaskRepresentationModel.class);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getWorkbasketSummary().getWorkbasketId())
                .isEqualTo("WBI:100000000000000000000000000000000006");
            assertThat(response.getBody().isTransferred()).isEqualTo(pair.getLeft());
            assertThat(response.getBody().getOwner()).isEqualTo(pair.getRight());
          };

      return DynamicTest.stream(iterator, c -> "for setTransferFlag: " + c, test);
    }

    @Test
    void should_SetTransferFlagToTrueAndOwnerToNull_When_TransferringWithoutRequestBody() {
      String url =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_TRANSFER_WORKBASKET_ID,
              "TKI:000000000000000000000000000000000003",
              "WBI:100000000000000000000000000000000006");
      ResponseEntity<TaskRepresentationModel> response =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getWorkbasketSummary().getWorkbasketId())
          .isEqualTo("WBI:100000000000000000000000000000000006");
      assertThat(response.getBody().isTransferred()).isTrue();
      assertThat(response.getBody().getOwner()).isNull();
    }

    @TestFactory
    Stream<DynamicTest> should_ReturnFailedTasks_When_TransferringTasks() {

      Iterator<Pair<Boolean, String>> iterator =
          Arrays.asList(Pair.of(true, "user-1-1"), Pair.of(false, "user-1-2")).iterator();
      String url =
          restHelper.toUrl(
              RestEndpoints.URL_TRANSFER_WORKBASKET_ID, "WBI:100000000000000000000000000000000006");

      List<String> taskIds =
          Arrays.asList(
              "TKI:000000000000000000000000000000000003",
              "TKI:000000000000000000000000000000000004",
              "TKI:000000000000000000000000000000000039");

      ThrowingConsumer<Pair<Boolean, String>> test =
          pair -> {
            ResponseEntity<Map<String, Object>> response =
                CLIENT
                    .post()
                    .uri(url)
                    .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                    .body(
                        new TransferTaskRepresentationModel(
                            pair.getLeft(), pair.getRight(), taskIds))
                    .retrieve()
                    .toEntity(BULK_RESULT_TASKS_MODEL_TYPE);

            assertThat(response.getBody()).isNotNull();
            Map<String, LinkedHashMap> failedTasks =
                (Map<String, LinkedHashMap>) response.getBody().get("tasksWithErrors");
            assertThat(failedTasks).hasSize(1);
            assertThat(failedTasks).containsKey("TKI:000000000000000000000000000000000039");
            String errorName =
                (String) failedTasks.get("TKI:000000000000000000000000000000000039").get("key");
            assertThat(errorName).isEqualTo("TASK_INVALID_STATE");
            LinkedHashMap messageVariables =
                (LinkedHashMap)
                    failedTasks
                        .get("TKI:000000000000000000000000000000000039")
                        .get("messageVariables");
            assertThat((List) messageVariables.get("requiredTaskStates"))
                .containsExactly("READY", "CLAIMED", "READY_FOR_REVIEW", "IN_REVIEW");
            assertThat(messageVariables).containsEntry("taskState", "COMPLETED");
            assertThat(messageVariables)
                .containsEntry("taskId", "TKI:000000000000000000000000000000000039");
          };
      return DynamicTest.stream(iterator, c -> "for setTransferFlag and owner: " + c, test);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class DistributeTasks {

    @Test
    void should_ThrowException_When_SourceWorkbasketIdIsMissing() {
      DistributionTasksRepresentationModel requestBody =
          new DistributionTasksRepresentationModel(null, null, null, null);

      String url =
          restHelper.toUrl(RestEndpoints.URL_DISTRIBUTE.replace("{workbasketId}", "dummyId"));

      assertThatThrownBy(
              () ->
                  CLIENT
                      .post()
                      .uri(url)
                      .headers(
                          headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                      .body(requestBody)
                      .retrieve()
                      .toEntity(TaskRepresentationModel.class))
          .isInstanceOf(NotFound.class)
          .hasMessageContaining("Workbasket with id 'dummyId' was not found.");
    }

    @Test
    void should_CallDistributeWithTaskIdsAndWithDestinationWorkbasketIds_When_Provided() {
      List<String> taskIds = List.of("TKI:000000000000000000000000000000000039");
      List<String> destinationWorkbasketIds = List.of("WBI:100000000000000000000000000000000006");

      DistributionTasksRepresentationModel requestBody =
          new DistributionTasksRepresentationModel(taskIds, destinationWorkbasketIds, null, null);

      String url =
          restHelper.toUrl(
              RestEndpoints.URL_DISTRIBUTE, "WBI:100000000000000000000000000000000006");

      ResponseEntity<Map<String, Object>> response =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .body(requestBody)
              .retrieve()
              .toEntity(BULK_RESULT_TASKS_MODEL_TYPE);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();

      Map<String, Object> result = response.getBody();
      assertThat(result).isNotNull().containsKey("tasksWithErrors");

      Map<String, Object> tasksWithErrors = (Map<String, Object>) result.get("tasksWithErrors");

      assertThat(tasksWithErrors)
          .hasSize(1)
          .containsKey("TKI:000000000000000000000000000000000039");

      Map<String, Object> errorDetails =
          (Map<String, Object>) tasksWithErrors.get("TKI:000000000000000000000000000000000039");

      assertThat(errorDetails)
          .containsEntry("key", "TASK_INVALID_STATE")
          .containsKey("messageVariables");

      Map<String, Object> messageVariables =
          (Map<String, Object>) errorDetails.get("messageVariables");

      // **Überprüfen, ob die `requiredTaskStates` korrekt sind**
      assertThat(messageVariables).containsKey("requiredTaskStates");
      assertThat((List<String>) messageVariables.get("requiredTaskStates"))
          .containsExactlyInAnyOrder("READY", "CLAIMED", "READY_FOR_REVIEW", "IN_REVIEW");

      // **Zusätzliche Variablen prüfen**
      assertThat(messageVariables)
          .containsEntry("taskState", "COMPLETED")
          .containsEntry("taskId", "TKI:000000000000000000000000000000000039");
    }

    @Test
    void should_ReturnPartiallyFailedTaskId_When_GivenTaskIdDoesNotExist() {
      List<String> taskIds = List.of("NonExistingIdentifier");
      List<String> destinationWorkbasketIds = List.of("WBI:100000000000000000000000000000000006");

      DistributionTasksRepresentationModel requestBody =
          new DistributionTasksRepresentationModel(taskIds, destinationWorkbasketIds, null, null);

      String url =
          restHelper.toUrl(
              RestEndpoints.URL_DISTRIBUTE, "WBI:100000000000000000000000000000000006");

      ResponseEntity<Map<String, Object>> response =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .body(requestBody)
              .retrieve()
              .toEntity(BULK_RESULT_TASKS_MODEL_TYPE);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();

      Map<String, Object> result = response.getBody();
      assertThat(result).isNotNull().containsKey("tasksWithErrors");

      Map<String, Object> tasksWithErrors = (Map<String, Object>) result.get("tasksWithErrors");

      assertThat(tasksWithErrors).hasSize(1).containsKey("NonExistingIdentifier");

      Map<String, Object> errorDetails =
          (Map<String, Object>) tasksWithErrors.get("NonExistingIdentifier");

      assertThat(errorDetails)
          .containsEntry("key", "TASK_NOT_FOUND")
          .containsKey("messageVariables");
    }

    @Test
    void should_ReturnPartiallyFailedTaskIds_When_GivenTaskIdDoesNotExist() {
      List<String> taskIds = List.of("NonExistingIdentifier1", "NonExistingIdentifier2");
      List<String> destinationWorkbasketIds = List.of("WBI:100000000000000000000000000000000006");

      DistributionTasksRepresentationModel requestBody =
          new DistributionTasksRepresentationModel(taskIds, destinationWorkbasketIds, null, null);

      String url =
          restHelper.toUrl(
              RestEndpoints.URL_DISTRIBUTE, "WBI:100000000000000000000000000000000006");

      ResponseEntity<Map<String, Object>> response =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .body(requestBody)
              .retrieve()
              .toEntity(BULK_RESULT_TASKS_MODEL_TYPE);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();

      Map<String, Object> result = response.getBody();
      assertThat(result).isNotNull().containsKey("tasksWithErrors");

      Map<String, Object> tasksWithErrors = (Map<String, Object>) result.get("tasksWithErrors");

      assertThat(tasksWithErrors)
          .hasSize(2)
          .containsKey("NonExistingIdentifier1")
          .containsKey("NonExistingIdentifier2");

      Map<String, Object> errorDetails1 =
          (Map<String, Object>) tasksWithErrors.get("NonExistingIdentifier2");
      assertThat(errorDetails1)
          .containsEntry("key", "TASK_NOT_FOUND")
          .containsKey("messageVariables");

      Map<String, Object> errorDetails2 =
          (Map<String, Object>) tasksWithErrors.get("NonExistingIdentifier2");
      assertThat(errorDetails2)
          .containsEntry("key", "TASK_NOT_FOUND")
          .containsKey("messageVariables");
    }

    @Test
    void should_ThrowException_When_InvalidDistributionStrategyProvided() {
      List<String> taskIds = List.of("TKI:000000000000000000000000000000000039");
      String invalidDistributionStrategyName = "ROUND_ROBIN";
      DistributionTasksRepresentationModel requestBody =
          new DistributionTasksRepresentationModel(
              taskIds, null, invalidDistributionStrategyName, null);

      String url =
          restHelper.toUrl(
              RestEndpoints.URL_DISTRIBUTE, "WBI:100000000000000000000000000000000006");

      assertThatThrownBy(
              () ->
                  CLIENT
                      .post()
                      .uri(url)
                      .headers(
                          headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                      .body(requestBody)
                      .retrieve()
                      .toEntity(BulkOperationResultsRepresentationModel.class))
          .isInstanceOf(HttpClientErrorException.class)
          .extracting(HttpClientErrorException.class::cast)
          .extracting(HttpClientErrorException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);

      assertThatThrownBy(
              () ->
                  CLIENT
                      .post()
                      .uri(url)
                      .headers(
                          headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                      .body(requestBody)
                      .retrieve()
                      .toEntity(BulkOperationResultsRepresentationModel.class))
          .isInstanceOf(HttpClientErrorException.class)
          .hasMessageContaining("The distribution strategy 'ROUND_ROBIN' does not exist.");
    }

    @Test
    void should_ThrowNotAuthorizedOnWorkbasketException() {
      HttpHeaders headers = RestHelper.generateHeadersForUser("user-1-1");
      headers.setContentType(MediaType.APPLICATION_JSON);

      String sourceWorkbasketId = "WBI:100000000000000000000000000000000001";
      DistributionTasksRepresentationModel requestBody =
          new DistributionTasksRepresentationModel(null, null, null, null);

      String url = restHelper.toUrl(RestEndpoints.URL_DISTRIBUTE, sourceWorkbasketId);

      ThrowingCallable response =
          () ->
              CLIENT
                  .post()
                  .uri(url)
                  .headers(headersInt -> headersInt.addAll(headers))
                  .body(requestBody)
                  .retrieve()
                  .toEntity(BulkOperationResultsRepresentationModel.class);

      assertThatThrownBy(response).isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void should_CallDistributeWithWIdWithAdditionalInformation_When_OnlySourceWtIdProvided() {
      String sourceWorkbasketId = "WBI:100000000000000000000000000000000006";
      DistributionTasksRepresentationModel requestBody =
          new DistributionTasksRepresentationModel(null, null, null, Map.of("priority", "high"));

      String url = restHelper.toUrl(RestEndpoints.URL_DISTRIBUTE, sourceWorkbasketId);
      ResponseEntity<BulkOperationResultsRepresentationModel> response =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .body(requestBody)
              .retrieve()
              .toEntity(BulkOperationResultsRepresentationModel.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
    }

    @Test
    void should_CallDistributeWithWIdAndWithDestinationWorkbasketIds_When_Provided() {
      String sourceWorkbasketId = "WBI:100000000000000000000000000000000006";
      List<String> destinationWorkbasketIds = List.of("WBI:100000000000000000000000000000000005");
      DistributionTasksRepresentationModel requestBody =
          new DistributionTasksRepresentationModel(
              null, destinationWorkbasketIds, null, Map.of("priority", "high"));

      String url = restHelper.toUrl(RestEndpoints.URL_DISTRIBUTE, sourceWorkbasketId);
      ResponseEntity<BulkOperationResultsRepresentationModel> response =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .body(requestBody)
              .retrieve()
              .toEntity(BulkOperationResultsRepresentationModel.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class RequestChangesOnTasks {

    @Test
    void should_RequestChangesOnATask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000136");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.IN_REVIEW);
      assertThat(repModel.getOwner()).isEqualTo("user-1-1");

      // request changes
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_CHANGES,
              "TKI:000000000000000000000000000000000136");
      ResponseEntity<TaskRepresentationModel> requestedChangesResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(requestedChangesResponse.getBody()).isNotNull();
      assertThat(requestedChangesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = requestedChangesResponse.getBody();
      assertThat(repModel.getOwner()).isNull();
      assertThat(repModel.getState()).isEqualTo(TaskState.READY);
    }

    @Test
    void should_ForceRequestChanges_When_CurrentUserIsNotTheOwner() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000100");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(repModel.getOwner()).isEqualTo("user-1-2");

      // request changes
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_CHANGES_FORCE,
              "TKI:000000000000000000000000000000000100");
      ResponseEntity<TaskRepresentationModel> requestedChangesResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(requestedChangesResponse.getBody()).isNotNull();
      assertThat(requestedChangesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = requestedChangesResponse.getBody();
      assertThat(repModel.getOwner()).isNull();
      assertThat(repModel.getState()).isEqualTo(TaskState.READY);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class RequestChangesWithWorkbasketIdOnTasks {
    @Test
    void should_RequestChangesOnATask_With_WorkbasketId() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000136");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.IN_REVIEW);
      assertThat(repModel.getOwner()).isEqualTo("user-1-1");

      // Prepare body for request review
      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("workbasketId", "WBI:100000000000000000000000000000000007");

      // request review
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_CHANGES,
              "TKI:000000000000000000000000000000000136");
      ResponseEntity<TaskRepresentationModel> requestedChangesResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .body(requestBody)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(requestedChangesResponse.getBody()).isNotNull();
      assertThat(requestedChangesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = requestedChangesResponse.getBody();
      assertThat(repModel.getOwner()).isNull();
      assertThat(repModel.getState()).isEqualTo(TaskState.READY);
      assertThat(repModel.getWorkbasketSummary().getWorkbasketId())
          .isEqualTo("WBI:100000000000000000000000000000000007");
    }

    @Test
    void should_RequestChangesOnATask_With_WorkbasketIdAndOwnerId() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000236");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.IN_REVIEW);
      assertThat(repModel.getOwner()).isEqualTo("admin");

      // Prepare body for request review
      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("workbasketId", "WBI:100000000000000000000000000000000008");
      requestBody.put("ownerId", "user-1-2");

      // request review
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_CHANGES,
              "TKI:000000000000000000000000000000000236");
      ResponseEntity<TaskRepresentationModel> requestedChangesResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .body(requestBody)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(requestedChangesResponse.getBody()).isNotNull();
      assertThat(requestedChangesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = requestedChangesResponse.getBody();
      assertThat(repModel.getOwner()).isEqualTo("user-1-2");
      assertThat(repModel.getState()).isEqualTo(TaskState.READY);
      assertThat(repModel.getWorkbasketSummary().getWorkbasketId())
          .isEqualTo("WBI:100000000000000000000000000000000008");
    }

    @Test
    void should_ThrowException_When_RequestChangesOnATask_With_EmptyWorkbasketId() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000136");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();

      // Prepare body for request review
      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("workbasketId", "");
      requestBody.put("ownerId", null);

      // request review
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW,
              "TKI:000000000000000000000000000000000136");
      ThrowingCallable requestChangesResponse =
          () ->
              CLIENT
                  .post()
                  .uri(url2)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                  .body(requestBody)
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(requestChangesResponse)
          .isInstanceOf(HttpStatusCodeException.class)
          .hasMessageContaining("WorkbasketId must not be null or empty");
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class RequestReviewOnTasks {

    @Test
    void should_RequestReviewOnATask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000035");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(repModel.getOwner()).isEqualTo("user-1-1");

      // request review
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW,
              "TKI:000000000000000000000000000000000035");
      ResponseEntity<TaskRepresentationModel> requestReviewResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(requestReviewResponse.getBody()).isNotNull();
      assertThat(requestReviewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = requestReviewResponse.getBody();
      assertThat(repModel.getOwner()).isNull();
      assertThat(repModel.getState()).isEqualTo(TaskState.READY_FOR_REVIEW);
    }

    @Test
    void should_ForceRequestReview_When_CurrentUserIsNotTheOwner() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000101");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(repModel.getOwner()).isEqualTo("user-1-2");

      // request review
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW_FORCE,
              "TKI:000000000000000000000000000000000101");
      ResponseEntity<TaskRepresentationModel> requestReviewResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(requestReviewResponse.getBody()).isNotNull();
      assertThat(requestReviewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = requestReviewResponse.getBody();
      assertThat(repModel.getOwner()).isNull();
      assertThat(repModel.getState()).isEqualTo(TaskState.READY_FOR_REVIEW);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class RequestReviewWithWorkbasketIdOnTasks {
    @Test
    void should_RequestReviewOnATask_With_WorkbasketId() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000035");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(repModel.getOwner()).isEqualTo("user-1-1");

      // Prepare body for request review
      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("workbasketId", "TestWorkbasketId");

      // request review
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW,
              "TKI:000000000000000000000000000000000035");
      ResponseEntity<TaskRepresentationModel> requestReviewResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .body(requestBody)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(requestReviewResponse.getBody()).isNotNull();
      assertThat(requestReviewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = requestReviewResponse.getBody();
      assertThat(repModel.getOwner()).isNull();
      assertThat(repModel.getState()).isEqualTo(TaskState.READY_FOR_REVIEW);
    }

    @Test
    void should_RequestReviewOnATask_With_WorkbasketIdAndOwner() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000100");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(repModel.getOwner()).isEqualTo("user-1-2");

      // Prepare body for request review
      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("workbasketId", "TestWorkbasketId");
      requestBody.put("ownerId", "user-1-1");

      // request review
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW,
              "TKI:000000000000000000000000000000000100");
      ResponseEntity<TaskRepresentationModel> requestReviewResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .body(requestBody)
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(requestReviewResponse.getBody()).isNotNull();
      assertThat(requestReviewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = requestReviewResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.READY_FOR_REVIEW);
      assertThat(repModel.getOwner()).isEqualTo("user-1-1");
    }

    @Test
    void should_ThrowException_When_RequestReviewOnATask_With_EmptyWorkbasketId() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000035");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();

      // Prepare body for request review
      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("workbasketId", "");
      requestBody.put("ownerId", null);

      // request review
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REQUEST_REVIEW,
              "TKI:000000000000000000000000000000000035");
      ThrowingCallable requestReviewResponse =
          () ->
              CLIENT
                  .post()
                  .uri(url2)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                  .body(requestBody)
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(requestReviewResponse)
          .isInstanceOf(HttpStatusCodeException.class)
          .hasMessageContaining("WorkbasketId must not be null or empty");
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CompleteTasks {
    @Test
    void should_CompleteTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000102");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(repModel.getOwner()).isEqualTo("user-1-2");

      // complete Task
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_COMPLETE, "TKI:000000000000000000000000000000000102");
      ResponseEntity<TaskRepresentationModel> completeResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(completeResponse.getBody()).isNotNull();
      assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = completeResponse.getBody();
      assertThat(repModel.getOwner()).isEqualTo("user-1-2");
      assertThat(repModel.getState()).isEqualTo(TaskState.COMPLETED);
    }

    @Test
    void should_partialFailCompleteTasks_when_UserHasNoAuthorization() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS_BULK_COMPLETE);

      List<String> taskIds = List.of(
              "TKI:000000000000000000000000000000000103",
              "TKI:000000000000000000000000000000000041"
      );

      TaskIdListRepresentationModel request = new TaskIdListRepresentationModel(taskIds);

      ResponseEntity<Map> response =
              CLIENT
                      .patch()
                      .uri(url)
                      .headers(h -> h.addAll(RestHelper.generateHeadersForUser("user-1-2")))
                      .body(request)
                      .retrieve()
                      .toEntity(Map.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      Map<?, ?> body = response.getBody();
      assertThat(body).isNotNull();

      Map<String, ?> failuresMap = (Map<String, ?>) body.get("tasksWithErrors");
      List<String> failures = new ArrayList<>(failuresMap.keySet());

      assertThat(failures).hasSize(1)
              .containsExactly(
                      "TKI:000000000000000000000000000000000041"
      );
    }

    @Test
    void should_ForceCompleteTask_When_CurrentUserIsNotTheOwner() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000028");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel repModel = getTaskResponse.getBody();
      assertThat(repModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(repModel.getOwner()).isEqualTo("user-1-1");

      // force complete task
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_COMPLETE_FORCE,
              "TKI:000000000000000000000000000000000028");
      ResponseEntity<TaskRepresentationModel> forceCompleteResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(forceCompleteResponse.getBody()).isNotNull();
      assertThat(forceCompleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      repModel = forceCompleteResponse.getBody();
      assertThat(repModel.getOwner()).isEqualTo("user-1-2");
      assertThat(repModel.getState()).isEqualTo(TaskState.COMPLETED);
    }

    @Test
    void should_ForceCompleteAllTasks_When_CurrentUserIsNotTheOwner() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS_BULK_COMPLETE_FORCE);

      List<String> taskIds = List.of(
          "TKI:000000000000000000000000000000000027",
          "TKI:000000000000000000000000000000000026"
      );

      TaskIdListRepresentationModel request = new TaskIdListRepresentationModel(taskIds);

      ResponseEntity<BulkOperationResultsRepresentationModel> response =
              CLIENT
                  .patch()
                  .uri(url)
                  .headers(h -> h.addAll(RestHelper.generateHeadersForUser("user-1-2")))
                  .body(request)
                  .retrieve()
                  .toEntity(BulkOperationResultsRepresentationModel.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      BulkOperationResultsRepresentationModel body = response.getBody();
      assertThat(body).isNotNull();

      assertThat(body.getTasksWithErrors()).isEmpty();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CancelTasks {

    @Test
    void should_CancelTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000103");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(responseGet.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = responseGet.getBody();
      assertThat(taskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);

      // cancel the task
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_CANCEL, "TKI:000000000000000000000000000000000103");
      ResponseEntity<TaskRepresentationModel> cancelResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(cancelResponse.getBody()).isNotNull();
      assertThat(cancelResponse.getBody().getState()).isEqualTo(TaskState.CANCELLED);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class TerminateTasks {
    @Test
    void should_TerminateTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:100000000000000000000000000000000000");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(responseGet.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = responseGet.getBody();
      assertThat(taskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);

      // terminate the task
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_TERMINATE, "TKI:000000000000000000000000000000000103");
      ResponseEntity<TaskRepresentationModel> terminateResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(terminateResponse.getBody()).isNotNull();
      assertThat(terminateResponse.getBody().getState()).isEqualTo(TaskState.TERMINATED);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class ClaimTasks {

    @Test
    void should_ClaimTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000033");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel readyTaskRepresentationModel = getTaskResponse.getBody();
      assertThat(readyTaskRepresentationModel.getState()).isEqualTo(TaskState.READY);
      assertThat(readyTaskRepresentationModel.getOwner()).isEqualTo("user-1-2");

      // claim
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_CLAIM, "TKI:000000000000000000000000000000000033");
      ResponseEntity<TaskRepresentationModel> claimResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(claimResponse.getBody()).isNotNull();
      assertThat(claimResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskRepresentationModel claimedTaskRepresentationModel = claimResponse.getBody();
      assertThat(claimedTaskRepresentationModel.getOwner()).isEqualTo("user-1-2");
      assertThat(claimedTaskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
    }

    @Test
    void should_ForceClaim_When_TaskIsClaimedByDifferentOwner() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000029");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel readyTaskRepresentationModel = getTaskResponse.getBody();
      assertThat(readyTaskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(readyTaskRepresentationModel.getOwner()).isEqualTo("user-1-2");

      // claim
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_CLAIM_FORCE, "TKI:000000000000000000000000000000000029");
      ResponseEntity<TaskRepresentationModel> claimResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(claimResponse.getBody()).isNotNull();
      assertThat(claimResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskRepresentationModel claimedTaskRepresentationModel = claimResponse.getBody();
      assertThat(claimedTaskRepresentationModel.getOwner()).isEqualTo("user-1-1");
      assertThat(claimedTaskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
    }

    @Test
    void should_SelectAndClaimTasks() {
      String url = restHelper.toUrl(RestEndpoints.URL_TASKS_ID_SELECT_AND_CLAIM + "?custom14=abc");
      ResponseEntity<TaskRepresentationModel> response =
          CLIENT
              .post()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(response.getBody()).isNotNull();

      String url2 = restHelper.toUrl(RestEndpoints.URL_TASKS_ID, response.getBody().getTaskId());
      ResponseEntity<TaskRepresentationModel> responseGetTask =
          CLIENT
              .get()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(responseGetTask).isNotNull();
      assertThat(responseGetTask.getBody().getOwner()).isEqualTo("admin");
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CancelClaimTasks {
    @Test
    void should_CancelClaimTask() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000032");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = getTaskResponse.getBody();
      assertThat(taskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(taskRepresentationModel.getOwner()).isEqualTo("user-1-2");

      // cancel claim
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_CLAIM, "TKI:000000000000000000000000000000000032");
      ResponseEntity<TaskRepresentationModel> cancelClaimResponse =
          CLIENT
              .delete()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(cancelClaimResponse.getBody()).isNotNull();
      assertThat(cancelClaimResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskRepresentationModel cancelClaimedtaskRepresentationModel = cancelClaimResponse.getBody();
      assertThat(cancelClaimedtaskRepresentationModel.getOwner()).isNull();
      assertThat(cancelClaimedtaskRepresentationModel.getClaimed()).isNull();
      assertThat(cancelClaimedtaskRepresentationModel.getState()).isEqualTo(TaskState.READY);
    }

    @Test
    void should_KeepOwnerAndOwnerLongName_When_CancelClaimWithKeepOwner() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000000");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = getTaskResponse.getBody();
      assertThat(taskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(taskRepresentationModel.getOwner()).isEqualTo("user-1-1");

      // cancel claim
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_CLAIM + "?keepOwner=true",
              "TKI:000000000000000000000000000000000000");
      ResponseEntity<TaskRepresentationModel> cancelClaimResponse =
          CLIENT
              .delete()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(cancelClaimResponse.getBody()).isNotNull();
      assertThat(cancelClaimResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskRepresentationModel cancelClaimedtaskRepresentationModel = cancelClaimResponse.getBody();
      assertThat(cancelClaimedtaskRepresentationModel.getClaimed()).isNull();
      assertThat(cancelClaimedtaskRepresentationModel.getState()).isEqualTo(TaskState.READY);
      assertThat(cancelClaimedtaskRepresentationModel.getOwner()).isEqualTo("user-1-1");
      assertThat(cancelClaimedtaskRepresentationModel.getOwnerLongName())
          .isEqualTo("Mustermann, Max - (user-1-1)");
    }

    @Test
    void should_KeepOwnerAndOwnerLongName_When_ForceCancelClaimWithKeepOwner() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000001");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = getTaskResponse.getBody();
      assertThat(taskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(taskRepresentationModel.getOwner()).isEqualTo("user-1-1");

      // cancel claim
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_CLAIM_FORCE + "?keepOwner=true",
              "TKI:000000000000000000000000000000000001");
      ResponseEntity<TaskRepresentationModel> cancelClaimResponse =
          CLIENT
              .delete()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(cancelClaimResponse.getBody()).isNotNull();
      assertThat(cancelClaimResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskRepresentationModel cancelClaimedtaskRepresentationModel = cancelClaimResponse.getBody();
      assertThat(cancelClaimedtaskRepresentationModel.getClaimed()).isNull();
      assertThat(cancelClaimedtaskRepresentationModel.getState()).isEqualTo(TaskState.READY);
      assertThat(cancelClaimedtaskRepresentationModel.getOwner()).isEqualTo("user-1-1");
      assertThat(cancelClaimedtaskRepresentationModel.getOwnerLongName())
          .isEqualTo("Mustermann, Max - (user-1-1)");
    }

    @Test
    void should_ForceCancelClaim_When_TaskIsClaimedByDifferentOwner() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000027");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = getTaskResponse.getBody();
      assertThat(taskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(taskRepresentationModel.getOwner()).isEqualTo("user-1-2");

      // force cancel claim
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_CLAIM_FORCE, "TKI:000000000000000000000000000000000027");
      ResponseEntity<TaskRepresentationModel> cancelClaimResponse =
          CLIENT
              .delete()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(cancelClaimResponse.getBody()).isNotNull();
      assertThat(cancelClaimResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskRepresentationModel cancelClaimedtaskRepresentationModel = cancelClaimResponse.getBody();
      assertThat(cancelClaimedtaskRepresentationModel.getOwner()).isNull();
      assertThat(cancelClaimedtaskRepresentationModel.getClaimed()).isNull();
      assertThat(cancelClaimedtaskRepresentationModel.getState()).isEqualTo(TaskState.READY);
    }

    @Test
    void should_ThrowException_When_CancelClaimingOfClaimedTaskByAnotherUser() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000026");
      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> responseGet =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(responseGet.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = responseGet.getBody();
      assertThat(taskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
      assertThat(taskRepresentationModel.getOwner()).isEqualTo("user-1-1");

      // try to cancel claim
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_CLAIM, "TKI:000000000000000000000000000000000026");
      ThrowingCallable httpCall =
          () ->
              CLIENT
                  .delete()
                  .uri(url2)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(httpCall)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class SetTasksRead {
    @Test
    void should_setTaskRead() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000025");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = getTaskResponse.getBody();
      assertThat(taskRepresentationModel.isRead()).isFalse();

      // set Task read
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_SET_READ, "TKI:000000000000000000000000000000000025");
      ResponseEntity<TaskRepresentationModel> setReadResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .body(new IsReadRepresentationModel(true))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(setReadResponse.getBody()).isNotNull();
      assertThat(setReadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskRepresentationModel setReadTaskRepresentationModel = setReadResponse.getBody();
      assertThat(setReadTaskRepresentationModel.isRead()).isTrue();
    }

    @Test
    void should_setTaskUnread() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000027");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel taskRepresentationModel = getTaskResponse.getBody();
      assertThat(taskRepresentationModel.isRead()).isTrue();

      // set Task unread
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_SET_READ, "TKI:000000000000000000000000000000000027");
      ResponseEntity<TaskRepresentationModel> setUnreadResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .body(new IsReadRepresentationModel(false))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(setUnreadResponse.getBody()).isNotNull();
      assertThat(setUnreadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskRepresentationModel setReadTaskRepresentationModel = setUnreadResponse.getBody();
      assertThat(setReadTaskRepresentationModel.isRead()).isFalse();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class ReopenTasks {

    @Test
    void should_ReopenTaskRespondingWith200() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000075");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel readyTaskRepresentationModel = getTaskResponse.getBody();
      assertThat(readyTaskRepresentationModel.getState()).isEqualTo(TaskState.COMPLETED);

      // reopen
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REOPEN, "TKI:000000000000000000000000000000000075");
      ResponseEntity<TaskRepresentationModel> reopenResponse =
          CLIENT
              .post()
              .uri(url2)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);

      assertThat(reopenResponse.getBody()).isNotNull();
      assertThat(reopenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskRepresentationModel reopenedTaskRepresentationModel = reopenResponse.getBody();
      assertThat(reopenedTaskRepresentationModel.getState()).isEqualTo(TaskState.CLAIMED);
    }

    @Test
    void should_FailReopeningTaskRespondingWith400_ForTaskWithInvalidState() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000076");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-2-2")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel readyTaskRepresentationModel = getTaskResponse.getBody();
      assertThat(readyTaskRepresentationModel.getState()).isEqualTo(TaskState.READY);

      // reopen
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REOPEN, "TKI:000000000000000000000000000000000076");

      ThrowingCallable call =
          () ->
              CLIENT
                  .post()
                  .uri(url2)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-2-2")))
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(call)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_FailReopeningTaskRespondingWith400_ForTaskWithCallback() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:100000000000000000000000000000000099");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel readyTaskRepresentationModel = getTaskResponse.getBody();
      assertThat(readyTaskRepresentationModel.getState()).isEqualTo(TaskState.COMPLETED);

      // reopen
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REOPEN, "TKI:100000000000000000000000000000000099");

      ThrowingCallable call =
          () ->
              CLIENT
                  .post()
                  .uri(url2)
                  .headers(
                      headers -> headers.addAll(RestHelper.generateHeadersForUser("teamlead-1")))
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(call)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_FailReopeningTaskRespondingWith403_ForTaskWithInsufficientPermissions() {
      String url =
          restHelper.toUrl(RestEndpoints.URL_TASKS_ID, "TKI:000000000000000000000000000000000070");

      // retrieve task from Rest Api
      ResponseEntity<TaskRepresentationModel> getTaskResponse =
          CLIENT
              .get()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskRepresentationModel.class);
      assertThat(getTaskResponse.getBody()).isNotNull();
      TaskRepresentationModel readyTaskRepresentationModel = getTaskResponse.getBody();
      assertThat(readyTaskRepresentationModel.getState()).isEqualTo(TaskState.COMPLETED);

      // reopen
      String url2 =
          restHelper.toUrl(
              RestEndpoints.URL_TASKS_ID_REOPEN, "TKI:000000000000000000000000000000000070");

      ThrowingCallable call =
          () ->
              CLIENT
                  .post()
                  .uri(url2)
                  .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                  .retrieve()
                  .toEntity(TaskRepresentationModel.class);

      assertThatThrownBy(call)
          .extracting(HttpStatusCodeException.class::cast)
          .extracting(HttpStatusCodeException::getStatusCode)
          .isEqualTo(HttpStatus.FORBIDDEN);
    }
  }
}
