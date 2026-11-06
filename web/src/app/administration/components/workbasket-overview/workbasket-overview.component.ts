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

import { Component, ElementRef, inject, OnInit, signal, viewChild, OnDestroy } from '@angular/core';
import { Store } from '@ngxs/store';
import { DomainSelectors } from '../../../shared/store/domain-store/domain.selectors';
import { Observable, Subject } from 'rxjs';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter, take, takeUntil } from 'rxjs/operators';
import { ACTION } from '../../../shared/models/action';
import { WorkbasketAndAction, WorkbasketSelectors } from '../../../shared/store/workbasket-store/workbasket.selectors';

import {
  CreateWorkbasket,
  DeselectWorkbasket,
  SelectWorkbasket
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { Workbasket } from '../../../shared/models/workbasket';
import { WorkbasketListComponent } from '../workbasket-list/workbasket-list.component';

import { MatIcon } from '@angular/material/icon';
import { WorkbasketDetailsComponent } from '../workbasket-details/workbasket-details.component';
import { SvgIconComponent } from 'angular-svg-icon';

@Component({
  selector: 'kadai-administration-workbasket-overview',
  templateUrl: './workbasket-overview.component.html',
  styleUrls: ['./workbasket-overview.component.scss'],
  imports: [WorkbasketListComponent, MatIcon, WorkbasketDetailsComponent, SvgIconComponent]
})
export class WorkbasketOverviewComponent implements OnInit, OnDestroy {
  showDetail = signal(false);
  selectedWorkbasketAndAction$: Observable<WorkbasketAndAction> = inject(Store).select(
    WorkbasketSelectors.selectedWorkbasketAndAction
  );
  selectedWorkbasket$: Observable<Workbasket> = inject(Store).select(WorkbasketSelectors.selectedWorkbasket);
  destroy$ = new Subject<void>();
  routerParams: any;
  expanded = true;
  workbasketList = viewChild<ElementRef>('workbasketList');
  toggleButton = viewChild<ElementRef>('toggleButton');
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private store = inject(Store);

  ngOnInit() {
    this.route.url.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      if (params[0].path === 'workbaskets') {
        this.selectedWorkbasket$.pipe(take(1)).subscribe((workbasket) => {
          if (typeof workbasket.workbasketId !== 'undefined') {
            this.store.dispatch(new SelectWorkbasket(workbasket.workbasketId));
          }
        });
      }
    });
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.applyDetailRoute());
    this.applyDetailRoute();
    this.selectedWorkbasketAndAction$.pipe(takeUntil(this.destroy$)).subscribe((state) => {
      this.showDetail.set(!!state.selectedWorkbasket || state.action === 1);
    });
  }

  private applyDetailRoute(): void {
    const id = this.route.firstChild?.snapshot.params['id'];
    this.routerParams = { id };

    if (!id) {
      this.store.dispatch(new DeselectWorkbasket());
      return;
    }

    this.showDetail.set(true);

    if (id === 'new-workbasket') {
      const activeAction = this.store.selectSnapshot(WorkbasketSelectors.workbasketActiveAction);
      if (activeAction !== ACTION.CREATE && activeAction !== ACTION.COPY) {
        this.store.dispatch(new CreateWorkbasket(this.store.selectSnapshot(DomainSelectors.selectedDomain)));
      }
      return;
    }

    if (this.store.selectSnapshot(WorkbasketSelectors.selectedWorkbasket)?.workbasketId !== id) {
      this.store.dispatch(new SelectWorkbasket(id));
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleWidth() {
    if (this.workbasketList()?.nativeElement.offsetWidth === 250) {
      this.expanded = true;
      this.workbasketList()!.nativeElement.style.width = '500px';
      this.workbasketList()!.nativeElement.style.minWidth = '500px';
      this.toggleButton()!.nativeElement.style.left = '480px';
    } else {
      this.expanded = false;
      this.workbasketList()!.nativeElement.style.width = '250px';
      this.workbasketList()!.nativeElement.style.minWidth = '250px';
      this.toggleButton()!.nativeElement.style.left = '230px';
    }
  }
}
