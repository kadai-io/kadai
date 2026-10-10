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

import { signal, WritableSignal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Store } from '@ngxs/store';
import { BehaviorSubject, finalize, firstValueFrom, of, Subject, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { TaskPriorityReportDataService } from './task-priority-report-data.service';
import { TaskPriorityReportFilterStateService } from './task-priority-report-filter-state.service';
import { SettingsSelectors } from '../../shared/store/settings-store/settings.selectors';
import { DomainService } from '../../shared/services/domain/domain.service';
import { MonitorService } from '../services/monitor.service';
import { RequestInProgressService } from '../../shared/services/request-in-progress/request-in-progress.service';
import { SettingMembers } from '../../settings/components/Settings/expected-members';
import { WorkbasketType } from '../../shared/models/workbasket-type';
import { ReportData } from '../models/report-data';
import { Settings } from '../../settings/models/settings';

const mockSettings: Settings = {
  schema: [],
  [SettingMembers.IntervalHighPriority]: [100, 200],
  [SettingMembers.IntervalMediumPriority]: [50, 99],
  [SettingMembers.IntervalLowPriority]: [0, 49]
};

const mockReportData: ReportData = {
  meta: {
    name: 'Priority Report',
    date: '2026-11-20',
    header: ['Priority', 'Tasks'],
    rowDesc: ['Workbasket'],
    sumRowDesc: 'Total'
  },
  rows: [
    { depth: 0, desc: ['WBK_A'], cells: [5, 2], total: 7, display: true },
    { depth: 0, desc: ['WBK_B'], cells: [1, 0], total: 1, display: true },
    { depth: 1, desc: ['WBK_A'], cells: [3, 2], total: 5, display: true },
    { depth: 1, desc: ['WBK_B'], cells: [1, 0], total: 1, display: true }
  ],
  sumRow: [{ depth: 0, desc: ['Total'], cells: [6, 2], total: 8, display: true }]
};

describe('TaskPriorityReportDataService', () => {
  let service: TaskPriorityReportDataService;

  let settingsSubject$: BehaviorSubject<Settings | undefined>;
  let domainSubject$: BehaviorSubject<string>;

  let workbasketKeySignal: WritableSignal<string | undefined>;
  let activeQuerySignal: WritableSignal<Record<string, string[]>>;

  let mockMonitorService: Partial<MonitorService>;
  let mockRequestInProgressService: { beginRequest: ReturnType<typeof vi.fn>; endRequest: ReturnType<typeof vi.fn> };
  let mockDomainService: Partial<DomainService>;

  beforeEach(() => {
    settingsSubject$ = new BehaviorSubject<Settings | undefined>(mockSettings);
    domainSubject$ = new BehaviorSubject<string>('DOMAIN_A');

    workbasketKeySignal = signal<string | undefined>(undefined);
    activeQuerySignal = signal<Record<string, string[]>>({});

    const mockStore = {
      select: vi.fn().mockImplementation((selector) => {
        if (selector === SettingsSelectors.getSettings) {
          return settingsSubject$.asObservable();
        }
        return of(undefined);
      })
    };

    mockDomainService = {
      getSelectedDomain: vi.fn().mockReturnValue(domainSubject$.asObservable())
    };

    mockMonitorService = {
      getTasksByPriorityReport: vi.fn().mockReturnValue(of(mockReportData)),
      getTasksByDetailedPriorityReport: vi.fn().mockReturnValue(of(mockReportData))
    };

    mockRequestInProgressService = {
      beginRequest: vi.fn(),
      endRequest: vi.fn()
    };

    const mockFilterStateService: Partial<TaskPriorityReportFilterStateService> = {
      workbasketKey: workbasketKeySignal,
      activeQuery: activeQuerySignal
    };

    TestBed.configureTestingModule({
      providers: [
        TaskPriorityReportDataService,
        { provide: Store, useValue: mockStore },
        { provide: DomainService, useValue: mockDomainService },
        { provide: MonitorService, useValue: mockMonitorService },
        { provide: RequestInProgressService, useValue: mockRequestInProgressService },
        { provide: TaskPriorityReportFilterStateService, useValue: mockFilterStateService }
      ]
    });

    service = TestBed.inject(TaskPriorityReportDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('Report Loading & Filtering Logic (getReportData$() / reportData)', () => {
    it('should emit undefined if settings are missing', async () => {
      settingsSubject$.next(undefined);

      const data = await firstValueFrom(service.getReportData$());

      expect(data).toBeUndefined();
      expect(mockMonitorService.getTasksByPriorityReport).not.toHaveBeenCalled();
      expect(mockMonitorService.getTasksByDetailedPriorityReport).not.toHaveBeenCalled();
    });

    it('should call getTasksByPriorityReport and filter rows with depth 0 when workbasketKey is undefined', async () => {
      workbasketKeySignal.set(undefined);

      const expectedIntervals = [
        { lowerBound: 100, upperBound: 200 },
        { lowerBound: 50, upperBound: 99 },
        { lowerBound: 0, upperBound: 49 }
      ];

      const data = await firstValueFrom(service.getReportData$());

      expect(data).toBeTruthy();
      expect(data?.rows.length).toBe(2);
      expect(data?.rows.every((row) => row.depth === 0)).toBe(true);

      expect(mockMonitorService.getTasksByPriorityReport).toHaveBeenCalledWith(
        [WorkbasketType.TOPIC],
        expectedIntervals,
        'DOMAIN_A',
        {}
      );
      expect(mockMonitorService.getTasksByDetailedPriorityReport).not.toHaveBeenCalled();
    });

    it('should call getTasksByDetailedPriorityReport and filter rows by depth 1 and matching workbasketKey', async () => {
      workbasketKeySignal.set('WBK_A');

      const data = await firstValueFrom(service.getReportData$());

      expect(data).toBeTruthy();
      expect(data?.rows.length).toBe(1);
      expect(data?.rows[0]).toEqual({
        depth: 1,
        desc: ['WBK_A'],
        cells: [3, 2],
        total: 5,
        display: true
      });

      expect(mockMonitorService.getTasksByDetailedPriorityReport).toHaveBeenCalledWith(
        [WorkbasketType.TOPIC],
        expect.any(Array),
        'DOMAIN_A',
        {}
      );
      expect(mockMonitorService.getTasksByPriorityReport).not.toHaveBeenCalled();
    });

    it('should pass activeQuery filter parameters to the monitor service request', async () => {
      const activeQuery = { state: ['READY', 'CLAIMED'] };
      activeQuerySignal.set(activeQuery);

      await firstValueFrom(service.getReportData$());

      expect(mockMonitorService.getTasksByPriorityReport).toHaveBeenCalledWith(
        [WorkbasketType.TOPIC],
        expect.any(Array),
        'DOMAIN_A',
        activeQuery
      );
    });

    it('should manage requestInProgress state during data fetch', async () => {
      await firstValueFrom(service.getReportData$());

      expect(mockRequestInProgressService.beginRequest).toHaveBeenCalled();
      expect(mockRequestInProgressService.endRequest).toHaveBeenCalled();
    });

    it('should handle errors gracefully and return undefined', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      (mockMonitorService.getTasksByPriorityReport as ReturnType<typeof vi.fn>).mockReturnValue(
        throwError(() => new Error('API Network Error'))
      );

      const data = await firstValueFrom(service.getReportData$());

      expect(data).toBeUndefined();
      expect(consoleErrorSpy).toHaveBeenCalledWith('Failed to load Task Priority Report', expect.any(Error));
      expect(mockRequestInProgressService.endRequest).toHaveBeenCalled();
      consoleErrorSpy.mockRestore();
    });

    it('should update signal value (reportData) reactively', async () => {
      const data = await firstValueFrom(service.getReportData$());
      expect(data).toBeTruthy();
      expect(data?.rows.length).toBe(2);
    });
  });

  describe('TaskPriorityReportDataService - Reloading Report on Filter Change', () => {
    it('should cancel previous pending request and maintain loading indicator state when report parameters change', () => {
      const pendingRequest1$ = new Subject<ReportData>();
      const pendingRequest2$ = new Subject<ReportData>();

      let isRequest1Unsubscribed = false;

      const trackedRequest1$ = pendingRequest1$.pipe(
        finalize(() => {
          isRequest1Unsubscribed = true;
        })
      );

      (mockMonitorService.getTasksByPriorityReport as ReturnType<typeof vi.fn>).mockReturnValueOnce(trackedRequest1$);
      (mockMonitorService.getTasksByDetailedPriorityReport as ReturnType<typeof vi.fn>).mockReturnValueOnce(
        pendingRequest2$.asObservable()
      );

      const subscription = service.getReportData$().subscribe();
      settingsSubject$.next(mockSettings);
      TestBed.tick();

      expect(mockRequestInProgressService.beginRequest).toHaveBeenCalledTimes(1);
      expect(isRequest1Unsubscribed).toBe(false);

      workbasketKeySignal.set('NEW_KEY');
      TestBed.tick();

      expect(isRequest1Unsubscribed).toBe(true);
      expect(mockRequestInProgressService.endRequest).toHaveBeenCalledTimes(1);
      expect(mockRequestInProgressService.beginRequest).toHaveBeenCalledTimes(2);

      pendingRequest2$.next(mockReportData);
      pendingRequest2$.complete();

      expect(mockRequestInProgressService.endRequest).toHaveBeenCalledTimes(2);
      subscription.unsubscribe();
    });

    it('should reload report with new domain when domainSubject$ emits a new value', () => {
      const subscription = service.getReportData$().subscribe();
      TestBed.tick();

      expect(mockMonitorService.getTasksByPriorityReport).toHaveBeenCalledWith(
        [WorkbasketType.TOPIC],
        expect.any(Array),
        'DOMAIN_A',
        {}
      );

      domainSubject$.next('DOMAIN_B');
      TestBed.tick();

      expect(mockMonitorService.getTasksByPriorityReport).toHaveBeenCalledWith(
        [WorkbasketType.TOPIC],
        expect.any(Array),
        'DOMAIN_B',
        {}
      );

      subscription.unsubscribe();
    });
  });
});
