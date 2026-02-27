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
import { beforeEach, describe, expect, it } from 'vitest';
import { ReportTableComponent } from './report-table.component';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ReportData } from 'app/monitor/models/report-data';

const makeRow = (i: number) => ({
  desc: [`row${i}`],
  cells: [i],
  total: i,
  depth: 0,
  display: true
});

const mockSmallReportData: ReportData = {
  meta: { header: ['H1', 'H2'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
  rows: Array.from({ length: 5 }, (_, i) => makeRow(i)),
  sumRow: [{ desc: ['Total'], cells: [10], total: 10, depth: 0, display: true }]
};

const mockLargeReportData: ReportData = {
  meta: { header: ['H1'], rowDesc: ['Desc1'], sumRowDesc: 'Total', name: 'report', date: '2024' },
  rows: Array.from({ length: 25 }, (_, i) => makeRow(i)),
  sumRow: [{ desc: ['Total'], cells: [100], total: 100, depth: 0, display: true }]
};

describe('ReportTableComponent', () => {
  let component: ReportTableComponent;
  let fixture: ComponentFixture<ReportTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportTableComponent],
      providers: [provideNoopAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(ReportTableComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.reportData = {
      meta: { header: ['H1'], rowDesc: ['Desc1'], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [{ desc: ['row0'], cells: [0], total: 0, depth: 0, display: true }],
      sumRow: [{ desc: ['Total'], cells: [10], total: 10, depth: 0, display: true }]
    };
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should return single chunk when rows < 20', () => {
      component.reportData = { ...mockSmallReportData, rows: [...mockSmallReportData.rows] };
      component.ngOnChanges();

      expect(component.reportData.rows.length).toBe(5);
      expect(component.fullRowsData).toBeTruthy();
    });

    it('should create multiple chunks when rows > 20', () => {
      component.reportData = {
        ...mockLargeReportData,
        rows: [...mockLargeReportData.rows]
      };
      component.ngOnChanges();

      expect(component.reportData.rows.length).toBe(20);
      expect(component.fullRowsData.length).toBeGreaterThan(0);
      expect(component.fullRowsData[0].length).toBe(5);
    });
  });

  describe('showMoreRows', () => {
    it('should append more rows when hasMoreRows is true', () => {
      component.reportData = {
        ...mockLargeReportData,
        rows: [...mockLargeReportData.rows]
      };
      component.ngOnChanges();

      const initialLength = component.reportData.rows.length;
      component.showMoreRows();

      expect(component.reportData.rows.length).toBeGreaterThan(initialLength);
    });

    it('should not change rows when hasMoreRows is false', () => {
      component.reportData = { ...mockSmallReportData, rows: [...mockSmallReportData.rows] };
      component.ngOnChanges();

      const initialLength = component.reportData.rows.length;
      component.fullRowsData = [];
      component.showMoreRows();

      expect(component.reportData.rows.length).toBe(initialLength);
    });
  });

  describe('hasMoreRows', () => {
    it('should return undefined/false when fullRowsData is empty', () => {
      component.reportData = { ...mockSmallReportData, rows: [...mockSmallReportData.rows] };
      component.ngOnChanges();
      component.fullRowsData = [];

      expect(component.hasMoreRows()).toBeFalsy();
    });

    it('should return truthy when there are more rows in fullRowsData', () => {
      component.reportData = {
        ...mockLargeReportData,
        rows: [...mockLargeReportData.rows]
      };
      component.ngOnChanges();

      expect(component.hasMoreRows()).toBeTruthy();
    });
  });

  describe('toggleFold', () => {
    it('should toggle display of child row (without detectChanges to avoid template rendering)', () => {
      const rows = [
        { desc: ['parent'], cells: [1], total: 1, depth: 0, display: true },
        { desc: ['child'], cells: [1], total: 1, depth: 1, display: true }
      ];
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: rows,
        sumRow: [{ desc: ['Total'], cells: [2], total: 2, depth: 0, display: true }]
      };
      const initialDisplay = rows[1].display;
      component.toggleFold(0);

      expect(rows[1].display).toBe(!initialDisplay);
    });
  });

  describe('canRowCollapse', () => {
    it('should return true when next row is not displayed', () => {
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: [
          { desc: ['row0'], cells: [0], total: 0, depth: 0, display: true },
          { desc: ['row1'], cells: [1], total: 1, depth: 1, display: false }
        ],
        sumRow: []
      };
      fixture.detectChanges();

      expect(component.canRowCollapse(0)).toBe(true);
    });

    it('should return false when next row is displayed', () => {
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: [
          { desc: ['row0'], cells: [0], total: 0, depth: 0, display: true },
          { desc: ['row1'], cells: [1], total: 1, depth: 1, display: true }
        ],
        sumRow: []
      };
      fixture.detectChanges();

      expect(component.canRowCollapse(0)).toBe(false);
    });
  });
});
