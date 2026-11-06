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

import { Component, inject, input, linkedSignal } from '@angular/core';
import { ALL_TYPES, WorkbasketType } from 'app/shared/models/workbasket-type';
import { WorkbasketQueryFilterParameter } from 'app/shared/models/workbasket-query-filter-parameter';
import { Store } from '@ngxs/store';
import { ClearWorkbasketFilter, SetWorkbasketFilter } from 'app/shared/store/filter-store/filter.actions';
import { FilterSelectors } from 'app/shared/store/filter-store/filter.selectors';

import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatTooltip } from '@angular/material/tooltip';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { IconTypeComponent } from 'app/administration/components/type-icon/icon-type.component';
import { MapValuesPipe } from 'app/shared/pipes/map-values.pipe';
import { toSignal } from '@angular/core/rxjs-interop';

type NormalizedWorkbasketFilter = Required<
  Pick<WorkbasketQueryFilterParameter, 'description-like' | 'key-like' | 'name-like' | 'owner-like' | 'type'>
>;

@Component({
  selector: 'kadai-shared-workbasket-filter',
  templateUrl: './workbasket-filter.component.html',
  styleUrls: ['./workbasket-filter.component.scss'],
  imports: [
    MatFormField,
    MatLabel,
    MatInput,
    FormsModule,
    MatTooltip,
    MatButton,
    MatIcon,
    MatMenuTrigger,
    IconTypeComponent,
    MatMenu,
    MatMenuItem,
    MapValuesPipe
  ]
})
export class WorkbasketFilterComponent {
  allTypes: Map<WorkbasketType, string> = ALL_TYPES;
  component = input<string>();
  isExpanded = input<boolean>();
  private availableFilter = toSignal(inject(Store).select(FilterSelectors.getAvailableDistributionTargetsFilter), {
    requireSync: true
  });
  private selectedFilter = toSignal(inject(Store).select(FilterSelectors.getSelectedDistributionTargetsFilter), {
    requireSync: true
  });
  private workbasketListFilter = toSignal(inject(Store).select(FilterSelectors.getWorkbasketListFilter), {
    requireSync: true
  });
  private store = inject(Store);

  filter = linkedSignal<WorkbasketQueryFilterParameter | undefined, NormalizedWorkbasketFilter>({
    source: () => {
      const comp = this.component();
      if (comp === 'availableDistributionTargets') return this.availableFilter();
      if (comp === 'selectedDistributionTargets') return this.selectedFilter();
      return this.workbasketListFilter();
    },
    computation: (f, previous) => (f ? normalizeFilter(f) : (previous?.value ?? normalizeFilter({})))
  });

  setFilter(filter: WorkbasketQueryFilterParameter) {
    this.filter.set(normalizeFilter(filter));
  }

  clear() {
    this.store.dispatch(new ClearWorkbasketFilter(this.component()!));
  }

  selectType(type: WorkbasketType) {
    this.filter.update((f) => ({ ...f, type: type !== WorkbasketType.ALL ? [type] : [] }));
  }

  search() {
    this.store.dispatch(new SetWorkbasketFilter(this.filter(), this.component()!));
  }
}

function normalizeFilter(filter: WorkbasketQueryFilterParameter): NormalizedWorkbasketFilter {
  return {
    'description-like': [...(filter['description-like'] ?? [])],
    'key-like': [...(filter['key-like'] ?? [])],
    'name-like': [...(filter['name-like'] ?? [])],
    'owner-like': [...(filter['owner-like'] ?? [])],
    type: [...(filter['type'] ?? [])]
  };
}
