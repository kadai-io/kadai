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
import { MapValuesPipe } from './map-values.pipe';

describe('MapValuesPipe', () => {
  let pipe: MapValuesPipe;

  pipe = new MapValuesPipe();

  it('should return an empty array for null input', () => {
    const result = pipe.transform(null as any);
    expect(result).toEqual([]);
  });

  it('should return an empty array for undefined input', () => {
    const result = pipe.transform(undefined as any);
    expect(result).toEqual([]);
  });

  it('should return an empty array for an empty Map', () => {
    const map = new Map<string, number>();
    const result = pipe.transform(map);
    expect(result).toEqual([]);
  });

  it('should transform a Map with one entry into an array with one object', () => {
    const map = new Map<string, number>([['alpha', 1]]);
    const result = pipe.transform(map);
    expect(result).toHaveLength(1);
    expect(result[0]).toEqual({ key: 'alpha', value: 1 });
  });

  it('should transform a Map with multiple entries into the correct array', () => {
    const map = new Map<string, string>([
      ['first', 'one'],
      ['second', 'two'],
      ['third', 'three']
    ]);
    const result = pipe.transform(map);
    expect(result).toHaveLength(3);
    expect(result).toContainEqual({ key: 'first', value: 'one' });
    expect(result).toContainEqual({ key: 'second', value: 'two' });
    expect(result).toContainEqual({ key: 'third', value: 'three' });
  });

  it('should preserve insertion order of Map entries', () => {
    const map = new Map<number, string>([
      [10, 'ten'],
      [20, 'twenty']
    ]);
    const result = pipe.transform(map);
    expect(result[0]).toEqual({ key: 10, value: 'ten' });
    expect(result[1]).toEqual({ key: 20, value: 'twenty' });
  });
});
