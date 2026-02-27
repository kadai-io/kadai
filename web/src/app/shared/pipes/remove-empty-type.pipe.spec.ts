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
import { RemoveNoneTypePipe } from './remove-empty-type.pipe';

describe('RemoveNoneTypePipe', () => {
  let pipe: RemoveNoneTypePipe;

  pipe = new RemoveNoneTypePipe();

  it('should return an empty array when all entries have an empty key', () => {
    const input = [
      { key: '', value: 'first' },
      { key: '', value: 'second' }
    ];
    const result = pipe.transform(input);
    expect(result).toEqual([]);
  });

  it('should keep entries with a non-empty key', () => {
    const input = [{ key: 'TYPE_A', value: 'someValue' }];
    const result = pipe.transform(input);
    expect(result).toEqual([{ key: 'TYPE_A', value: 'someValue' }]);
  });

  it('should filter out entries with empty key and keep entries with non-empty key', () => {
    const input = [
      { key: '', value: 'empty' },
      { key: 'TYPE_B', value: 'kept' },
      { key: '', value: 'also empty' },
      { key: 'TYPE_C', value: 'also kept' }
    ];
    const result = pipe.transform(input);
    expect(result).toHaveLength(2);
    expect(result).toContainEqual({ key: 'TYPE_B', value: 'kept' });
    expect(result).toContainEqual({ key: 'TYPE_C', value: 'also kept' });
  });

  it('should return an empty array for an empty input array', () => {
    const result = pipe.transform([]);
    expect(result).toEqual([]);
  });

  it('should return all entries when none have an empty key', () => {
    const input = [
      { key: 'A', value: '1' },
      { key: 'B', value: '2' },
      { key: 'C', value: '3' }
    ];
    const result = pipe.transform(input);
    expect(result).toHaveLength(3);
  });
});
