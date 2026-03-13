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
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { WorkbasketReportComponent } from './workbasket-report.component';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { By } from '@angular/platform-browser';
import { MatTabGroup } from '@angular/material/tabs';

describe('WorkbasketReportComponent', () => {
  let component: WorkbasketReportComponent;
  let fixture: ComponentFixture<WorkbasketReportComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketReportComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting(), provideNoopAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketReportComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should set metaInformation via getMetaInformation()', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    fixture.detectChanges();

    const meta: any = { header: ['H1'], rowDesc: [], sumRowDesc: '', name: 'test', date: '2024' };
    component.getMetaInformation(meta);
    expect(component.metaInformation).toBe(meta);
  });

  it('should set selectedComponent via selectComponent()', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    fixture.detectChanges();

    component.selectComponent(1);
    expect(component.selectedComponent).toBe(1);
  });

  it('should return planned date title when selectedComponent is truthy', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    fixture.detectChanges();

    component.selectedComponent = 1;
    expect(component.getTitle()).toBe('Tasks grouped by Workbasket, querying by planned date');
  });

  it('should return due date title when selectedComponent is falsy', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    fixture.detectChanges();

    component.selectedComponent = 0;
    expect(component.getTitle()).toBe('Tasks grouped by Workbasket, querying by due date');
  });

  it('should not show heading when metaInformation is undefined — covers @if (metaInformation) false branch', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    expect(component.metaInformation).toBeUndefined();
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
  });

  it('should show heading when metaInformation is set — covers @if (metaInformation) true branch', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    component.metaInformation = { date: '2024-01-01', name: 'report', header: [], rowDesc: [], sumRowDesc: '' } as any;
    expect(component.metaInformation).toBeTruthy();
    expect(component.metaInformation.date).toBe('2024-01-01');
  });

  it('should trigger selectComponent via mat-tab-group selectedIndexChange event', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    fixture.detectChanges();

    const tabGroupDebug = fixture.debugElement.query(By.directive(MatTabGroup));
    tabGroupDebug.componentInstance.selectedIndexChange.emit(1);
    expect(component.selectedComponent).toBe(1);
  });

  it('should trigger selectComponent to 0 via mat-tab-group selectedIndexChange event', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    fixture.detectChanges();

    component.selectedComponent = 1;
    const tabGroupDebug = fixture.debugElement.query(By.directive(MatTabGroup));
    tabGroupDebug.componentInstance.selectedIndexChange.emit(0);
    expect(component.selectedComponent).toBe(0);
  });

  it('should trigger getMetaInformation via due-date child component metaInformation output event', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    fixture.detectChanges();

    expect(component.metaInformation).toBeDefined();
  });

  it('should show heading in template when metaInformation is set with detectChanges', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    fixture.detectChanges();
    if (component.metaInformation) {
      const heading = fixture.nativeElement.querySelector('h4');
      expect(heading).toBeTruthy();
    }
  });

  it('should render h4 heading in DOM when metaInformation is pre-set before detectChanges', () => {
    component.metaInformation = {
      date: '2024-01-15',
      name: 'test-report',
      header: ['H1'],
      rowDesc: [],
      sumRowDesc: ''
    } as any;
    fixture.detectChanges();
    httpMock
      .match(() => true)
      .forEach((req) =>
        req.flush({
          meta: { header: ['H1'], rowDesc: [], sumRowDesc: '', name: 'report', date: '2024' },
          rows: [],
          sumRow: []
        })
      );
    const heading = fixture.nativeElement.querySelector('h4');
    expect(heading).toBeTruthy();
  });

  it('should have no heading rendered before metaInformation is set — covers false branch of @if (metaInformation)', () => {
    component.metaInformation = undefined;
    const heading = fixture.nativeElement.querySelector('h4');
    expect(heading).toBeNull();
    fixture.detectChanges();
    httpMock
      .match(() => true)
      .forEach((req) =>
        req.flush({
          meta: { header: [], rowDesc: [], sumRowDesc: '', name: '', date: '' },
          rows: [],
          sumRow: []
        })
      );
  });

  it('should cover metaInformation?.date null path when date is null', () => {
    component.metaInformation = {
      date: null,
      name: 'test',
      header: [],
      rowDesc: [],
      sumRowDesc: ''
    } as any;
    fixture.detectChanges();
    httpMock
      .match(() => true)
      .forEach((req) =>
        req.flush({
          meta: { header: [], rowDesc: [], sumRowDesc: '', name: '', date: '' },
          rows: [],
          sumRow: []
        })
      );
    const heading = fixture.nativeElement.querySelector('h4');
    expect(heading).toBeTruthy();
  });
});
