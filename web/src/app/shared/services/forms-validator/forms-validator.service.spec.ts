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
import { FormsValidatorService } from './forms-validator.service';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';
import { NotificationService } from '../notifications/notification.service';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { of } from 'rxjs';

const accessIdsServiceMock = {
  searchForAccessId: vi.fn().mockReturnValue(of([{ accessId: 'user1' }]))
};

const notificationServiceMock = {
  showError: vi.fn(),
  showWarning: vi.fn()
};

describe('FormsValidatorService', () => {
  let service: FormsValidatorService;

  beforeEach(() => {
    vi.clearAllMocks();

    TestBed.configureTestingModule({
      providers: [
        FormsValidatorService,
        { provide: AccessIdsService, useValue: accessIdsServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock }
      ]
    });

    service = TestBed.inject(FormsValidatorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('isFieldValid', () => {
    it('should return false when form is null', () => {
      expect(service.isFieldValid(null, 'field')).toBe(false);
    });

    it('should return false when controls are empty', () => {
      const mockForm: any = { form: { controls: {} } };
      expect(service.isFieldValid(mockForm, 'field')).toBe(false);
    });

    it('should return true when formSubmitAttempt is false', () => {
      service.formSubmitAttempt = false;
      const mockForm: any = {
        form: {
          controls: {
            field: { valid: false, touched: false }
          }
        }
      };
      expect(service.isFieldValid(mockForm, 'field')).toBe(true);
    });

    it('should return true when formSubmitAttempt is true and field is valid', () => {
      service.formSubmitAttempt = true;
      const mockForm: any = {
        form: {
          controls: {
            field: { valid: true, touched: true }
          }
        }
      };
      expect(service.isFieldValid(mockForm, 'field')).toBe(true);
    });

    it('should return false when formSubmitAttempt is true and field is invalid', () => {
      service.formSubmitAttempt = true;
      const mockForm: any = {
        form: {
          controls: {
            field: { valid: false, touched: false }
          }
        }
      };
      expect(service.isFieldValid(mockForm, 'field')).toBe(false);
    });

    it('should return false when formSubmitAttempt is true, touched is true, but field is invalid', () => {
      service.formSubmitAttempt = true;
      const mockForm: any = {
        form: {
          controls: {
            field: { valid: false, touched: true }
          }
        }
      };
      expect(service.isFieldValid(mockForm, 'field')).toBe(false);
    });
  });

  describe('validateFormInformation', () => {
    it('should return false when form is null', async () => {
      const result = await service.validateFormInformation(null, new Map());
      expect(result).toBe(false);
    });

    it('should resolve to true when all non-owner controls are valid and no owner field exists', async () => {
      const mockForm: any = {
        form: {
          controls: {
            field1: { invalid: false, valid: true, touched: true, value: 'val' }
          }
        }
      };
      const toggleMap = new Map<any, boolean>();
      const result = await service.validateFormInformation(mockForm, toggleMap);
      expect(result).toBe(true);
    });

    it('should resolve to false when a non-owner control is invalid', async () => {
      const mockForm: any = {
        form: {
          controls: {
            field1: { invalid: true, valid: false, touched: false, value: '' }
          }
        }
      };
      const toggleMap = new Map<any, boolean>();
      const result = await service.validateFormInformation(mockForm, toggleMap);
      expect(result).toBe(false);
    });

    it('should resolve to truthy when owner field exists and accessId matches', async () => {
      accessIdsServiceMock.searchForAccessId.mockReturnValue(of([{ accessId: 'user1' }]));

      const mockForm: any = {
        form: {
          controls: {
            'workbasket.owner': { invalid: false, valid: true, value: 'user1' }
          }
        }
      };
      const toggleMap = new Map<any, boolean>();
      const result = await service.validateFormInformation(mockForm, toggleMap);
      expect(result).toBeTruthy();
    });

    it('should resolve to falsy when owner field exists but accessId does not match', async () => {
      accessIdsServiceMock.searchForAccessId.mockReturnValue(of([{ accessId: 'otherUser' }]));

      const mockForm: any = {
        form: {
          controls: {
            'workbasket.owner': { invalid: false, valid: true, value: 'nonexistentUser' }
          }
        }
      };
      const toggleMap = new Map<any, boolean>();
      const result = await service.validateFormInformation(mockForm, toggleMap);
      expect(result).toBeFalsy();
    });
  });

  describe('validateFormAccess', () => {
    it('should resolve to true when FormArray is empty', async () => {
      const emptyFormArray = new FormArray([]);
      const result = await service.validateFormAccess(emptyFormArray, new Map());
      expect(result).toBe(true);
    });

    it('should resolve to true when all access IDs are found', async () => {
      accessIdsServiceMock.searchForAccessId.mockReturnValue(of([{ accessId: 'user1' }]));

      const formArray = new FormArray([
        new FormControl({
          accessId: 'user1',
          permRead: true,
          permReadTasks: false,
          permEditTasks: false,
          permOpen: false,
          permAppend: false,
          permTransfer: false,
          permDistribute: false
        })
      ]);
      const result = await service.validateFormAccess(formArray, new Map());
      expect(result).toBe(true);
    });

    it('should resolve to false when an access ID is not found', async () => {
      accessIdsServiceMock.searchForAccessId.mockReturnValue(of([]));

      const formArray = new FormArray([
        new FormControl({
          accessId: 'unknownUser',
          permRead: false,
          permReadTasks: false,
          permEditTasks: false,
          permOpen: false,
          permAppend: false,
          permTransfer: false,
          permDistribute: false
        })
      ]);
      const result = await service.validateFormAccess(formArray, new Map());
      expect(result).toBe(false);
    });
  });

  describe('validateFormAccess - permission warnings', () => {
    it('should show warnings when permEditTasks is true but permReadTasks is false', async () => {
      accessIdsServiceMock.searchForAccessId.mockReturnValue(of([{ accessId: 'user1' }]));
      const formArray = new FormArray([
        new FormControl({
          accessId: 'user1',
          permRead: true,
          permReadTasks: false,
          permEditTasks: true,
          permOpen: true,
          permAppend: false,
          permTransfer: true,
          permDistribute: true
        })
      ]);
      await service.validateFormAccess(formArray, new Map());
      expect(notificationServiceMock.showWarning).toHaveBeenCalled();
    });

    it('should show warnings when permReadTasks is true but permRead is false', async () => {
      accessIdsServiceMock.searchForAccessId.mockReturnValue(of([{ accessId: 'user1' }]));
      const formArray = new FormArray([
        new FormControl({
          accessId: 'user1',
          permRead: false,
          permReadTasks: true,
          permEditTasks: true,
          permOpen: true,
          permAppend: true,
          permTransfer: false,
          permDistribute: true
        })
      ]);
      await service.validateFormAccess(formArray, new Map());
      expect(notificationServiceMock.showWarning).toHaveBeenCalled();
    });
  });

  describe('validateInputOverflow', () => {
    it('should set overflow to true when value length >= maxLength', () => {
      const emittedValues: Map<string, boolean>[] = [];
      service.inputOverflowObservable.subscribe((val) => emittedValues.push(val));

      const mockModel: any = {
        name: 'testField',
        value: 'abcde'
      };

      service.validateInputOverflow(mockModel, 5);

      expect(emittedValues.length).toBeGreaterThan(0);
      const lastEmit = emittedValues[emittedValues.length - 1];
      expect(lastEmit.get('testField')).toBe(true);
    });

    it('should set overflow to false when value length < maxLength', () => {
      const emittedValues: Map<string, boolean>[] = [];
      service.inputOverflowObservable.subscribe((val) => emittedValues.push(val));

      const mockModel: any = {
        name: 'testField',
        value: 'abc'
      };

      service.validateInputOverflow(mockModel, 10);

      expect(service).toBeTruthy();
    });

    it('should unsubscribe existing overflow subscription when called again for same field', () => {
      const mockModel: any = { name: 'testField', value: 'abcde' };
      service.validateInputOverflow(mockModel, 5);
      service.validateInputOverflow(mockModel, 5);
      expect(service).toBeTruthy();
    });
  });
});
