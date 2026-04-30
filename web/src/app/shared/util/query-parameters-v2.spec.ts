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

import { asUrlQueryString } from './query-parameters-v2';
import { describe, expect, it } from 'vitest';

describe('asUrlQueryString', () => {
  it('should create a empty query', () => {
    expect(asUrlQueryString({})).toBe('');
  });

  it('should create a query string with one argument', () => {
    expect(asUrlQueryString({ foo: 'bar' })).toBe('?foo=bar');
  });

  it('should create a query string with multiple argument', () => {
    expect(asUrlQueryString({ foo1: 'bar1', foo2: 'bar2' })).toBe('?foo1=bar1&foo2=bar2');
  });

  it('should expand any array argument', () => {
    expect(asUrlQueryString({ foo: ['bar1', 'bar2'] })).toBe('?foo=bar1&foo=bar2');
  });

  it('should skip undefined values', () => {
    expect(asUrlQueryString({ foo: 'bar', foo1: undefined })).toBe('?foo=bar');
  });

  it('should skip undefined values in array', () => {
    expect(asUrlQueryString({ foo: ['bar1', undefined, 'bar2'] })).toBe('?foo=bar1&foo=bar2');
  });
});
