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

import { Component, inject, input, OnInit, signal } from '@angular/core';
import { Task } from 'app/workplace/models/task';
import { Workbasket } from 'app/shared/models/workbasket';
import { WorkbasketService } from 'app/shared/services/workbasket/workbasket.service';
import { Sorting, TASK_SORT_PARAMETER_NAMING, TaskQuerySortParameter } from 'app/shared/models/sorting';
import { expandDown } from 'app/shared/animations/expand.animation';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskQueryFilterParameter } from '../../../shared/models/task-query-filter-parameter';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { KadaiEngineService } from '../../../shared/services/kadai-engine/kadai-engine.service';
import { Actions, ofActionCompleted, Store } from '@ngxs/store';
import { ClearTaskFilter, SetTaskFilter } from '../../../shared/store/filter-store/filter.actions';
import { WorkplaceSelectors } from '../../../shared/store/workplace-store/workplace.selectors';
import { SetFilterExpansion } from '../../../shared/store/workplace-store/workplace.actions';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { AsyncPipe } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatAutocomplete, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { MatOption } from '@angular/material/core';
import { TaskFilterComponent } from '../../../shared/components/task-filter/task-filter.component';
import { SortComponent } from '../../../shared/components/sort/sort.component';
import { SearchType } from '../../models/search';
import { TaskSelectors } from '../../../shared/store/task-store/task.selectors';
import {
  SelectTask,
  SelectWorkbasket,
  SetPage,
  SetSearchType,
  SetSort
} from '../../../shared/store/task-store/task.actions';

