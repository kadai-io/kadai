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

import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Observable } from 'rxjs';
import { RequestInProgressService } from './request-in-progress.service';

describe('RequestInProgressService', () => {
  let service: RequestInProgressService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RequestInProgressService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getRequestInProgress() should return an Observable', () => {
    const result = service.getRequestInProgress();
    expect(result).toBeInstanceOf(Observable);
  });

  it('should emit initial value (false) upon subscription', () => {
    let emitted: boolean | undefined;
    service.getRequestInProgress().subscribe((value) => {
      emitted = value;
    });
    expect(emitted).toBe(false);
  });

  it('should emit true when setRequestInProgress(true) is called', () => {
    let emitted: boolean | undefined;
    service.getRequestInProgress().subscribe((value) => {
      emitted = value;
    });
    service.setRequestInProgress(true);
    expect(emitted).toBe(true);
  });

  it('should emit false when setRequestInProgress(false) is called', () => {
    let emitted: boolean | undefined;
    service.getRequestInProgress().subscribe((value) => {
      emitted = value;
    });
    service.setRequestInProgress(false);
    expect(emitted).toBe(false);
  });

  it('should emit multiple values sequentially', () => {
    const emittedValues: boolean[] = [];
    service.getRequestInProgress().subscribe((value) => {
      emittedValues.push(value);
    });
    service.setRequestInProgress(true);
    service.setRequestInProgress(false);
    service.setRequestInProgress(true);
    expect(emittedValues).toEqual([false, true, false, true]);
  });

  it('multiple subscribers should all receive current and emitted values', () => {
    const values1: boolean[] = [];
    const values2: boolean[] = [];

    service.getRequestInProgress().subscribe((v) => values1.push(v));
    service.setRequestInProgress(true);
    service.getRequestInProgress().subscribe((v) => values2.push(v));
    service.setRequestInProgress(true);

    expect(values1).toEqual([false, true]);
    expect(values2).toEqual([true]);
  });

  describe('Reference Counting (Overlap Safety)', () => {
    it('should remain true when one of multiple overlapping requests finishes', () => {
      const emittedValues: boolean[] = [];
      service.getRequestInProgress().subscribe((val) => emittedValues.push(val));

      service.setRequestInProgress(true);
      expect(emittedValues[emittedValues.length - 1]).toBe(true);

      service.setRequestInProgress(true);
      expect(emittedValues[emittedValues.length - 1]).toBe(true);

      service.setRequestInProgress(false);
      expect(emittedValues[emittedValues.length - 1]).toBe(true);

      service.setRequestInProgress(false);
      expect(emittedValues[emittedValues.length - 1]).toBe(false);
    });

    it('should not let counter drop below zero if setRequestInProgress(false) is called redundantly', () => {
      const emittedValues: boolean[] = [];
      service.getRequestInProgress().subscribe((v) => emittedValues.push(v));

      service.setRequestInProgress(false);
      service.setRequestInProgress(true);
      service.setRequestInProgress(false);

      expect(emittedValues).toEqual([false, true, false]);
    });
  });
});
