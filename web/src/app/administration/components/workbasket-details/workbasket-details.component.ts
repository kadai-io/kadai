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

import { Component, inject, input, OnInit, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';
import { Workbasket } from 'app/shared/models/workbasket';
import { ACTION } from 'app/shared/models/action';
import { Actions, ofActionSuccessful, Store } from '@ngxs/store';
import {
  WorkbasketAndComponentAndAction,
  WorkbasketSelectors
} from 'app/shared/store/workbasket-store/workbasket.selectors';
import { AsyncPipe } from '@angular/common';
import {
  CopyWorkbasket,
  DeselectWorkbasket,
  OnButtonPressed,
  SaveNewWorkbasket,
  SelectComponent,
  UpdateWorkbasket,
  UpdateWorkbasketDistributionTargets
} from 'app/shared/store/workbasket-store/workbasket.actions';
import { ButtonAction } from 'app/administration/models/button-action';
import { MatToolbar } from '@angular/material/toolbar';
import { MatTooltip } from '@angular/material/tooltip';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { MatTab, MatTabContent, MatTabGroup } from '@angular/material/tabs';
import { WorkbasketInformationComponent } from 'app/administration/components/workbasket-information/workbasket-information.component';
import { WorkbasketAccessItemsComponent } from 'app/administration/components/workbasket-access-items/workbasket-access-items.component';
import { WorkbasketDistributionTargetsComponent } from 'app/administration/components/workbasket-distribution-targets/workbasket-distribution-targets.component';

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
export class WorkbasketDetailsComponent implements OnInit {
  workbasket = signal<Workbasket>({} as Workbasket);
  action = signal<ACTION>(ACTION.READ);
  selectedTab$: Observable<number> = inject(Store).select(WorkbasketSelectors.selectedComponent);
  badgeMessage$: Observable<string> = inject(Store).select(WorkbasketSelectors.badgeMessage);
  selectedWorkbasketAndComponentAndAction$: Observable<WorkbasketAndComponentAndAction> = inject(Store).select(
    WorkbasketSelectors.selectedWorkbasketAndComponentAndAction
  );
  expanded = input<boolean>();
  private destroyRef = inject(DestroyRef);
  areAllAccessItemsValid = true;
  protected readonly ACTION = ACTION;
  private store = inject(Store);
  private ngxsActions$ = inject(Actions);

  ngOnInit(): void {
    const workbasketAndComponentAndAction = this.store.selectSnapshot(
      WorkbasketSelectors.selectedWorkbasketAndComponentAndAction
    );
    if (workbasketAndComponentAndAction?.selectedWorkbasket) {
      this.workbasket.set(structuredClone(workbasketAndComponentAndAction.selectedWorkbasket));
    }
    if (workbasketAndComponentAndAction?.action) {
      this.action.set(workbasketAndComponentAndAction.action);
    }

    this.getWorkbasketFromStore();
  }

  getWorkbasketFromStore() {
    /*
        get workbasket from store only when (to avoid discarding changes):
        a) workbasket with another ID is selected (includes copying)
        b) empty workbasket is created
      */
    this.selectedWorkbasketAndComponentAndAction$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((object) => {
      const selectedWorkbasket = object.selectedWorkbasket;
      const action = object.action;

      const isAnotherId = this.workbasket()?.workbasketId !== selectedWorkbasket?.workbasketId;
      const isCreation = action !== this.action() && action === ACTION.CREATE;
      if ((isAnotherId || isCreation) && selectedWorkbasket) {
        this.workbasket.set(structuredClone(selectedWorkbasket));
      }

      this.action.set(action);
    });

    // c) saving the workbasket
    this.ngxsActions$.pipe(ofActionSuccessful(UpdateWorkbasket), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.store
        .dispatch(new UpdateWorkbasketDistributionTargets())
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => {
          const workbasket = this.store.selectSnapshot(WorkbasketSelectors.selectedWorkbasket);
          if (workbasket) {
            this.workbasket.set(workbasket);
          }
        });
    });

    this.ngxsActions$.pipe(ofActionSuccessful(SaveNewWorkbasket), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.store
        .dispatch(new UpdateWorkbasketDistributionTargets())
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => {
          const workbasket = this.store.selectSnapshot(WorkbasketSelectors.selectedWorkbasket);
          if (workbasket) {
            this.workbasket.set(workbasket);
          }
        });
    });
  }

  selectComponent(index: number) {
    this.store.dispatch(new SelectComponent(index));
  }

  onSubmit() {
    this.store.dispatch(new OnButtonPressed(ButtonAction.SAVE));
  }

  handleAccessItemsValidityChanged(isValid: boolean) {
    this.areAllAccessItemsValid = isValid;
  }

  onRestore() {
    this.store.dispatch(new OnButtonPressed(ButtonAction.UNDO));
  }

  onCopy() {
    this.store.dispatch(new OnButtonPressed(ButtonAction.COPY));
    this.store.dispatch(new CopyWorkbasket(this.workbasket()));
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
}
