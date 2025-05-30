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

import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { Select, Store } from '@ngxs/store';
import { takeUntil } from 'rxjs/operators';
import { ClassificationSelectors } from '../../../shared/store/classification-store/classification.selectors';
import {
  CreateClassification,
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
  showDetail = false;
  @Select(ClassificationSelectors.selectedClassification) selectedClassification$: Observable<Classification>;
  routerParams: any;
  private route = inject(ActivatedRoute);
  private store = inject(Store);
  private destroy$ = new Subject<void>();

  ngOnInit() {
    if (this.route.firstChild) {
      this.route.firstChild.params.pipe(takeUntil(this.destroy$)).subscribe((params) => {
        this.routerParams = params;

        if (this.routerParams.id) {
          this.showDetail = true;
          this.store
            .dispatch(new SelectClassification(this.routerParams.id))
            .subscribe(() => this.store.dispatch(new GetClassifications()));
        }
        if (this.routerParams.id && this.routerParams.id.indexOf('new-classification') !== -1) {
          this.store.dispatch(new CreateClassification());
        }
      });
    }

    this.selectedClassification$.pipe(takeUntil(this.destroy$)).subscribe((selectedClassification) => {
      this.showDetail = !!selectedClassification;
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
