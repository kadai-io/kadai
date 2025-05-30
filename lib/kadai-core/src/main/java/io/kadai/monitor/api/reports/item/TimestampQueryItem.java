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

package io.kadai.monitor.api.reports.item;

import io.kadai.monitor.api.TaskTimestamp;
import io.kadai.monitor.api.reports.TimestampReport;

/** The TimestampQueryItem represents the content of a cell in the {@linkplain TimestampReport}. */
public class TimestampQueryItem implements AgeQueryItem {

  private static final String N_A = "N/A";
  private int count;
  private TaskTimestamp status;
  private int ageInDays;
  private String orgLevel1;
  private String orgLevel2;
  private String orgLevel3;
  private String orgLevel4;

  @Override
  public String getKey() {
    return status.name();
  }

  @Override
  public int getValue() {
    return count;
  }

  @Override
  public int getAgeInDays() {
    return ageInDays;
  }

  @Override
  public void setAgeInDays(int ageInDays) {
    this.ageInDays = ageInDays;
  }

  public String getOrgLevel1() {
    return orgLevel1 == null || orgLevel1.isEmpty() ? N_A : orgLevel1;
  }

  public String getOrgLevel2() {
    return orgLevel2 == null || orgLevel2.isEmpty() ? N_A : orgLevel2;
  }

  public String getOrgLevel3() {
    return orgLevel3 == null || orgLevel3.isEmpty() ? N_A : orgLevel3;
  }

  public String getOrgLevel4() {
    return orgLevel4 == null || orgLevel4.isEmpty() ? N_A : orgLevel4;
  }
}
