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

import { Component, computed, input, linkedSignal, signal } from '@angular/core';
import { ReportData } from 'app/monitor/models/report-data';
import { ReportRow } from 'app/monitor/models/report-row';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'kadai-monitor-report-table',
  templateUrl: './report-table.component.html',
  styleUrls: ['./report-table.component.scss'],
  imports: [MatButton]
})
export class ReportTableComponent {
  reportDataInput = input<ReportData>(undefined, { alias: 'reportData' });
  private rowChunks = computed(() => chunkRows(this.reportDataInput()?.rows));
  reportData = linkedSignal<ReportData>(() => {
    const data = this.reportDataInput();
    // undefined until the parent provides data; the template guards with @if
    return (data && { ...data, rows: this.rowChunks()?.[0] ?? data.rows }) as ReportData;
  });
  fullRowsData = linkedSignal<ReportRow[][]>(() => this.rowChunks()?.slice(1) as ReportRow[][]);
  currentExpHeaders = signal(0);

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

const ROWS_PER_CHUNK = 20;

function chunkRows(rows: ReportRow[] | undefined): ReportRow[][] | undefined {
  if (!rows) return undefined;
  const chunks: ReportRow[][] = [];
  for (let i = 0; i < rows.length; i += ROWS_PER_CHUNK) {
    chunks.push(rows.slice(i, i + ROWS_PER_CHUNK));
  }
  return chunks;
}
