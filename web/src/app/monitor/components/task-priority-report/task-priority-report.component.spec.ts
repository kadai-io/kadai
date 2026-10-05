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

import { Component, input, Signal, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatExpansionPanel } from '@angular/material/expansion';
import { provideRouter, ActivatedRoute, Params } from '@angular/router';
import { BehaviorSubject, of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { TaskPriorityReportComponent } from './task-priority-report.component';
import { TaskPriorityReportFilterStateService } from '../../services/task-priority-report-filter-state.service';
import { TaskPriorityReportDataService } from '../../services/task-priority-report-data.service';
import { SettingMembers } from '../../../settings/components/Settings/expected-members';
import { ReportData } from '../../models/report-data';
import { Settings } from 'app/settings/models/settings';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { CanvasComponent } from '../canvas/canvas.component';

@Component({
  selector: 'kadai-monitor-canvas',
  template: '',
  standalone: true
})
class MockCanvasComponent {
  id = input<string>();
  row = input<unknown>();
}

const mockReportData: ReportData = {
  meta: {
    name: 'Test Report',
    date: '2026-11-20',
    header: ['Priority', 'Number of Tasks'],
    rowDesc: ['Workbasket'],
    sumRowDesc: 'Total'
  },
  rows: [
    { depth: 0, desc: ['TPK_VIP'], cells: [3, 0, 0], total: 3, display: true },
    { depth: 0, desc: ['TPK_VIP_2'], cells: [0, 1, 0], total: 1, display: true }
  ],
  sumRow: [{ depth: 0, desc: ['Total'], cells: [3, 1, 0], total: 4, display: true }]
};

const mockSettings = {
  [SettingMembers.NameHighPriority]: 'High Priority',
  [SettingMembers.NameMediumPriority]: 'Medium Priority',
  [SettingMembers.NameLowPriority]: 'Low Priority',
  [SettingMembers.ColorHighPriority]: '#FF0000',
  [SettingMembers.ColorMediumPriority]: '#FFFF00',
  [SettingMembers.ColorLowPriority]: '#00FF00'
};

describe('TaskPriorityReportComponent', () => {
  let fixture: ComponentFixture<TaskPriorityReportComponent>;
  let component: TaskPriorityReportComponent;
  let paramsSubject: BehaviorSubject<Params>;

  const settingsSignal = signal<Record<string, unknown>>(mockSettings);
  const activeFiltersSignal = signal<string[]>([]);
  const workbasketKeySignal = signal<string | undefined>(undefined);
  const filterKeysSignal = signal<string[]>(['State READY', 'State CLAIMED']);
  const filtersAreSpecifiedSignal = signal<boolean>(true);
  const reportDataSignal = signal<ReportData | undefined>(mockReportData);

  let mockFilterStateService: Partial<TaskPriorityReportFilterStateService>;
  let mockDataService: Partial<TaskPriorityReportDataService>;

  beforeEach(async () => {
    paramsSubject = new BehaviorSubject<Params>({});

    settingsSignal.set(mockSettings);
    activeFiltersSignal.set([]);
    workbasketKeySignal.set(undefined);
    filterKeysSignal.set(['State READY', 'State CLAIMED']);
    filtersAreSpecifiedSignal.set(true);
    reportDataSignal.set(mockReportData);

    mockFilterStateService = {
      settings: settingsSignal as unknown as Signal<Settings | undefined>,
      activeFilters: activeFiltersSignal,
      workbasketKey: workbasketKeySignal,
      filterKeys: filterKeysSignal,
      filtersAreSpecified: filtersAreSpecifiedSignal,
      toggleFilter: vi.fn((key: string, isEnabled: boolean) => {
        const current = activeFiltersSignal();
        const next = isEnabled ? [...current, key] : current.filter((k) => k !== key);
        activeFiltersSignal.set(next);
      })
    };

    mockDataService = {
      reportData: reportDataSignal
    };

    await TestBed.configureTestingModule({
      imports: [TaskPriorityReportComponent],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            params: paramsSubject.asObservable()
          }
        },
        { provide: TaskPriorityReportFilterStateService, useValue: mockFilterStateService },
        { provide: TaskPriorityReportDataService, useValue: mockDataService }
      ]
    })
      .overrideComponent(TaskPriorityReportComponent, {
        remove: { imports: [CanvasComponent] },
        add: { imports: [MockCanvasComponent] }
      })
      .compileComponents();

    fixture = TestBed.createComponent(TaskPriorityReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Signal Computations & Pure Helpers', () => {
    it('should compute isDepthZero correctly when workbasketKey is undefined or defined', () => {
      workbasketKeySignal.set(undefined);
      expect(component.isDepthZero()).toBe(true);

      workbasketKeySignal.set('WBK_123');
      expect(component.isDepthZero()).toBe(false);
    });

    it('should convert number index to string via indexToString()', () => {
      expect(component.indexToString(0)).toBe('0');
      expect(component.indexToString(5)).toBe('5');
    });

    it('should compute colors from settings signals', () => {
      expect(component.colorHigh()).toBe('#FF0000');
      expect(component.colorMedium()).toBe('#FFFF00');
      expect(component.colorLow()).toBe('#00FF00');
    });

    it('should compute fallback color "inherit" when setting is missing', () => {
      settingsSignal.set({});
      expect(component.colorHigh()).toBe('inherit');
      expect(component.colorMedium()).toBe('inherit');
      expect(component.colorLow()).toBe('inherit');
    });

    it('should build tableDataArray correctly based on reportData and priority names from settings', () => {
      const tableData = component.tableDataArray();

      expect(tableData.length).toBe(2);
      expect(tableData[0]).toEqual([
        { priority: 'High Priority', number: 3 },
        { priority: 'Medium Priority', number: 0 },
        { priority: 'Low Priority', number: 0 },
        { priority: 'Total', number: 3 }
      ]);
    });

    it('should return empty tableDataArray when reportData or settings are missing', () => {
      reportDataSignal.set(undefined);
      expect(component.tableDataArray()).toEqual([]);
    });
  });

  describe('Host Binding Styles', () => {
    it('should set CSS variables on component host element', () => {
      const hostElement: HTMLElement = fixture.nativeElement;

      expect(hostElement.style.getPropertyValue('--color-high-priority')).toBe('#FF0000');
      expect(hostElement.style.getPropertyValue('--color-medium-priority')).toBe('#FFFF00');
      expect(hostElement.style.getPropertyValue('--color-low-priority')).toBe('#00FF00');
    });
  });

  describe('User Interactions & Filter Actions', () => {
    it('should delegate toggleFilter to filterState service onFilterChange', () => {
      component.onFilterChange(true, 'State READY');

      expect(mockFilterStateService.toggleFilter).toHaveBeenCalledWith('State READY', true);
      expect(component.activeFilters()).toContain('State READY');
    });

    it('should update isPanelOpen when expansion panel emits opened/closed events', () => {
      const panelDebug = fixture.debugElement.query(By.directive(MatExpansionPanel));
      expect(panelDebug).toBeTruthy();

      panelDebug.triggerEventHandler('opened', {});
      fixture.detectChanges();
      expect(component.isPanelOpen).toBe(true);

      panelDebug.triggerEventHandler('closed', {});
      fixture.detectChanges();
      expect(component.isPanelOpen).toBe(false);
    });

    it('should call onFilterChange when mat-checkbox changes state', () => {
      const checkboxDebug = fixture.debugElement.query(By.directive(MatCheckbox));
      expect(checkboxDebug).toBeTruthy();

      const spy = vi.spyOn(component, 'onFilterChange');
      checkboxDebug.triggerEventHandler('change', { checked: true });

      expect(spy).toHaveBeenCalledWith(true, 'State READY');
    });
  });

  describe('Template Rendering Branches', () => {
    it('should render headline with report name and formatted date', () => {
      const headline = fixture.nativeElement.querySelector('.task-priority-report__headline');
      expect(headline).toBeTruthy();
      expect(headline.textContent).toContain('Test Report');
    });

    it('should render depth-zero breadcrumb when isDepthZero is true', () => {
      workbasketKeySignal.set(undefined);
      fixture.detectChanges();

      const breadcrumb = fixture.nativeElement.querySelector('.breadcrumb');
      expect(breadcrumb.textContent).toContain('Workbaskets');
      expect(breadcrumb.querySelector('a')).toBeNull();
    });

    it('should render depth-one breadcrumb with parent link when workbasketKey is set', () => {
      workbasketKeySignal.set('WBK_123');
      fixture.detectChanges();

      const breadcrumb = fixture.nativeElement.querySelector('.breadcrumb');
      expect(breadcrumb.textContent).toContain('WBK_123');

      const parentLink = breadcrumb.querySelector('a');
      expect(parentLink).toBeTruthy();
      expect(parentLink.getAttribute('href')).toContain('/kadai/monitor/tasks-priority');
    });

    it('should display "No filters defined." when filtersAreSpecified is false', () => {
      filtersAreSpecifiedSignal.set(false);
      fixture.detectChanges();

      const el = fixture.nativeElement.querySelector('.breadcrumb-filter-row');
      expect(el.textContent).toContain('No filters defined.');
      expect(fixture.debugElement.query(By.directive(MatExpansionPanel))).toBeNull();
    });

    it('should display empty message when reportData has no rows', () => {
      reportDataSignal.set({ ...mockReportData, rows: [] });
      fixture.detectChanges();

      const emptyMsg = fixture.nativeElement.querySelector('.task-priority-report__empty');
      expect(emptyMsg).toBeTruthy();
      expect(emptyMsg.textContent).toContain('Could not find any tasks which fulfill the current filter criteria.');
    });

    it('should render tables for each workbasket row', () => {
      const tables = fixture.nativeElement.querySelectorAll('table');
      expect(tables.length).toBe(2);
    });

    it('should not render report container if reportData is undefined', () => {
      reportDataSignal.set(undefined);
      fixture.detectChanges();

      const reportEl = fixture.nativeElement.querySelector('.task-priority-report');
      expect(reportEl).toBeNull();
    });
  });
});
