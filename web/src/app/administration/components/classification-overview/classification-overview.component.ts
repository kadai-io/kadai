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

import { Component, inject, OnInit, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Params } from '@angular/router';
import { Observable } from 'rxjs';
import { Store } from '@ngxs/store';
import { ClassificationSelectors } from 'app/shared/store/classification-store/classification.selectors';
import {
  CreateClassification,
  DeselectClassification,
  GetClassifications,
  SelectClassification
} from 'app/shared/store/classification-store/classification.actions';
import { Classification } from 'app/shared/models/classification';
import { ClassificationListComponent } from 'app/administration/components/classification-list/classification-list.component';

import { ClassificationDetailsComponent } from 'app/administration/components/classification-details/classification-details.component';
import { SvgIconComponent } from 'app/shared/components/svg-icon/svg-icon.component';

@Component({
  selector: 'kadai-administration-classification-overview',
  templateUrl: './classification-overview.component.html',
  styleUrls: ['./classification-overview.component.scss'],
  imports: [ClassificationListComponent, ClassificationDetailsComponent, SvgIconComponent]
})
export class ClassificationOverviewComponent implements OnInit {
  showDetail = signal(false);
  selectedClassification$: Observable<Classification> = inject(Store).select(
    ClassificationSelectors.selectedClassification
  );
  routerParams: Params = {};
  private route = inject(ActivatedRoute);
  private store = inject(Store);
  private destroyRef = inject(DestroyRef);

  ngOnInit() {
    if (this.route.firstChild) {
      this.route.firstChild.params.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
        this.routerParams = params;

        if (this.routerParams.id) {
          this.store
            .dispatch(new SelectClassification(this.routerParams.id))
            .subscribe(() => this.store.dispatch(new GetClassifications()));
        } else {
          this.store.dispatch(new DeselectClassification());
        }
        if (this.routerParams.id && this.routerParams.id.indexOf('new-classification') !== -1) {
          this.store.dispatch(new CreateClassification());
        }
      });
    }

    this.selectedClassification$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((selectedClassification) => {
      this.showDetail.set(!!selectedClassification);
    });
  }
}
