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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { forkJoin, Observable, Subject } from 'rxjs';

import { Workbasket } from 'app/shared/models/workbasket';
import { WorkbasketSummary } from 'app/shared/models/workbasket-summary';
import { Actions, Select, Store } from '@ngxs/store';
import { filter, take, takeUntil } from 'rxjs/operators';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import {
  FetchAvailableDistributionTargets,
  FetchWorkbasketDistributionTargets,
  UpdateWorkbasketDistributionTargets
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { WorkbasketSelectors } from '../../../shared/store/workbasket-store/workbasket.selectors';
import { ButtonAction } from '../../models/button-action';
import { Side } from '../../models/workbasket-distribution-enums';
import { ClearWorkbasketFilter } from '../../../shared/store/filter-store/filter.actions';

@Component({
  selector: 'kadai-administration-workbasket-distribution-targets',
  templateUrl: './workbasket-distribution-targets.component.html',
  styleUrls: ['./workbasket-distribution-targets.component.scss'],
  standalone: false
})
export class WorkbasketDistributionTargetsComponent implements OnInit, OnDestroy {
  sideEnum = Side;
  sideBySide = true;
  displayingDistributionTargetsPicker = true;

  transferDistributionTargetObservable = new Subject<Side>();

  @Select(WorkbasketSelectors.buttonAction)
  buttonAction$: Observable<ButtonAction>;

  @Select(WorkbasketSelectors.selectedWorkbasket)
  selectedWorkbasket$: Observable<Workbasket>;

  destroy$ = new Subject<void>();
  private selectedWorkbasket: WorkbasketSummary;

  constructor(
    private notificationsService: NotificationService,
    private store: Store
  ) {}

  /**
   * Rework with modification based on old components,
   * would be ideal to completely redo whole components using drag and drop angular components and clearer logics
   */
  ngOnInit() {
    this.selectedWorkbasket$.pipe(takeUntil(this.destroy$)).subscribe((wb) => {
      if (wb !== undefined && wb.workbasketId !== this.selectedWorkbasket?.workbasketId) {
        if (this.selectedWorkbasket?.workbasketId) {
          this.store.dispatch(new FetchWorkbasketDistributionTargets(true));
          this.store.dispatch(new FetchAvailableDistributionTargets(true));
        }
        this.selectedWorkbasket = wb;
      }
    });

    this.buttonAction$
      .pipe(takeUntil(this.destroy$))
      .pipe(filter((buttonAction) => typeof buttonAction !== 'undefined'))
      .subscribe((button) => {
        switch (button) {
          case ButtonAction.UNDO:
            this.onClear();
            break;
          default:
            break;
        }
      });
  }

  toggleDistributionTargetsPicker() {
    this.displayingDistributionTargetsPicker = !this.displayingDistributionTargetsPicker;
  }

  onSave(): void {
    this.store.dispatch(new UpdateWorkbasketDistributionTargets());
  }

  moveDistributionTargets(targetSide: Side): void {
    this.transferDistributionTargetObservable.next(targetSide);
  }

  onClear() {
    forkJoin([
      this.store.dispatch(new FetchWorkbasketDistributionTargets(true)),
      this.store.dispatch(new FetchAvailableDistributionTargets(true)),
      this.store.dispatch(new ClearWorkbasketFilter('selectedDistributionTargets')),
      this.store.dispatch(new ClearWorkbasketFilter('availableDistributionTargets'))
    ])
      .pipe(take(1))
      .subscribe(() => this.notificationsService.showSuccess('WORKBASKET_DISTRIBUTION_TARGET_RESTORE'));
  }

  toggleSideBySideView() {
    this.sideBySide = !this.sideBySide;
    this.displayingDistributionTargetsPicker = true; //always display picker when toggle from side-by-side to single
  }

  ngOnDestroy() {
    this.transferDistributionTargetObservable.complete();
    this.destroy$.next();
    this.destroy$.complete();
  }
}
