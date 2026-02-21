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

import { ChangeDetectionStrategy, Component, input, OnInit, output } from '@angular/core';
import { Direction, Sorting } from 'app/shared/models/sorting';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { MatIcon } from '@angular/material/icon';

import { MapValuesPipe } from '../../pipes/map-values.pipe';

@Component({
  selector: 'kadai-shared-sort',
  templateUrl: './sort.component.html',
  styleUrls: ['./sort.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButton, MatTooltip, MatMenuTrigger, MatIcon, MatMenu, MatMenuItem, MapValuesPipe]
})
export class SortComponent<T> implements OnInit {
  sortingFields = input.required<Map<T, string>>();
  menuPosition = input('right');
  defaultSortBy = input<T>();

  performSorting = output<Sorting<T>>();

  sort: Sorting<T> = {
    'sort-by': undefined,
    order: Direction.ASC
  };

  // this allows the html template to use the Direction enum.
  sortDirectionEnum = Direction;

  ngOnInit() {
    this.sort['sort-by'] = this.defaultSortBy();
  }

  changeOrder(sortDirection: Direction) {
    this.sort.order = sortDirection;
    this.search();
  }

  changeSortBy(sortBy: T) {
    this.sort['sort-by'] = sortBy;
    this.search();
  }

  private search() {
    this.performSorting.emit(this.sort);
  }
}
