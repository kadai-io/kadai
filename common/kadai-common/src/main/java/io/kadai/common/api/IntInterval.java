/*
 * Copyright [2025] [envite consulting GmbH]
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

package io.kadai.common.api;

import io.kadai.common.internal.Interval;

/**
 * IntInterval captures an Integer interval. A fixed interval has defined begin and end. An open
 * ended interval has either begin == null or end ==null.
 */
public class IntInterval extends Interval<Integer> {

  public IntInterval(Integer begin, Integer end) {
    super(begin, end);
  }
}
