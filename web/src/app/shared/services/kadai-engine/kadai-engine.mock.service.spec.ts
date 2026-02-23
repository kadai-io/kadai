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
import { beforeEach, describe, expect, it } from 'vitest';
import { KadaiEngineServiceMock } from './kadai-engine.mock.service';
import { Version } from '../../models/version';

describe('KadaiEngineServiceMock', () => {
  let service: KadaiEngineServiceMock;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [KadaiEngineServiceMock]
    });

    service = TestBed.inject(KadaiEngineServiceMock);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getUserInformation()', () => {
    it('should set currentUserInfo with userId "userid"', async () => {
      await service.getUserInformation();

      expect(service.currentUserInfo).toBeDefined();
      expect(service.currentUserInfo.userId).toBe('userid');
    });

    it('should set currentUserInfo with roles containing "admin"', async () => {
      await service.getUserInformation();

      expect(service.currentUserInfo.roles).toContain('admin');
    });

    it('should return a Promise that resolves', async () => {
      const result = service.getUserInformation();

      expect(result).toBeInstanceOf(Promise);
      await expect(result).resolves.toBeUndefined();
    });
  });

  describe('hasRole()', () => {
    it('should return true when the role "admin" is present', () => {
      expect(service.hasRole(['admin'])).toBe(true);
    });

    it('should return false when the role does not exist', () => {
      expect(service.hasRole(['nonexistent'])).toBe(false);
    });

    it('should return false when given an empty array', () => {
      expect(service.hasRole([])).toBe(false);
    });

    it('should return true when at least one role in the array matches', () => {
      expect(service.hasRole(['nonexistent', 'admin'])).toBe(true);
    });

    it('should return false when currentUserInfo has no roles', () => {
      service.currentUserInfo.roles = [];

      expect(service.hasRole(['admin'])).toBe(false);
    });
  });

  describe('getVersion()', () => {
    it('should return an Observable', () => {
      const result = service.getVersion();

      expect(result).toBeDefined();
      expect(typeof result.subscribe).toBe('function');
    });

    it('should emit a Version with version "1.0.0"', () => {
      let emittedVersion: Version | undefined;

      service.getVersion().subscribe((v) => (emittedVersion = v));

      expect(emittedVersion).toBeDefined();
      expect(emittedVersion).toBeInstanceOf(Version);
      expect(emittedVersion!.version).toBe('1.0.0');
    });
  });

  describe('isHistoryProviderEnabled()', () => {
    it('should return an Observable', () => {
      const result = service.isHistoryProviderEnabled();

      expect(result).toBeDefined();
      expect(typeof result.subscribe).toBe('function');
    });

    it('should emit true', () => {
      let emittedValue: boolean | undefined;

      service.isHistoryProviderEnabled().subscribe((v) => (emittedValue = v));

      expect(emittedValue).toBe(true);
    });
  });
});
