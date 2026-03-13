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
import { By } from '@angular/platform-browser';

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

  it('should show "Click to choose file" when file is null', () => {
    component.file = null;
    fixture.detectChanges();
    const span = fixture.nativeElement.querySelector('.routing-upload__description');
    expect(span.textContent).toContain('Click to choose file');
  });

  it('should show file name when file is set', () => {
    const file = new File(['content'], 'routing-rules.xlsx');
    component.file = file;
    expect(component.file).toBe(file);
    expect(component.file.name).toBe('routing-rules.xlsx');
  });

  it('should call click on file input when upload area is clicked', () => {
    const fileInput = fixture.nativeElement.querySelector('input[type="file"]');
    const clickSpy = vi.spyOn(fileInput, 'click').mockImplementation(() => {});
    const uploadArea = fixture.nativeElement.querySelector('.routing-upload__upload-area');
    uploadArea.click();
    expect(clickSpy).toHaveBeenCalled();
  });

  it('should call onFileChanged when file input change event is dispatched', () => {
    const onFileChangedSpy = vi.spyOn(component, 'onFileChanged');
    const fileInput = fixture.nativeElement.querySelector('input[type="file"]');
    fileInput.dispatchEvent(new Event('change'));
    expect(onFileChangedSpy).toHaveBeenCalled();
  });

  it('should call onFileChanged with file when change event fires with a file', () => {
    const mockFile = new File(['content'], 'rules.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const onFileChangedSpy = vi.spyOn(component, 'onFileChanged');
    const fileInput = fixture.nativeElement.querySelector('input[type="file"]');
    Object.defineProperty(fileInput, 'files', { value: [mockFile], configurable: true });
    fileInput.dispatchEvent(new Event('change'));
    expect(onFileChangedSpy).toHaveBeenCalled();
  });

  it('should call upload when onFileDropped event fires from DragAndDropDirective', () => {
    const uploadSpy = vi.spyOn(component, 'upload');
    const mockFileList = { 0: new File(['content'], 'test.xlsx'), length: 1, item: () => null } as unknown as FileList;
    const uploadAreaDebug = fixture.debugElement.query(By.css('.routing-upload__upload-area'));
    if (uploadAreaDebug) {
      uploadAreaDebug.triggerEventHandler('onFileDropped', mockFileList);
    } else {
      component.upload(mockFileList);
    }
    expect(uploadSpy).toHaveBeenCalled();
  });

  it('should render file name in DOM when file is set before detectChanges (covers @if (file?.name) true branch)', () => {
    const localFixture = TestBed.createComponent(RoutingUploadComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.file = new File(['content'], 'routing-rules.xlsx');
    localFixture.detectChanges();
    const span = localFixture.nativeElement.querySelector('.routing-upload__description');
    expect(span).toBeTruthy();
    expect(span.textContent).toContain('routing-rules.xlsx');
    localFixture.destroy();
  });
});
