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
import { DebugElement, NO_ERRORS_SCHEMA } from '@angular/core';
import { ImportExportComponent } from './import-export.component';
import { StartupService } from '../../../shared/services/startup/startup.service';
import { KadaiEngineService } from '../../../shared/services/kadai-engine/kadai-engine.service';
import { WindowRefService } from '../../../shared/services/window/window.service';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { WorkbasketDefinitionService } from '../../services/workbasket-definition.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { ImportExportService } from '../../services/import-export.service';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { ClassificationDefinitionService } from '../../services/classification-definition.service';
import { KadaiType } from '../../../shared/models/kadai-type';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { HotToastService } from '@ngneat/hot-toast';

describe('ImportExportComponent', () => {
  let fixture: ComponentFixture<ImportExportComponent>;
  let debugElement: DebugElement;
  let app: ImportExportComponent;

  const domainServiceSpy = {
    getSelectedDomainValue: vi.fn().mockReturnValue('A'),
    getSelectedDomain: vi.fn().mockReturnValue(of('A')),
    getDomains: vi.fn().mockReturnValue(of(['A']))
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
    observe: vi.fn().mockImplementation(() => (source$) => source$)
  } as Partial<HotToastService>;

  beforeEach(async () => {
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

    vi.clearAllMocks();

    fixture = TestBed.createComponent(ImportExportComponent);
    debugElement = fixture.debugElement;
    app = fixture.debugElement.componentInstance;
    app.currentSelection = KadaiType.WORKBASKETS;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(app).toBeTruthy();
  });
  it('should successfully upload a valid file', () => {
    app.selectedFileInput = {
      nativeElement: {
        files: [
          {
            lastModified: 1599117374674,
            name: 'Workbaskets_2020-09-03T09_16_14.1414Z.json',
            size: 59368,
            type: 'application/json',
            webkitRelativePath: ''
          }
        ]
      }
    };
    app.currentSelection = KadaiType.WORKBASKETS;
    app.uploadFile();
    expect(workbasketDefinitionServiceSpy.importWorkbasket).toHaveBeenCalledTimes(1);
    expect(importExportServiceSpy.setImportingFinished).toHaveBeenCalledWith(true);
  });

  it('should trigger an error when uploading an invalid file format', () => {
    app.selectedFileInput = {
      nativeElement: {
        files: [
          {
            lastModified: 1599117374674,
            name: 'Workbaskets_2020-09-03T09_16_14.1414Z.pdf',
            size: 59368,
            type: 'application/pdf',
            webkitRelativePath: ''
          }
        ]
      }
    };
    app.uploadFile();
    expect(notificationServiceSpy.showError).toHaveBeenCalledWith('IMPORT_EXPORT_UPLOAD_FILE_FORMAT');
    expect(workbasketDefinitionServiceSpy.importWorkbasket).not.toHaveBeenCalled();
  });

  it('should export the workbaskets', () => {
    app.currentSelection = KadaiType.WORKBASKETS;
    app.export('A');
    expect(workbasketDefinitionServiceSpy.exportWorkbaskets).toHaveBeenCalledWith('A');
  });

  it('should export the classifications', () => {
    app.currentSelection = KadaiType.CLASSIFICATIONS;
    app.export('A');
    expect(classificationDefinitionServiceSpy.exportClassifications).toHaveBeenCalledWith('A');
  });
});
