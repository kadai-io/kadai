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

import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { ReportData } from '../../models/report-data';
import { ChartData } from '../../models/chart-data';
import { MonitorService } from '../../services/monitor.service';
import { MetaInfoData } from '../../models/meta-info-data';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { ChartConfiguration } from 'chart.js';
import { NgIf } from '@angular/common';
import { ReportTableComponent } from '../report-table/report-table.component';
import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';

@Component({
  selector: 'kadai-monitor-workbasket-report-planned-date',
  templateUrl: './workbasket-report-planned-date.component.html',
  styleUrls: ['./workbasket-report-planned-date.component.scss'],
  standalone: true,
  imports: [NgIf, ReportTableComponent, BaseChartDirective],
  providers: [provideCharts(withDefaultRegisterables())]
})
export class WorkbasketReportPlannedDateComponent implements OnInit {
  @Output()
  metaInformation = new EventEmitter<MetaInfoData>();

  reportData: ReportData;

  lineChartLabels: Array<any>;
  lineChartLegend = true;
  lineChartType = 'line';
  lineChartData: Array<ChartData>;
  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: true,
    elements: {
      line: {
        tension: 0.4
      }
    }
  };
  constructor(
    private restConnectorService: MonitorService,
    private requestInProgressService: RequestInProgressService
  ) {}

  ngOnInit() {
    this.requestInProgressService.setRequestInProgress(true);
    this.restConnectorService.getWorkbasketStatisticsQueryingByPlannedDate().subscribe((report) => {
      this.reportData = report;
      this.metaInformation.emit(this.reportData.meta);
      this.lineChartLabels = this.reportData.meta.header;
      this.lineChartData = this.restConnectorService.getChartData(this.reportData);
      this.requestInProgressService.setRequestInProgress(false);
    });
  }
}
