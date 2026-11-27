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

package io.kadai.task.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaskModifiedTimestampsTest {

  private static final Instant PREVIOUS = Instant.parse("2026-01-01T10:00:00.123Z");

  @Test
  void should_ReturnNowTruncatedToMillis_When_NowIsAfterPreviousModified() {
    Instant now = Instant.parse("2026-01-01T10:00:01.987654321Z");

    Instant next = TaskModifiedTimestamps.next(now, PREVIOUS);

    assertThat(next).isEqualTo(Instant.parse("2026-01-01T10:00:01.987Z"));
  }

  @Test
  void should_ReturnNowTruncatedToMillis_When_ThereIsNoPreviousModified() {
    Instant now = Instant.parse("2026-01-01T10:00:01.987654321Z");

    Instant next = TaskModifiedTimestamps.next(now, (Instant) null);

    assertThat(next).isEqualTo(Instant.parse("2026-01-01T10:00:01.987Z"));
  }

  @Test
  void should_ReturnOneMilliAfterPreviousModified_When_NowEqualsPreviousModified() {
    Instant next = TaskModifiedTimestamps.next(PREVIOUS, PREVIOUS);

    assertThat(next).isEqualTo(PREVIOUS.plus(1, ChronoUnit.MILLIS));
  }

  @Test
  void should_ReturnOneMilliAfterPreviousModified_When_NowIsWithinTheSameMilli() {
    Instant now = PREVIOUS.plusNanos(999_999);

    Instant next = TaskModifiedTimestamps.next(now, PREVIOUS);

    assertThat(next).isEqualTo(PREVIOUS.plus(1, ChronoUnit.MILLIS));
  }

  @Test
  void should_ReturnOneMilliAfterPreviousModified_When_PreviousModifiedHasSubMilliPrecision() {
    Instant previous = PREVIOUS.plus(456, ChronoUnit.MICROS);

    Instant next = TaskModifiedTimestamps.next(previous, previous);

    assertThat(next).isEqualTo(PREVIOUS.plus(1, ChronoUnit.MILLIS));
  }

  @Test
  void should_ReturnOneMilliAfterPreviousModified_When_ClockIsBehindPreviousModified() {
    Instant now = PREVIOUS.minus(5, ChronoUnit.SECONDS);

    Instant next = TaskModifiedTimestamps.next(now, PREVIOUS);

    assertThat(next).isEqualTo(PREVIOUS.plus(1, ChronoUnit.MILLIS));
  }

  @Test
  void should_ReturnTimestampAfterAllPreviousModified_When_MultipleAreGiven() {
    Instant latest = PREVIOUS.plus(10, ChronoUnit.MILLIS);
    List<Instant> previous = Arrays.asList(PREVIOUS, null, latest);

    Instant next = TaskModifiedTimestamps.next(PREVIOUS, previous);

    assertThat(next).isEqualTo(latest.plus(1, ChronoUnit.MILLIS));
  }

  @Test
  void should_ReturnNow_When_NoneOfMultiplePreviousModifiedIsAfterNow() {
    Instant now = Instant.parse("2026-01-01T10:00:01.987Z");

    Instant next = TaskModifiedTimestamps.next(now, List.of(PREVIOUS));

    assertThat(next).isEqualTo(now);
  }

  @Test
  void should_ReturnUpperBoundOfMilli_When_ComputingExclusiveUpperBoundOfModified() {
    Instant modified = PREVIOUS.plus(456, ChronoUnit.MICROS);

    assertThat(TaskModifiedTimestamps.lowerBound(modified)).isEqualTo(PREVIOUS);
    assertThat(TaskModifiedTimestamps.upperBound(modified))
        .isEqualTo(PREVIOUS.plus(1, ChronoUnit.MILLIS));
  }
}
