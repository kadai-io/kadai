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

import { Component, computed, effect, ElementRef, inject, OnDestroy, OnInit, input, viewChild } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

import { WorkbasketSummaryRepresentation } from 'app/shared/models/workbasket-summary-representation';
import { WorkbasketSummary } from 'app/shared/models/workbasket-summary';
import { Direction, Sorting, WorkbasketQuerySortParameter } from 'app/shared/models/sorting';

import { WorkbasketService } from 'app/shared/services/workbasket/workbasket.service';
import { OrientationService } from 'app/shared/services/orientation/orientation.service';
import { ImportExportService } from 'app/administration/services/import-export.service';
import { Actions, ofActionCompleted, ofActionDispatched, Store } from '@ngxs/store';
import { delay, takeUntil, tap } from 'rxjs/operators';
import {
  DeselectWorkbasket,
  GetWorkbasketsSummary,
  SelectWorkbasket
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { WorkbasketSelectors } from '../../../shared/store/workbasket-store/workbasket.selectors';
import { Workbasket } from '../../../shared/models/workbasket';
import { MatListOption, MatSelectionList } from '@angular/material/list';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { WorkbasketQueryFilterParameter } from '../../../shared/models/workbasket-query-filter-parameter';
import { QueryPagingParameter } from '../../../shared/models/query-paging-parameter';
import { FilterSelectors } from '../../../shared/store/filter-store/filter.selectors';
import { WorkbasketListToolbarComponent } from '../workbasket-list-toolbar/workbasket-list-toolbar.component';
import { AsyncPipe } from '@angular/common';
import { IconTypeComponent } from '../type-icon/icon-type.component';
import { MatDivider } from '@angular/material/divider';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'kadai-administration-workbasket-list',
  templateUrl: './workbasket-list.component.html',
  styleUrls: ['./workbasket-list.component.scss'],
  imports: [
    WorkbasketListToolbarComponent,
    MatSelectionList,
    MatListOption,
    IconTypeComponent,
    MatDivider,
    PaginationComponent,
    AsyncPipe
  ]
})
export class WorkbasketListComponent implements OnInit, OnDestroy {
  selectedId = '';
  type = 'workbaskets';
  workbasketDefaultSortBy: WorkbasketQuerySortParameter = WorkbasketQuerySortParameter.NAME;
  sort: Sorting<WorkbasketQuerySortParameter> = {
    'sort-by': this.workbasketDefaultSortBy,
    order: Direction.ASC
  };
  filterBy: WorkbasketQueryFilterParameter = {};
  pageParameter: QueryPagingParameter = {
    page: 1,
    'page-size': 9
  };
  requestInProgress: boolean;
  requestInProgressLocal = false;
  resetPagingSubject = new Subject<null>();
  readonly expanded = input<boolean>(undefined);
  workbasketsSummary$: Observable<WorkbasketSummary[]> = inject(Store).select(WorkbasketSelectors.workbasketsSummary);
  workbasketsSummaryRepresentation$: Observable<WorkbasketSummaryRepresentation> = inject(Store).select(
    WorkbasketSelectors.workbasketsSummaryRepresentation
  );
  // Convert store selectors to signals
  private selectedWorkbasket = toSignal(inject(Store).select(WorkbasketSelectors.selectedWorkbasket), {
    initialValue: undefined
  });
  private workbasketListFilter = toSignal(inject(Store).select(FilterSelectors.getWorkbasketListFilter), {
    initialValue: {}
  });
  // Computed selectedId from workbasket signal
  private selectedIdComputed = computed(() => {
    const workbasket = this.selectedWorkbasket();
    return workbasket?.workbasketId;
  });
  destroy$ = new Subject<void>();
  readonly workbasketList = viewChild<MatSelectionList>('workbasket');
  private store = inject(Store);
  private workbasketService = inject(WorkbasketService);
  private orientationService = inject(OrientationService);
  private importExportService = inject(ImportExportService);
  private domainService = inject(DomainService);
  private requestInProgressService = inject(RequestInProgressService);
  private ngxsActions$ = inject(Actions);
  private readonly toolbarElement = viewChild<ElementRef>('wbToolbar');

  constructor() {
    this.ngxsActions$.pipe(ofActionDispatched(GetWorkbasketsSummary), takeUntil(this.destroy$)).subscribe(() => {
      this.requestInProgressService.setRequestInProgress(true);
      this.requestInProgressLocal = true;
    });
    this.ngxsActions$.pipe(ofActionCompleted(GetWorkbasketsSummary), takeUntil(this.destroy$)).subscribe(() => {
      this.requestInProgressService.setRequestInProgress(false);
      this.requestInProgressLocal = false;
    });

    // Use effect to sync selectedId with signal
    effect(() => {
      const id = this.selectedIdComputed();
      this.selectedId = id ?? undefined;
    });

    // Use effect to react to filter changes
    effect(() => {
      const filter = this.workbasketListFilter();
      this.performFilter(filter);
    });
  }

  ngOnInit() {
    this.requestInProgressService.setRequestInProgress(true);

    this.workbasketService
      .workbasketSavedTriggered()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.performRequest();
      });

    this.orientationService
      .getOrientation()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.refreshWorkbasketList();
      });

    this.importExportService
      .getImportingFinished()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.refreshWorkbasketList();
      });

    this.domainService
      .getSelectedDomain()
      .pipe(takeUntil(this.destroy$))
      .subscribe((domain) => {
        this.filterBy.domain = [domain];
        this.performRequest();
      });

    this.workbasketService
      .getWorkbasketActionToolbarExpansion()
      .pipe(
        takeUntil(this.destroy$),
        tap(() => this.requestInProgressService.setRequestInProgress(true)),
        delay(1),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.refreshWorkbasketList();
      });

    this.requestInProgressService
      .getRequestInProgress()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.requestInProgress = value;
      });
  }

  selectWorkbasket(id: string) {
    this.requestInProgressService.setRequestInProgress(true);
    if (this.selectedId === id) {
      this.store
        .dispatch(new DeselectWorkbasket())
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => this.requestInProgressService.setRequestInProgress(false));
    } else {
      this.store
        .dispatch(new SelectWorkbasket(id))
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => this.requestInProgressService.setRequestInProgress(false));
    }
  }

  performSorting(sort: Sorting<WorkbasketQuerySortParameter>) {
    this.sort = sort;
    this.performRequest();
  }

  performFilter(filterBy: WorkbasketQueryFilterParameter) {
    const domain = this.filterBy.domain;
    this.filterBy = { ...filterBy };
    this.filterBy.domain = domain;
    this.resetPagingSubject.next(null);
  }

  changePage(page) {
    this.pageParameter.page = page;
    this.performRequest();
  }

  refreshWorkbasketList() {
    this.pageParameter['page-size'] = this.orientationService.calculateNumberItemsList(
      window.innerHeight,
      92,
      200 + this.toolbarElement().nativeElement.offsetHeight,
      false
    );
    this.performRequest();
  }

  performRequest() {
    this.store
      .dispatch(new GetWorkbasketsSummary(true, this.filterBy, this.sort, this.pageParameter))
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.requestInProgressService.setRequestInProgress(false);
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
