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
import { ImportExportService } from './import-export.service';

describe('ImportExportService', () => {
  let service: ImportExportService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ImportExportService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getImportingFinished() should return an Observable', () => {
    const result = service.getImportingFinished();
    expect(result).toBeInstanceOf(Observable);
  });

  it('should emit true when setImportingFinished(true) is called', () => {
    let emitted: boolean | undefined;
    service.getImportingFinished().subscribe((value) => {
      emitted = value;
    });
    service.setImportingFinished(true);
    expect(emitted).toBe(true);
  });

  it('should emit false when setImportingFinished(false) is called', () => {
    let emitted: boolean | undefined;
    service.getImportingFinished().subscribe((value) => {
      emitted = value;
    });
    service.setImportingFinished(false);
    expect(emitted).toBe(false);
  });

  it('should emit multiple values sequentially', () => {
    const emittedValues: boolean[] = [];
    service.getImportingFinished().subscribe((value) => {
      emittedValues.push(value);
    });
    service.setImportingFinished(true);
    service.setImportingFinished(false);
    service.setImportingFinished(true);
    expect(emittedValues).toEqual([true, false, true]);
  });

  it('multiple subscribers should all receive the emitted value', () => {
    const values: boolean[] = [];
    service.getImportingFinished().subscribe((v) => values.push(v));
    service.getImportingFinished().subscribe((v) => values.push(v));
    service.setImportingFinished(false);
    expect(values).toEqual([false, false]);
  });
});
