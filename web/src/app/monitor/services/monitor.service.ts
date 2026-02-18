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

import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'environments/environment';
import { Observable } from 'rxjs';
import { ChartData } from 'app/monitor/models/chart-data';
import { ReportData } from '../models/report-data';
import { asUrlQueryString } from '../../shared/util/query-parameters-v2';
import { TaskState } from '../../shared/models/task-state';

const monitorUrl = '/v1/monitor';

@Injectable({
  providedIn: 'root'
})
export class MonitorService {
  private httpClient = inject(HttpClient);

  getTaskStatusReport(): Observable<ReportData> {
    const queryParams = {
      states: [TaskState.READY, TaskState.CLAIMED, TaskState.COMPLETED]
    };
    return this.httpClient.get<ReportData>(
      `${environment.kadaiRestUrl + monitorUrl}/task-status-report${asUrlQueryString(queryParams)}`
    );
  }

  getWorkbasketStatisticsQueryingByDueDate(): Observable<ReportData> {
    const queryParams = {
      states: [TaskState.READY, TaskState.CLAIMED, TaskState.COMPLETED]
    };
    return this.httpClient.get<ReportData>(
      `${environment.kadaiRestUrl + monitorUrl}/workbasket-report${asUrlQueryString(queryParams)}`
    );
  }

  getWorkbasketStatisticsQueryingByPlannedDate(): Observable<ReportData> {
    const queryParams = {
      'task-timetamp': 'PLANNED',
      states: [TaskState.READY, TaskState.CLAIMED, TaskState.COMPLETED]
    };
    return this.httpClient.get<ReportData>(
      `${environment.kadaiRestUrl + monitorUrl}/workbasket-report${asUrlQueryString(queryParams)}`
    );
  }

  getClassificationTasksReport(): Observable<ReportData> {
    return this.httpClient.get<ReportData>(`${environment.kadaiRestUrl + monitorUrl}/classification-report`);
  }

  getDailyEntryExitReport(): Observable<ReportData> {
    return this.httpClient.get<ReportData>(`${environment.kadaiRestUrl + monitorUrl}/timestamp-report`);
  }

  getChartData(source: ReportData): ChartData[] {
    return source.rows.map((row) => {
      const rowData = new ChartData();
      [rowData.label] = row.desc;
      rowData.data = row.cells;
      return rowData;
    });
  }

  getTasksByPriorityReport(
    type: string[],
    priority: any[],
    domain: string,
    customFilters: {} = {}
  ): Observable<ReportData> {
    const queryParams = {
      'workbasket-type': type,
      domain: domain,
      state: 'READY',
      columnHeader: priority,
      ...customFilters
    };

    return this.httpClient.get<ReportData>(
      `${environment.kadaiRestUrl + monitorUrl}/workbasket-priority-report${asUrlQueryString(queryParams)}`
    );
  }

  getTasksByDetailedPriorityReport(
    type: string[],
    priority: any[],
    domain: string,
    customFilters: {} = {}
  ): Observable<ReportData> {
    const queryParams = {
      'workbasket-type': type,
      domain: domain,
      state: 'READY',
      columnHeader: priority,
      ...customFilters
    };

    return this.httpClient.get<ReportData>(
      `${environment.kadaiRestUrl + monitorUrl}/detailed-workbasket-priority-report${asUrlQueryString(queryParams)}`
    );
  }
}
