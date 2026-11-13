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
import { CLASSIFICATIONS_ROUTE } from 'app/shared/util/administration-routes';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Actions, ofActionCompleted, ofActionDispatched, Store } from '@ngxs/store';

import { ImportExportService } from 'app/administration/services/import-export.service';

import { KadaiType } from 'app/shared/models/kadai-type';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { ClassificationSelectors } from 'app/shared/store/classification-store/classification.selectors';
import { AsyncPipe } from '@angular/common';
import { ClassificationCategoryImages } from 'app/shared/models/customisation';

import { GetClassifications } from 'app/shared/store/classification-store/classification.actions';
import { DomainSelectors } from 'app/shared/store/domain-store/domain.selectors';
import { ClassificationSummary } from 'app/shared/models/classification-summary';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { Pair } from 'app/shared/models/pair';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { ImportExportComponent } from 'app/administration/components/import-export/import-export.component';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { SvgIconComponent } from 'app/shared/components/svg-icon/svg-icon.component';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { ClassificationTypesSelectorComponent } from 'app/administration/components/classification-types-selector/classification-types-selector.component';
import { KadaiTreeComponent } from 'app/administration/components/tree/tree.component';

@Component({
  selector: 'kadai-administration-classification-list',
  templateUrl: './classification-list.component.html',
  styleUrls: ['./classification-list.component.scss'],
  imports: [
    MatButton,
    MatTooltip,
    MatIcon,
    ImportExportComponent,
    MatMenuTrigger,
    SvgIconComponent,
    MatMenu,
    MatMenuItem,
    MatFormField,
    MatLabel,
    MatInput,
    FormsModule,
    ClassificationTypesSelectorComponent,
    KadaiTreeComponent,
    AsyncPipe
  ]
})
export class ClassificationListComponent implements OnInit, OnDestroy {
  kadaiType = KadaiType;
  requestInProgress = signal(true);
  inputValue = '';
  selectedCategory = signal('');
  classificationTypeSelected$: Observable<string> = inject(Store).select(
    ClassificationSelectors.selectedClassificationType
  );
  categories$: Observable<string[]> = inject(Store).select(ClassificationSelectors.selectCategories);
  classifications$: Observable<ClassificationSummary[]> = inject(Store).select(ClassificationSelectors.classifications);
  categoryIcons$: Observable<ClassificationCategoryImages> = inject(Store).select(
    EngineConfigurationSelectors.selectCategoryIcons
  );
  destroy$ = new Subject<void>();
  classifications = signal<ClassificationSummary[]>([]);
  private importExportService = inject(ImportExportService);
  private requestInProgressService = inject(RequestInProgressService);
  private store = inject(Store);
  private router = inject(Router);
  private ngxsActions$ = inject(Actions);

  constructor() {
    this.ngxsActions$.pipe(ofActionDispatched(GetClassifications), takeUntil(this.destroy$)).subscribe(() => {
      this.requestInProgressService.setRequestInProgress(true);
    });
    this.ngxsActions$.pipe(ofActionCompleted(GetClassifications), takeUntil(this.destroy$)).subscribe(() => {
      this.requestInProgressService.setRequestInProgress(false);
    });
  }

  ngOnInit() {
    this.classifications$.pipe(takeUntil(this.destroy$)).subscribe((classifications) => {
      this.classifications.set(classifications ?? []);
    });

    this.requestInProgressService
      .getRequestInProgress()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.requestInProgress.set(value);
      });

    this.classificationTypeSelected$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.refreshClassifications();
      this.selectedCategory.set('');
    });

    this.importExportService
      .getImportingFinished()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.refreshClassifications();
      });

    this.store
      .select(DomainSelectors.selectedDomain)
      .pipe(takeUntil(this.destroy$))
      .subscribe((domain) => {
        if (domain !== undefined) {
          this.store.dispatch(new GetClassifications(domain));
        }
      });
  }

  private refreshClassifications(): void {
    const domain = this.store.selectSnapshot(DomainSelectors.selectedDomain);
    if (domain !== undefined) {
      this.store.dispatch(new GetClassifications(domain));
    }
  }

  addClassification() {
    this.router.navigate([CLASSIFICATIONS_ROUTE, { outlets: { detail: ['new-classification'] } }], {
      queryParamsHandling: 'merge'
    });
  }

  getCategoryIcon(category: string): Observable<Pair<string, string>> {
    return this.categoryIcons$.pipe(
      map((iconMap) => {
        if (category === '') {
          return { left: iconMap['all'], right: 'All' };
        }
        return iconMap[category]
          ? { left: iconMap[category], right: category }
          : { left: iconMap.missing, right: 'Category does not match with the configuration' };
      })
    );
  }

  selectCategory(category: string) {
    this.selectedCategory.set(category);
  }

  setRequestInProgress(value: boolean) {
    this.requestInProgressService.setRequestInProgress(value);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
