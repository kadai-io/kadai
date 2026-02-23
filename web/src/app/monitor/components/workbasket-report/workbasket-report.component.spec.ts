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

    component.selectedComponent = 0;
    expect(component.getTitle()).toBe('Tasks grouped by Workbasket, querying by due date');
  });
});
