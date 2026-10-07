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

import { Injectable, signal, computed, WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatExpansionPanel } from '@angular/material/expansion';
import { provideRouter, ActivatedRoute, Params } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { TaskPriorityReportComponent } from './task-priority-report.component';
import { TaskPriorityReportFilterStateService } from '../../services/task-priority-report-filter-state.service';
import { TaskPriorityReportDataService } from '../../services/task-priority-report-data.service';
import { SettingMembers } from '../../../settings/components/Settings/expected-members';
import { ReportData } from '../../models/report-data';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideStore, State } from '@ngxs/store';

@State<Record<string, unknown>>({
  name: 'settings',
  defaults: {
    settings: {
      [SettingMembers.NameHighPriority]: 'High Priority',
      [SettingMembers.NameMediumPriority]: 'Medium Priority',
      [SettingMembers.NameLowPriority]: 'Low Priority',
      [SettingMembers.ColorHighPriority]: '#FF0000',
      [SettingMembers.ColorMediumPriority]: '#FFFF00',
      [SettingMembers.ColorLowPriority]: '#00FF00'
    }
  }
})
class MockSettingsState {}

const mockReportData: ReportData = {
  meta: {
    name: 'Test Report',
    date: '2026-11-20',
    header: ['Priority', 'Number of Tasks'],
    rowDesc: ['Workbasket'],
    sumRowDesc: 'Total'
  },
  rows: [
    // Depth 0 rows (workbasket level)
    { depth: 0, desc: ['TPK_VIP'], cells: [3, 0, 0], total: 3, display: true },
    { depth: 0, desc: ['TPK_VIP_2'], cells: [0, 1, 0], total: 1, display: true },
    { depth: 0, desc: ['TPK_VIP_3'], cells: [3, 2, 1], total: 6, display: true },
    // Depth 1 rows (classification level)
    { depth: 1, desc: ['TPK_VIP', 'L1050'], cells: [2, 0, 0], total: 2, display: true },
    { depth: 1, desc: ['TPK_VIP', 'L2000'], cells: [1, 0, 0], total: 1, display: true }
  ],
  sumRow: [{ depth: 0, desc: ['Total'], cells: [6, 3, 1], total: 10, display: true }]
};

const mockSettings = {
  [SettingMembers.NameHighPriority]: 'High Priority',
  [SettingMembers.NameMediumPriority]: 'Medium Priority',
  [SettingMembers.NameLowPriority]: 'Low Priority',
  [SettingMembers.ColorHighPriority]: '#FF0000',
  [SettingMembers.ColorMediumPriority]: '#FFFF00',
  [SettingMembers.ColorLowPriority]: '#00FF00'
};

@Injectable()
class MockTaskPriorityReportFilterStateService {
  readonly settings = signal<Record<string, unknown> | undefined>(mockSettings);
  readonly activeFilters = signal<string[]>([]);
  readonly workbasketKey = signal<string | undefined>(undefined);
  readonly filterKeys = signal<string[]>(['State READY', 'State CLAIMED']);
  readonly filtersAreSpecified = signal<boolean>(true);
  readonly parsedFilterConfig = signal<{ config: Record<string, any>; isValid: boolean }>({
    config: {},
    isValid: true
  });
  readonly activeQuery = computed(() => ({}));

  toggleFilter = vi.fn((key: string, isEnabled: boolean) => {
    const current = this.activeFilters();
    const next = isEnabled ? [...current, key] : current.filter((k) => k !== key);
    this.activeFilters.set(next);
  });
}

@Injectable()
class MockTaskPriorityReportDataService {
  readonly reportData: WritableSignal<ReportData | undefined> = signal<ReportData | undefined>(mockReportData);
}

