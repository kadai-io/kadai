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

import { describe, expect, it } from 'vitest';
import { OrderBy } from './order-by.pipe';

describe('OrderBy', () => {
  let pipe: OrderBy;

  pipe = new OrderBy();

  it('should sort records ascending by a single key', () => {
    const records = [{ name: 'Charlie' }, { name: 'Alice' }, { name: 'Bob' }];
    const result = pipe.transform(records, ['name']);
    expect(result.map((r: any) => r.name)).toEqual(['Alice', 'Bob', 'Charlie']);
  });

  it('should sort records descending when key is prefixed with "-"', () => {
    const records = [{ name: 'Alice' }, { name: 'Charlie' }, { name: 'Bob' }];
    const result = pipe.transform(records, ['-name']);
    expect(result.map((r: any) => r.name)).toEqual(['Charlie', 'Bob', 'Alice']);
  });

  it('should treat missing fields as empty string and sort them first ascending', () => {
    const records = [{ name: 'Bob' }, { name: undefined }, { name: 'Alice' }];
    const result = pipe.transform(records, ['name']);
    expect(result[0].name).toBeUndefined();
    expect(result[1].name).toBe('Alice');
    expect(result[2].name).toBe('Bob');
  });

  it('should return 0 for equal values and preserve relative order implicitly', () => {
    const records = [
      { name: 'Same', id: 1 },
      { name: 'Same', id: 2 }
    ];
    const result = pipe.transform(records, ['name']);
    expect(result).toHaveLength(2);
    result.forEach((r: any) => expect(r.name).toBe('Same'));
  });

  it('should sort by multiple keys, using the second key as tiebreaker', () => {
    const records = [
      { type: 'B', name: 'Zebra' },
      { type: 'A', name: 'Charlie' },
      { type: 'A', name: 'Alice' }
    ];
    const result = pipe.transform(records, ['type', 'name']);
    expect(result[0]).toEqual({ type: 'A', name: 'Alice' });
    expect(result[1]).toEqual({ type: 'A', name: 'Charlie' });
    expect(result[2]).toEqual({ type: 'B', name: 'Zebra' });
  });

  it('should handle mixed ascending and descending sort keys', () => {
    const records = [
      { type: 'A', name: 'Charlie' },
      { type: 'A', name: 'Alice' },
      { type: 'B', name: 'Zebra' }
    ];
    const result = pipe.transform(records, ['type', '-name']);
    expect(result[0]).toEqual({ type: 'A', name: 'Charlie' });
    expect(result[1]).toEqual({ type: 'A', name: 'Alice' });
    expect(result[2]).toEqual({ type: 'B', name: 'Zebra' });
  });

  it('should be case-insensitive when comparing string values', () => {
    const records = [{ name: 'banana' }, { name: 'Apple' }, { name: 'cherry' }];
    const result = pipe.transform(records, ['name']);
    expect(result.map((r: any) => r.name)).toEqual(['Apple', 'banana', 'cherry']);
  });
});
