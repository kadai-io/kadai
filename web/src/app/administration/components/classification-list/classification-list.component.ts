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
import { Observable, Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { Actions, ofActionCompleted, ofActionDispatched, Select, Store } from '@ngxs/store';

import { ImportExportService } from 'app/administration/services/import-export.service';

import { KadaiType } from 'app/shared/models/kadai-type';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { ClassificationSelectors } from 'app/shared/store/classification-store/classification.selectors';
import { AsyncPipe, Location } from '@angular/common';
import { ClassificationCategoryImages } from '../../../shared/models/customisation';

import {
  CreateClassification,
  GetClassifications
} from '../../../shared/store/classification-store/classification.actions';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { ClassificationSummary } from '../../../shared/models/classification-summary';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { Pair } from '../../../shared/models/pair';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { ImportExportComponent } from '../import-export/import-export.component';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { SvgIconComponent } from 'angular-svg-icon';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { ClassificationTypesSelectorComponent } from '../classification-types-selector/classification-types-selector.component';
import { KadaiTreeComponent } from '../tree/tree.component';

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
  requestInProgress = true;
  inputValue: string;
  selectedCategory = '';
  @Select(ClassificationSelectors.classificationTypes) classificationTypes$: Observable<string[]>;
  @Select(ClassificationSelectors.selectedClassificationType) classificationTypeSelected$: Observable<string>;
  @Select(ClassificationSelectors.selectCategories) categories$: Observable<string[]>;
  @Select(ClassificationSelectors.classifications) classifications$: Observable<ClassificationSummary[]>;
  @Select(EngineConfigurationSelectors.selectCategoryIcons) categoryIcons$: Observable<ClassificationCategoryImages>;
  destroy$ = new Subject<void>();
  classifications: ClassificationSummary[];
  private location = inject(Location);
  private importExportService = inject(ImportExportService);
  private domainService = inject(DomainService);
  private requestInProgressService = inject(RequestInProgressService);
  private store = inject(Store);
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
      this.classifications = classifications;
    });

    this.classificationTypeSelected$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.store.dispatch(new GetClassifications());
      this.selectedCategory = '';
    });

    this.importExportService
      .getImportingFinished()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.store.dispatch(new GetClassifications());
      });

    // needed, so that the list updates, when domain gets changed (could be placed anywhere and should be removed, when domain is in store)
    this.domainService
      .getSelectedDomain()
      .pipe(takeUntil(this.destroy$))
      .subscribe((domain) => {
        this.store.dispatch(GetClassifications);
      });

    this.requestInProgressService
      .getRequestInProgress()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.requestInProgress = value;
      });
  }

  addClassification() {
    this.store.dispatch(new CreateClassification());
    this.location.go(this.location.path().replace(/(classifications).*/g, 'classifications/new-classification'));
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
    this.selectedCategory = category;
  }

  setRequestInProgress(value: boolean) {
    this.requestInProgressService.setRequestInProgress(value);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