@Component({
  selector: 'kadai-task-list-toolbar',
  animations: [expandDown],
  templateUrl: './task-list-toolbar.component.html',
  styleUrls: ['./task-list-toolbar.component.scss'],
  imports: [
    MatTabGroup,
    MatTab,
    MatButton,
    MatTooltip,
    MatIcon,
    MatFormField,
    MatLabel,
    MatInput,
    FormsModule,
    MatAutocompleteTrigger,
    MatAutocomplete,
    MatOption,
    AsyncPipe,
    TaskFilterComponent,
    SortComponent
  ]
})
export class TaskListToolbarComponent implements OnInit {
  taskDefaultSortBy = input<TaskQuerySortParameter>();
  sortingFields: Map<TaskQuerySortParameter, string> = TASK_SORT_PARAMETER_NAMING;
  tasks: Task[] = [];
  workbasketNames: string[] = [];
  filteredWorkbasketNames = signal<string[]>([]);
  resultName = signal('');
  resultId = signal('');
  workbaskets = signal<Workbasket[] | undefined>(undefined);
  currentBasket = signal<Workbasket | undefined>(undefined);
  workbasketSelected = signal(false);
  searched = signal(false);
  search = SearchType;
  searchSelected: SearchType = SearchType.byWorkbasket;
  activeTab = signal(0);
  filterInput = signal('');
  isFilterExpanded$: Observable<boolean> = inject(Store).select(WorkplaceSelectors.getFilterExpansion);
  destroy$ = new Subject<void>();
  private kadaiEngineService = inject(KadaiEngineService);
  private workbasketService = inject(WorkbasketService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private store = inject(Store);
  private ngxsActions$ = inject(Actions);
  private requestInProgressService = inject(RequestInProgressService);

  ngOnInit() {
    this.ngxsActions$.pipe(ofActionCompleted(ClearTaskFilter), takeUntil(this.destroy$)).subscribe(() => {
      this.filterInput.set('');
    });

    this.workbasketService
      .getAllWorkBaskets()
      .pipe(takeUntil(this.destroy$))
      .subscribe((workbaskets) => {
        this.workbaskets.set(workbaskets.workbaskets);
        this.workbaskets()?.forEach((workbasket) => {
          if (workbasket.name) {
            this.workbasketNames.push(workbasket.name);
          }
        });
        this.filteredWorkbasketNames.set([...this.workbasketNames]);

        // get workbasket of current user
        const user = this.kadaiEngineService.currentUserInfo;
        const filteredWorkbasketsByUser = (this.workbaskets() ?? []).filter(
          (workbasket) => workbasket.key == user.userId || workbasket.key == user.userId.toUpperCase()
        );
        if (filteredWorkbasketsByUser.length > 0) {
          const workbasketOfUser = filteredWorkbasketsByUser[0];
          this.resultName.set(workbasketOfUser.name ?? '');
          this.resultId.set(workbasketOfUser.workbasketId ?? '');
          this.store.dispatch(new SelectWorkbasket(workbasketOfUser));
          this.currentBasket.set(workbasketOfUser);
          this.workbasketSelected.set(true);
          this.searched.set(true);
        }
      });

    this.store
      .select(TaskSelectors.getSelectedTask)
      .pipe(takeUntil(this.destroy$))
      .subscribe((task) => {
        if (typeof task !== 'undefined') {
          const workbasketSummary = task.workbasketSummary;
          if (
            workbasketSummary &&
            this.searchSelected === this.search.byWorkbasket &&
            this.resultName() !== workbasketSummary.name
          ) {
            this.resultName.set(workbasketSummary.name ?? '');
            this.resultId.set(workbasketSummary.workbasketId ?? '');
            this.currentBasket.set(workbasketSummary);
            this.store.dispatch(new SelectWorkbasket(this.currentBasket()));
            this.workbasketSelected.set(true);
          }
        }
      });

    this.route.queryParams.subscribe((params) => {
      const component = params.component;
      if (component == 'workbaskets') {
        this.activeTab.set(0);
        const basket = this.currentBasket();
        if (basket) {
          this.resultName.set(basket.name ?? '');
          this.resultId.set(basket.workbasketId ?? '');
        }
        this.selectSearch(this.search.byWorkbasket);
      }
      if (component == 'task-search') {
        this.activeTab.set(1);
        this.searched.set(true);
        this.selectSearch(this.search.byTypeAndValue);
      }
    });

    if (this.router.url.includes('taskdetail')) {
      this.searched.set(true);
    }
  }

  setFilterExpansion() {
    this.store.dispatch(new SetFilterExpansion());
  }

  onTabChange(search: any) {
    const tab = search.target.innerText;
    this.requestInProgressService.setRequestInProgress(true);
    if (tab === 'Workbaskets') {
      this.router.navigate(['kadai/workplace'], { queryParams: { component: 'workbaskets' } });
    }
    if (tab === 'Task search') {
      this.router.navigate(['kadai/workplace'], { queryParams: { component: 'task-search' } });
    }
  }

  updateState() {
    const wildcardFilter: TaskQueryFilterParameter = {
      'wildcard-search-value': [this.filterInput()]
    };
    this.store.dispatch(new SetTaskFilter(wildcardFilter));
  }

  filterWorkbasketNames() {
    this.filteredWorkbasketNames.set(
      this.workbasketNames.filter((value) => value.toLowerCase().includes(this.resultName().toLowerCase()))
    );
  }

  searchBasket() {
    this.store.dispatch(new SetFilterExpansion(false));
    this.workbasketSelected.set(true);
    if (this.searchSelected === this.search.byWorkbasket && this.workbaskets()) {
      this.workbaskets()?.forEach((workbasket) => {
        if (workbasket.name === this.resultName()) {
          this.resultId.set(workbasket.workbasketId ?? '');
          this.currentBasket.set(workbasket);
          this.store.dispatch(new SelectWorkbasket(this.currentBasket()));
        }
      });

      this.searched.set(!!this.currentBasket());

      if (!this.resultId()) {
        this.currentBasket.set(undefined);
        this.store.dispatch(new SelectWorkbasket(undefined));
      }
    }

    this.resultId.set('');
  }

  sorting(sort: Sorting<TaskQuerySortParameter>) {
    this.store.dispatch(new SetSort(sort));
  }

  onFilter() {
    this.store.dispatch(new SetPage(1));
  }

  onClearFilter() {
    this.store.dispatch(new ClearTaskFilter()).subscribe(() => {
      this.store.dispatch(new SetPage(1));
    });
  }

  createTask() {
    this.store.dispatch(new SelectTask(undefined));
    this.router.navigate([{ outlets: { detail: 'taskdetail/new-task' } }], {
      relativeTo: this.route.parent,
      queryParamsHandling: 'merge'
    });
  }

  selectSearch(type: SearchType) {
    this.searchSelected = type;
    this.resultId.set('');
    this.store.dispatch(new SetSearchType(type));
    this.searchBasket();
    this.onClearFilter();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
