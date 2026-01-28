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

import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { Sorting, WORKBASKET_SORT_PARAMETER_NAMING, WorkbasketQuerySortParameter } from 'app/shared/models/sorting';
import { WorkbasketSummary } from 'app/shared/models/workbasket-summary';
import { KadaiType } from 'app/shared/models/kadai-type';
import { expandDown } from 'app/shared/animations/expand.animation';
import { Store } from '@ngxs/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ACTION } from '../../../shared/models/action';
import { CreateWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import { WorkbasketSelectors } from '../../../shared/store/workbasket-store/workbasket.selectors';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';

import { ImportExportComponent } from '../import-export/import-export.component';
import { SortComponent } from '../../../shared/components/sort/sort.component';
import { WorkbasketFilterComponent } from '../../../shared/components/workbasket-filter/workbasket-filter.component';

@Component({
  selector: 'kadai-administration-workbasket-list-toolbar',
  animations: [expandDown],
  templateUrl: './workbasket-list-toolbar.component.html',
  styleUrls: ['./workbasket-list-toolbar.component.scss'],
  imports: [MatButton, MatTooltip, MatIcon, ImportExportComponent, SortComponent, WorkbasketFilterComponent]
})
export class WorkbasketListToolbarComponent implements OnInit {
  @Input() workbasketListExpanded: boolean = true;
  @Input() workbaskets: WorkbasketSummary[];
  @Input() workbasketDefaultSortBy: WorkbasketQuerySortParameter;
  @Output() performSorting = new EventEmitter<Sorting<WorkbasketQuerySortParameter>>();
  selectionToImport = KadaiType.WORKBASKETS;
  sortingFields: Map<WorkbasketQuerySortParameter, string> = WORKBASKET_SORT_PARAMETER_NAMING;
  isExpanded = false;
  showFilter = false;
  workbasketActiveAction$: Observable<ACTION> = inject(Store).select(WorkbasketSelectors.workbasketActiveAction);
  destroy$ = new Subject<void>();
  action: ACTION;
  private store = inject(Store);
  private workbasketService = inject(WorkbasketService);

  ngOnInit() {
    this.workbasketActiveAction$.pipe(takeUntil(this.destroy$)).subscribe((action) => {
      this.action = action;
    });
  }

  sorting(sort: Sorting<WorkbasketQuerySortParameter>) {
    this.performSorting.emit(sort);
  }

  addWorkbasket() {
    if (this.action !== ACTION.CREATE) {
      this.store.dispatch(new CreateWorkbasket());
    }
  }

  onClickFilter() {
    this.isExpanded = !this.isExpanded;
    this.workbasketService.expandWorkbasketActionToolbar(this.isExpanded);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
