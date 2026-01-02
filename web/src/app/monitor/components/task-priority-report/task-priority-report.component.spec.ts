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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideStore, Store } from '@ngxs/store';
import { TaskPriorityReportComponent } from './task-priority-report.component';
import { SettingsState } from '../../../shared/store/settings-store/settings.state';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { MonitorService } from '../../services/monitor.service';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { of } from 'rxjs';
import { SettingMembers } from '../../../settings/components/Settings/expected-members';
import { settingsStateMock } from '../../../shared/store/mock-data/mock-store';

const mockReportData = {
  meta: { name: 'Test Report', date: '2025-11-20' },
  rows: [
    // Depth 0 rows (workbasket level)
    { depth: 0, desc: ['TPK_VIP'], cells: [3, 0, 0], total: 3 },
    { depth: 0, desc: ['TPK_VIP_2'], cells: [0, 1, 0], total: 1 },
    { depth: 0, desc: ['TPK_VIP_3'], cells: [3, 2, 1], total: 6 }, // All priorities
    // Depth 1 rows (classification level)
    { depth: 1, desc: ['TPK_VIP', 'L1050'], cells: [2, 0, 0], total: 2 },
    { depth: 1, desc: ['TPK_VIP', 'L2000'], cells: [1, 0, 0], total: 1 }
  ],
  sumRow: { cells: [6, 3, 1], total: 10 }
};

const mockSettings = {
  [SettingMembers.NameHighPriority]: 'High Priority',
  [SettingMembers.NameMediumPriority]: 'Medium Priority',
  [SettingMembers.NameLowPriority]: 'Low Priority',
  [SettingMembers.ColorHighPriority]: '#FF0000',
  [SettingMembers.ColorMediumPriority]: '#FFFF00',
  [SettingMembers.ColorLowPriority]: '#00FF00'
};

const mockMonitorService = {
  getTasksByPriorityReport: jest.fn().mockReturnValue(of(mockReportData)),
  getTasksByDetailedPriorityReport: jest.fn().mockReturnValue(of(mockReportData))
};

const mockDomainService = {
  getSelectedDomain: jest.fn().mockReturnValue(of('DOMAIN_A')),
  getSelectedDomainValue: jest.fn().mockReturnValue('DOMAIN_A')
};

const mockRequestInProgressService = {
  setRequestInProgress: jest.fn()
};

