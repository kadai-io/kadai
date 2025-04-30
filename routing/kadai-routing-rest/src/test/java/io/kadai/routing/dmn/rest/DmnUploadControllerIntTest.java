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

package io.kadai.routing.dmn.rest;

import static io.kadai.rest.test.RestHelper.CLIENT;

import io.kadai.rest.test.KadaiSpringBootTest;
import io.kadai.rest.test.RestHelper;
import java.io.File;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Test DmnUploadController. */
@KadaiSpringBootTest
class DmnUploadControllerIntTest {

  private static final String EXCEL_NAME = "testExcelRouting.xlsx";
  private static final String HTTP_BODY_FILE_NAME = "excelRoutingFile";
  private final RestHelper restHelper;

  @Autowired
  DmnUploadControllerIntTest(RestHelper restHelper) {
    this.restHelper = restHelper;
  }

  @Test
  void should_returnCorrectAmountOfImportedRoutingRules() throws Exception {

    File excelRoutingFile = new ClassPathResource(EXCEL_NAME).getFile();

    MultiValueMap<String, FileSystemResource> body = new LinkedMultiValueMap<>();
    body.add(HTTP_BODY_FILE_NAME, new FileSystemResource(excelRoutingFile));

    HttpHeaders headers = RestHelper.generateHeadersForUser("admin");
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    String url = restHelper.toUrl(RoutingRestEndpoints.URL_ROUTING_RULES_DEFAULT);

    ResponseEntity<RoutingUploadResultRepresentationModel> responseEntity =
        CLIENT
            .put()
            .uri(url)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(body)
            .retrieve()
            .toEntity(RoutingUploadResultRepresentationModel.class);

    SoftAssertions softly = new SoftAssertions();

    softly
        .assertThat(responseEntity.getBody())
        .extracting(RoutingUploadResultRepresentationModel::getAmountOfImportedRows)
        .isEqualTo(3);

    softly
        .assertThat(responseEntity.getBody())
        .extracting(RoutingUploadResultRepresentationModel::getResult)
        .isEqualTo("Successfully imported 3 routing rules from the provided excel sheet");

    softly.assertAll();
  }
}
