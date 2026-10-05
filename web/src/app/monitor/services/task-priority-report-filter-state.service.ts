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
 */

import { inject, Injectable, computed, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Store } from '@ngxs/store';
import { SettingsSelectors } from '../../shared/store/settings-store/settings.selectors';

export interface FilterConfig {
  [filterKey: string]: Record<string, string[]>;
}

export interface TableRowData {
  priority: string;
  number: number;
}

export const SETTING_MEMBER_FILTER = 'filter';

@Injectable({ providedIn: 'root' })
export class TaskPriorityReportFilterStateService {
  private readonly store = inject(Store);

  readonly settings = toSignal(this.store.select(SettingsSelectors.getSettings));
  readonly activeFilters = signal<string[]>([]);
  readonly workbasketKey = signal<string | undefined>(undefined);

  readonly parsedFilterConfig = computed<{ config: FilterConfig; isValid: boolean }>(() => {
    const rawSettings = this.settings() as Record<string, unknown> | undefined;
    const rawFilter = rawSettings?.[SETTING_MEMBER_FILTER];

    if (!rawFilter || typeof rawFilter !== 'string' || !rawFilter.trim()) {
      return { config: {}, isValid: false };
    }

    try {
      const parsed = JSON.parse(rawFilter) as FilterConfig;
      return { config: parsed, isValid: true };
    } catch {
      return { config: {}, isValid: false };
    }
  });

  readonly filterKeys = computed(() => Object.keys(this.parsedFilterConfig().config));
  readonly filtersAreSpecified = computed(() => this.parsedFilterConfig().isValid && this.filterKeys().length > 0);

  readonly activeQuery = computed(() => {
    const config = this.parsedFilterConfig().config;
    const active = this.activeFilters();
    const query: Record<string, string[]> = {};

    active.forEach((key) => {
      const filterGroup = config[key];
      if (!filterGroup) return;

      Object.entries(filterGroup).forEach(([field, values]) => {
        query[field] = query[field] ? [...query[field], ...values] : [...values];
      });
    });

    return query;
  });

  toggleFilter(key: string, isEnabled: boolean): void {
    const current = this.activeFilters();
    const next = isEnabled ? [...current, key] : current.filter((k) => k !== key);
    this.activeFilters.set(next);
  }
}