describe('TaskPriorityReportComponent', () => {
  let fixture: ComponentFixture<TaskPriorityReportComponent>;
  let component: TaskPriorityReportComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskPriorityReportComponent],
      providers: [
        provideStore([SettingsState]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: MonitorService, useValue: mockMonitorService },
        { provide: DomainService, useValue: mockDomainService },
        { provide: RequestInProgressService, useValue: mockRequestInProgressService }
      ]
    }).compileComponents();

    const store: Store = TestBed.inject(Store);
    store.reset({
      ...store.snapshot(),
      settings: settingsStateMock
    });

    fixture = TestBed.createComponent(TaskPriorityReportComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  afterEach(() => {
    jest.restoreAllMocks();
    document.body.innerHTML = '';
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should return true when workbasketKey is undefined', () => {
    component.workbasketKey.set(undefined);

    const result = component['isDepthZero']();

    expect(result).toBe(true);
  });

  it('should return false when workbasketKey is defined', () => {
    component.workbasketKey.set('WBK_1');

    const result = component['isDepthZero']();

    expect(result).toBe(false);
  });

  it('should convert number to string', () => {
    const result = component.indexToString(1);

    expect(result).toBe('1');
    expect(typeof result).toBe('string');
  });

  it('should query for elements with correct class name', () => {
    component.colorShouldChange = true;
    const getElementsSpy = jest.spyOn(document, 'getElementsByClassName').mockReturnValue([] as any);

    component.ngAfterViewChecked();

    expect(getElementsSpy).toHaveBeenCalledWith('task-priority-report__row--high');
  });

  it('should not query DOM when colorShouldChange is false', () => {
    component.colorShouldChange = false;
    const getElementsSpy = jest.spyOn(document, 'getElementsByClassName');

    component.ngAfterViewChecked();

    expect(getElementsSpy).not.toHaveBeenCalled();
  });

  it('should call changeColor and set colorShouldChange to false when high priority elements exist', () => {
    component.colorShouldChange = true;
    const changeColorSpy = jest.spyOn(component, 'changeColor');

    const mockElements = [document.createElement('div')] as any;
    jest.spyOn(document, 'getElementsByClassName').mockReturnValue(mockElements);

    component.ngAfterViewChecked();

    expect(changeColorSpy).toHaveBeenCalled();
    expect(component.colorShouldChange).toBe(false);
  });

  it('should apply colors to DOM elements in changeColor', () => {
    const high = document.createElement('div');
    const med = document.createElement('div');
    const low = document.createElement('div');
    high.className = 'task-priority-report__row--high';
    med.className = 'task-priority-report__row--medium';
    low.className = 'task-priority-report__row--low';
    document.body.appendChild(high);
    document.body.appendChild(med);
    document.body.appendChild(low);

    component.colorHighPriority = '#aa0000';
    component.colorMediumPriority = '#bbbb00';
    component.colorLowPriority = '#00bb00';

    component.changeColor();

    expect((high as HTMLElement).style.color).toMatch(/#aa0000|rgb\(170, 0, 0\)/);
    expect((med as HTMLElement).style.color).toMatch(/#bbbb00|rgb\(187, 187, 0\)/);
    expect((low as HTMLElement).style.color).toMatch(/#00bb00|rgb\(0, 187, 0\)/);

    document.body.removeChild(high);
    document.body.removeChild(med);
    document.body.removeChild(low);
  });

  it('should set all priority names from settings', () => {
    component.setValuesFromSettings(mockSettings as any);

    expect(component.nameHighPriority).toBe('High Priority');
    expect(component.nameMediumPriority).toBe('Medium Priority');
    expect(component.nameLowPriority).toBe('Low Priority');
  });

  it('should set reportData meta and sumRow', () => {
    component.workbasketKey.set(undefined);

    component.setValuesFromReportData(mockReportData as any);

    expect(component.reportData.meta).toEqual({ name: 'Test Report', date: '2025-11-20' });
    expect(component.reportData.sumRow).toEqual({ cells: [6, 3, 1], total: 10 });
  });

  it('should filter rows by depth and workbasketKey and build tableDataArray', () => {
    component.setValuesFromSettings(mockSettings as any);
    component.workbasketKey.set('TPK_VIP');

    component.setValuesFromReportData(mockReportData as any);

    expect(component.reportData.rows.every((r) => r.depth === 1)).toBe(true);
    expect(component.reportData.rows.every((r) => r.desc[0] === 'TPK_VIP')).toBe(true);

    const firstRow = component.tableDataArray[0];
    expect(firstRow).toEqual([
      { priority: 'High Priority', number: 2 },
      { priority: 'Medium Priority', number: 0 },
      { priority: 'Low Priority', number: 0 },
      { priority: 'Total', number: 2 }
    ]);
  });

  it('should set priority when workbasketKey is set (detailed report)', () => {
    component.workbasketKey.set('TPK_VIP');
    component['currentFilter'].set({ state: ['READY'] });
    fixture.detectChanges();

    expect(component.priority).toBeDefined();
    expect(component.priority.length).toBe(3);
  });

  it('should set workbasketKey for detailed report', () => {
    component.workbasketKey.set('WBK_TEST');
    component['currentFilter'].set({});
    fixture.detectChanges();

    expect(component.workbasketKey()).toBe('WBK_TEST');
    expect(component['isDepthZero']()).toBe(false);
  });

  it('applyColorOnClasses should set color style on provided collection', () => {
    const container = document.createElement('div');
    const el1 = document.createElement('span');
    const el2 = document.createElement('span');
    el1.className = 'x';
    el2.className = 'x';
    container.appendChild(el1);
    container.appendChild(el2);
    document.body.appendChild(container);

    const collection = container.getElementsByClassName('x');
    component.applyColorOnClasses(collection as any, '#123456');

    expect((el1 as HTMLElement).style.color).toMatch(/#123456|rgb\(18, 52, 86\)/);
    expect((el2 as HTMLElement).style.color).toMatch(/#123456|rgb\(18, 52, 86\)/);

    document.body.removeChild(container);
  });

  it('should call monitorService.getTasksByPriorityReport in applyFilter for depth 0 and toggle request state', () => {
    component.workbasketKey.set(undefined);
    component.priority = [
      { lowerBound: 0, upperBound: 1 },
      { lowerBound: 1, upperBound: 2 },
      { lowerBound: 2, upperBound: 3 }
    ] as any;

    const filter = { a: 1 } as any;
    const setValuesSpy = jest.spyOn(component, 'setValuesFromReportData');
    const getTasksByPriorityReportSpy = jest
      .spyOn((component as any)['monitorService'], 'getTasksByPriorityReport')
      .mockReturnValue(of(mockReportData) as any);
    const getTasksByDetailedPriorityReportSpy = jest
      .spyOn((component as any)['monitorService'], 'getTasksByDetailedPriorityReport')
      .mockReturnValue(of(mockReportData) as any);

    component.applyFilter(filter);

    expect(getTasksByPriorityReportSpy).toHaveBeenCalled();
    expect(getTasksByDetailedPriorityReportSpy).not.toHaveBeenCalled();
    expect(setValuesSpy).toHaveBeenCalledWith(mockReportData as any);
    expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
  });

  it('should call monitorService.getTasksByDetailedPriorityReport in applyFilter for depth 1 and toggle request state', () => {
    component.workbasketKey.set('WBK_1');
    component.priority = [
      { lowerBound: 0, upperBound: 1 },
      { lowerBound: 1, upperBound: 2 },
      { lowerBound: 2, upperBound: 3 }
    ] as any;

    const filter = { b: 2 } as any;
    const setValuesSpy = jest.spyOn(component, 'setValuesFromReportData');
    const getTasksByPriorityReportSpy = jest
      .spyOn((component as any)['monitorService'], 'getTasksByPriorityReport')
      .mockReturnValue(of(mockReportData) as any);
    const getTasksByDetailedPriorityReportSpy = jest
      .spyOn((component as any)['monitorService'], 'getTasksByDetailedPriorityReport')
      .mockReturnValue(of(mockReportData) as any);

    component.applyFilter(filter);

    expect(getTasksByDetailedPriorityReportSpy).toHaveBeenCalled();
    expect(getTasksByPriorityReportSpy).not.toHaveBeenCalled();
    expect(setValuesSpy).toHaveBeenCalledWith(mockReportData as any);
    expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(true);
    expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    const nextSpy = jest.spyOn(component.destroy$, 'next');
    const completeSpy = jest.spyOn(component.destroy$, 'complete');

    component.ngOnDestroy();

    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should set priority array when effect runs', () => {
    component['currentFilter'].set({ state: ['CLAIMED'] });
    fixture.detectChanges();

    expect(component.priority).toBeDefined();
    expect(component.priority.length).toBe(3);
  });

  it('should populate tableDataArray after effect runs', () => {
    component['currentFilter'].set({ state: ['READY'] });
    fixture.detectChanges();

    expect(component.tableDataArray).toBeDefined();
    expect(Array.isArray(component.tableDataArray)).toBe(true);
  });

  it('should have priority with lowerBound and upperBound properties', () => {
    component['currentFilter'].set({ state: ['READY'] });
    fixture.detectChanges();

    expect(component.priority[0]).toHaveProperty('lowerBound');
    expect(component.priority[0]).toHaveProperty('upperBound');
  });

  it('should append filter name to activeFilters list when it is selected (migrated)', () => {
    component.activeFilters.set(['Tasks with state READY']);

    component.emitFilter(true, 'Tasks with state CLAIMED');

    expect(component.activeFilters()).toStrictEqual(['Tasks with state READY', 'Tasks with state CLAIMED']);
  });

  it('should remove filter name from list when it is not selected anymore (migrated)', () => {
    component.activeFilters.set(['Tasks with state READY', 'Tasks with state CLAIMED']);

    component.emitFilter(false, 'Tasks with state CLAIMED');

    expect(component.activeFilters()).toStrictEqual(['Tasks with state READY']);
  });

  it('should apply query according to values in activeFilters (migrated)', () => {
    const applySpy = jest.spyOn(component, 'applyFilter').mockImplementation(() => {});
    component.activeFilters.set(['Tasks with state READY']);

    component.emitFilter(true, 'Tasks with state CLAIMED');

    expect(applySpy).toHaveBeenCalledWith({ state: ['READY', 'CLAIMED'] });
  });
});
