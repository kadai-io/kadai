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
import { ClassificationReportComponent } from './classification-report.component';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

describe('ClassificationReportComponent', () => {
  let component: ClassificationReportComponent;
  let fixture: ComponentFixture<ClassificationReportComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClassificationReportComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting(), provideNoopAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(ClassificationReportComponent);
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

  it('should not show report content when reportData is null', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    component.reportData = null;
    fixture.detectChanges();
    const panel = fixture.nativeElement.querySelector('.panel');
    expect(panel).toBeNull();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
  });

  it('should show report content after data is loaded', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'Test Report', date: '2024-01-01' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component.reportData).toBeTruthy();
    expect(component.reportData.meta.name).toBe('Test Report');
  });

  it('should render the panel element when reportData is set', () => {
    const mockReport = {
      meta: {
        header: ['H1', 'H2'],
        rowDesc: [],
        sumRowDesc: 'Total',
        name: 'Classification Report',
        date: '2024-06-01'
      },
      rows: [{ desc: ['classA'], cells: [5, 10], total: 15, depth: 0, display: true }],
      sumRow: []
    };
    component.reportData = mockReport as any;
    component.lineChartLabels = mockReport.meta.header;
    component.lineChartData = mockReport.rows.map((row) => ({ data: row.cells, label: row.desc[0] }));
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    const panel = fixture.nativeElement.querySelector('.panel');
    expect(panel).toBeTruthy();
    expect(component.reportData.meta.name).toBe('Classification Report');
  });

  it('should display report name in the panel heading when reportData is loaded', () => {
    const mockReport = {
      meta: {
        header: ['H1', 'H2'],
        rowDesc: [],
        sumRowDesc: 'Total',
        name: 'My Classification Report',
        date: '2024-06-01'
      },
      rows: [{ desc: ['classA'], cells: [5, 10], total: 15, depth: 0, display: true }],
      sumRow: []
    };
    component.reportData = mockReport as any;
    component.lineChartLabels = mockReport.meta.header;
    component.lineChartData = mockReport.rows.map((row) => ({ data: row.cells, label: row.desc[0] }));
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    const heading = fixture.nativeElement.querySelector('h4');
    expect(heading).toBeTruthy();
    expect(heading.textContent).toContain('My Classification Report');
  });

  it('should set lineChartData and lineChartLabels from the loaded report', () => {
    const mockReport = {
      meta: { header: ['Jan', 'Feb'], rowDesc: [], sumRowDesc: 'Total', name: 'Chart Report', date: '2024-01-01' },
      rows: [
        { desc: ['classA'], cells: [3, 7], total: 10, depth: 0, display: true },
        { desc: ['classB'], cells: [1, 2], total: 3, depth: 0, display: true }
      ],
      sumRow: []
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component.lineChartLabels).toEqual(['Jan', 'Feb']);
    expect(component.lineChartData).toHaveLength(2);
    expect(component.lineChartData[0].label).toBe('classA');
    expect(component.lineChartData[1].label).toBe('classB');
  });
});
