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

package io.kadai.testapi.benchmark;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Locale;

/** Output of the benchmarks, which goes nowhere until a runner directs it to a stream. */
public final class BenchmarkLog {

  private static volatile PrintStream out = new PrintStream(OutputStream.nullOutputStream());

  private BenchmarkLog() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Directs the output of the benchmarks to a stream.
   *
   * @param stream the stream to print to, usually the console
   */
  public static void printTo(PrintStream stream) {
    out = stream;
  }

  /**
   * Prints a line marked as benchmark output.
   *
   * @param format the {@link String#format(String, Object...)} format, without a line break
   * @param args the arguments of the format
   */
  public static void print(String format, Object... args) {
    out.printf(Locale.ROOT, "[benchmark] " + format + "%n", args);
  }

  /**
   * Formats a duration for humans.
   *
   * @param duration the duration to format
   * @return seconds with a decimal below a minute, hours, minutes and seconds above
   */
  public static String format(Duration duration) {
    long seconds = duration.toSeconds();
    if (seconds < 60) {
      return String.format(Locale.ROOT, "%.1fs", seconds(duration));
    }
    return String.format(
        Locale.ROOT, "%dh %02dm %02ds", seconds / 3600, seconds % 3600 / 60, seconds % 60);
  }

  /**
   * Converts a duration to seconds.
   *
   * @param duration the duration to convert
   * @return the duration in seconds, with fractions
   */
  public static double seconds(Duration duration) {
    return duration.toNanos() / 1e9;
  }
}
