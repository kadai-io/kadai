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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Direction, Sorting } from 'app/shared/models/sorting';

@Component({
  selector: 'kadai-shared-sort',
  templateUrl: './sort.component.html',
  styleUrls: ['./sort.component.scss'],
  standalone: false
})
export class SortComponent<T> implements OnInit {
  @Input() sortingFields: Map<T, string>;
  @Input() menuPosition = 'right';
  @Input() defaultSortBy: T;

  @Output() performSorting = new EventEmitter<Sorting<T>>();

  sort: Sorting<T> = {
    'sort-by': undefined,
    order: Direction.ASC
  };

  // this allows the html template to use the Direction enum.
  sortDirectionEnum = Direction;

  ngOnInit() {
    this.sort['sort-by'] = this.defaultSortBy;
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
