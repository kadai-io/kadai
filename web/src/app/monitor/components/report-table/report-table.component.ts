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

import { Component, effect, input, signal, untracked } from '@angular/core';
import { ReportData } from 'app/monitor/models/report-data';
import { ReportRow } from '../../models/report-row';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'kadai-monitor-report-table',
  templateUrl: './report-table.component.html',
  styleUrls: ['./report-table.component.scss'],
  imports: [MatButton]
})
export class ReportTableComponent {
  reportDataInput = input<ReportData>(undefined, { alias: 'reportData' });
  reportData = signal<ReportData>(undefined);
  fullReportData: ReportData;
  fullRowsData = signal<ReportRow[][]>(undefined);
  currentExpHeaders = signal(0);

  constructor() {
    effect(() => {
      const data = this.reportDataInput();
      untracked(() => {
        if (data) {
          this.processReportData(data);
        }
      });
    });
  }

  private processReportData(data: ReportData) {
    this.fullReportData = { ...data };
    this.reportData.set({ ...data });
    const computed = this.fullReportData.rows?.reduce((resultArray: ReportRow[][], item, index) => {
      const itemsPerChunk = 20;
      if (this.fullReportData.rows.length > itemsPerChunk) {
        const chunkIndex = Math.floor(index / itemsPerChunk);

        if (!resultArray[chunkIndex]) {
          resultArray[chunkIndex] = [];
        }

        resultArray[chunkIndex].push(item);
      } else {
        return [this.fullReportData.rows];
      }
      return resultArray;
    }, []);
    this.fullRowsData.set(computed);
    if (this.fullRowsData()) {
      this.reportData.update((reportData) => ({ ...reportData, rows: this.fullRowsData()![0] }));
      this.fullRowsData.update((fullRowsData) => {
        const rowsData = [...fullRowsData];
        rowsData.splice(0, 1);
        return rowsData;
      });
    }
  }

  showMoreRows() {
    if (this.hasMoreRows()) {
      this.reportData.update((reportData) => ({
        ...reportData,
        rows: [...reportData.rows, ...this.fullRowsData()![0]]
      }));
      this.fullRowsData.update((fullRowsData) => {
        const rowsData = [...fullRowsData];
        rowsData.splice(0, 1);
        return rowsData;
      });
    }
  }

  hasMoreRows() {
    return this.fullRowsData() != null && this.fullRowsData()![0] != null;
  }

  toggleFold(index: number, sumRow: boolean = false) {
    const reportData = this.reportData()!;
    let rows = sumRow ? reportData.sumRow : reportData.rows;
    const toggleRow = rows[index];
    if (toggleRow.depth < reportData.meta.rowDesc.length - 1) {
      const firstChildRow = rows[index + 1];
      firstChildRow.display = !firstChildRow.display;

      const endIndex = rows.findIndex((row) => row.depth <= toggleRow.depth);
      rows = endIndex >= 0 ? rows.slice(0, endIndex) : rows;
      rows.forEach((row) => {
        row.display = firstChildRow.display && row.depth === firstChildRow.depth;
      });

      this.currentExpHeaders.set(
        Math.max(
          ...this.reportData()!
            .rows.filter((row) => row.display)
            .map((row) => row.depth),
          ...this.reportData()!
            .sumRow.filter((sumRow) => sumRow.display)
            .map((sumRow) => sumRow.depth)
        )
      );
      this.reportData.update((reportData) => ({ ...reportData }));
    }
  }

  canRowCollapse(index: number, sumRow: boolean = false) {
    const reportData = this.reportData()!;
    const rows = sumRow ? reportData.sumRow : reportData.rows;
    return rows[index + 1] ? !rows[index + 1].display : false;
  }
}
