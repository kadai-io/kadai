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

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'spreadNumber' })
export class SpreadNumberPipe implements PipeTransform {
  transform(maxPageNumber: number, currentIndex: number, maxArrayElements: number): number[] {
    const returnArray = [];
    if (maxPageNumber <= 5) {
      for (let i = 0; i < maxPageNumber; i += 1) {
        returnArray.push(i);
      }
      return returnArray;
    }

    let minArrayValue = currentIndex - maxArrayElements / 2;
    let maxArrayValue = currentIndex + maxArrayElements / 2;
    let leftDifference = 0;
    let rightDifference = 0;

    if (minArrayValue < 0) {
      leftDifference = Math.abs(minArrayValue);
      minArrayValue = 0;
    }
    if (maxArrayValue > maxPageNumber) {
      rightDifference = maxArrayValue - maxPageNumber;
      maxArrayValue = maxPageNumber;
    }
    const minIndex = minArrayValue - rightDifference <= 0 ? 0 : minArrayValue - rightDifference;
    const maxIndex = maxArrayValue + leftDifference > maxPageNumber ? maxPageNumber : maxArrayValue + leftDifference;

    for (let i = minIndex; i < maxIndex; i += 1) {
      returnArray.push(i);
    }
    return returnArray;
  }
}
