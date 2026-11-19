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

package io.kadai.routing.dmn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kadai.KadaiConfiguration;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.KadaiEngine.ConnectionManagementMode;
import io.kadai.common.api.exceptions.KadaiRuntimeException;
import io.kadai.common.api.exceptions.NotAuthorizedException;
import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.test.config.DataSourceGenerator;
import io.kadai.common.test.security.JaasExtension;
import io.kadai.common.test.security.WithAccessId;
import io.kadai.sampledata.SampleDataGenerator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xlsx4j.sml.CTRst;

@ExtendWith(JaasExtension.class)
class DmnConverterServiceAccTest {

  private static final String EXCEL_NAME = "testExcelRouting.xlsx";
  private static final String EXCEL_NAME_INVALID_OUTPUTS =
      "testExcelRoutingWithInvalidOutputs.xlsx";
  // values of the testExcelRouting.xlsx cells which are replaced in the tests below
  private static final String INPUT_CELL_B8_VALUE = "12345678";
  private static final String DESCRIPTION_CELL_F6_VALUE = "VIP-Team";
  private static final String OUTPUT_CELLS_D6_TO_D8_VALUE = "GPK_KSC";
  private static KadaiEngine kadaiEngine;

  @TempDir Path tempDir;

  @BeforeAll
  protected static void setupTest() throws Exception {
    resetDb(false);
  }

