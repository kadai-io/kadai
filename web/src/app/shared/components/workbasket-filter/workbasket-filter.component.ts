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

import { ChangeDetectionStrategy, Component, effect, inject, input, signal, untracked } from '@angular/core';
import { ALL_TYPES, WorkbasketType } from '../../models/workbasket-type';
import { WorkbasketQueryFilterParameter } from '../../models/workbasket-query-filter-parameter';
import { Store } from '@ngxs/store';
import { ClearWorkbasketFilter, SetWorkbasketFilter } from '../../store/filter-store/filter.actions';
import { FilterSelectors } from '../../store/filter-store/filter.selectors';

import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatTooltip } from '@angular/material/tooltip';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { IconTypeComponent } from '../../../administration/components/type-icon/icon-type.component';
import { MapValuesPipe } from '../../pipes/map-values.pipe';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'kadai-shared-workbasket-filter',
  templateUrl: './workbasket-filter.component.html',
  styleUrls: ['./workbasket-filter.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  filter = signal<WorkbasketQueryFilterParameter>(null);
  private store = inject(Store);

  constructor() {
    effect(() => {
      const comp = this.component();
      let f: WorkbasketQueryFilterParameter;
      if (comp === 'availableDistributionTargets') {
        f = this.availableFilter();
      } else if (comp === 'selectedDistributionTargets') {
        f = this.selectedFilter();
      } else {
        f = this.workbasketListFilter();
      }
      untracked(() => {
        if (f) {
          this.setFilter(f);
        }
      });
    });
  }

  setFilter(filter: WorkbasketQueryFilterParameter) {
    this.filter.set({
      'description-like': [...filter['description-like']],
      'key-like': [...filter['key-like']],
      'name-like': [...filter['name-like']],
      'owner-like': [...filter['owner-like']],
      type: [...filter['type']]
    });
  }

  clear() {
    this.store.dispatch(new ClearWorkbasketFilter(this.component()));
  }

  selectType(type: WorkbasketType) {
    this.filter.update((f) => ({ ...f, type: type !== WorkbasketType.ALL ? [type] : [] }));
  }

  search() {
    this.store.dispatch(new SetWorkbasketFilter(this.filter(), this.component()));
  }
}