describe('TaskPriorityReportComponent', () => {
  let fixture: ComponentFixture<TaskPriorityReportComponent>;
  let component: TaskPriorityReportComponent;
  let paramsSubject: BehaviorSubject<Params>;

  let mockFilterStateService: MockTaskPriorityReportFilterStateService;
  let mockDataService: MockTaskPriorityReportDataService;

  beforeEach(async () => {
    paramsSubject = new BehaviorSubject<Params>({});

    await TestBed.configureTestingModule({
      imports: [TaskPriorityReportComponent],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        provideStore([MockSettingsState]),
        {
          provide: ActivatedRoute,
          useValue: {
            params: paramsSubject.asObservable()
          }
        },
        { provide: TaskPriorityReportFilterStateService, useClass: MockTaskPriorityReportFilterStateService }
      ]
    })
      .overrideComponent(TaskPriorityReportComponent, {
        set: {
          providers: [{ provide: TaskPriorityReportDataService, useClass: MockTaskPriorityReportDataService }]
        }
      })
      .compileComponents();

    mockFilterStateService = TestBed.inject(
      TaskPriorityReportFilterStateService
    ) as unknown as MockTaskPriorityReportFilterStateService;

    mockFilterStateService.settings.set(mockSettings);
    mockFilterStateService.activeFilters.set([]);
    mockFilterStateService.workbasketKey.set(undefined);
    mockFilterStateService.filterKeys.set(['State READY', 'State CLAIMED']);
    mockFilterStateService.filtersAreSpecified.set(true);

    fixture = TestBed.createComponent(TaskPriorityReportComponent);
    component = fixture.componentInstance;

    mockDataService = fixture.debugElement.injector.get(
      TaskPriorityReportDataService
    ) as unknown as MockTaskPriorityReportDataService;
    mockDataService.reportData.set(mockReportData);

    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Report Data Processing & Priority Distribution', () => {
    it('should return true when workbasketKey is undefined and false when defined', () => {
      mockFilterStateService.workbasketKey.set(undefined);
      fixture.detectChanges();
      expect(component.isDepthZero()).toBe(true);

      mockFilterStateService.workbasketKey.set('WBK_123');
      fixture.detectChanges();
      expect(component.isDepthZero()).toBe(false);
    });

    it('should convert number to string via indexToString()', () => {
      expect(component.indexToString(0)).toBe('0');
      expect(component.indexToString(5)).toBe('5');
    });

    it('should read high, medium, and low priority colors from settings', () => {
      expect(component.colorHigh()).toBe('#FF0000');
      expect(component.colorMedium()).toBe('#FFFF00');
      expect(component.colorLow()).toBe('#00FF00');
    });

    it('should fall back to "inherit" when color settings are missing', () => {
      mockFilterStateService.settings.set({});
      fixture.detectChanges();

      expect(component.colorHigh()).toBe('inherit');
      expect(component.colorMedium()).toBe('inherit');
      expect(component.colorLow()).toBe('inherit');
    });

    it('should build tableDataArray using reportData and priority names from settings', () => {
      const tableData = component.tableDataArray();

      expect(tableData.length).toBe(5);
      expect(tableData[0]).toEqual([
        { priority: 'High Priority', number: 3 },
        { priority: 'Medium Priority', number: 0 },
        { priority: 'Low Priority', number: 0 },
        { priority: 'Total', number: 3 }
      ]);
    });

    it('should return an empty tableDataArray when reportData is undefined', () => {
      mockDataService.reportData.set(undefined);
      fixture.detectChanges();

      expect(component.tableDataArray()).toEqual([]);
    });
  });

  describe('Visual Styling & Dynamic Branding', () => {
    it('should set CSS color variables on component host element', () => {
      const hostElement: HTMLElement = fixture.nativeElement;

      expect(hostElement.style.getPropertyValue('--color-high-priority')).toBe('#FF0000');
      expect(hostElement.style.getPropertyValue('--color-medium-priority')).toBe('#FFFF00');
      expect(hostElement.style.getPropertyValue('--color-low-priority')).toBe('#00FF00');
    });
  });

  describe('Report Filtering & User Interaction', () => {
    it('should call toggleFilter on filterStateService when onFilterChange is triggered', () => {
      component.onFilterChange(true, 'State READY');

      expect(mockFilterStateService.toggleFilter).toHaveBeenCalledWith('State READY', true);
    });

    it('should toggle isPanelOpen on expansion panel (opened) and (closed) events', () => {
      const panelDebug = fixture.debugElement.query(By.directive(MatExpansionPanel));
      expect(panelDebug).toBeTruthy();

      panelDebug.triggerEventHandler('opened', {});
      fixture.detectChanges();
      expect(component.isPanelOpen).toBe(true);

      panelDebug.triggerEventHandler('closed', {});
      fixture.detectChanges();
      expect(component.isPanelOpen).toBe(false);
    });

    it('should trigger onFilterChange when mat-checkbox fires change event', () => {
      const checkboxDebug = fixture.debugElement.query(By.directive(MatCheckbox));
      expect(checkboxDebug).toBeTruthy();

      const spy = vi.spyOn(component, 'onFilterChange');
      checkboxDebug.triggerEventHandler('change', { checked: true });

      expect(spy).toHaveBeenCalledWith(true, 'State READY');
    });
  });

  describe('Report View Layout & Navigation States', () => {
    it('should render headline with report name', () => {
      const headline = fixture.nativeElement.querySelector('.task-priority-report__headline');
      expect(headline).toBeTruthy();
      expect(headline.textContent).toContain('Test Report');
    });

    it('should render breadcrumb for workbaskets when workbasketKey is undefined', () => {
      mockFilterStateService.workbasketKey.set(undefined);
      fixture.detectChanges();

      const breadcrumb = fixture.nativeElement.querySelector('.breadcrumb');
      expect(breadcrumb).toBeTruthy();
      expect(breadcrumb.textContent).toContain('Workbaskets');
      expect(breadcrumb.querySelector('a')).toBeNull();
    });

    it('should render breadcrumb link and workbasketKey when workbasketKey is set', () => {
      mockFilterStateService.workbasketKey.set('WBK_123');
      fixture.detectChanges();

      const breadcrumb = fixture.nativeElement.querySelector('.breadcrumb');
      expect(breadcrumb).toBeTruthy();
      expect(breadcrumb.textContent).toContain('WBK_123');

      const parentLink = breadcrumb.querySelector('a');
      expect(parentLink).toBeTruthy();
      expect(parentLink.getAttribute('href')).toContain('/kadai/monitor/tasks-priority');
    });

    it('should display information message when filtersAreSpecified is false', () => {
      mockFilterStateService.filtersAreSpecified.set(false);
      fixture.detectChanges();

      const el = fixture.nativeElement.querySelector('.breadcrumb-filter-row');
      expect(el).toBeTruthy();
      expect(el.textContent).toContain('No filters defined.');
      expect(fixture.debugElement.query(By.directive(MatExpansionPanel))).toBeNull();
    });

    it('should display information message when rows is empty', () => {
      mockDataService.reportData.set({ ...mockReportData, rows: [] });
      fixture.detectChanges();

      const emptyMsg = fixture.nativeElement.querySelector('.task-priority-report__empty');
      expect(emptyMsg).toBeTruthy();
      expect(emptyMsg.textContent).toContain('Could not find any tasks which fulfill the current filter criteria.');
    });

    it('should render tables with priority and number of tasks', () => {
      const tables = fixture.nativeElement.querySelectorAll('table');
      expect(tables.length).toBe(5);
    });

    it('should not show report when reportData is null or undefined', () => {
      mockDataService.reportData.set(undefined);
      fixture.detectChanges();

      const reportEl = fixture.nativeElement.querySelector('.task-priority-report');
      expect(reportEl).toBeNull();
    });
  });
});
