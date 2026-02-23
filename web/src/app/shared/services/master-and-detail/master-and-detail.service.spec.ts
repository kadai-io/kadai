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

import { inject, TestBed } from '@angular/core/testing';

import { MasterAndDetailService } from './master-and-detail.service';
import { beforeEach, describe, expect, it } from 'vitest';

describe('MasterAndDetailService', () => {
  let service: MasterAndDetailService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MasterAndDetailService]
    });
    service = TestBed.inject(MasterAndDetailService);
  });

  it('should be created', inject([MasterAndDetailService], (injectedService: MasterAndDetailService) => {
    expect(injectedService).toBeTruthy();
  }));

  describe('getShowDetail', () => {
    it('should return an observable', () => {
      const obs = service.getShowDetail();
      expect(obs).toBeDefined();
      expect(typeof obs.subscribe).toBe('function');
    });

    it('should initially emit false as the default BehaviorSubject value', () => {
      let emitted: boolean = null;
      service.getShowDetail().subscribe((val) => (emitted = val));
      expect(emitted).toBe(false);
    });
  });

  describe('setShowDetail', () => {
    it('should emit true when called with true', () => {
      const emitted: boolean[] = [];
      service.getShowDetail().subscribe((val) => emitted.push(val));

      service.setShowDetail(true);

      expect(emitted).toContain(true);
    });

    it('should emit false when called with false', () => {
      service.setShowDetail(true);

      const emitted: boolean[] = [];
      service.getShowDetail().subscribe((val) => emitted.push(val));

      service.setShowDetail(false);

      expect(emitted).toContain(false);
    });

    it('should emit updated value for existing subscribers', () => {
      const emitted: boolean[] = [];
      service.getShowDetail().subscribe((val) => emitted.push(val));

      service.setShowDetail(true);
      service.setShowDetail(false);
      service.setShowDetail(true);

      expect(emitted).toEqual([false, true, false, true]);
    });

    it('should provide latest value to new subscribers immediately', () => {
      service.setShowDetail(true);

      let lastEmitted: boolean;
      service.getShowDetail().subscribe((val) => (lastEmitted = val));

      expect(lastEmitted).toBe(true);
    });
  });
});
