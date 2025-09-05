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

import { Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { ALL_TYPES, WorkbasketType } from '../../models/workbasket-type';
import { WorkbasketQueryFilterParameter } from '../../models/workbasket-query-filter-parameter';
import { Store } from '@ngxs/store';
import { ClearWorkbasketFilter, SetWorkbasketFilter } from '../../store/filter-store/filter.actions';
import { FilterSelectors } from '../../store/filter-store/filter.selectors';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatTooltip } from '@angular/material/tooltip';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { IconTypeComponent } from '../../../administration/components/type-icon/icon-type.component';
import { MapValuesPipe } from '../../pipes/map-values.pipe';

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
export class WorkbasketFilterComponent implements OnInit, OnDestroy {
  allTypes: Map<WorkbasketType, string> = ALL_TYPES;
  @Input() component: string;
  @Input() isExpanded: boolean;
  availableDistributionTargetsFilter$: Observable<WorkbasketQueryFilterParameter> = inject(Store).select(
    FilterSelectors.getAvailableDistributionTargetsFilter
  );
  selectedDistributionTargetsFilter$: Observable<WorkbasketQueryFilterParameter> = inject(Store).select(
    FilterSelectors.getSelectedDistributionTargetsFilter
  );
  workbasketListFilter$: Observable<WorkbasketQueryFilterParameter> = inject(Store).select(
    FilterSelectors.getWorkbasketListFilter
  );
  destroy$ = new Subject<void>();
  filter: WorkbasketQueryFilterParameter;
  private store = inject(Store);

  ngOnInit(): void {
    if (this.component === 'availableDistributionTargets') {
      this.availableDistributionTargetsFilter$.pipe(takeUntil(this.destroy$)).subscribe((filter) => {
        this.setFilter(filter);
      });
    } else if (this.component === 'selectedDistributionTargets') {
      this.selectedDistributionTargetsFilter$.pipe(takeUntil(this.destroy$)).subscribe((filter) => {
        this.setFilter(filter);
      });
    } else if (this.component === 'workbasketList') {
      this.workbasketListFilter$.pipe(takeUntil(this.destroy$)).subscribe((filter) => {
        this.setFilter(filter);
      });
    }
  }

  setFilter(filter: WorkbasketQueryFilterParameter) {
    this.filter = {
      'description-like': [...filter['description-like']],
      'key-like': [...filter['key-like']],
      'name-like': [...filter['name-like']],
      'owner-like': [...filter['owner-like']],
      type: [...filter['type']]
    };
  }

  clear() {
    this.store.dispatch(new ClearWorkbasketFilter(this.component));
  }

  selectType(type: WorkbasketType) {
    this.filter.type = type !== WorkbasketType.ALL ? [type] : [];
  }

  search() {
    this.store.dispatch(new SetWorkbasketFilter(this.filter, this.component));
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
