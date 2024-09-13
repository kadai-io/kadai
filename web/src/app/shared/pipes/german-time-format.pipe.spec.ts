/*
 * Copyright [2024] [envite consulting GmbH]
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

import { GermanTimeFormatPipe } from './german-time-format.pipe';

describe('GermanTimeFormatPipe', () => {
  it('create an instance', () => {
    const pipe = new GermanTimeFormatPipe();
    expect(pipe).toBeTruthy();
  });

  // This test currently doesn't work in GitHub CI, but runs on local machine
  // Re-enable test when developing this pipe
  it.skip('should convert ISO time to german time', () => {
    const pipe = new GermanTimeFormatPipe();
    expect(pipe.transform('2021-08-20T09:31:41Z')).toMatch('20.08.2021, 11:31:41');
  });

  it('should return input value when input is string but not a date', () => {
    const pipe = new GermanTimeFormatPipe();
    expect(pipe.transform('totally not a date')).toMatch('totally not a date');
  });
});
