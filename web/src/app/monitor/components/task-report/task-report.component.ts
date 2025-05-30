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

import { Component, inject, OnInit } from '@angular/core';
import { ReportData } from 'app/monitor/models/report-data';
import { MonitorService } from '../../services/monitor.service';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { ReportTableComponent } from '../report-table/report-table.component';
import { BaseChartDirective } from 'ng2-charts';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'kadai-monitor-task-report',
  templateUrl: './task-report.component.html',
  styleUrls: ['./task-report.component.scss'],
  imports: [ReportTableComponent, BaseChartDirective, DatePipe],
  providers: [MonitorService]
})
export class TaskReportComponent implements OnInit {
  pieChartData: ChartData<'pie', number[], string> = { labels: [], datasets: [] };
  pieChartType: ChartType = 'pie';
  pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: true
  };
  reportData: ReportData;
  private monitorService = inject(MonitorService);
  private requestInProgressService = inject(RequestInProgressService);
  private destroy$ = new Subject<void>();

  ngOnInit() {
    this.requestInProgressService.setRequestInProgress(true);
    this.monitorService
      .getTaskStatusReport()
      .pipe(takeUntil(this.destroy$))
      .subscribe((report) => {
        this.reportData = report;
        this.pieChartData.labels = this.reportData.meta.header;
        this.pieChartData.datasets.push({ data: this.reportData.sumRow[0].cells });
        this.requestInProgressService.setRequestInProgress(false);
      });
  }
}
