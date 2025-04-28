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

package io.kadai.common.internal.configuration.parser;

import io.kadai.common.api.LocalTimeInterval;
import io.kadai.common.api.exceptions.SystemException;
import java.time.LocalTime;
import java.util.List;

public class LocalTimeIntervalParser extends SimpleParser<LocalTimeInterval> {
  public LocalTimeIntervalParser() {
    super(LocalTimeInterval.class, LocalTimeIntervalParser::parse);
  }

  private static LocalTimeInterval parse(String value) {
    List<String> startAndEnd = splitStringAndTrimElements(value, "-");
    if (startAndEnd.size() != 2) {
      throw new SystemException(
          String.format("Cannot convert '%s' to '%s'", value, LocalTimeInterval.class));
    }
    LocalTime start = LocalTime.parse(startAndEnd.get(0));
    LocalTime end = LocalTime.parse(startAndEnd.get(1));
    if (end.equals(LocalTime.MIN)) {
      end = LocalTime.MAX;
    }
    return new LocalTimeInterval(start, end);
  }
}
