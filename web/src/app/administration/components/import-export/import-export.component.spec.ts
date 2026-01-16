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
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('../../../shared/util/blob-generator');

describe.skip('ImportExportComponent', () => {
  let fixture: ComponentFixture<ImportExportComponent>;
  let debugElement: DebugElement;
  let app: ImportExportComponent;

  const domainServiceSpy = vi.fn().mockImplementation(
    (): Partial<DomainService> => ({
      getSelectedDomainValue: vi.fn().mockReturnValue('A'),
      getSelectedDomain: vi.fn().mockReturnValue(of('A')),
      getDomains: vi.fn().mockReturnValue(of('A'))
    })
  );

  const httpSpy = vi.fn().mockImplementation(
    (): Partial<HttpClient> => ({
      get: vi.fn().mockReturnValue(of([])),
      post: vi.fn().mockReturnValue(of([]))
    })
  );

  const showDialogFn = vi.fn().mockReturnValue(true);
  const notificationServiceSpy = vi.fn().mockImplementation(
    (): Partial<NotificationService> => ({
      showDialog: showDialogFn,
      showSuccess: showDialogFn
    })
  );

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [],
      declarations: [ImportExportComponent],
      providers: [
        StartupService,
        KadaiEngineService,
        WindowRefService,
        WorkbasketDefinitionService,
        ClassificationDefinitionService,
        ImportExportService,
        { provide: DomainService, useClass: domainServiceSpy },
        { provide: NotificationService, useClass: notificationServiceSpy },
        { provide: HttpClient, useClass: httpSpy }
      ]
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
  /*
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
    app.uploadFile();
    expect(app.uploadService.isInUse).toBeTruthy();
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
    expect(notificationServiceSpy).toHaveBeenCalled();
  });

    it('should successfully export the workbaskets', async (done) => {
      app
        .export()
        .pipe(take(1))
        .subscribe(() => {
          expect(BlobGenerator.saveFile).toHaveBeenCalledWith([], expect.stringMatching(/Workbaskets_.*\.json/));
          done();
        });
    });

    it('should successfully export the classifications', async (done) => {
      app.currentSelection = KadaiType.CLASSIFICATIONS;
      app
        .export()
        .pipe(take(1))
        .subscribe(() => {
          expect(BlobGenerator.saveFile).toHaveBeenCalledWith([], expect.stringMatching(/Classifications_.*\.json/));
          done();
        });
    });*/
});
