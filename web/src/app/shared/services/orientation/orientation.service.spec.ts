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
import { OrientationService } from './orientation.service';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Orientation } from 'app/shared/models/orientation';

describe('OrientationService', () => {
  let service: OrientationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OrientationService]
    });
    service = TestBed.inject(OrientationService);
  });

  it('should be created', inject([OrientationService], (injectedService: OrientationService) => {
    expect(injectedService).toBeTruthy();
  }));

  describe('getOrientation', () => {
    it('should return an observable', () => {
      const obs = service.getOrientation();
      expect(obs).toBeDefined();
      expect(typeof obs.subscribe).toBe('function');
    });

    it('should emit the current orientation on subscription', () => {
      let emitted: Orientation;
      service.getOrientation().subscribe((o) => (emitted = o));
      expect(emitted).toBe(Orientation.landscape);
    });
  });

  describe('changeOrientation', () => {
    it('should emit the given orientation', () => {
      const emitted: Orientation[] = [];
      service.getOrientation().subscribe((o) => emitted.push(o));

      service.changeOrientation(Orientation.portrait);

      expect(emitted).toContain(Orientation.portrait);
    });

    it('should toggle the lock state', () => {
      const lockBefore = (service as any).lock;
      service.changeOrientation(Orientation.landscape);
      const lockAfter = (service as any).lock;
      expect(lockBefore).not.toBe(lockAfter);
    });

    it('should emit landscape orientation when changed to landscape', () => {
      const emitted: Orientation[] = [];
      service.getOrientation().subscribe((o) => emitted.push(o));

      service.changeOrientation(Orientation.landscape);

      expect(emitted).toContain(Orientation.landscape);
    });
  });

  describe('onResize', () => {
    it('should not change orientation if it has not changed', () => {
      Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1280 });
      Object.defineProperty(window, 'innerHeight', { writable: true, configurable: true, value: 720 });

      const emitted: Orientation[] = [];
      let first = true;
      service.getOrientation().subscribe((o) => {
        if (first) {
          first = false;
          return;
        }
        emitted.push(o);
      });

      service.onResize();

      expect(emitted).toHaveLength(0);
    });

    it('should detect portrait when height exceeds width', () => {
      Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 400 });
      Object.defineProperty(window, 'innerHeight', { writable: true, configurable: true, value: 900 });

      (service as any).currentOrientation = Orientation.landscape;
      (service as any).lock = false;

      vi.useFakeTimers();
      service.onResize();
      vi.runAllTimers();
      vi.useRealTimers();

      expect((service as any).currentOrientation).toBe(Orientation.portrait);
    });
  });

  describe('calculateNumberItemsList', () => {
    it('should calculate number of items based on height, cardHeight and unusedHeight', () => {
      Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1280 });

      const result = service.calculateNumberItemsList(1000, 100, 200);
      expect(result).toBe(8);
    });

    it('should floor cards in half when doubleList is true and width < 992', () => {
      Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 800 });

      const result = service.calculateNumberItemsList(1000, 100, 200, true);
      expect(result).toBe(4);
    });

    it('should not halve when doubleList is true and width >= 992', () => {
      Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1200 });

      const result = service.calculateNumberItemsList(1000, 100, 200, true);
      expect(result).toBe(8);
    });

    it('should round the card count', () => {
      Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1280 });

      const result = service.calculateNumberItemsList(950, 100, 200);
      expect(result).toBe(8);
    });

    it('should return 0 when container height equals unused height', () => {
      Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1280 });

      const result = service.calculateNumberItemsList(200, 100, 200);
      expect(result).toBe(0);
    });
  });
});
