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

import { Injectable } from '@angular/core';
import { Orientation } from 'app/shared/models/orientation';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OrientationService {
  private lock = false;
  private currentOrientation = Orientation.landscape;
  public orientation = new BehaviorSubject<Orientation>(this.currentOrientation);

  private static detectOrientation(): Orientation {
    if (window.innerHeight > window.innerWidth) {
      return Orientation.portrait;
    }
    return Orientation.landscape;
  }

  onResize() {
    const orientation = OrientationService.detectOrientation();
    if (orientation !== this.currentOrientation) {
      this.currentOrientation = orientation;
      if (!this.lock) {
        this.lock = !this.lock;
        setTimeout(() => this.changeOrientation(orientation), 250);
      }
    }
  }

  getOrientation(): Observable<Orientation> {
    return this.orientation.asObservable();
  }

  changeOrientation(orientation: Orientation) {
    this.lock = !this.lock;
    if (this.currentOrientation) {
      this.orientation.next(orientation);
    }
  }

  calculateNumberItemsList(
    heightContainer: number,
    cardHeight: number,
    unusedHeight: number,
    doubleList = false
  ): number {
    let cards = Math.round((heightContainer - unusedHeight) / cardHeight);
    if (doubleList && window.innerWidth < 992) {
      cards = Math.floor(cards / 2);
    }
    return cards;
  }
}
