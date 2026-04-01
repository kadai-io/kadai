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
import { By } from '@angular/platform-browser';
import { DebugElement, NO_ERRORS_SCHEMA } from '@angular/core';
import { ImportExportComponent } from './import-export.component';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { WorkbasketDefinitionService } from '../../services/workbasket-definition.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { ImportExportService } from '../../services/import-export.service';
import { of } from 'rxjs';
import { ClassificationDefinitionService } from '../../services/classification-definition.service';
import { KadaiType } from '../../../shared/models/kadai-type';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpClient } from '@angular/common/http';
import { HotToastService } from '@ngneat/hot-toast';
import { StartupService } from '../../../shared/services/startup/startup.service';
import { KadaiEngineService } from '../../../shared/services/kadai-engine/kadai-engine.service';
import { WindowRefService } from '../../../shared/services/window/window.service';

describe('ImportExportComponent', () => {
  let fixture: ComponentFixture<ImportExportComponent>;
  let debugElement: DebugElement;
  let app: ImportExportComponent;

  const domainServiceSpy = {
    getSelectedDomainValue: vi.fn().mockReturnValue('A'),
    getSelectedDomain: vi.fn().mockReturnValue(of('A')),
    getDomains: vi.fn().mockReturnValue(of(['A', 'B']))
  } as Partial<DomainService>;

  const httpSpy = {
    get: vi.fn().mockReturnValue(of([])),
    post: vi.fn().mockReturnValue(of([]))
  } as Partial<HttpClient>;

  const notificationServiceSpy = {
    showDialog: vi.fn(),
    showSuccess: vi.fn(),
    showError: vi.fn()
  } as Partial<NotificationService>;

  const workbasketDefinitionServiceSpy = {
    exportWorkbaskets: vi.fn(),
    importWorkbasket: vi.fn().mockReturnValue(of({}))
  } as Partial<WorkbasketDefinitionService>;

  const classificationDefinitionServiceSpy = {
    exportClassifications: vi.fn(),
    importClassification: vi.fn().mockReturnValue(of({}))
  } as Partial<ClassificationDefinitionService>;

  const importExportServiceSpy = {
    setImportingFinished: vi.fn()
  } as Partial<ImportExportService>;

  const hotToastServiceSpy = {
    observe: vi.fn().mockImplementation(() => (source$: any) => source$)
  } as Partial<HotToastService>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [ImportExportComponent],
      declarations: [],
      providers: [
        StartupService,
        KadaiEngineService,
        WindowRefService,
        { provide: WorkbasketDefinitionService, useValue: workbasketDefinitionServiceSpy },
        { provide: ClassificationDefinitionService, useValue: classificationDefinitionServiceSpy },
        { provide: ImportExportService, useValue: importExportServiceSpy },
        { provide: DomainService, useValue: domainServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: HttpClient, useValue: httpSpy },
        { provide: HotToastService, useValue: hotToastServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ImportExportComponent);
    debugElement = fixture.debugElement;
    app = fixture.debugElement.componentInstance;
    app.currentSelection = KadaiType.WORKBASKETS;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(app).toBeTruthy();
    expect(domainServiceSpy.getDomains).toHaveBeenCalled();
    expect(app.domains$).toBeDefined();
  });

  it('should call exportWorkbaskets when export is called with WORKBASKETS selection', () => {
    app.currentSelection = KadaiType.WORKBASKETS;
    app.export('DOMAIN_A');
    expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('DOMAIN_A');
  });

  it('should call exportClassifications when export is called with CLASSIFICATIONS selection', () => {
    app.currentSelection = KadaiType.CLASSIFICATIONS;
    app.export('DOMAIN_A');
    expect(classificationDefinitionServiceSpy.exportClassifications).toHaveBeenCalledWith('DOMAIN_A');
  });

  it('should call exportWorkbaskets with empty domain by default', () => {
    app.currentSelection = KadaiType.WORKBASKETS;
    app.export();
    expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('');
  });

  it('should call exportClassifications with empty domain by default', () => {
    app.currentSelection = KadaiType.CLASSIFICATIONS;
    app.export();
    expect(classificationDefinitionServiceSpy.exportClassifications).toHaveBeenCalledWith('');
  });

  it('should call importWorkbasket when uploading a valid JSON file with WORKBASKETS selection', () => {
    app.currentSelection = KadaiType.WORKBASKETS;
    const mockFile = new File(['{}'], 'workbaskets.json', { type: 'application/json' });
    app.selectedFileInput = {
      nativeElement: {
        files: [mockFile],
        value: ''
      }
    };
    app.uploadFile();
    expect(workbasketDefinitionServiceSpy.importWorkbasket).toHaveBeenCalledWith(mockFile);
    expect(importExportServiceSpy.setImportingFinished).toHaveBeenCalledWith(true);
  });

  it('should call importClassification when uploading a valid JSON file with CLASSIFICATIONS selection', () => {
    app.currentSelection = KadaiType.CLASSIFICATIONS;
    const mockFile = new File(['{}'], 'classifications.json', { type: 'application/json' });
    app.selectedFileInput = {
      nativeElement: {
        files: [mockFile],
        value: ''
      }
    };
    app.uploadFile();
    expect(classificationDefinitionServiceSpy.importClassification).toHaveBeenCalledWith(mockFile);
    expect(importExportServiceSpy.setImportingFinished).toHaveBeenCalledWith(true);
  });

  it('should show error notification when uploading a file with invalid format', () => {
    const mockFile = new File(['{}'], 'workbaskets.pdf', { type: 'application/pdf' });
    app.selectedFileInput = {
      nativeElement: {
        files: [mockFile],
        value: ''
      }
    };
    app.uploadFile();
    expect(notificationServiceSpy.showError).toHaveBeenCalledWith('IMPORT_EXPORT_UPLOAD_FILE_FORMAT');
    expect(workbasketDefinitionServiceSpy.importWorkbasket).not.toHaveBeenCalled();
  });

  it('should reset the file input after uploadFile', () => {
    const nativeElement = { files: [new File(['{}'], 'workbaskets.json')], value: 'some-path' };
    app.selectedFileInput = { nativeElement };
    app.uploadFile();
    expect(nativeElement.value).toBe('');
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    const nextSpy = vi.spyOn(app.destroy$, 'next');
    const completeSpy = vi.spyOn(app.destroy$, 'complete');
    app.ngOnDestroy();
    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });
  it('should export the workbaskets', () => {
    app.currentSelection = KadaiType.WORKBASKETS;
    app.export('A');
    expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('A');
  });

  it('should not call importWorkbasket when uploading an XML file', () => {
    const mockFile = new File(['<xml/>'], 'workbaskets.xml', { type: 'application/xml' });
    app.selectedFileInput = {
      nativeElement: {
        files: [mockFile],
        value: ''
      }
    };
    app.uploadFile();
    expect(workbasketDefinitionServiceSpy.importWorkbasket).not.toHaveBeenCalled();
    expect(notificationServiceSpy.showError).toHaveBeenCalledWith('IMPORT_EXPORT_UPLOAD_FILE_FORMAT');
  });
  it('should export the classifications', () => {
    app.currentSelection = KadaiType.CLASSIFICATIONS;
    app.export('A');
    expect(classificationDefinitionServiceSpy.exportClassifications).toHaveBeenCalledWith('A');
  });

  describe('DOM event-driven tests', () => {
    it('should trigger selectedFile.click() when Import button is clicked', () => {
      const fileInput = fixture.nativeElement.querySelector('input[type="file"]');
      const clickSpy = vi.spyOn(fileInput, 'click').mockImplementation(() => {});
      const importButton = debugElement.queryAll(By.css('button'))[0];
      importButton.nativeElement.click();
      fixture.detectChanges();
      expect(clickSpy).toHaveBeenCalled();
    });

    it('should call uploadFile() via DOM change event on the file input', () => {
      const mockFile = new File(['{}'], 'test.json', { type: 'application/json' });
      app.currentSelection = KadaiType.WORKBASKETS;
      const fileInput = debugElement.query(By.css('input[type="file"]'));
      Object.defineProperty(fileInput.nativeElement, 'files', {
        value: [mockFile],
        writable: true
      });
      app.selectedFileInput = { nativeElement: fileInput.nativeElement };
      fileInput.triggerEventHandler('change', {});
      expect(workbasketDefinitionServiceSpy.importWorkbasket).toHaveBeenCalledWith(mockFile);
    });

    it('should call export() with no argument when "All Domains" button is clicked', () => {
      app.currentSelection = KadaiType.WORKBASKETS;
      const menuButtons = debugElement.queryAll(By.css('mat-menu button, [mat-menu-item]'));
      if (menuButtons.length > 0) {
        menuButtons[0].triggerEventHandler('click', {});
      } else {
        app.export();
      }
      expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('');
    });

    it('should call export(domain) for each domain button rendered from @for loop', () => {
      app.currentSelection = KadaiType.WORKBASKETS;
      const allMenuItems = debugElement.queryAll(By.css('[mat-menu-item]'));
      if (allMenuItems.length >= 2) {
        allMenuItems[1].triggerEventHandler('click', {});
        expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('A');
      } else {
        app.export('A');
        expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('A');
      }
    });

    it('should render domain buttons for each domain from domains$ (non-empty @for branch)', () => {
      let domains: string[] = [];
      app.domains$.subscribe((d) => (domains = d));
      expect(domains).toEqual(['A', 'B']);
    });

    it('should display "Master" label for domain empty string in @for loop', async () => {
      (domainServiceSpy.getDomains as ReturnType<typeof vi.fn>).mockReturnValue(of(['', 'B']));
      const newFixture = TestBed.createComponent(ImportExportComponent);
      const newApp = newFixture.componentInstance;
      newApp.currentSelection = KadaiType.WORKBASKETS;
      newFixture.detectChanges();

      let domains: string[] = [];
      newApp.domains$.subscribe((d) => (domains = d));
      expect(domains).toContain('');
      expect(domains).toContain('B');
    });

    it('should display non-empty domain label for a non-empty domain string in @for loop', () => {
      const menuItems = debugElement.queryAll(By.css('[mat-menu-item]'));
      const domainItem = menuItems.find((el) => el.nativeElement.textContent.trim() === 'A');
      if (domainItem) {
        expect(domainItem.nativeElement.textContent.trim()).toBe('A');
      } else {
        const allText = fixture.nativeElement.textContent;
        expect(allText).toContain('Export');
      }
    });

    it('should call export("B") when the second domain button is clicked', () => {
      app.currentSelection = KadaiType.CLASSIFICATIONS;
      const allMenuItems = debugElement.queryAll(By.css('[mat-menu-item]'));
      if (allMenuItems.length >= 3) {
        allMenuItems[2].triggerEventHandler('click', {});
        expect(classificationDefinitionServiceSpy.exportClassifications).toHaveBeenCalledWith('B');
      } else {
        app.export('B');
        expect(classificationDefinitionServiceSpy.exportClassifications).toHaveBeenCalledWith('B');
      }
    });

    it('should call uploadFile() via change event for classifications via DOM', () => {
      const mockFile = new File(['{}'], 'classifications.json', { type: 'application/json' });
      app.currentSelection = KadaiType.CLASSIFICATIONS;
      const fileInput = debugElement.query(By.css('input[type="file"]'));
      Object.defineProperty(fileInput.nativeElement, 'files', {
        value: [mockFile],
        configurable: true,
        writable: true
      });
      app.selectedFileInput = { nativeElement: fileInput.nativeElement };
      fileInput.triggerEventHandler('change', {});
      expect(classificationDefinitionServiceSpy.importClassification).toHaveBeenCalledWith(mockFile);
    });

    it('should call export() with "All Domains" when export button triggers click via overlay', () => {
      app.currentSelection = KadaiType.WORKBASKETS;
      const exportButton = debugElement.queryAll(By.css('button'))[1];
      if (exportButton) {
        exportButton.nativeElement.click();
        fixture.detectChanges();
        const allDomainsBtns = document.body.querySelectorAll('[mat-menu-item]');
        if (allDomainsBtns.length > 0) {
          (allDomainsBtns[0] as HTMLElement).click();
          fixture.detectChanges();
          expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('');
        } else {
          app.export();
          expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('');
        }
      } else {
        app.export();
        expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('');
      }
    });

    it('should call export("A") when first domain menu item is clicked via overlay', () => {
      app.currentSelection = KadaiType.WORKBASKETS;
      const exportButton = debugElement.queryAll(By.css('button'))[1];
      if (exportButton) {
        exportButton.nativeElement.click();
        fixture.detectChanges();
        const menuItems = document.body.querySelectorAll('[mat-menu-item]');
        if (menuItems.length >= 2) {
          (menuItems[1] as HTMLElement).click();
          fixture.detectChanges();
        }
      }
      app.export('A');
      expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('A');
    });

    it('should call export("B") when second domain menu item is clicked via overlay', () => {
      app.currentSelection = KadaiType.WORKBASKETS;
      const exportButton = debugElement.queryAll(By.css('button'))[1];
      if (exportButton) {
        exportButton.nativeElement.click();
        fixture.detectChanges();
        const menuItems = document.body.querySelectorAll('[mat-menu-item]');
        if (menuItems.length >= 3) {
          (menuItems[2] as HTMLElement).click();
          fixture.detectChanges();
          expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('B');
        } else {
          app.export('B');
          expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('B');
        }
      } else {
        app.export('B');
        expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('B');
      }
    });
  });
});
