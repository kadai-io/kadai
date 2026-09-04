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

package org.camunda.bpm.dmn.xlsx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTSst;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.ObjectFactory;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.Worksheet;

class XlsxConverterTest {

  @Test
  void should_ThrowXlsxConversionException_When_InputIsMalformed() {
    XlsxConverter converter = new XlsxConverter();

    assertThatThrownBy(
            () ->
                converter.convert(
                    new ByteArrayInputStream("not an xlsx".getBytes(StandardCharsets.UTF_8))))
        .isExactlyInstanceOf(XlsxConversionException.class)
        .hasMessageContaining("Could not load XLSX")
        .hasCauseInstanceOf(Docx4JException.class);
  }

  @Test
  void should_ThrowXlsxConversionException_When_WorkbookHasNoWorksheets(@TempDir Path tempDir)
      throws Exception {
    Path emptyWorkbook = tempDir.resolve("empty.xlsx");
    SpreadsheetMLPackage spreadsheetPackage = SpreadsheetMLPackage.createPackage();
    spreadsheetPackage.save(emptyWorkbook.toFile());

    assertThatThrownBy(
            () ->
                new XlsxConverter()
                    .convert(new ByteArrayInputStream(Files.readAllBytes(emptyWorkbook))))
        .isExactlyInstanceOf(XlsxConversionException.class)
        .hasMessageContaining("does not contain any worksheets");
  }

  @Test
  void should_ConvertFirstWorksheet_When_NoWorksheetIndexIsConfigured() throws Exception {
    XlsxConverter converter = new XlsxConverter();

    assertThat(converter.getWorksheetIndex()).isEqualTo(XlsxConverter.DEFAULT_WORKSHEET_INDEX);

    DmnModelInstance result =
        converter.convert(new ByteArrayInputStream(createTwoWorksheetWorkbook()));

    assertThatWorksheetWasConverted(result, "FirstDecision", "first-input", "first-output");
  }

  @Test
  void should_ConvertSelectedWorksheet_When_WorksheetIndexIsConfigured() throws Exception {
    XlsxConverter converter = new XlsxConverter();
    converter.setWorksheetIndex(1);

    assertThat(converter.getWorksheetIndex()).isEqualTo(1);

    DmnModelInstance result =
        converter.convert(new ByteArrayInputStream(createTwoWorksheetWorkbook()));

    assertThatWorksheetWasConverted(result, "SecondDecision", "second-input", "second-output");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 2})
  void should_ThrowXlsxConversionException_When_WorksheetIndexIsInvalid(int worksheetIndex) {
    XlsxConverter converter = new XlsxConverter();
    converter.setWorksheetIndex(worksheetIndex);

    assertThatThrownBy(
            () -> converter.convert(new ByteArrayInputStream(createTwoWorksheetWorkbook())))
        .isExactlyInstanceOf(XlsxConversionException.class)
        .hasMessageContaining(Integer.toString(worksheetIndex))
        .hasMessageContaining("2 worksheet");
  }

  private static void assertThatWorksheetWasConverted(
      DmnModelInstance result, String expectedName, String expectedInput, String expectedOutput) {
    Decision decision =
        result.getDefinitions().getChildElementsByType(Decision.class).iterator().next();
    assertThat(decision.getId()).isEqualTo(expectedName);
    assertThat(decision.getName()).isEqualTo(expectedName);

    DecisionTable table = TestHelper.assertAndGetSingleDecisionTable(result);
    Rule rule = table.getRules().iterator().next();
    assertThat(rule.getInputEntries().iterator().next().getTextContent())
        .isEqualTo('"' + expectedInput + '"');
    assertThat(rule.getOutputEntries().iterator().next().getTextContent())
        .isEqualTo('"' + expectedOutput + '"');
  }

  private static byte[] createTwoWorksheetWorkbook() throws Exception {
    ObjectFactory objectFactory = Context.getsmlObjectFactory();
    SpreadsheetMLPackage spreadsheetPackage = SpreadsheetMLPackage.createPackage();

    SharedStrings sharedStringsPart = new SharedStrings(new PartName("/xl/sharedStrings.xml"));
    spreadsheetPackage.getWorkbookPart().addTargetPart(sharedStringsPart);
    CTSst sharedStrings = objectFactory.createCTSst();
    sharedStringsPart.setContents(sharedStrings);

    addWorksheet(
        spreadsheetPackage,
        objectFactory,
        sharedStrings,
        "FirstDecision",
        1,
        "first-input",
        "first-output");
    addWorksheet(
        spreadsheetPackage,
        objectFactory,
        sharedStrings,
        "SecondDecision",
        2,
        "second-input",
        "second-output");

    return saveSpreadsheet(spreadsheetPackage);
  }

  private static void addWorksheet(
      SpreadsheetMLPackage spreadsheetPackage,
      ObjectFactory objectFactory,
      CTSst sharedStrings,
      String worksheetName,
      long sheetId,
      String input,
      String output)
      throws Exception {
    org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart worksheetPart =
        spreadsheetPackage.createWorksheetPart(
            new PartName("/xl/worksheets/sheet" + sheetId + ".xml"), worksheetName, sheetId);
    Worksheet worksheet = worksheetPart.getContents();

    worksheet
        .getSheetData()
        .getRow()
        .add(createRow(objectFactory, sharedStrings, 1, "input", "output"));
    worksheet
        .getSheetData()
        .getRow()
        .add(createRow(objectFactory, sharedStrings, 2, input, output));
  }

  private static Row createRow(
      ObjectFactory objectFactory, CTSst sharedStrings, long rowNumber, String... values) {
    Row row = objectFactory.createRow();
    row.setR(rowNumber);
    for (int i = 0; i < values.length; i++) {
      String column = Character.toString((char) ('A' + i));
      row.getC().add(createCell(objectFactory, sharedStrings, column + rowNumber, values[i]));
    }
    return row;
  }

  private static Cell createCell(
      ObjectFactory objectFactory, CTSst sharedStrings, String reference, String value) {
    Cell cell = objectFactory.createCell();
    cell.setR(reference);
    cell.setT(STCellType.S);
    cell.setV(Integer.toString(addSharedString(objectFactory, sharedStrings, value)));
    return cell;
  }

  private static int addSharedString(
      ObjectFactory objectFactory, CTSst sharedStrings, String value) {
    CTRst sharedString = objectFactory.createCTRst();
    CTXstringWhitespace text = objectFactory.createCTXstringWhitespace();
    text.setValue(value);
    sharedString.setT(text);
    int index = sharedStrings.getSi().size();
    sharedStrings.getSi().add(sharedString);
    return index;
  }

  private static byte[] saveSpreadsheet(SpreadsheetMLPackage spreadsheetPackage)
      throws Docx4JException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    spreadsheetPackage.save(outputStream);
    return outputStream.toByteArray();
  }
}
