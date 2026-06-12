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

import { Component, inject, OnInit, signal } from '@angular/core';
import { MonitorService } from 'app/monitor/services/monitor.service';
import { ChartData } from 'app/monitor/models/chart-data';
import { ReportData } from '../../models/report-data';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { ChartConfiguration, ChartType } from 'chart.js';
import { ReportTableComponent } from '../report-table/report-table.component';
import { BaseChartDirective } from 'ng2-charts';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'kadai-monitor-classification-report',
  templateUrl: './classification-report.component.html',
  styleUrls: ['./classification-report.component.scss'],
  imports: [ReportTableComponent, BaseChartDirective, DatePipe],
  providers: [MonitorService]
})
export class ClassificationReportComponent implements OnInit {
  reportData = signal<ReportData | undefined>(undefined);
  lineChartLabels = signal<Array<any>>([]);
  lineChartLegend = true;
  lineChartType: ChartType = 'line';
  lineChartData = signal<Array<ChartData>>([]);
  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: true,
    elements: {
      line: {
        tension: 0.4
      }
    }
  };
  private restConnectorService = inject(MonitorService);
  private requestInProgressService = inject(RequestInProgressService);

  ngOnInit() {
    this.requestInProgressService.setRequestInProgress(true);
    this.restConnectorService.getClassificationTasksReport().subscribe((report) => {
      this.reportData.set(report);
      this.lineChartData.set(this.restConnectorService.getChartData(report));
      this.lineChartLabels.set(report.meta.header);
      this.requestInProgressService.setRequestInProgress(false);
    });
  }
}
