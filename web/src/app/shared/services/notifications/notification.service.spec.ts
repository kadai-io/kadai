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

import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { HotToastService } from '@ngneat/hot-toast';
import { NotificationService } from './notification.service';
import { ObtainMessageService } from '../obtain-message/obtain-message.service';

describe('NotificationService', () => {
  let service: NotificationService;

  const mockDialogRef = {
    beforeClosed: vi.fn().mockReturnValue(of(null))
  };

  const mockMatDialog = {
    open: vi.fn().mockReturnValue(mockDialogRef)
  };

  const mockHotToastService = {
    error: vi.fn(),
    success: vi.fn(),
    show: vi.fn(),
    warning: vi.fn()
  };

  const mockObtainMessageService = {
    getMessage: vi.fn().mockReturnValue('mocked message')
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockDialogRef.beforeClosed.mockReturnValue(of(null));
    mockMatDialog.open.mockReturnValue(mockDialogRef);

    TestBed.configureTestingModule({
      providers: [
        NotificationService,
        { provide: MatDialog, useValue: mockMatDialog },
        { provide: HotToastService, useValue: mockHotToastService },
        { provide: ObtainMessageService, useValue: mockObtainMessageService }
      ]
    });

    service = TestBed.inject(NotificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('generateToastId', () => {
    it('should return errorKey alone when messageVariables is empty', () => {
      const result = service.generateToastId('SOME_ERROR', {});
      expect(result).toBe('SOME_ERROR');
    });

    it('should concatenate errorKey with variable keys and values', () => {
      const result = service.generateToastId('SOME_ERROR', { taskId: '123', name: 'test' });
      expect(result).toBe('SOME_ERRORtaskId123nametest');
    });

    it('should handle multiple variables', () => {
      const result = service.generateToastId('KEY', { a: '1', b: '2' });
      expect(result).toBe('KEYa1b2');
    });
  });

  describe('showError', () => {
    it('should call toastService.error with message and options', () => {
      service.showError('ERROR_KEY', { id: 'abc' });

      expect(mockObtainMessageService.getMessage).toHaveBeenCalledWith('ERROR_KEY', { id: 'abc' }, expect.anything());
      expect(mockHotToastService.error).toHaveBeenCalledWith(
        'mocked message',
        expect.objectContaining({
          dismissible: true,
          autoClose: false,
          id: expect.any(String)
        })
      );
    });

    it('should call showError with empty messageVariables by default', () => {
      service.showError('ERROR_KEY');

      expect(mockHotToastService.error).toHaveBeenCalledWith(
        'mocked message',
        expect.objectContaining({ dismissible: true, autoClose: false })
      );
    });

    it('should generate toast id using errorKey and messageVariables', () => {
      service.showError('ERR', { key: 'val' });

      const call = mockHotToastService.error.mock.calls[0];
      expect(call[1].id).toBe('ERRkeyval');
    });
  });

  describe('showSuccess', () => {
    it('should call toastService.success with message and duration option', () => {
      service.showSuccess('SUCCESS_KEY', { name: 'task' });

      expect(mockObtainMessageService.getMessage).toHaveBeenCalledWith(
        'SUCCESS_KEY',
        { name: 'task' },
        expect.anything()
      );
      expect(mockHotToastService.success).toHaveBeenCalledWith('mocked message', { duration: 5000 });
    });

    it('should call showSuccess with empty messageVariables by default', () => {
      service.showSuccess('SUCCESS_KEY');

      expect(mockHotToastService.success).toHaveBeenCalledWith('mocked message', { duration: 5000 });
    });
  });

  describe('showInformation', () => {
    it('should call toastService.show with info icon and message', () => {
      service.showInformation('INFO_KEY', {});

      expect(mockObtainMessageService.getMessage).toHaveBeenCalledWith('INFO_KEY', {}, expect.anything());
      expect(mockHotToastService.show).toHaveBeenCalledWith(
        expect.stringContaining('mocked message'),
        expect.objectContaining({ id: 'empty-workbasket' })
      );
    });

    it('should include material icons span in the message', () => {
      service.showInformation('INFO_KEY');

      const callArgs = mockHotToastService.show.mock.calls[0][0];
      expect(callArgs).toContain('material-icons');
      expect(callArgs).toContain('info');
    });

    it('should use default empty messageVariables', () => {
      service.showInformation('INFO_KEY');
      expect(mockObtainMessageService.getMessage).toHaveBeenCalledWith('INFO_KEY', {}, expect.anything());
    });
  });

  describe('showWarning', () => {
    it('should call toastService.warning with message', () => {
      service.showWarning('WARN_KEY', { key: 'value' });

      expect(mockObtainMessageService.getMessage).toHaveBeenCalledWith('WARN_KEY', { key: 'value' }, expect.anything());
      expect(mockHotToastService.warning).toHaveBeenCalledWith('mocked message');
    });

    it('should use default empty messageVariables', () => {
      service.showWarning('WARN_KEY');
      expect(mockHotToastService.warning).toHaveBeenCalledWith('mocked message');
    });
  });

  describe('showDialog', () => {
    it('should open dialog with correct data', () => {
      const callback = vi.fn();
      service.showDialog('DIALOG_KEY', {}, callback);

      expect(mockMatDialog.open).toHaveBeenCalledWith(
        expect.anything(),
        expect.objectContaining({
          data: expect.objectContaining({ message: 'mocked message', callback }),
          backdropClass: 'backdrop'
        })
      );
    });

    it('should subscribe to beforeClosed', () => {
      const callback = vi.fn();
      service.showDialog('DIALOG_KEY', {}, callback);

      expect(mockDialogRef.beforeClosed).toHaveBeenCalled();
    });

    it('should invoke the call function from beforeClosed when it is a function', () => {
      const returnedCallback = vi.fn();
      mockDialogRef.beforeClosed.mockReturnValue(of(returnedCallback));

      const callback = vi.fn();
      service.showDialog('DIALOG_KEY', {}, callback);

      expect(returnedCallback).toHaveBeenCalled();
    });

    it('should not invoke anything when beforeClosed emits null', () => {
      mockDialogRef.beforeClosed.mockReturnValue(of(null));
      const callback = vi.fn();

      expect(() => service.showDialog('DIALOG_KEY', {}, callback)).not.toThrow();
    });

    it('should not invoke anything when beforeClosed emits non-function', () => {
      mockDialogRef.beforeClosed.mockReturnValue(of('not a function'));
      const callback = vi.fn();

      expect(() => service.showDialog('DIALOG_KEY', {}, callback)).not.toThrow();
    });

    it('should return the dialog ref', () => {
      const callback = vi.fn();
      const result = service.showDialog('DIALOG_KEY', {}, callback);

      expect(result).toBe(mockDialogRef);
    });

    it('should use default empty messageVariables', () => {
      const callback = vi.fn();
      service.showDialog('DIALOG_KEY', undefined, callback);

      expect(mockObtainMessageService.getMessage).toHaveBeenCalledWith('DIALOG_KEY', {}, expect.anything());
    });
  });
});
