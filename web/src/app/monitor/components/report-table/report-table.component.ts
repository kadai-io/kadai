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

import { ChangeDetectionStrategy, Component, effect, input, signal, untracked } from '@angular/core';
import { ReportData } from 'app/monitor/models/report-data';
import { ReportRow } from '../../models/report-row';
import { NgClass } from '@angular/common';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'kadai-monitor-report-table',
  templateUrl: './report-table.component.html',
  styleUrls: ['./report-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgClass, MatButton]
})
export class ReportTableComponent {
  // Use alias so parent can still bind with [reportData]="..."
  reportDataInput = input<ReportData>(undefined, { alias: 'reportData' });

  // Local mutable copy used by the template and mutation methods
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

  showMoreRows() {
    if (this.hasMoreRows()) {
      this.reportData.update((rd) => ({ ...rd, rows: [...rd.rows, ...this.fullRowsData()![0]] }));
      this.fullRowsData.update((frd) => {
        const r = [...frd];
        r.splice(0, 1);
        return r;
      });
    }
  }

  hasMoreRows() {
    return this.fullRowsData() != null && this.fullRowsData()![0] != null;
  }

  toggleFold(index: number, sumRow: boolean = false) {
    const rd = this.reportData()!;
    let rows = sumRow ? rd.sumRow : rd.rows;
    const toggleRow = rows[index];
    if (toggleRow.depth < rd.meta.rowDesc.length - 1) {
      const firstChildRow = rows[index + 1];
      firstChildRow.display = !firstChildRow.display;

      const endIndex = rows.findIndex((row) => row.depth <= toggleRow.depth);
      rows = endIndex >= 0 ? rows.slice(0, endIndex) : rows;
      rows.forEach((row) => {
        row.display = firstChildRow.display && row.depth === firstChildRow.depth;
      });

      this.currentExpHeaders.set(
        Math.max(
          ...this.reportData()!.rows.filter((r) => r.display).map((r) => r.depth),
          ...this.reportData()!.sumRow.filter((r) => r.display).map((r) => r.depth)
        )
      );
      // Force signal update to re-render after in-place mutations
      this.reportData.update((r) => ({ ...r }));
    }
  }

  canRowCollapse(index: number, sumRow: boolean = false) {
    const rd = this.reportData()!;
    const rows = sumRow ? rd.sumRow : rd.rows;
    return !rows[index + 1].display;
  }

  private processReportData(data: ReportData) {
    this.fullReportData = { ...data };
    this.reportData.set({ ...data });
    const computed = this.fullReportData.rows?.reduce((resultArray: ReportRow[][], item, index) => {
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
    this.fullRowsData.set(computed);
    if (this.fullRowsData()) {
      this.reportData.update((rd) => ({ ...rd, rows: this.fullRowsData()![0] }));
      this.fullRowsData.update((frd) => {
        const r = [...frd];
        r.splice(0, 1);
        return r;
      });
    }
  }
}
