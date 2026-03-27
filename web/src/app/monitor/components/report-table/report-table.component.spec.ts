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

  describe('template rendering with expanded headers', () => {
    it('should render table with currentExpHeaders > 0 to exercise depth branches', () => {
      component.reportData = {
        meta: { header: ['H1', 'H2'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: [
          { desc: ['parent', 'child-desc'], cells: [1, 2], total: 3, depth: 0, display: true },
          { desc: ['child'], cells: [1, 0], total: 1, depth: 1, display: true }
        ],
        sumRow: [
          { desc: ['Total', 'Sub'], cells: [2, 2], total: 4, depth: 0, display: true },
          { desc: ['SubTotal'], cells: [2], total: 2, depth: 1, display: true }
        ]
      };
      component.currentExpHeaders = 1;
      fixture.detectChanges();
      const table = fixture.nativeElement.querySelector('.report');
      expect(table).toBeTruthy();
    });

    it('should render rows with display=false hidden', () => {
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: [
          { desc: ['visible'], cells: [1], total: 1, depth: 0, display: true },
          { desc: ['hidden'], cells: [2], total: 2, depth: 1, display: false }
        ],
        sumRow: [{ desc: ['Total'], cells: [3], total: 3, depth: 0, display: true }]
      };
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });

    it('should render sumRow correctly', () => {
      component.reportData = {
        meta: { header: ['H1', 'H2'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Sum', name: 'report', date: '2024' },
        rows: [
          { desc: ['row1'], cells: [5, 3], total: 8, depth: 0, display: true },
          { desc: ['row1-child'], cells: [2, 1], total: 3, depth: 1, display: true }
        ],
        sumRow: [
          { desc: ['Sum', 'Sub'], cells: [5, 3], total: 8, depth: 0, display: true },
          { desc: ['SubSum'], cells: [2], total: 2, depth: 1, display: true }
        ]
      };
      component.currentExpHeaders = 1;
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });

    it('should not render anything when reportData is null', () => {
      component.reportData = null;
      fixture.detectChanges();
      const table = fixture.nativeElement.querySelector('.report');
      expect(table).toBeNull();
    });

    it('should render clickable cells and toggleFold works correctly', () => {
      const rows = [
        { desc: ['parent'], cells: [1], total: 1, depth: 0, display: true },
        { desc: ['child'], cells: [1], total: 1, depth: 1, display: true }
      ];
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows,
        sumRow: []
      };
      component.currentExpHeaders = 1;
      fixture.detectChanges();
      const cells = fixture.nativeElement.querySelectorAll('.table-cell--justify');
      expect(cells.length).toBeGreaterThan(0);
      const initialDisplay = rows[1].display;
      component.toggleFold(0);
      expect(rows[1].display).toBe(!initialDisplay);
    });
  });

  describe('DOM event triggers', () => {
    it('should call toggleFold when a body row description cell is clicked', () => {
      const rows = [
        { desc: ['parent'], cells: [1], total: 1, depth: 0, display: true },
        { desc: ['child'], cells: [2], total: 2, depth: 1, display: true }
      ];
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows,
        sumRow: [
          { desc: ['Total'], cells: [3], total: 3, depth: 0, display: true },
          { desc: ['SubTotal'], cells: [1], total: 1, depth: 1, display: true }
        ]
      };
      component.currentExpHeaders = 1;
      fixture.detectChanges();

      const initialDisplay = rows[1].display;
      const bodyRows = fixture.nativeElement.querySelectorAll('.table-body .table-cell--justify');
      expect(bodyRows.length).toBeGreaterThan(0);
      bodyRows[0].click();
      fixture.detectChanges();

      expect(rows[1].display).toBe(!initialDisplay);
    });

    it('should call toggleFold with sumRow=true when a sumRow description cell is clicked', () => {
      const sumRows = [
        { desc: ['Total'], cells: [3], total: 3, depth: 0, display: true },
        { desc: ['SubTotal'], cells: [1], total: 1, depth: 1, display: true }
      ];
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: [
          { desc: ['row1'], cells: [3], total: 3, depth: 0, display: true },
          { desc: ['child'], cells: [1], total: 1, depth: 1, display: true }
        ],
        sumRow: sumRows
      };
      component.currentExpHeaders = 1;
      fixture.detectChanges();

      const initialDisplay = sumRows[1].display;
      const footerCells = fixture.nativeElement.querySelectorAll('.table-footer .table-cell--justify');
      expect(footerCells.length).toBeGreaterThan(0);
      footerCells[0].click();
      fixture.detectChanges();

      expect(sumRows[1].display).toBe(!initialDisplay);
    });

    it('should call showMoreRows when "Show more rows" button is clicked and there are more rows', () => {
      component.reportData = {
        ...mockLargeReportData,
        rows: [...mockLargeReportData.rows]
      };
      component.ngOnChanges();
      fixture.detectChanges();

      const initialRowCount = component.reportData.rows.length;
      const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[mat-flat-button]');
      expect(button).toBeTruthy();
      expect(button.disabled).toBe(false);
      button.click();
      fixture.detectChanges();

      expect(component.reportData.rows.length).toBeGreaterThan(initialRowCount);
    });

    it('should disable "Show more rows" button when there are no more rows', () => {
      const smallData = {
        meta: { header: ['H1'], rowDesc: ['Desc1'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: Array.from({ length: 5 }, (_, i) => ({
          desc: [`row${i}`],
          cells: [i],
          total: i,
          depth: 0,
          display: true
        })),
        sumRow: [{ desc: ['Total'], cells: [10], total: 10, depth: 0, display: true }]
      };
      component.reportData = { ...smallData, rows: [...smallData.rows] };
      component.ngOnChanges();
      fixture.detectChanges();

      const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[mat-flat-button]');
      expect(button).toBeTruthy();
      expect(button.disabled).toBe(true);
    });

    it('should not change rows when "Show more rows" button is clicked while disabled', () => {
      const smallData = {
        meta: { header: ['H1'], rowDesc: ['Desc1'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: Array.from({ length: 5 }, (_, i) => ({
          desc: [`row${i}`],
          cells: [i],
          total: i,
          depth: 0,
          display: true
        })),
        sumRow: [{ desc: ['Total'], cells: [10], total: 10, depth: 0, display: true }]
      };
      component.reportData = { ...smallData, rows: [...smallData.rows] };
      component.ngOnChanges();
      fixture.detectChanges();

      const initialRowCount = component.reportData.rows.length;
      const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[mat-flat-button]');
      button.click();
      fixture.detectChanges();

      expect(component.reportData.rows.length).toBe(initialRowCount);
    });

    it('should render expand_less icon when child row is visible (canRowCollapse returns false)', () => {
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: [
          { desc: ['parent'], cells: [1], total: 1, depth: 0, display: true },
          { desc: ['child'], cells: [1], total: 1, depth: 1, display: true }
        ],
        sumRow: []
      };
      component.currentExpHeaders = 1;
      fixture.detectChanges();

      const icons = fixture.nativeElement.querySelectorAll('.material-icons');
      expect(icons.length).toBeGreaterThan(0);
      expect(icons[0].textContent.trim()).toBe('expand_less');
    });

    it('should render expand_more icon when child row is hidden (canRowCollapse returns true)', () => {
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: [
          { desc: ['parent'], cells: [1], total: 1, depth: 0, display: true },
          { desc: ['child'], cells: [1], total: 1, depth: 1, display: false }
        ],
        sumRow: []
      };
      component.currentExpHeaders = 1;
      fixture.detectChanges();

      const icons = fixture.nativeElement.querySelectorAll('.material-icons');
      expect(icons.length).toBeGreaterThan(0);
      expect(icons[0].textContent.trim()).toBe('expand_more');
    });

    it('should render expand_less icon in sumRow footer when child sumRow is visible', () => {
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: [
          { desc: ['row1'], cells: [3], total: 3, depth: 0, display: true },
          { desc: ['child'], cells: [1], total: 1, depth: 1, display: true }
        ],
        sumRow: [
          { desc: ['Total'], cells: [3], total: 3, depth: 0, display: true },
          { desc: ['SubTotal'], cells: [1], total: 1, depth: 1, display: true }
        ]
      };
      component.currentExpHeaders = 1;
      fixture.detectChanges();

      const footerIcons = fixture.nativeElement.querySelectorAll('.table-footer .material-icons');
      expect(footerIcons.length).toBeGreaterThan(0);
      expect(footerIcons[0].textContent.trim()).toBe('expand_less');
    });

    it('should render expand_more icon in sumRow footer when child sumRow is hidden', () => {
      component.reportData = {
        meta: { header: ['H1'], rowDesc: ['Desc1', 'Desc2'], sumRowDesc: 'Total', name: 'report', date: '2024' },
        rows: [
          { desc: ['row1'], cells: [3], total: 3, depth: 0, display: true },
          { desc: ['child'], cells: [1], total: 1, depth: 1, display: true }
        ],
        sumRow: [
          { desc: ['Total'], cells: [3], total: 3, depth: 0, display: true },
          { desc: ['SubTotal'], cells: [1], total: 1, depth: 1, display: false }
        ]
      };
      component.currentExpHeaders = 1;
      fixture.detectChanges();

      const footerIcons = fixture.nativeElement.querySelectorAll('.table-footer .material-icons');
      expect(footerIcons.length).toBeGreaterThan(0);
      expect(footerIcons[0].textContent.trim()).toBe('expand_more');
    });
  });
});
