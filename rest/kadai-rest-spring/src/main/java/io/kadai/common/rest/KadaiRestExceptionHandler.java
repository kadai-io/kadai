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

package io.kadai.common.rest;

import static java.util.function.Predicate.not;

import io.kadai.classification.api.exceptions.ClassificationAlreadyExistException;
import io.kadai.classification.api.exceptions.ClassificationInUseException;
import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.classification.api.exceptions.MalformedServiceLevelException;
import io.kadai.common.api.exceptions.AutocommitFailedException;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.ConnectionNotSetException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.ErrorCode;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.api.exceptions.UnsupportedDatabaseException;
import io.kadai.common.api.exceptions.WrongCustomHolidayFormatException;
import io.kadai.common.rest.models.ExceptionRepresentationModel;
import io.kadai.spi.history.api.exceptions.KadaiHistoryEventNotFoundException;
import io.kadai.task.api.exceptions.AttachmentPersistenceException;
import io.kadai.task.api.exceptions.InvalidCallbackStateException;
import io.kadai.task.api.exceptions.InvalidOwnerException;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.exceptions.NotAuthorizedOnTaskCommentException;
import io.kadai.task.api.exceptions.TaskAlreadyExistException;
import io.kadai.task.api.exceptions.TaskCommentNotFoundException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.user.api.exceptions.UserAlreadyExistException;
import io.kadai.user.api.exceptions.UserNotFoundException;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.NotAuthorizedToQueryWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketAccessItemAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketInUseException;
import io.kadai.workbasket.api.exceptions.WorkbasketMarkedForDeletionException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Global handler for exceptions thrown in mapped request handlers. */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class KadaiRestExceptionHandler extends ResponseEntityExceptionHandler {

  public static final String ERROR_KEY_QUERY_MALFORMED = "QUERY_PARAMETER_MALFORMED";
  public static final String ERROR_KEY_PAYLOAD = "PAYLOAD_TOO_LARGE";
  public static final String ERROR_KEY_UNKNOWN_ERROR = "UNKNOWN_ERROR";

  @ExceptionHandler(MalformedServiceLevelException.class)
  public ResponseEntity<Object> handleMalformedServiceLevelException(
      MalformedServiceLevelException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(WrongCustomHolidayFormatException.class)
  public ResponseEntity<Object> handleWrongCustomHolidayFormatException(
      WrongCustomHolidayFormatException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DomainNotFoundException.class)
  public ResponseEntity<Object> handleDomainNotFoundException(
      DomainNotFoundException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidArgumentException.class)
  public ResponseEntity<Object> handleInvalidArgumentException(
      InvalidArgumentException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidCallbackStateException.class)
  public ResponseEntity<Object> handleInvalidCallbackStateException(
      InvalidCallbackStateException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidOwnerException.class)
  public ResponseEntity<Object> handleInvalidOwnerException(
      InvalidOwnerException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidTaskStateException.class)
  public ResponseEntity<Object> handleInvalidTaskStateException(
      InvalidTaskStateException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotAuthorizedException.class)
  public ResponseEntity<Object> handleNotAuthorizedException(
      NotAuthorizedException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(NotAuthorizedOnTaskCommentException.class)
  public ResponseEntity<Object> handleNotAuthorizedOnTaskCommentException(
      NotAuthorizedOnTaskCommentException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(NotAuthorizedOnWorkbasketException.class)
  public ResponseEntity<Object> handleNotAuthorizedOnWorkbasketException(
      NotAuthorizedOnWorkbasketException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(NotAuthorizedToQueryWorkbasketException.class)
  public ResponseEntity<Object> handleNotAuthorizedOnWorkbasketException(
      NotAuthorizedToQueryWorkbasketException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(ClassificationNotFoundException.class)
  public ResponseEntity<Object> handleClassificationNotFoundException(
      ClassificationNotFoundException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(TaskCommentNotFoundException.class)
  public ResponseEntity<Object> handleTaskCommentNotFoundException(
      TaskCommentNotFoundException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<Object> handleTaskNotFoundException(
      TaskNotFoundException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Object> handleUserNotFoundException(
      UserNotFoundException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(WorkbasketNotFoundException.class)
  public ResponseEntity<Object> handleWorkbasketNotFoundException(
      WorkbasketNotFoundException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(KadaiHistoryEventNotFoundException.class)
  public ResponseEntity<Object> handleKadaiHistoryEventNotFoundException(
      KadaiHistoryEventNotFoundException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(AttachmentPersistenceException.class)
  public ResponseEntity<Object> handleAttachmentPersistenceException(
      AttachmentPersistenceException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ClassificationAlreadyExistException.class)
  public ResponseEntity<Object> handleClassificationAlreadyExistException(
      ClassificationAlreadyExistException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ConcurrencyException.class)
  public ResponseEntity<Object> handleConcurrencyException(
      ConcurrencyException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(TaskAlreadyExistException.class)
  public ResponseEntity<Object> handleTaskAlreadyExistException(
      TaskAlreadyExistException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UserAlreadyExistException.class)
  public ResponseEntity<Object> handleUserAlreadyExistException(
      UserAlreadyExistException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(WorkbasketAccessItemAlreadyExistException.class)
  public ResponseEntity<Object> handleWorkbasketAccessItemAlreadyExistException(
      WorkbasketAccessItemAlreadyExistException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(WorkbasketAlreadyExistException.class)
  public ResponseEntity<Object> handleWorkbasketAlreadyExistException(
      WorkbasketAlreadyExistException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(WorkbasketMarkedForDeletionException.class)
  public ResponseEntity<Object> handleWorkbasketMarkedForDeletionException(
      WorkbasketMarkedForDeletionException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ClassificationInUseException.class)
  public ResponseEntity<Object> handleClassificationInUseException(
      ClassificationInUseException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.LOCKED);
  }

  @ExceptionHandler(WorkbasketInUseException.class)
  public ResponseEntity<Object> handleWorkbasketInUseException(
      WorkbasketInUseException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.LOCKED);
  }

  @ExceptionHandler(AutocommitFailedException.class)
  public ResponseEntity<Object> handleAutocommitFailedException(
      AutocommitFailedException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ConnectionNotSetException.class)
  public ResponseEntity<Object> handleConnectionNotSetException(
      ConnectionNotSetException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(SystemException.class)
  public ResponseEntity<Object> handleSystemException(
      SystemException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(UnsupportedDatabaseException.class)
  public ResponseEntity<Object> handleUnsupportedDatabaseException(
      UnsupportedDatabaseException ex, WebRequest req) {
    return handle(ex.getErrorCode(), ex, req, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
      @NonNull MaxUploadSizeExceededException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest req) {
    return handle(
        ErrorCode.of(ERROR_KEY_PAYLOAD),
        ex,
        req,
        HttpStatus.PAYLOAD_TOO_LARGE);
  }

  @ExceptionHandler(BeanInstantiationException.class)
  protected ResponseEntity<Object> handleBeanInstantiationException(
      BeanInstantiationException ex, WebRequest req) {
    return ex.getCause() instanceof InvalidArgumentException cause
        ? handleInvalidArgumentException(cause, req)
        : handle(null, ex, req, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // This ExceptionHandler exists to convert IllegalArgumentExceptions to InvalidArgumentExceptions.
  // Once IllegalArgumentExceptions are no longer in use, you can delete this \(*_*)/
  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<Object> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest req) {
    return handle(
        ErrorCode.of(InvalidArgumentException.ERROR_KEY),
        ex,
        req,
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<Object> handleGeneralException(Exception ex, WebRequest req) {
    ExceptionRepresentationModel errorData =
        new ExceptionRepresentationModel(
            ErrorCode.of(ERROR_KEY_UNKNOWN_ERROR), HttpStatus.INTERNAL_SERVER_ERROR, ex, req);
    logger.error(
        String.format("Unknown error occurred during processing of rest request: %s", errorData),
        ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorData);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    MalformedQueryParameter[] wrongQueryParameters =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::extractMalformedQueryParameters)
            .flatMap(Collection::stream)
            .toArray(MalformedQueryParameter[]::new);

    // if we have no wrong query parameter then this BindException is representing something else.
    // Therefore, we only create an ErrorCode when we have found a wrong query parameter.
    ErrorCode errorCode =
        wrongQueryParameters.length != 0
            ? ErrorCode.of(
                ERROR_KEY_QUERY_MALFORMED, Map.of("malformedQueryParameters", wrongQueryParameters))
            : null;

    return handle(errorCode, ex, request, HttpStatus.BAD_REQUEST);
  }

  private ResponseEntity<Object> handle(
      ErrorCode errorCode, Throwable ex, WebRequest req, HttpStatus status) {
    final ExceptionRepresentationModel errorData =
        new ExceptionRepresentationModel(errorCode, status, ex, req);

    switch (status.series()) {
      case CLIENT_ERROR -> logger.warn(
          String.format("Exception thrown during processing of rest request: %s", errorData));
      case SERVER_ERROR -> logger.error(
          String.format("Error occurred during processing of rest request: %s", errorData), ex);
      default -> logger.warn(
          String.format("Something occurred during processing of rest request: %s", errorData));
    }

    return ResponseEntity.status(status).body(errorData);
  }

  private List<MalformedQueryParameter> extractMalformedQueryParameters(FieldError fieldError) {
    if (fieldError.contains(TypeMismatchException.class)) {
      TypeMismatchException typeMismatchException = fieldError.unwrap(TypeMismatchException.class);
      if (typeMismatchException.getCause()
          instanceof ConversionFailedException conversionFailedException) {
        Class<?> targetType = conversionFailedException.getTargetType().getType();
        if (targetType.isEnum()) {
          String queryParameter = fieldError.getField();
          // the redundancy below exists because we want to keep the enums sorted by their ordinal
          // value for the error output and want to use the contains performance boost of a HashSet.
          List<String> enumConstants =
              Arrays.stream(targetType.getEnumConstants())
                  .map(Enum.class::cast)
                  .map(Enum::name)
                  .toList();
          Set<String> enumConstantSet = new HashSet<>(enumConstants);

          return getRejectedValues(typeMismatchException)
              .filter(not(enumConstantSet::contains))
              .map(value -> new MalformedQueryParameter(queryParameter, value, enumConstants))
              .toList();
        }
      }
    }

    return Collections.emptyList();
  }

  private Stream<String> getRejectedValues(TypeMismatchException ex) {
    Object value = ex.getValue();
    if (value != null && value.getClass().isArray()) {
      return Arrays.stream((Object[]) value).map(Objects::toString);
    }
    if (value != null && value.getClass().isAssignableFrom(Collection.class)) {
      return ((Collection<?>) value).stream().map(Objects::toString);
    }
    return Stream.of(value).map(Objects::toString);
  }

  public static class MalformedQueryParameter implements Serializable {
    private final String queryParameter;
    private final String actualValue;
    private final Collection<String> expectedValues;

    MalformedQueryParameter(
        String queryParameter, String actualValue, Collection<String> expectedValues) {
      this.queryParameter = queryParameter;
      this.actualValue = actualValue;
      this.expectedValues = expectedValues;
    }

    @SuppressWarnings("unused")
    public String getActualValue() {
      return actualValue;
    }

    @SuppressWarnings("unused")
    public Collection<String> getExpectedValues() {
      return expectedValues;
    }

    @SuppressWarnings("unused")
    public String getQueryParameter() {
      return queryParameter;
    }
  }
}
