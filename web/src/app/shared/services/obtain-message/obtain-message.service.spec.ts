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

import { describe, expect, it, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { ObtainMessageService } from './obtain-message.service';
import { messageTypes } from './message-types';
import { messageByErrorCode } from './message-by-error-code';

describe('ObtainMessageService', () => {
  let service: ObtainMessageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ObtainMessageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return the FALLBACK error message when key is "FALLBACK"', () => {
    const result = service.getMessage('FALLBACK', {}, messageTypes.ERROR);
    expect(result).toBe(messageByErrorCode[messageTypes.ERROR]['FALLBACK']);
  });

  it('should return the FALLBACK success message when key is "FALLBACK"', () => {
    const result = service.getMessage('FALLBACK', {}, messageTypes.SUCCESS);
    expect(result).toBe(messageByErrorCode[messageTypes.SUCCESS]['FALLBACK']);
  });

  it('should return a known ERROR message for a valid key', () => {
    const result = service.getMessage('CRITICAL_SYSTEM_ERROR', {}, messageTypes.ERROR);
    expect(result).toBe(messageByErrorCode[messageTypes.ERROR]['CRITICAL_SYSTEM_ERROR']);
  });

  it('should fall back to FALLBACK when the key is unknown', () => {
    const result = service.getMessage('NON_EXISTENT_KEY_XYZ', {}, messageTypes.ERROR);
    expect(result).toBe(messageByErrorCode[messageTypes.ERROR]['FALLBACK']);
  });

  it('should return a not-configured message when both key and FALLBACK are absent for the type', () => {
    const result = service.getMessage('NO_SUCH_KEY', {}, messageTypes.INFORMATION);
    expect(result).toContain('is not configured');
  });

  it('should replace a single placeholder with the provided variable value', () => {
    const result = service.getMessage('DOMAIN_NOT_FOUND', { domain: 'TEST_DOMAIN' }, messageTypes.ERROR);
    expect(result).toContain("'TEST_DOMAIN'");
    expect(result).not.toContain('{domain}');
  });

  it('should replace multiple placeholders with the provided variable values', () => {
    const result = service.getMessage(
      'NOT_AUTHORIZED',
      { currentUserId: 'user123', roles: 'ADMIN' },
      messageTypes.ERROR
    );
    expect(result).toContain("'user123'");
    expect(result).toContain("'ADMIN'");
  });

  it('should leave unreferenced placeholders untouched', () => {
    const result = service.getMessage('DOMAIN_NOT_FOUND', {}, messageTypes.ERROR);
    expect(result).toContain('{domain}');
  });

  it('should return a dialog message for a known DIALOG key', () => {
    const result = service.getMessage('WORKBASKET_DELETE', { workbasketKey: 'WB-1' }, messageTypes.DIALOG);
    expect(result).toContain("'WB-1'");
  });
});
