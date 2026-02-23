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
    expect(emittedValues).toEqual([true, false, true]);
  });

  it('multiple subscribers should all receive the emitted value', () => {
    const values: boolean[] = [];
    service.getRequestInProgress().subscribe((v) => values.push(v));
    service.getRequestInProgress().subscribe((v) => values.push(v));
    service.setRequestInProgress(true);
    expect(values).toEqual([true, true]);
  });
});
