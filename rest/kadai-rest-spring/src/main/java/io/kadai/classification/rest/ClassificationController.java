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

package io.kadai.classification.rest;

import io.kadai.classification.api.ClassificationQuery;
import io.kadai.classification.api.ClassificationService;
import io.kadai.classification.api.exceptions.ClassificationAlreadyExistException;
import io.kadai.classification.api.exceptions.ClassificationInUseException;
import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.classification.api.exceptions.MalformedServiceLevelException;
import io.kadai.classification.api.models.Classification;
import io.kadai.classification.api.models.ClassificationSummary;
import io.kadai.classification.rest.assembler.ClassificationRepresentationModelAssembler;
import io.kadai.classification.rest.assembler.ClassificationSummaryRepresentationModelAssembler;
import io.kadai.classification.rest.models.ClassificationRepresentationModel;
import io.kadai.classification.rest.models.ClassificationSummaryPagedRepresentationModel;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.common.api.exceptions.DomainNotFoundException;
import io.kadai.common.api.exceptions.InvalidArgumentException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.rest.QueryPagingParameter;
import io.kadai.common.rest.QuerySortParameter;
import io.kadai.common.rest.RestEndpoints;
import io.kadai.common.rest.util.QueryParamsValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Controller for all {@link Classification} related endpoints. */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class ClassificationController implements ClassificationApi {

  private final ClassificationService classificationService;
  private final ClassificationRepresentationModelAssembler modelAssembler;
  private final ClassificationSummaryRepresentationModelAssembler summaryModelAssembler;

  @Autowired
  ClassificationController(
      ClassificationService classificationService,
      ClassificationRepresentationModelAssembler modelAssembler,
      ClassificationSummaryRepresentationModelAssembler summaryModelAssembler) {
    this.classificationService = classificationService;
    this.modelAssembler = modelAssembler;
    this.summaryModelAssembler = summaryModelAssembler;
  }

  @GetMapping(path = RestEndpoints.URL_CLASSIFICATIONS)
  public ResponseEntity<ClassificationSummaryPagedRepresentationModel> getClassifications(
      HttpServletRequest request,
      @ParameterObject final ClassificationQueryFilterParameter filterParameter,
      @ParameterObject final ClassificationQuerySortParameter sortParameter,
      @ParameterObject
          final QueryPagingParameter<ClassificationSummary, ClassificationQuery> pagingParameter) {

    QueryParamsValidator.validateParams(
        request,
        ClassificationQueryFilterParameter.class,
        QuerySortParameter.class,
        QueryPagingParameter.class);

    final ClassificationQuery query = classificationService.createClassificationQuery();
    filterParameter.apply(query);
    sortParameter.apply(query);
    List<ClassificationSummary> classificationSummaries = pagingParameter.apply(query);

    return ResponseEntity.ok(
        summaryModelAssembler.toPagedModel(
            classificationSummaries, pagingParameter.getPageMetadata()));
  }

  @GetMapping(path = RestEndpoints.URL_CLASSIFICATIONS_ID, produces = MediaTypes.HAL_JSON_VALUE)
  public ResponseEntity<ClassificationRepresentationModel> getClassification(
      @PathVariable("classificationId") String classificationId)
      throws ClassificationNotFoundException {
    Classification classification = classificationService.getClassification(classificationId);
    return ResponseEntity.ok(modelAssembler.toModel(classification));
  }

  @PostMapping(path = RestEndpoints.URL_CLASSIFICATIONS)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> createClassification(
      @RequestBody ClassificationRepresentationModel repModel)
      throws ClassificationAlreadyExistException,
          DomainNotFoundException,
          InvalidArgumentException,
          MalformedServiceLevelException,
          NotAuthorizedException {
    Classification classification = modelAssembler.toEntityModel(repModel);
    classification = classificationService.createClassification(classification);

    return ResponseEntity.status(HttpStatus.CREATED).body(modelAssembler.toModel(classification));
  }

  @PutMapping(path = RestEndpoints.URL_CLASSIFICATIONS_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> updateClassification(
      @PathVariable("classificationId") String classificationId,
      @RequestBody ClassificationRepresentationModel resource)
      throws ClassificationNotFoundException,
          ConcurrencyException,
          InvalidArgumentException,
          MalformedServiceLevelException,
          NotAuthorizedException {
    if (!classificationId.equals(resource.getClassificationId())) {
      throw new InvalidArgumentException(
          String.format(
              "ClassificationId ('%s') of the URI is not identical"
                  + " with the classificationId ('%s') of the object in the payload.",
              classificationId, resource.getClassificationId()));
    }
    Classification classification = modelAssembler.toEntityModel(resource);
    classification = classificationService.updateClassification(classification);

    return ResponseEntity.ok(modelAssembler.toModel(classification));
  }

  @DeleteMapping(path = RestEndpoints.URL_CLASSIFICATIONS_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> deleteClassification(
      @PathVariable("classificationId") String classificationId)
      throws ClassificationNotFoundException, ClassificationInUseException, NotAuthorizedException {
    classificationService.deleteClassification(classificationId);
    return ResponseEntity.noContent().build();
  }
}
