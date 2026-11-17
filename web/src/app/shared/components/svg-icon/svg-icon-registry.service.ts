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

import { DOCUMENT, inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';

/** Loads SVG files by URL and caches the parsed element (replaces angular-svg-icon's registry). */
@Injectable({ providedIn: 'root' })
export class SvgIconRegistryService {
  private readonly http = inject(HttpClient);
  private readonly document = inject(DOCUMENT);
  private readonly icons = new Map<string, Observable<SVGElement | null>>();

  loadSvg(url: string): Observable<SVGElement | null> {
    let icon = this.icons.get(url);
    if (!icon) {
      icon = this.http.get(url, { responseType: 'text' }).pipe(
        map((text) => {
          const div = this.document.createElement('div');
          div.innerHTML = text;
          return div.querySelector('svg');
        }),
        shareReplay(1)
      );
      this.icons.set(url, icon);
    }
    return icon;
  }
}
