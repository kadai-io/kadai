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
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { RoutingUploadComponent } from './routing-upload.component';
import { RoutingUploadService } from '@task-routing/services/routing-upload.service';
import { HotToastService } from '@ngneat/hot-toast';
import { NotificationService } from 'app/shared/services/notifications/notification.service';
import { of, throwError } from 'rxjs';

describe('RoutingUploadComponent', () => {
  let component: RoutingUploadComponent;
  let fixture: ComponentFixture<RoutingUploadComponent>;

  const routingUploadServiceMock = {
    uploadRoutingRules: vi.fn().mockReturnValue(of({ amountOfImportedRow: 1, result: 'Upload successful' }))
  };

  const hotToastServiceMock = {
    success: vi.fn()
  };

  const notificationServiceMock = {
    showError: vi.fn()
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [RoutingUploadComponent],
      providers: [
        { provide: RoutingUploadService, useValue: routingUploadServiceMock },
        { provide: HotToastService, useValue: hotToastServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RoutingUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit should not throw', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  it('should call uploadRoutingRules and toastService.success when upload is called with a fileList', () => {
    const mockFile = new File(['content'], 'test.dmn', { type: 'application/xml' });
    const mockFileList = { 0: mockFile, length: 1, item: () => mockFile } as unknown as FileList;

    const mockInput = { value: '' };
    vi.spyOn(document, 'getElementById').mockReturnValue(mockInput as any);

    component.upload(mockFileList);

    expect(routingUploadServiceMock.uploadRoutingRules).toHaveBeenCalledWith(mockFile);
    expect(hotToastServiceMock.success).toHaveBeenCalledWith('Upload successful');
  });

  it('should call uploadRoutingRules with null when upload is called with undefined fileList', () => {
    const mockInput = { value: '' };
    vi.spyOn(document, 'getElementById').mockReturnValue(mockInput as any);

    component.file = null;
    component.upload(undefined);

    expect(routingUploadServiceMock.uploadRoutingRules).toHaveBeenCalledWith(null);
  });

  it('should call notificationService.showError on upload error', () => {
    routingUploadServiceMock.uploadRoutingRules.mockReturnValue(
      throwError(() => ({ error: { message: { key: 'SOME_ERROR' } } }))
    );

    const mockInput = { value: '' };
    vi.spyOn(document, 'getElementById').mockReturnValue(mockInput as any);

    component.file = null;
    component.upload(undefined);

    expect(notificationServiceMock.showError).toHaveBeenCalledWith('SOME_ERROR');
  });

  it('onFileChanged should do nothing when files array is empty', () => {
    const mockEvent = {
      target: { files: { length: 0 } } as unknown as HTMLInputElement
    } as unknown as Event;

    expect(() => component.onFileChanged(mockEvent)).not.toThrow();
    expect(routingUploadServiceMock.uploadRoutingRules).not.toHaveBeenCalled();
  });

  it('clearInput should reset input element value and set file to null', () => {
    const mockInput = { value: 'somefile.dmn' };
    vi.spyOn(document, 'getElementById').mockReturnValue(mockInput as any);

    component.file = new File(['content'], 'test.dmn');
    component.clearInput();

    expect(mockInput.value).toBe('');
    expect(component.file).toBeNull();
  });
});
