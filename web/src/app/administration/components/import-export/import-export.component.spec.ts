/*
 * Copyright [2025] [envite consulting GmbH]
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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
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

jest.mock('../../../shared/util/blob-generator');

describe('ImportExportComponent', () => {
  let fixture: ComponentFixture<ImportExportComponent>;
  let debugElement: DebugElement;
  let component: ImportExportComponent;

  const domainServiceMock: Partial<DomainService> = {
    getSelectedDomainValue: jest.fn(),
    getSelectedDomain: jest.fn(),
    getDomains: jest.fn().mockReturnValue(of(['A', 'B']))
  };

  const workbasketDefinitionServiceMock: Partial<WorkbasketDefinitionService> = {
    exportWorkbaskets: jest.fn(),
    importWorkbasket: jest.fn().mockReturnValue(of({}))
  } as any;

  const classificationDefinitionServiceMock: Partial<ClassificationDefinitionService> = {
    exportClassifications: jest.fn(),
    importClassification: jest.fn().mockReturnValue(of({}))
  } as any;

  const notificationServiceMock: Partial<NotificationService> = {
    showError: jest.fn()
  } as any;

  const importExportServiceMock: Partial<ImportExportService> = {
    setImportingFinished: jest.fn()
  } as any;

  const hotToastServiceMock: any = {
    observe: jest.fn().mockImplementation(() => (source$) => source$)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ImportExportComponent],
      providers: [
        { provide: DomainService, useValue: domainServiceMock },
        { provide: WorkbasketDefinitionService, useValue: workbasketDefinitionServiceMock },
        { provide: ClassificationDefinitionService, useValue: classificationDefinitionServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: ImportExportService, useValue: importExportServiceMock },
        { provide: require('@ngneat/hot-toast').HotToastService, useValue: hotToastServiceMock }
      ]
    }).compileComponents();

    jest.clearAllMocks();

    fixture = TestBed.createComponent(ImportExportComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    component.currentSelection = KadaiType.WORKBASKETS;
    fixture.detectChanges();
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should load domains on init', (done) => {
    component.ngOnInit();
    component.domains$.subscribe((domains) => {
      expect(domains).toEqual(['A', 'B']);
      done();
    });
  });

  it('should call exportWorkbaskets when currentSelection is WORKBASKETS', () => {
    component.currentSelection = KadaiType.WORKBASKETS;
    component.export('D1');
    expect(workbasketDefinitionServiceMock.exportWorkbaskets).toHaveBeenCalledWith('D1');
  });

  it('should call exportClassifications when currentSelection is CLASSIFICATIONS', () => {
    component.currentSelection = KadaiType.CLASSIFICATIONS;
    component.export('D2');
    expect(classificationDefinitionServiceMock.exportClassifications).toHaveBeenCalledWith('D2');
  });

  it('should upload workbaskets file when valid .json file is selected', () => {
    const file: any = { name: 'Workbaskets_2025-09-26.json' };
    const native = { files: [file], value: 'somepath' } as any;
    (component as any).selectedFileInput = { nativeElement: native };
    component.currentSelection = KadaiType.WORKBASKETS;

    component.uploadFile();

    expect(workbasketDefinitionServiceMock.importWorkbasket).toHaveBeenCalledWith(file);
    expect(importExportServiceMock.setImportingFinished).toHaveBeenCalledWith(true);
    expect(native.value).toBe('');
  });

  it('should upload classifications file when valid .json file is selected', () => {
    const file: any = { name: 'Classifications_2025-09-26.json' };
    const native = { files: [file], value: 'abc' } as any;
    (component as any).selectedFileInput = { nativeElement: native };
    component.currentSelection = KadaiType.CLASSIFICATIONS;

    component.uploadFile();

    expect(classificationDefinitionServiceMock.importClassification).toHaveBeenCalledWith(file);
    expect(importExportServiceMock.setImportingFinished).toHaveBeenCalledWith(true);
    expect(native.value).toBe('');
  });

  it('should show error and reset input when uploading invalid file format', () => {
    const file: any = { name: 'Workbaskets_2025-09-26.pdf', value: 'x' };
    const native = { files: [file], value: 'xyz' } as any;
    (component as any).selectedFileInput = { nativeElement: native };
    component.currentSelection = KadaiType.WORKBASKETS;

    component.uploadFile();

    expect(notificationServiceMock.showError).toHaveBeenCalledWith('IMPORT_EXPORT_UPLOAD_FILE_FORMAT');
    expect(native.value).toBe('');
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    component.ngOnDestroy();
    expect((component.destroy$ as any).closed || (component.destroy$ as any).isStopped).toBe(true);
  });
});
