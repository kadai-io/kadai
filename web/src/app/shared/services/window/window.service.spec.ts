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
import { WindowRefService } from './window.service';

describe('WindowRefService', () => {
  let service: WindowRefService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WindowRefService]
    });
    service = TestBed.inject(WindowRefService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('nativeWindow', () => {
    it('should return the native window object', () => {
      const nativeWindow = service.nativeWindow;
      expect(nativeWindow).toBeDefined();
      expect(nativeWindow).toBeTruthy();
    });

    it('should return the global window object', () => {
      const nativeWindow = service.nativeWindow;
      expect(nativeWindow).toBe(window);
    });

    it('should have typical window properties', () => {
      const nativeWindow = service.nativeWindow;
      expect(typeof nativeWindow.document).toBe('object');
      expect(typeof nativeWindow.location).toBe('object');
    });
  });
});
