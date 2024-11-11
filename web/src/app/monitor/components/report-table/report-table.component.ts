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

import { Component, Input, OnChanges } from '@angular/core';
import { ReportData } from 'app/monitor/models/report-data';
import { ReportRow } from '../../models/report-row';
import { NgIf, NgFor, NgClass } from '@angular/common';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'kadai-monitor-report-table',
  templateUrl: './report-table.component.html',
  styleUrls: ['./report-table.component.scss'],
  standalone: true,
  imports: [NgIf, NgFor, NgClass, MatButton]
})
export class ReportTableComponent implements OnChanges {
  @Input()
  reportData: ReportData;

  fullReportData: ReportData;
  fullRowsData: ReportRow[][];
  currentExpHeaders = 0;

  ngOnChanges() {
    this.fullReportData = { ...this.reportData };
    this.fullRowsData = this.fullReportData.rows?.reduce((resultArray: ReportRow[][], item, index) => {
      const itemsPerChunk = 20;
      if (this.fullReportData.rows.length > itemsPerChunk) {
        const chunkIndex = Math.floor(index / itemsPerChunk);

        if (!resultArray[chunkIndex]) {
          resultArray[chunkIndex] = []; // start a new chunk
        }

        resultArray[chunkIndex].push(item);
      } else {
        return [this.fullReportData.rows];
      }
      return resultArray;
    }, []);
    if (this.fullRowsData) {
      this.reportData.rows = this.fullRowsData[0];
      this.fullRowsData.splice(0, 1);
    }
  }

  showMoreRows() {
    if (this.hasMoreRows()) {
      this.reportData.rows = [...this.reportData.rows, ...this.fullRowsData[0]];
      this.fullRowsData.splice(0, 1);
    }
  }

  hasMoreRows() {
    return typeof this.fullRowsData !== 'undefined' && this.fullRowsData[0];
  }

  toggleFold(indexNumber: number, sumRow: boolean = false) {
    let rows = sumRow ? this.reportData.sumRow : this.reportData.rows;
    let index = indexNumber;
    const toggleRow = rows[index];
    if (toggleRow.depth < this.reportData.meta.rowDesc.length - 1) {
      const firstChildRow = rows[(index += 1)];
      firstChildRow.display = !firstChildRow.display;

      const endIndex = rows.findIndex((row) => row.depth <= toggleRow.depth);
      rows = endIndex >= 0 ? rows.slice(0, endIndex) : rows;
      rows.forEach((row) => {
        row.display = firstChildRow.display && row.depth === firstChildRow.depth;
      });

      this.currentExpHeaders = Math.max(
        ...this.reportData.rows.filter((r) => r.display).map((r) => r.depth),
        ...this.reportData.sumRow.filter((r) => r.display).map((r) => r.depth)
      );
    }
  }

  canRowCollapse(index: number, sumRow: boolean = false) {
    const rows = sumRow ? this.reportData.sumRow : this.reportData.rows;
    return !rows[index + 1].display;
  }
}
