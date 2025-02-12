package io.kadai.workbasket.rest;

import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import io.kadai.workbasket.api.exceptions.WorkbasketAccessItemAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketAlreadyExistException;
import io.kadai.workbasket.api.exceptions.WorkbasketNotFoundException;
import io.kadai.workbasket.api.models.Workbasket;
import io.kadai.workbasket.rest.models.WorkbasketDefinitionCollectionRepresentationModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface WorkbasketDefinitionApi {

  /**
   * This endpoint exports all Workbaskets with the corresponding Workbasket Access Items and
   * Distribution Targets. We call this data structure Workbasket Definition.
   *
   * @title Export Workbaskets
   * @param domain Filter the export for a specific domain.
   * @return all workbaskets.
   */
  @Operation(
      summary = "Export Workbaskets",
      description =
          "This endpoint exports all Workbaskets with the corresponding Workbasket Access Items "
              + "and Distribution Targets. We call this data structure Workbasket Definition.",
      parameters = {
        @Parameter(name = "domain", description = "Filter the export for a specific domain.")
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "all workbaskets.",
            content = {
              @Content(
                  mediaType = MediaTypes.HAL_JSON_VALUE,
                  schema =
                      @Schema(
                          implementation = WorkbasketDefinitionCollectionRepresentationModel.class))
            })
      })
  @GetMapping(path = RestEndpoints.URL_WORKBASKET_DEFINITIONS)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  ResponseEntity<WorkbasketDefinitionCollectionRepresentationModel> exportWorkbaskets(
      @RequestParam(value = "domain", required = false) String[] domain);

  /**
   * This endpoint imports a list of Workbasket Definitions.
   *
   * <p>This does not exactly match the REST norm, but we want to have an option to import all
   * settings at once. When a logical equal (key and domain are equal) Workbasket already exists an
   * update will be executed. Otherwise a new Workbasket will be created.
   *
   * @title Import Workbaskets
   * @param file the list of Workbasket Definitions which will be imported to the current system.
   * @return no content
   * @throws IOException if multipart file cannot be parsed.
   * @throws NotAuthorizedException if the user is not authorized.
   * @throws DomainNotFoundException if domain information is incorrect.
   * @throws WorkbasketAlreadyExistException if any Workbasket already exists when trying to create
   *     a new one.
   * @throws WorkbasketNotFoundException if do not exists a {@linkplain Workbasket} in the system
   *     with the used id.
   * @throws InvalidArgumentException if any Workbasket has invalid information or authorization
   *     information in {@linkplain Workbasket}s' definitions is incorrect.
   * @throws WorkbasketAccessItemAlreadyExistException if a WorkbasketAccessItem for the same
   *     Workbasket and access id already exists.
   * @throws ConcurrencyException if Workbasket was updated by an other user
   * @throws NotAuthorizedOnWorkbasketException if the current user has not correct permissions
   */
  @Operation(
      summary = "Import Workbaskets",
      description =
          "This endpoint imports a list of Workbasket Definitions.<p>This does not exactly match "
              + "the REST norm, but we want to have an option to import all settings at once. When"
              + " a logical equal (key and domain are equal) Workbasket already exists an update "
              + "will be executed. Otherwise a new Workbasket will be created.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "the list of Workbasket Definitions which will be imported to the current system."
                      + " To get an example file containing Workbasket Definitions, go to the "
                      + "[KADAI UI](http://localhost:8080/kadai/index.html) and export the"
                      + "Workbaskets",
              required = true,
              content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)),
      responses = {
        @ApiResponse(
            responseCode = "204",
            content = {@Content(schema = @Schema())}),
        @ApiResponse(
            responseCode = "400",
            description = "DOMAIN_NOT_FOUND, INVALID_ARGUMENT",
            content = {
              @Content(
                  schema =
                      @Schema(
                          oneOf = {DomainNotFoundException.class, InvalidArgumentException.class}))
            }),
        @ApiResponse(
            responseCode = "403",
            description =
                "NOT_AUTHORIZED_ON_WORKBASKET_WITH_ID, "
                    + "NOT_AUTHORIZED_ON_WORKBASKET_WITH_KEY_AND_DOMAIN, NOT_AUTHORIZED",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            NotAuthorizedOnWorkbasketException.class,
                            NotAuthorizedException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "WORKBASKET_WITH_ID_NOT_FOUND, WORKBASKET_WITH_KEY_NOT_FOUND",
            content = {
              @Content(schema = @Schema(implementation = WorkbasketNotFoundException.class))
            }),
        @ApiResponse(
            responseCode = "409",
            description =
                "WORKBASKET_ALREADY_EXISTS, WORKBASKET_ACCESS_ITEM_ALREADY_EXISTS, "
                    + "ENTITY_NOT_UP_TO_DATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            WorkbasketAlreadyExistException.class,
                            WorkbasketAccessItemAlreadyExistException.class,
                            ConcurrencyException.class
                          }))
            }),
      })
  @PostMapping(
      path = RestEndpoints.URL_WORKBASKET_DEFINITIONS,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<Void> importWorkbaskets(@RequestParam("file") MultipartFile file)
      throws IOException,
          DomainNotFoundException,
          InvalidArgumentException,
          WorkbasketAlreadyExistException,
          WorkbasketNotFoundException,
          WorkbasketAccessItemAlreadyExistException,
          ConcurrencyException,
          NotAuthorizedOnWorkbasketException,
          NotAuthorizedException;
}
