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

import { Component, inject, OnInit } from '@angular/core';
import { ReportData } from '../../models/report-data';
import { MonitorService } from '../../services/monitor.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { ReportTableComponent } from '../report-table/report-table.component';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'kadai-monitor-timestamp-report',
  templateUrl: './timestamp-report.component.html',
  styleUrls: ['./timestamp-report.component.scss'],
  imports: [ReportTableComponent, DatePipe],
  providers: [MonitorService]
})
export class TimestampReportComponent implements OnInit {
  reportData: ReportData;
  private restConnectorService = inject(MonitorService);
  private requestInProgressService = inject(RequestInProgressService);

  ngOnInit() {
    this.requestInProgressService.setRequestInProgress(true);
    this.restConnectorService.getDailyEntryExitReport().subscribe((data: ReportData) => {
      this.reportData = data;
      this.requestInProgressService.setRequestInProgress(false);
    });
  }
}
