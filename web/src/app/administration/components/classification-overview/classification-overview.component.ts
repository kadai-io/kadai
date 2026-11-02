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

import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { Store } from '@ngxs/store';
import { DomainSelectors } from '../../../shared/store/domain-store/domain.selectors';
import { filter, takeUntil } from 'rxjs/operators';
import { ClassificationSelectors } from '../../../shared/store/classification-store/classification.selectors';
import {
  CreateClassification,
  DeselectClassification,
  GetClassifications,
  SelectClassification
} from '../../../shared/store/classification-store/classification.actions';
import { Classification } from '../../../shared/models/classification';
import { ClassificationListComponent } from '../classification-list/classification-list.component';

import { ClassificationDetailsComponent } from '../classification-details/classification-details.component';
import { SvgIconComponent } from 'angular-svg-icon';

@Component({
  selector: 'kadai-administration-classification-overview',
  templateUrl: './classification-overview.component.html',
  styleUrls: ['./classification-overview.component.scss'],
  imports: [ClassificationListComponent, ClassificationDetailsComponent, SvgIconComponent]
})
export class ClassificationOverviewComponent implements OnInit, OnDestroy {
  showDetail = signal(false);
  selectedClassification$: Observable<Classification> = inject(Store).select(
    ClassificationSelectors.selectedClassification
  );
  routerParams: any;
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private store = inject(Store);
  private destroy$ = new Subject<void>();

  ngOnInit() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.applyDetailRoute());
    this.applyDetailRoute();

    this.selectedClassification$.pipe(takeUntil(this.destroy$)).subscribe((selectedClassification) => {
      this.showDetail.set(!!selectedClassification);
    });
  }

  // route.firstChild is a snapshot, so reading it once in ngOnInit missed every detail opened later and
  // the url could not drive the selection. Re-reading it per navigation makes the url the single input,
  // and the guards keep a navigation that merely catches up with the store from dispatching again.
  private applyDetailRoute(): void {
    const id = this.route.firstChild?.snapshot.params['id'];
    this.routerParams = { id };

    if (!id) {
      this.store.dispatch(new DeselectClassification());
      return;
    }

    const selected = this.store.selectSnapshot(ClassificationSelectors.selectedClassification);

    if (id.indexOf('new-classification') !== -1) {
      if (selected === undefined || selected.classificationId !== undefined) {
        this.store.dispatch(new CreateClassification(this.store.selectSnapshot(DomainSelectors.selectedDomain)));
      }
      return;
    }

    if (selected?.classificationId !== id) {
      this.store.dispatch(new SelectClassification(id)).subscribe(() => {
        const domain = this.store.selectSnapshot(DomainSelectors.selectedDomain);
        if (domain !== undefined) {
          this.store.dispatch(new GetClassifications(domain));
        }
      });
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
