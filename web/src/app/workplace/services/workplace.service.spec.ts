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
import { WorkplaceService } from './workplace.service';
import { Workbasket } from '../../shared/models/workbasket';

describe('WorkplaceService', () => {
  let service: WorkplaceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WorkplaceService]
    });
    service = TestBed.inject(WorkplaceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getSelectedWorkbasket', () => {
    it('should return an observable', () => {
      const obs = service.getSelectedWorkbasket();
      expect(obs).toBeDefined();
      expect(typeof obs.subscribe).toBe('function');
    });

    it('should initially emit undefined as BehaviorSubject default', () => {
      let emitted: Workbasket = null;
      service.getSelectedWorkbasket().subscribe((wb) => (emitted = wb));
      expect(emitted).toBeUndefined();
    });
  });

  describe('selectWorkbasket', () => {
    it('should emit the selected workbasket', () => {
      const workbasket: Workbasket = { workbasketId: 'wb-1', name: 'Test WB', key: 'KEY1' };
      let emitted: Workbasket;
      service.getSelectedWorkbasket().subscribe((wb) => (emitted = wb));

      service.selectWorkbasket(workbasket);

      expect(emitted).toBe(workbasket);
    });

    it('should emit undefined when called without argument', () => {
      const workbasket: Workbasket = { workbasketId: 'wb-1', name: 'Test WB', key: 'KEY1' };
      service.selectWorkbasket(workbasket);

      let emitted: Workbasket = { workbasketId: 'some' } as Workbasket;
      service.getSelectedWorkbasket().subscribe((wb) => (emitted = wb));

      service.selectWorkbasket();

      expect(emitted).toBeUndefined();
    });

    it('should update the value for new subscribers as BehaviorSubject', () => {
      const workbasket: Workbasket = { workbasketId: 'wb-2', name: 'Another WB', key: 'KEY2' };
      service.selectWorkbasket(workbasket);

      let emitted: Workbasket;
      service.getSelectedWorkbasket().subscribe((wb) => (emitted = wb));

      expect(emitted).toBe(workbasket);
    });

    it('should emit the last selected workbasket when a new subscriber subscribes', () => {
      const wb1: Workbasket = { workbasketId: 'wb-1', key: 'K1' };
      const wb2: Workbasket = { workbasketId: 'wb-2', key: 'K2' };

      service.selectWorkbasket(wb1);
      service.selectWorkbasket(wb2);

      let emitted: Workbasket;
      service.getSelectedWorkbasket().subscribe((wb) => (emitted = wb));

      expect(emitted).toBe(wb2);
    });
  });
});
