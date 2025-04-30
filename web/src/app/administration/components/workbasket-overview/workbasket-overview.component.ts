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

import { Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { Select, Store } from '@ngxs/store';
import { Observable, Subject } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { take, takeUntil } from 'rxjs/operators';
import { WorkbasketSelectors } from '../../../shared/store/workbasket-store/workbasket.selectors';

import { CreateWorkbasket, SelectWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import { Workbasket } from '../../../shared/models/workbasket';
import { WorkbasketListComponent } from '../workbasket-list/workbasket-list.component';
import { NgIf } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import { WorkbasketDetailsComponent } from '../workbasket-details/workbasket-details.component';
import { SvgIconComponent } from 'angular-svg-icon';

@Component({
  selector: 'kadai-administration-workbasket-overview',
  templateUrl: './workbasket-overview.component.html',
  styleUrls: ['./workbasket-overview.component.scss'],
  imports: [WorkbasketListComponent, NgIf, MatIcon, WorkbasketDetailsComponent, SvgIconComponent]
})
export class WorkbasketOverviewComponent implements OnInit {
  showDetail = false;
  @Select(WorkbasketSelectors.selectedWorkbasketAndAction) selectedWorkbasketAndAction$: Observable<any>;
  @Select(WorkbasketSelectors.selectedWorkbasket) selectedWorkbasket$: Observable<Workbasket>;
  destroy$ = new Subject<void>();
  routerParams: any;
  expanded = true;
  @ViewChild('workbasketList') workbasketList: ElementRef;
  @ViewChild('toggleButton') toggleButton: ElementRef;
  private route = inject(ActivatedRoute);
  private store = inject(Store);

  ngOnInit() {
    if (this.route.url) {
      this.route.url.pipe(takeUntil(this.destroy$)).subscribe((params) => {
        if (params[0].path === 'workbaskets') {
          this.selectedWorkbasket$.pipe(take(1)).subscribe((workbasket) => {
            if (typeof workbasket.workbasketId !== 'undefined') {
              this.store.dispatch(new SelectWorkbasket(workbasket.workbasketId));
            }
          });
        }
      });
    }
    if (this.route.firstChild) {
      this.route.firstChild.params.pipe(takeUntil(this.destroy$)).subscribe((params) => {
        this.routerParams = params;
        if (this.routerParams.id) {
          this.showDetail = true;
          if (this.routerParams.id === 'new-workbasket') {
            this.store.dispatch(new CreateWorkbasket());
          } else {
            this.store.dispatch(new SelectWorkbasket(this.routerParams.id));
          }
        }
      });
    }
    this.selectedWorkbasketAndAction$.pipe(takeUntil(this.destroy$)).subscribe((state) => {
      this.showDetail = !!state.selectedWorkbasket || state.action === 1;
    });
  }

  toggleWidth() {
    if (this.workbasketList.nativeElement.offsetWidth === 250) {
      this.expanded = true;
      this.workbasketList.nativeElement.style.width = '500px';
      this.workbasketList.nativeElement.style.minWidth = '500px';
      this.toggleButton.nativeElement.style.left = '480px';
    } else {
      this.expanded = false;
      this.workbasketList.nativeElement.style.width = '250px';
      this.workbasketList.nativeElement.style.minWidth = '250px';
      this.toggleButton.nativeElement.style.left = '230px';
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
