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
    const rawFilter = this.settings()?.[SETTING_MEMBER_FILTER];

    if (typeof rawFilter !== 'string' || !rawFilter.trim()) {
      return { config: {}, isValid: false };
    }

    const parsed = this.safeJsonParse(rawFilter);
    if (this.isValidFilterConfig(parsed)) {
      return { config: parsed, isValid: true };
    }

    return { config: {}, isValid: false };
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
        const existing = query[field] ?? [];
        query[field] = Array.from(new Set([...existing, ...values]));
      });
    });

    return query;
  });

  toggleFilter(key: string, isEnabled: boolean): void {
    this.activeFilters.update((filters) => {
      if (isEnabled) {
        return filters.includes(key) ? filters : [...filters, key];
      }
      return filters.filter((f) => f !== key);
    });
  }

  private safeJsonParse(jsonString: string): unknown {
    try {
      return JSON.parse(jsonString);
    } catch {
      return null;
    }
  }

  private isValidFilterConfig(value: unknown): value is FilterConfig {
    if (typeof value !== 'object' || value === null || Array.isArray(value)) {
      return false;
    }

    return Object.values(value).every(
      (group) =>
        typeof group === 'object' &&
        group !== null &&
        !Array.isArray(group) &&
        Object.values(group).every((field) => Array.isArray(field) && field.every((item) => typeof item === 'string'))
    );
  }
}
