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
 */

import { inject, Injectable } from '@angular/core';
import { combineLatest, Observable, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { Store } from '@ngxs/store';
import { toSignal, toObservable } from '@angular/core/rxjs-interop';

import { SettingsSelectors } from '../../shared/store/settings-store/settings.selectors';
import { DomainService } from '../../shared/services/domain/domain.service';
import { MonitorService } from '../services/monitor.service';
import { RequestInProgressService } from '../../shared/services/request-in-progress/request-in-progress.service';
import { TaskPriorityReportFilterStateService } from './task-priority-report-filter-state.service';
import { SettingMembers } from '../../settings/components/Settings/expected-members';
import { WorkbasketType } from '../../shared/models/workbasket-type';
import { ReportData } from '../models/report-data';
import { PriorityInterval } from '../models/priority-interval';

@Injectable({ providedIn: 'root' })
export class TaskPriorityReportDataService {
  private readonly store = inject(Store);
  private readonly domainService = inject(DomainService);
  private readonly monitorService = inject(MonitorService);
  private readonly requestInProgressService = inject(RequestInProgressService);
  private readonly filterState = inject(TaskPriorityReportFilterStateService);

  readonly reportData$: Observable<ReportData | undefined> = combineLatest([
    this.store.select(SettingsSelectors.getSettings),
    this.domainService.getSelectedDomain(),
    toObservable(this.filterState.workbasketKey),
    toObservable(this.filterState.activeQuery)
  ]).pipe(
    tap(() => this.requestInProgressService.setRequestInProgress(true)),
    switchMap(([settings, domain, workbasketKey, query]) => {
      if (!settings) return of(undefined);

      const intervals: PriorityInterval[] = [
        settings[SettingMembers.IntervalHighPriority],
        settings[SettingMembers.IntervalMediumPriority],
        settings[SettingMembers.IntervalLowPriority]
      ].map(([lowerBound, upperBound]) => ({ lowerBound, upperBound }));

      const isDepthZero = workbasketKey === undefined;
      const request$ = isDepthZero
        ? this.monitorService.getTasksByPriorityReport([WorkbasketType.TOPIC], intervals, domain, query)
        : this.monitorService.getTasksByDetailedPriorityReport([WorkbasketType.TOPIC], intervals, domain, query);

      return request$.pipe(
        map((reportData) => this.filterRows(reportData, isDepthZero, workbasketKey)),
        catchError((err) => {
          console.error('Failed to load Task Priority Report', err);
          return of(undefined);
        })
      );
    }),
    tap(() => this.requestInProgressService.setRequestInProgress(false))
  );

  readonly reportData = toSignal(this.reportData$);

  private filterRows(reportData: ReportData, isDepthZero: boolean, workbasketKey?: string): ReportData {
    const depth = isDepthZero ? 0 : 1;
    return {
      ...reportData,
      rows: reportData.rows
        .filter((row) => row.depth === depth)
        .filter((row) => isDepthZero || row.desc[0] === workbasketKey)
    };
  }
}
