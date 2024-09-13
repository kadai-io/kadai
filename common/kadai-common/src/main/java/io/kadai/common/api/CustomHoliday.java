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

package io.kadai.common.api;

import java.util.Objects;

public final class CustomHoliday {

  private final Integer day;
  private final Integer month;

  public CustomHoliday(Integer day, Integer month) {
    this.day = day;
    this.month = month;
  }

  public static CustomHoliday of(Integer day, Integer month) {
    return new CustomHoliday(day, month);
  }

  public Integer getDay() {
    return day;
  }

  public Integer getMonth() {
    return month;
  }

  @Override
  public int hashCode() {
    return Objects.hash(day, month);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CustomHoliday)) {
      return false;
    }
    CustomHoliday other = (CustomHoliday) obj;
    return Objects.equals(day, other.day) && Objects.equals(month, other.month);
  }

  @Override
  public String toString() {
    return "CustomHoliday [day=" + day + ", month=" + month + "]";
  }
}
