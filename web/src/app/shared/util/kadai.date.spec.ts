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
import { KadaiDate } from './kadai.date';

describe('KadaiDate', () => {
  it('should return a string from getDate()', () => {
    const result = KadaiDate.getDate();
    expect(typeof result).toBe('string');
    expect(result.length).toBeGreaterThan(0);
    expect(result.endsWith('Z')).toBe(true);
    const currentYear = new Date().getFullYear().toString();
    expect(result.startsWith(currentYear)).toBe(true);
  });

  it('dateFormat constant should be the expected format string', () => {
    expect(KadaiDate.dateFormat).toBe('yyyy-MM-ddTHH:mm:ss.sss');
  });

  it('should return different values on successive calls as time passes', async () => {
    const first = KadaiDate.getDate();
    await new Promise((resolve) => setTimeout(resolve, 1001));
    const second = KadaiDate.getDate();
    expect(first).not.toBe(second);
  });
});
