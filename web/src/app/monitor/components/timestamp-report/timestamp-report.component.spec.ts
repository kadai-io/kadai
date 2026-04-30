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
import { TimestampReportComponent } from './timestamp-report.component';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

describe('TimestampReportComponent', () => {
  let component: TimestampReportComponent;
  let fixture: ComponentFixture<TimestampReportComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimestampReportComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting(), provideNoopAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(TimestampReportComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'Timestamp Report', date: '2024-01-01' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component).toBeTruthy();
  });

  it('should not show report content when reportData is null — covers @if (reportData) false branch', () => {
    const mockReport = {
      meta: { header: ['H1'], rowDesc: [], sumRowDesc: 'Total', name: 'Timestamp Report', date: '2024-01-01' },
      rows: [{ desc: ['Created'], cells: [5], total: 5, depth: 0, display: true }],
      sumRow: []
    };
    component.reportData = null;
    fixture.detectChanges();
    const panel = fixture.nativeElement.querySelector('.panel-default');
    expect(panel).toBeNull();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
  });

  it('should show report content after data is loaded — covers @if (reportData) true branch', () => {
    const mockReport = {
      meta: { header: ['H1'], rowDesc: [], sumRowDesc: 'Total', name: 'Timestamp Report', date: '2024-01-01' },
      rows: [{ desc: ['Created'], cells: [5], total: 5, depth: 0, display: true }],
      sumRow: []
    };
    component.reportData = mockReport as any;
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component.reportData).toBeTruthy();
    expect(component.reportData.meta.name).toBe('Timestamp Report');
    const panel = fixture.nativeElement.querySelector('.panel-default');
    expect(panel).toBeTruthy();
  });

  it('should render the panel heading with report name after data is loaded', () => {
    const mockReport = {
      meta: { header: ['H1'], rowDesc: [], sumRowDesc: 'Total', name: 'My Report', date: '2024-06-15T10:00:00' },
      rows: [{ desc: ['Created'], cells: [5], total: 5, depth: 0, display: true }],
      sumRow: []
    };
    component.reportData = mockReport as any;
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component.reportData).toBeTruthy();
    expect(component.reportData.meta.name).toBe('My Report');
    const heading = fixture.nativeElement.querySelector('h4');
    expect(heading).toBeTruthy();
    expect(heading.textContent).toContain('My Report');
  });

  it('should not render panel heading when reportData is absent before HTTP completes', () => {
    const mockReport = {
      meta: { header: ['H1'], rowDesc: [], sumRowDesc: 'Total', name: 'Timestamp Report', date: '2024-01-01' },
      rows: [],
      sumRow: []
    };
    fixture.detectChanges();
    expect(component.reportData).toBeUndefined();
    const panelBefore = fixture.nativeElement.querySelector('.panel-default');
    expect(panelBefore).toBeNull();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component.reportData).toBeTruthy();
  });
});
