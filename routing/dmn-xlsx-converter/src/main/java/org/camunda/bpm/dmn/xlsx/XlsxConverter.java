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

import java.io.InputStream;
import java.util.List;
import org.camunda.bpm.dmn.xlsx.api.SpreadsheetAdapter;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.xlsx4j.sml.CTSst;
import org.xlsx4j.sml.Sheet;
import org.xlsx4j.sml.Workbook;
import org.xlsx4j.sml.Worksheet;

/**
 * @author Thorben Lindhauer
 */
public class XlsxConverter {

  public static final String DEFAULT_HISTORY_TIME_TO_LIVE = "P180D";
  public static final int DEFAULT_WORKSHEET_INDEX = 0;

  protected String historyTimeToLive = DEFAULT_HISTORY_TIME_TO_LIVE;

  protected SpreadsheetAdapter ioDetectionStrategy = new SimpleInputOutputDetectionStrategy();

  protected int worksheetIndex = DEFAULT_WORKSHEET_INDEX;

  /**
   * Converts one worksheet from an XLSX document into a DMN model.
   *
   * <p>The worksheet index is zero-based and defaults to {@value #DEFAULT_WORKSHEET_INDEX}. Only
   * the selected worksheet is converted per invocation.
   *
   * @param inputStream the XLSX document to convert
   * @return the DMN model generated from the selected worksheet
   * @throws XlsxConversionException if the XLSX document or selected worksheet cannot be read
   */
  public DmnModelInstance convert(InputStream inputStream) throws XlsxConversionException {
    SpreadsheetMLPackage spreadsheetPackage = loadSpreadsheet(inputStream);
    XlsxWorksheetContext worksheetContext = createWorksheetContext(spreadsheetPackage);

    return new XlsxWorksheetConverter(worksheetContext, ioDetectionStrategy, historyTimeToLive)
        .convert();
  }

  private SpreadsheetMLPackage loadSpreadsheet(InputStream inputStream)
      throws XlsxConversionException {
    try {
      OpcPackage opcPackage = OpcPackage.load(inputStream);
      if (!(opcPackage instanceof SpreadsheetMLPackage spreadsheetPackage)) {
        throw new XlsxConversionException("Input document is not an XLSX spreadsheet");
      }
      return spreadsheetPackage;
    } catch (Docx4JException e) {
      throw new XlsxConversionException("Could not load XLSX document", e);
    }
  }

  private XlsxWorksheetContext createWorksheetContext(SpreadsheetMLPackage spreadsheetPackage)
      throws XlsxConversionException {
    WorkbookPart workbookPart = spreadsheetPackage.getWorkbookPart();
    if (workbookPart == null) {
      throw new XlsxConversionException("Workbook part is missing");
    }

    List<Sheet> sheets = getWorkbookSheets(workbookPart);
    if (sheets.isEmpty()) {
      throw new XlsxConversionException("Workbook does not contain any worksheets");
    }

    if (worksheetIndex < 0 || worksheetIndex >= sheets.size()) {
      throw new XlsxConversionException(
          "Worksheet index %d is out of bounds; workbook contains %d worksheet(s)"
              .formatted(worksheetIndex, sheets.size()));
    }

    WorksheetPart worksheetPart;
    try {
      worksheetPart = workbookPart.getWorksheet(worksheetIndex);
    } catch (Xlsx4jException e) {
      throw new XlsxConversionException(
          "Could not resolve worksheet at index %d".formatted(worksheetIndex), e);
    }
    if (worksheetPart == null) {
      throw new XlsxConversionException(
          "Could not resolve worksheet at index %d".formatted(worksheetIndex));
    }

    Sheet selectedSheet = sheets.get(worksheetIndex);
    String worksheetName = selectedSheet == null ? null : selectedSheet.getName();
    if (worksheetName == null || worksheetName.isBlank()) {
      worksheetName = "default";
    }

    try {
      Worksheet worksheet = worksheetPart.getContents();
      if (worksheet == null || worksheet.getSheetData() == null) {
        throw new XlsxConversionException("Could not read worksheet data");
      }

      CTSst sharedStringsContents = new CTSst();
      SharedStrings sharedStrings = workbookPart.getSharedStrings();
      if (sharedStrings != null) {
        CTSst contents = sharedStrings.getContents();
        if (contents != null) {
          sharedStringsContents = contents;
        }
      }

      return new XlsxWorksheetContext(sharedStringsContents, worksheet, worksheetName);
    } catch (Docx4JException e) {
      throw new XlsxConversionException("Could not read worksheet data", e);
    }
  }

  private List<Sheet> getWorkbookSheets(WorkbookPart workbookPart) throws XlsxConversionException {
    try {
      Workbook workbook = workbookPart.getContents();
      if (workbook == null || workbook.getSheets() == null) {
        return List.of();
      }
      return workbook.getSheets().getSheet();
    } catch (Docx4JException e) {
      throw new XlsxConversionException("Could not read workbook metadata", e);
    }
  }

  public SpreadsheetAdapter getIoDetectionStrategy() {
    return ioDetectionStrategy;
  }

  public void setIoDetectionStrategy(SpreadsheetAdapter ioDetectionStrategy) {
    this.ioDetectionStrategy = ioDetectionStrategy;
  }

  /**
   * Returns the zero-based index of the worksheet converted by {@link #convert(InputStream)}.
   *
   * @return the selected worksheet index
   */
  public int getWorksheetIndex() {
    return worksheetIndex;
  }

  /**
   * Sets the zero-based index of the worksheet converted by {@link #convert(InputStream)}. The
   * index must be within the workbook's worksheet range when conversion is performed.
   *
   * @param worksheetIndex the worksheet index to convert
   */
  public void setWorksheetIndex(int worksheetIndex) {
    this.worksheetIndex = worksheetIndex;
  }

  public String getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public void setHistoryTimeToLive(String historyTimeToLive) {
    this.historyTimeToLive = historyTimeToLive;
  }
}
