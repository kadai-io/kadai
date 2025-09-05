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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import io.kadai.task.rest.models.BulkOperationResultsRepresentationModel;
import io.kadai.task.rest.models.TaskCommentCollectionRepresentationModel;
import io.kadai.task.rest.models.TaskCommentRepresentationModel;
import io.kadai.task.rest.models.TasksCommentBatchRepresentationModel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

/** Test TaskCommentController. */
@KadaiSpringBootTest
class TaskCommentControllerIntTest {

  private final RestHelper restHelper;

  @Autowired
  TaskCommentControllerIntTest(RestHelper restHelper) {
    this.restHelper = restHelper;
  }

  @Test
  void should_ReturnTaskComment_When_GivenTaskCommentId() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENT, "TCI:000000000000000000000000000000000000");

    ResponseEntity<TaskCommentRepresentationModel> response =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(TaskCommentRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void should_FailToReturnTaskComment_When_TaskCommentIsNotExisting() {
    String url = restHelper.toUrl(RestEndpoints.URL_TASK_COMMENT, "Non existing task comment Id");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                .retrieve()
                .toEntity(TaskCommentRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void should_FailToReturnTaskComments_When_TaskIstNotVisible() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENTS, "TKI:000000000000000000000000000000000004");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                .retrieve()
                .toEntity(TaskCommentCollectionRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_ReturnSortedAndOrederedTaskCommentsSortedByModified_When_UsingSortAndOrderParams() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENTS, "TKI:000000000000000000000000000000000000");

    HttpHeaders headers = RestHelper.generateHeadersForUser("admin");

    String url1 = url + "?sort-by=MODIFIED&order=DESCENDING";
    ResponseEntity<TaskCommentCollectionRepresentationModel>
        getTaskCommentsSortedByModifiedOrderedByDescendingResponse =
            CLIENT
                .get()
                .uri(url1)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(TaskCommentCollectionRepresentationModel.class);
    assertThat(getTaskCommentsSortedByModifiedOrderedByDescendingResponse.getBody()).isNotNull();
    assertThat(getTaskCommentsSortedByModifiedOrderedByDescendingResponse.getBody().getContent())
        .hasSize(3)
        .extracting(TaskCommentRepresentationModel::getModified)
        .isSortedAccordingTo(Comparator.reverseOrder());

    String url2 = url + "?sort-by=MODIFIED";
    ResponseEntity<TaskCommentCollectionRepresentationModel>
        getTaskCommentsSortedByModifiedOrderedByAscendingResponse =
            CLIENT
                .get()
                .uri(url2)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(TaskCommentCollectionRepresentationModel.class);
    assertThat(getTaskCommentsSortedByModifiedOrderedByAscendingResponse.getBody()).isNotNull();
    assertThat(getTaskCommentsSortedByModifiedOrderedByAscendingResponse.getBody().getContent())
        .hasSize(3)
        .extracting(TaskCommentRepresentationModel::getModified)
        .isSortedAccordingTo(Comparator.naturalOrder());

    String url3 = url + "?sort-by=CREATED&order=DESCENDING";
    ResponseEntity<TaskCommentCollectionRepresentationModel>
        getTaskCommentsSortedByCreatedOrderedByDescendingResponse =
            CLIENT
                .get()
                .uri(url3)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(TaskCommentCollectionRepresentationModel.class);
    assertThat(getTaskCommentsSortedByCreatedOrderedByDescendingResponse.getBody()).isNotNull();
    assertThat(getTaskCommentsSortedByCreatedOrderedByDescendingResponse.getBody().getContent())
        .hasSize(3)
        .extracting(TaskCommentRepresentationModel::getCreated)
        .isSortedAccordingTo(Comparator.reverseOrder());

    String url4 = url + "?sort-by=CREATED";
    ResponseEntity<TaskCommentCollectionRepresentationModel>
        getTaskCommentsSortedByCreatedOrderedByAscendingResponse =
            CLIENT
                .get()
                .uri(url4)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(TaskCommentCollectionRepresentationModel.class);
    assertThat(getTaskCommentsSortedByCreatedOrderedByAscendingResponse.getBody()).isNotNull();
    assertThat(getTaskCommentsSortedByCreatedOrderedByAscendingResponse.getBody().getContent())
        .hasSize(3)
        .extracting(TaskCommentRepresentationModel::getCreated)
        .isSortedAccordingTo(Comparator.naturalOrder());
  }

  @Test
  void should_ThrowException_When_UsingInvalidSortParam() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENTS, "TKI:000000000000000000000000000000000000");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url + "?sort-by=invalidSortParam")
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                .retrieve()
                .toEntity(TaskCommentCollectionRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_FailToReturnTaskComment_When_TaskIstNotVisible() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENT, "TCI:000000000000000000000000000000000012");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
                .retrieve()
                .toEntity(TaskCommentRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_CreateTaskComment_When_TaskIsVisible() {
    TaskCommentRepresentationModel taskCommentRepresentationModelToCreate =
        new TaskCommentRepresentationModel();
    taskCommentRepresentationModelToCreate.setTaskId("TKI:000000000000000000000000000000000004");
    taskCommentRepresentationModelToCreate.setTextField("newly created task comment");
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENTS, "TKI:000000000000000000000000000000000004");

    ResponseEntity<TaskCommentRepresentationModel> response =
        CLIENT
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .body(taskCommentRepresentationModelToCreate)
            .retrieve()
            .toEntity(TaskCommentRepresentationModel.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTaskCommentId()).isNotNull();
    assertThat(response.getBody().getCreator()).isEqualTo("admin");
    assertThat(response.getBody().getTaskId())
        .isEqualTo("TKI:000000000000000000000000000000000004");
    assertThat(response.getBody().getTextField()).isEqualTo("newly created task comment");
    assertThat(response.getBody().getCreated()).isNotNull();
    assertThat(response.getBody().getModified()).isNotNull();
    assertThat(response.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
  }

  @Test
  void should_FailToCreateTaskComment_When_TaskIsNotVisible() {
    TaskCommentRepresentationModel taskCommentRepresentationModelToCreate =
        new TaskCommentRepresentationModel();
    taskCommentRepresentationModelToCreate.setTaskId("TKI:000000000000000000000000000000000000");
    taskCommentRepresentationModelToCreate.setTextField("newly created task comment");
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENTS, "TKI:000000000000000000000000000000000000");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .post()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-b-1")))
                .body(taskCommentRepresentationModelToCreate)
                .retrieve()
                .toEntity(TaskCommentRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_FailToCreateTaskComment_When_TaskIdIsNonExisting() {
    TaskCommentRepresentationModel taskCommentRepresentationModelToCreate =
        new TaskCommentRepresentationModel();
    taskCommentRepresentationModelToCreate.setTaskId("DefinatelyNotExistingId");
    taskCommentRepresentationModelToCreate.setTextField("newly created task comment");
    String url = restHelper.toUrl(RestEndpoints.URL_TASK_COMMENTS, "DefinatelyNotExistingId");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .post()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                .body(taskCommentRepresentationModelToCreate)
                .retrieve()
                .toEntity(TaskCommentRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void should_CreateTaskCommentForMultipleTasks_When_TasksAreExisting() {
    List<String> taskIds = List.of(
            "TKI:000000000000000000000000000000000001",
            "TKI:000000000000000000000000000000000004"
    );
    String textField = "check for test "
            + "should_CreateTaskCommentForMultipleTasks_When_TasksAreExisting()";
    TasksCommentBatchRepresentationModel request =
            new TasksCommentBatchRepresentationModel(taskIds, textField);

    String url = restHelper.toUrl(RestEndpoints.URL_TASKS_COMMENTS);

    ResponseEntity<BulkOperationResultsRepresentationModel> response = CLIENT
            .post()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .body(request)
            .retrieve()
            .toEntity(BulkOperationResultsRepresentationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    Map<String, ErrorCode> errors =
            Objects.requireNonNull(response.getBody()).getTasksWithErrors();

    assertThat(errors).isEmpty();

    for (String taskId : taskIds) {
      String commentUrl = restHelper.toUrl(RestEndpoints.URL_TASK_COMMENTS, taskId);

      ResponseEntity<TaskCommentCollectionRepresentationModel> getResponse = CLIENT
              .get()
              .uri(commentUrl)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskCommentCollectionRepresentationModel.class);

      assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      TaskCommentCollectionRepresentationModel body = getResponse.getBody();
      assertThat(body).isNotNull();

      List<TaskCommentRepresentationModel> comments = new ArrayList<>(body.getContent());

      assertThat(comments)
              .anyMatch(c -> textField.equals(c.getTextField()));
    }
  }

  @Test
  void should_PartiallyCreateTaskComments_When_SomeTasksDoNotExist() {
    List<String> taskIds = List.of(
            "TKI:000000000000000000000000000000000001",  // valid
            "TKI:400000000000000000000000000000000004"  // invalid
    );
    String textField = "check for "
            + "test should_PartiallyCreateTaskComments_When_SomeTasksDoNotExist()";
    TasksCommentBatchRepresentationModel request =
            new TasksCommentBatchRepresentationModel(taskIds, textField);

    String url = restHelper.toUrl(RestEndpoints.URL_TASKS_COMMENTS);

    ResponseEntity<Map> response =
            CLIENT
                    .post()
                    .uri(url)
                    .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
                    .body(request)
                    .retrieve()
                    .toEntity(Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    Map<String, ?> responseBody = response.getBody();
    assertThat(responseBody).isNotNull();

    @SuppressWarnings("unchecked")
    Map<String, Map<String, Object>> tasksWithErrors = (Map<String, Map<String, Object>>)
            responseBody.get("tasksWithErrors");

    assertThat(tasksWithErrors)
            .hasSize(1)
            .containsKey("TKI:400000000000000000000000000000000004");

    Map<String, Object> errorDetails = tasksWithErrors
            .get("TKI:400000000000000000000000000000000004");
    assertThat(errorDetails).containsEntry("key", "TASK_NOT_FOUND");

    String taskId = "TKI:000000000000000000000000000000000001";
    String commentUrl = restHelper.toUrl(RestEndpoints.URL_TASK_COMMENTS, taskId);

    ResponseEntity<TaskCommentCollectionRepresentationModel> getResponse = CLIENT
            .get()
            .uri(commentUrl)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(TaskCommentCollectionRepresentationModel.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    TaskCommentCollectionRepresentationModel body = getResponse.getBody();
    assertThat(body).isNotNull();

    List<TaskCommentRepresentationModel> comments = new ArrayList<>(body.getContent());

    assertThat(comments)
            .anyMatch(c -> textField.equals(c.getTextField()));
  }

  @Test
  void should_FailToCreateTaskCommentsForMultipleTasks_When_TaskIdsAreNull() {
    String textField = "newly created task comment for multiple tasks";
    TasksCommentBatchRepresentationModel request =
            new TasksCommentBatchRepresentationModel(null, textField);

    String url = restHelper.toUrl(RestEndpoints.URL_TASKS_COMMENTS);

    ThrowingCallable httpCall = () ->
            CLIENT.post()
                    .uri(url)
                    .headers(h -> h.addAll(RestHelper.generateHeadersForUser("admin")))
                    .body(request)
                    .retrieve()
                    .toEntity(BulkOperationResultsRepresentationModel.class);

    assertThatThrownBy(httpCall)
            .isInstanceOf(HttpStatusCodeException.class)
            .extracting(ex -> ((HttpStatusCodeException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_FailToCreateTaskCommentsForMultipleTasks_When_TextFieldIsNull() {
    List<String> taskIds = List.of(
            "TKI:000000000000000000000000000000000001",
            "TKI:000000000000000000000000000000000004"
    );
    TasksCommentBatchRepresentationModel request =
            new TasksCommentBatchRepresentationModel(taskIds, null);

    String url = restHelper.toUrl(RestEndpoints.URL_TASKS_COMMENTS);

    ThrowingCallable httpCall = () ->
            CLIENT.post()
                    .uri(url)
                    .headers(h -> h.addAll(RestHelper.generateHeadersForUser("admin")))
                    .body(request)
                    .retrieve()
                    .toEntity(BulkOperationResultsRepresentationModel.class);

    assertThatThrownBy(httpCall)
            .isInstanceOf(HttpStatusCodeException.class)
            .extracting(ex -> ((HttpStatusCodeException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_FailToCreateTaskCommentsForMultipleTasks_When_TextIsEmpty() {
    List<String> taskIds = List.of(
            "TKI:000000000000000000000000000000000001",
            "TKI:000000000000000000000000000000000004"
    );
    String emptyText = "";
    TasksCommentBatchRepresentationModel request =
            new TasksCommentBatchRepresentationModel(taskIds, emptyText);

    String url = restHelper.toUrl(RestEndpoints.URL_TASKS_COMMENTS);

    ThrowingCallable httpCall = () ->
            CLIENT.post()
                    .uri(url)
                    .headers(h -> h.addAll(RestHelper.generateHeadersForUser("admin")))
                    .body(request)
                    .retrieve()
                    .toEntity(BulkOperationResultsRepresentationModel.class);

    assertThatThrownBy(httpCall)
            .isInstanceOf(HttpStatusCodeException.class)
            .extracting(ex -> ((HttpStatusCodeException) ex).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_UpdateTaskComment() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENT, "TCI:000000000000000000000000000000000003");

    HttpHeaders headers = RestHelper.generateHeadersForUser("admin");

    ResponseEntity<TaskCommentRepresentationModel> getTaskCommentResponse =
        CLIENT
            .get()
            .uri(url)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .retrieve()
            .toEntity(TaskCommentRepresentationModel.class);
    TaskCommentRepresentationModel taskCommentToUpdate = getTaskCommentResponse.getBody();
    assertThat(taskCommentToUpdate).isNotNull();
    assertThat(taskCommentToUpdate.getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(taskCommentToUpdate.getCreator()).isEqualTo("user-1-2");
    assertThat(taskCommentToUpdate.getTextField()).isEqualTo("some text in textfield");

    taskCommentToUpdate.setTextField("updated text in textfield");

    ResponseEntity<TaskCommentRepresentationModel> responseUpdate =
        CLIENT
            .put()
            .uri(url)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(taskCommentToUpdate)
            .retrieve()
            .toEntity(TaskCommentRepresentationModel.class);

    assertThat(responseUpdate.getBody()).isNotNull();
    assertThat(responseUpdate.getBody().getTextField()).isEqualTo("updated text in textfield");
  }

  @Test
  void should_FailToUpdateTaskComment_When_TaskCommentWasModifiedConcurrently() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENT, "TCI:000000000000000000000000000000000000");

    ResponseEntity<TaskCommentRepresentationModel> getTaskCommentResponse =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(TaskCommentRepresentationModel.class);
    TaskCommentRepresentationModel taskCommentToUpdate = getTaskCommentResponse.getBody();
    assertThat(taskCommentToUpdate).isNotNull();
    assertThat(taskCommentToUpdate.getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(taskCommentToUpdate.getCreator()).isEqualTo("user-1-1");
    assertThat(taskCommentToUpdate.getTextField()).isEqualTo("some text in textfield");

    taskCommentToUpdate.setModified(Instant.now());

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .put()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                .body(taskCommentToUpdate)
                .retrieve()
                .toEntity(TaskCommentRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void should_FailToUpdateTaskComment_When_UserHasNoAuthorization() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENT, "TCI:000000000000000000000000000000000000");

    ResponseEntity<TaskCommentRepresentationModel> getTaskCommentResponse =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
            .retrieve()
            .toEntity(TaskCommentRepresentationModel.class);
    TaskCommentRepresentationModel taskComment = getTaskCommentResponse.getBody();
    assertThat(taskComment).isNotNull();
    assertThat(taskComment.getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(taskComment.getCreator()).isEqualTo("user-1-1");
    assertThat(taskComment.getTextField()).isEqualTo("some text in textfield");

    taskComment.setTextField("updated textfield");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .put()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-2")))
                .body(taskComment)
                .retrieve()
                .toEntity(TaskCommentRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_FailToUpdateTaskComment_When_TaskCommentIdInResourceDoesNotMatchPathVariable() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENT, "TCI:000000000000000000000000000000000000");

    ResponseEntity<TaskCommentRepresentationModel> getTaskCommentResponse =
        CLIENT
            .get()
            .uri(url)
            .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
            .retrieve()
            .toEntity(TaskCommentRepresentationModel.class);
    assertThat(getTaskCommentResponse.getBody()).isNotNull();
    assertThat(getTaskCommentResponse.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(getTaskCommentResponse.getBody().getCreator()).isEqualTo("user-1-1");
    assertThat(getTaskCommentResponse.getBody().getTextField()).isEqualTo("some text in textfield");

    TaskCommentRepresentationModel taskCommentRepresentationModelToUpdate =
        getTaskCommentResponse.getBody();
    taskCommentRepresentationModelToUpdate.setTextField("updated text");
    taskCommentRepresentationModelToUpdate.setTaskCommentId("DifferentTaskCommentId");

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .put()
                .uri(url)
                .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("user-1-1")))
                .body(taskCommentRepresentationModelToUpdate)
                .retrieve()
                .toEntity(TaskCommentRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void should_DeleteTaskComment_When_UserHasAuthorization() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENT, "TCI:000000000000000000000000000000000006");

    HttpHeaders headers = RestHelper.generateHeadersForUser("admin");

    ResponseEntity<TaskCommentRepresentationModel> response =
        CLIENT
            .delete()
            .uri(url)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .retrieve()
            .toEntity(TaskCommentRepresentationModel.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ThrowingCallable httpCall =
        () ->
            CLIENT
                .get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(TaskCommentRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .hasMessageContaining("TASK_COMMENT_NOT_FOUND")
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void should_FailToDeleteTaskComment_When_UserHasNoAuthorization() {
    String url =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENTS, "TKI:000000000000000000000000000000000001");

    HttpHeaders headers = RestHelper.generateHeadersForUser("user-1-2");

    ResponseEntity<TaskCommentCollectionRepresentationModel>
        getTaskCommentsBeforeDeleteionResponse =
            CLIENT
                .get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(TaskCommentCollectionRepresentationModel.class);

    assertThat(getTaskCommentsBeforeDeleteionResponse.getBody()).isNotNull();
    assertThat(getTaskCommentsBeforeDeleteionResponse.getBody().getContent()).hasSize(2);

    String url2 =
        restHelper.toUrl(
            RestEndpoints.URL_TASK_COMMENT, "TCI:000000000000000000000000000000000004");
    ThrowingCallable httpCall =
        () ->
            CLIENT
                .delete()
                .uri(url2)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(TaskCommentRepresentationModel.class);

    assertThatThrownBy(httpCall)
        .isInstanceOf(HttpStatusCodeException.class)
        .hasMessageContaining("NOT_AUTHORIZED_ON_TASK_COMMENT")
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void should_FailToDeleteTaskComment_When_TaskCommentIsNotExisting() {

    String url = restHelper.toUrl(RestEndpoints.URL_TASK_COMMENT, "NotExistingTaskComment");

    ThrowingCallable httpCall =
        () -> {
          CLIENT
              .delete()
              .uri(url)
              .headers(headers -> headers.addAll(RestHelper.generateHeadersForUser("admin")))
              .retrieve()
              .toEntity(TaskCommentRepresentationModel.class);
        };

    assertThatThrownBy(httpCall)
        .extracting(HttpStatusCodeException.class::cast)
        .extracting(HttpStatusCodeException::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
  }
}
