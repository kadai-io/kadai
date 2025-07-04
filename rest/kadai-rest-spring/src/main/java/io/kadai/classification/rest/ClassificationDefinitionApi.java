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

package io.kadai.classification.rest;

import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.classification.api.exceptions.MalformedServiceLevelException;
import io.kadai.classification.rest.assembler.ClassificationDefinitionCollectionRepresentationModel;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.LogicalDuplicateInPayloadException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.RestEndpoints;
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

public interface ClassificationDefinitionApi {

  @Operation(
      summary = "Export Classifications",
      description = "This endpoint exports all configured Classifications.",
      parameters = {@Parameter(name = "domain", description = "Filter the export by domain")},
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "the configured Classifications.",
            content =
                @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE,
                    schema =
                        @Schema(
                            implementation =
                                ClassificationDefinitionCollectionRepresentationModel.class)))
      })
  @GetMapping(path = RestEndpoints.URL_CLASSIFICATION_DEFINITIONS)
  ResponseEntity<ClassificationDefinitionCollectionRepresentationModel> exportClassifications(
      @RequestParam(value = "domain", required = false) String[] domain);

  @Operation(
      summary = "Import Classifications",
      description =
          "This endpoint imports all Classifications. Existing Classifications will not be removed."
              + " Existing Classifications with the same key/domain will be overridden.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "the file containing the Classifications which should be imported. To get an "
                      + "example file containing the Classificatioins, go to the "
                      + "[KADAI UI](http://localhost:8080/kadai/index.html) and export the "
                      + "Classifications",
              required = true,
              content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)),
      responses = {
        @ApiResponse(
            responseCode = "204",
            content = {@Content(schema = @Schema())}),
        @ApiResponse(
            responseCode = "400",
            description =
                "INVALID_ARGUMENT, CLASSIFICATION_SERVICE_LEVEL_MALFORMED,"
                    + " LOGICAL_DUPLICATE_IN_PAYLOAD",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            InvalidArgumentException.class,
                            MalformedServiceLevelException.class,
                            LogicalDuplicateInPayloadException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "403",
            description = "NOT_AUTHORIZED",
            content = {@Content(schema = @Schema(implementation = NotAuthorizedException.class))}),
        @ApiResponse(
            responseCode = "404",
            description =
                "CLASSIFICATION_WITH_ID_NOT_FOUND, CLASSIFICATION_WITH_KEY_NOT_FOUND, "
                    + "DOMAIN_NOT_FOUND",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            ClassificationNotFoundException.class,
                            DomainNotFoundException.class
                          }))
            }),
        @ApiResponse(
            responseCode = "409",
            description = "ENTITY_NOT_UP_TO_DATE",
            content = {
              @Content(
                  schema =
                      @Schema(
                          anyOf = {
                            ConcurrencyException.class,
                          }))
            }),
      })
  @PostMapping(
      path = RestEndpoints.URL_CLASSIFICATION_DEFINITIONS,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional(rollbackFor = Exception.class)
  ResponseEntity<Void> importClassifications(@RequestParam("file") MultipartFile file)
      throws InvalidArgumentException,
          ConcurrencyException,
          ClassificationNotFoundException,
          LogicalDuplicateInPayloadException,
          DomainNotFoundException,
          IOException,
          MalformedServiceLevelException,
          NotAuthorizedException;
}
