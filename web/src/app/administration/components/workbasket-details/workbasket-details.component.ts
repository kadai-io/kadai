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
import { Observable, of, Subject, timeout } from 'rxjs';
import { Workbasket } from 'app/shared/models/workbasket';
import { ACTION } from 'app/shared/models/action';
import { Actions, ofActionSuccessful, Store } from '@ngxs/store';
import { catchError, filter, take, takeUntil } from 'rxjs/operators';
import {
  WorkbasketAndComponentAndAction,
  WorkbasketSelectors
} from '../../../shared/store/workbasket-store/workbasket.selectors';
import { AsyncPipe } from '@angular/common';
import {
  CopyWorkbasket,
  DeselectWorkbasket,
  OnButtonPressed,
  SaveNewWorkbasket,
  SelectComponent,
  UpdateWorkbasket,
  UpdateWorkbasketDistributionTargets
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { ButtonAction } from '../../models/button-action';
import { cloneDeep } from 'lodash';
import { MatToolbar } from '@angular/material/toolbar';
import { MatTooltip } from '@angular/material/tooltip';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { MatTab, MatTabContent, MatTabGroup } from '@angular/material/tabs';
import { WorkbasketInformationComponent } from '../workbasket-information/workbasket-information.component';
import { WorkbasketAccessItemsComponent } from '../workbasket-access-items/workbasket-access-items.component';
import { WorkbasketDistributionTargetsComponent } from '../workbasket-distribution-targets/workbasket-distribution-targets.component';

@Component({
  selector: 'kadai-administration-workbasket-details',
  templateUrl: './workbasket-details.component.html',
  styleUrls: ['./workbasket-details.component.scss'],
  imports: [
    MatToolbar,
    MatTooltip,
    MatButton,
    MatIcon,
    MatMenuTrigger,
    MatMenu,
    MatMenuItem,
    MatTabGroup,
    MatTab,
    WorkbasketInformationComponent,
    WorkbasketAccessItemsComponent,
    MatTabContent,
    WorkbasketDistributionTargetsComponent,
    AsyncPipe
  ]
})
export class WorkbasketDetailsComponent implements OnInit, OnDestroy {
  workbasket: Workbasket;
  action: ACTION;
  selectedTab$: Observable<number> = inject(Store).select(WorkbasketSelectors.selectedComponent);
  badgeMessage$: Observable<string> = inject(Store).select(WorkbasketSelectors.badgeMessage);
  selectedWorkbasketAndComponentAndAction$: Observable<WorkbasketAndComponentAndAction> = inject(Store).select(
    WorkbasketSelectors.selectedWorkbasketAndComponentAndAction
  );
  selectedWorkbasket$: Observable<Workbasket> = inject(Store).select(WorkbasketSelectors.selectedWorkbasket);
  destroy$ = new Subject<void>();
  @Input() expanded: boolean;
  private store = inject(Store);
  private ngxsActions$ = inject(Actions);
  areAllAccessItemsValid = true;

  ngOnInit() {
    this.getWorkbasketFromStore();
  }

  getWorkbasketFromStore() {
    /*
        get workbasket from store only when (to avoid discarding changes):
        a) workbasket with another ID is selected (includes copying)
        b) empty workbasket is created
      */
    this.selectedWorkbasketAndComponentAndAction$.pipe(takeUntil(this.destroy$)).subscribe((object) => {
      const workbasket = object.selectedWorkbasket;
      const action = object.action;

      const isAnotherId = this.workbasket?.workbasketId !== workbasket?.workbasketId;
      const isCreation = action !== this.action && action === ACTION.CREATE;
      if (isAnotherId || isCreation) {
        this.workbasket = cloneDeep(workbasket);
      }

      this.action = action;
    });

    // c) saving the workbasket
    this.ngxsActions$.pipe(ofActionSuccessful(UpdateWorkbasket), takeUntil(this.destroy$)).subscribe(() => {
      this.store
        .dispatch(new UpdateWorkbasketDistributionTargets())
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          this.selectedWorkbasket$
            .pipe(
              take(5),
              timeout(250),
              catchError(() => of(null)),
              filter((val) => val !== null)
            )
            .subscribe((wb) => (this.workbasket = wb));
        });
    });

    this.ngxsActions$.pipe(ofActionSuccessful(SaveNewWorkbasket), takeUntil(this.destroy$)).subscribe(() => {
      this.store
        .dispatch(new UpdateWorkbasketDistributionTargets())
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          this.selectedWorkbasket$
            .pipe(
              take(5),
              timeout(250),
              catchError(() => of(null)),
              filter((val) => val !== null)
            )
            .subscribe((wb) => (this.workbasket = wb));
        });
    });
  }

  selectComponent(index) {
    this.store.dispatch(new SelectComponent(index));
  }

  onSubmit() {
    this.store.dispatch(new OnButtonPressed(ButtonAction.SAVE));
  }

  onAccessItemsValidityChanged(isValid: boolean) {
    this.areAllAccessItemsValid = isValid;
  }

  onRestore() {
    this.store.dispatch(new OnButtonPressed(ButtonAction.UNDO));
  }

  onCopy() {
    this.store.dispatch(new OnButtonPressed(ButtonAction.COPY));
    this.store.dispatch(new CopyWorkbasket(this.workbasket));
  }

  onRemoveAsDistributionTarget() {
    this.store.dispatch(new OnButtonPressed(ButtonAction.REMOVE_AS_DISTRIBUTION_TARGETS));
  }

  onRemoveWorkbasket() {
    this.store.dispatch(new OnButtonPressed(ButtonAction.DELETE));
  }

  onClose() {
    this.store.dispatch(new OnButtonPressed(ButtonAction.CLOSE));
    this.store.dispatch(new DeselectWorkbasket());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
