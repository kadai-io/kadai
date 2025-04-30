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

import { DatePipe } from '@angular/common';

export class KadaiDate {
  public static dateFormat = 'yyyy-MM-ddTHH:mm:ss.sss';

  public static getDate(): string {
    const dateLocale = 'en-US';
    const datePipe = new DatePipe(dateLocale);

    return `${datePipe.transform(Date.now(), this.dateFormat)}Z`;
  }

  public static convertSimpleDate(date: Date): string {
    const dateFormat = 'yyyy-MM-dd';
    const dateLocale = 'en-US';
    const datePipe = new DatePipe(dateLocale);
    return datePipe.transform(date, dateFormat);
  }

  public static getDateToDisplay(date: string, dateFormat: string = this.dateFormat): string {
    return this.applyTimeZone(date, dateFormat);
  }

  public static applyTimeZone(date: string, dateFormat): string | null {
    const dateLocale = 'en-US';
    const datePipe = new DatePipe(dateLocale);

    return datePipe.transform(date, dateFormat, Intl.DateTimeFormat().resolvedOptions().timeZone);
  }
}