  protected static void resetDb(boolean dropTables) throws Exception {

    DataSource dataSource = DataSourceGenerator.createDataSourceForH2();
    String schemaName = "KADAI";
    KadaiConfiguration kadaiConfiguration =
        new KadaiConfiguration.Builder(dataSource, false, schemaName)
            .initKadaiProperties()
            .germanPublicHolidaysEnabled(true)
            .build();
    SampleDataGenerator sampleDataGenerator =
        new SampleDataGenerator(dataSource, kadaiConfiguration.getSchemaName());
    if (dropTables) {
      sampleDataGenerator.dropDb();
    }
    kadaiEngine =
        KadaiEngine.buildKadaiEngine(kadaiConfiguration, ConnectionManagementMode.AUTOCOMMIT);

    sampleDataGenerator.clearDb();
    sampleDataGenerator.generateTestData();
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_ConvertExcelToDmn() throws Exception {

    File excelRoutingFile = new ClassPathResource(EXCEL_NAME).getFile();
    InputStream targetStream = new FileInputStream(excelRoutingFile);

    MultipartFile routingMultiPartFile = new MockMultipartFile(EXCEL_NAME, targetStream);

    DmnConverterService dmnConverterService = new DmnConverterService(kadaiEngine);
    dmnConverterService.setDmnUploadPath("target\\routing.dmn");
    DmnModelInstance dmnModelInstance = dmnConverterService.convertExcelToDmn(routingMultiPartFile);

    assertThat(dmnModelInstance.getModelElementsByType(Rule.class)).hasSize(3);
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_ThrowException_When_ProvidingInvalidOutputKeyDomains() throws Exception {

    File excelRoutingFile = new ClassPathResource(EXCEL_NAME_INVALID_OUTPUTS).getFile();
    InputStream targetStream = new FileInputStream(excelRoutingFile);

    MultipartFile routingMultiPartFile =
        new MockMultipartFile(EXCEL_NAME_INVALID_OUTPUTS, targetStream);

    DmnConverterService dmnConverterService = new DmnConverterService(kadaiEngine);

    ThrowingCallable call = () -> dmnConverterService.convertExcelToDmn(routingMultiPartFile);
    assertThatThrownBy(call)
        .extracting(KadaiRuntimeException.class::cast)
        .extracting(KadaiRuntimeException::getMessage)
        .isEqualTo(
            "Unknown workbasket Key/Domain pairs defined in DMN Table: "
                + "[KeyDomain [key=GPK_KSC1, domain=DOMAIN_A], "
                + "KeyDomain [key=GPK_KSC, domain=DOMAIN_XZ]]");
  }

  @Test
  @WithAccessId(user = "user-1-1")
  void should_ThrowException_When_NotAuthorized() throws Exception {

    File excelRoutingFile = new ClassPathResource(EXCEL_NAME).getFile();
    InputStream targetStream = new FileInputStream(excelRoutingFile);

    MultipartFile routingMultiPartFile = new MockMultipartFile(EXCEL_NAME, targetStream);

    DmnConverterService dmnConverterService = new DmnConverterService(kadaiEngine);

    ThrowingCallable call = () -> dmnConverterService.convertExcelToDmn(routingMultiPartFile);
    assertThatThrownBy(call).isInstanceOf(NotAuthorizedException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"A&B", "A<B", "A>B", "\"A\"", "&amp;", "a & b < c > d", "A\r\nB\rC"})
  @WithAccessId(user = "businessadmin")
  void should_StoreCellValuesExactlyAsEntered_When_TheyContainXmlSpecialCharacters(String cellValue)
      throws Exception {
    MultipartFile routingMultiPartFile =
        excelRoutingFileWithReplacedCellValues(
            Map.of(INPUT_CELL_B8_VALUE, cellValue, DESCRIPTION_CELL_F6_VALUE, cellValue));

    DmnModelInstance storedDmn = convertExcelToDmnAndReadStoredDmn(routingMultiPartFile);

    InputEntry inputEntry = storedDmn.getModelElementById("B8");
    assertThat(inputEntry.getText().getTextContent()).isEqualTo("\"" + cellValue + "\"");
    Rule rule = storedDmn.getModelElementById("excelRow6");
    assertThat(rule.getDescription().getTextContent()).isEqualTo(cellValue);
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_StoreFeelComparisonExactlyAsEntered_When_CellValueStartsWithLessThan()
      throws Exception {
    MultipartFile routingMultiPartFile =
        excelRoutingFileWithReplacedCellValues(Map.of(INPUT_CELL_B8_VALUE, "<= 10"));

    DmnModelInstance storedDmn = convertExcelToDmnAndReadStoredDmn(routingMultiPartFile);

    InputEntry inputEntry = storedDmn.getModelElementById("B8");
    assertThat(inputEntry.getText().getTextContent()).isEqualTo("<= 10");
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_NotChangeDmnStructure_When_CellValueContainsXmlMarkup() throws Exception {
    String injection = "12345678\"</text></inputEntry><inputEntry id=\"INJECTED\"><text>\"injected";
    MultipartFile routingMultiPartFile =
        excelRoutingFileWithReplacedCellValues(Map.of(INPUT_CELL_B8_VALUE, injection));

    DmnModelInstance storedDmn = convertExcelToDmnAndReadStoredDmn(routingMultiPartFile);

    ModelElementInstance injectedElement = storedDmn.getModelElementById("INJECTED");
    assertThat(injectedElement).isNull();
    assertThat(storedDmn.getModelElementsByType(Rule.class)).hasSize(3);
    assertThat(storedDmn.getModelElementsByType(InputEntry.class)).hasSize(6);
    Rule rule = storedDmn.getModelElementById("excelRow8");
    assertThat(rule.getInputEntries()).extracting(DmnElement::getId).containsExactly("B8", "C8");
    InputEntry inputEntry = storedDmn.getModelElementById("B8");
    assertThat(inputEntry.getText().getTextContent()).isEqualTo("\"" + injection + "\"");
  }

  @Test
  @WithAccessId(user = "businessadmin")
  void should_KeepXmlSpecialCharactersInOutputEntries_When_ValidatingOutputs() throws Exception {
    MultipartFile routingMultiPartFile =
        excelRoutingFileWithReplacedCellValues(Map.of(OUTPUT_CELLS_D6_TO_D8_VALUE, "GPK_KSC&<>"));

    DmnConverterService dmnConverterService = new DmnConverterService(kadaiEngine);

    ThrowingCallable call = () -> dmnConverterService.convertExcelToDmn(routingMultiPartFile);
    assertThatThrownBy(call)
        .isInstanceOf(SystemException.class)
        .hasMessage(
            "Unknown workbasket Key/Domain pairs defined in DMN Table: "
                + "[KeyDomain [key=GPK_KSC&<>, domain=DOMAIN_A]]");
  }

  private DmnModelInstance convertExcelToDmnAndReadStoredDmn(MultipartFile routingMultiPartFile)
      throws Exception {
    File dmnFile = tempDir.resolve("routing.dmn").toFile();
    DmnConverterService dmnConverterService = new DmnConverterService(kadaiEngine);
    dmnConverterService.setDmnUploadPath(dmnFile.getPath());

    dmnConverterService.convertExcelToDmn(routingMultiPartFile);

    return Dmn.readModelFromFile(dmnFile);
  }

  private static MultipartFile excelRoutingFileWithReplacedCellValues(
      Map<String, String> replacementsByOriginalCellValue) throws Exception {
    SpreadsheetMLPackage spreadsheet;
    try (InputStream inputStream = new ClassPathResource(EXCEL_NAME).getInputStream()) {
      spreadsheet = SpreadsheetMLPackage.load(inputStream);
    }
    // the excel file stores all string cell values in its shared strings table
    List<CTRst> sharedStrings =
        spreadsheet.getWorkbookPart().getSharedStrings().getContents().getSi();
    replacementsByOriginalCellValue.forEach((originalValue, replacement) -> {
      List<CTRst> matchingSharedStrings =
          sharedStrings.stream()
              .filter(sharedString -> originalValue.equals(sharedString.getT().getValue()))
              .toList();
      assertThat(matchingSharedStrings).hasSize(1);
      matchingSharedStrings.get(0).getT().setValue(replacement);
    });

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    spreadsheet.save(outputStream);
    return new MockMultipartFile(EXCEL_NAME, outputStream.toByteArray());
  }
}
