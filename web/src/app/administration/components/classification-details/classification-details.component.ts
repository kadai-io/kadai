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

import { Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Select, Store } from '@ngxs/store';
import { Observable, Subject } from 'rxjs';

import { highlight } from 'app/shared/animations/validation.animation';

import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';

import { DomainService } from 'app/shared/services/domain/domain.service';
import { FormsModule, NgForm } from '@angular/forms';
import { FormsValidatorService } from 'app/shared/services/forms-validator/forms-validator.service';
import { ImportExportService } from 'app/administration/services/import-export.service';
import { map, take, takeUntil } from 'rxjs/operators';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';
import { ClassificationSelectors } from 'app/shared/store/classification-store/classification.selectors';
import { AsyncPipe, Location } from '@angular/common';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { ClassificationCategoryImages, CustomField, getCustomFields } from '../../../shared/models/customisation';
import { Classification } from '../../../shared/models/classification';
import { customFieldCount } from '../../../shared/models/classification-summary';

import {
  CopyClassification,
  DeselectClassification,
  RemoveSelectedClassification,
  RestoreSelectedClassification,
  SaveCreatedClassification,
  SaveModifiedClassification,
  SelectClassification
} from '../../../shared/store/classification-store/classification.actions';
import { Pair } from '../../../shared/models/pair';
import { trimForm } from '../../../shared/util/form-trimmer';
import { MatToolbar } from '@angular/material/toolbar';
import { MatTooltip } from '@angular/material/tooltip';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { MatDivider } from '@angular/material/divider';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { FieldErrorDisplayComponent } from '../../../shared/components/field-error-display/field-error-display.component';
import { MatSelect, MatSelectTrigger } from '@angular/material/select';
import { SvgIconComponent } from 'angular-svg-icon';
import { MatOption } from '@angular/material/core';

@Component({
  selector: 'kadai-administration-classification-details',
  templateUrl: './classification-details.component.html',
  animations: [highlight],
  styleUrls: ['./classification-details.component.scss'],
  imports: [
    MatToolbar,
    MatTooltip,
    MatButton,
    MatIcon,
    MatMenuTrigger,
    MatMenu,
    MatMenuItem,
    FormsModule,
    MatDivider,
    MatFormField,
    MatLabel,
    MatInput,
    CdkTextareaAutosize,
    FieldErrorDisplayComponent,
    MatSelect,
    MatSelectTrigger,
    SvgIconComponent,
    MatOption,
    AsyncPipe
  ]
})
export class ClassificationDetailsComponent implements OnInit, OnDestroy {
  classification: Classification;
  @Select(ClassificationSelectors.selectCategories) categories$: Observable<string[]>;
  @Select(EngineConfigurationSelectors.selectCategoryIcons) categoryIcons$: Observable<ClassificationCategoryImages>;
  @Select(ClassificationSelectors.selectedClassificationType) selectedClassificationType$: Observable<string>;
  @Select(ClassificationSelectors.selectedClassification) selectedClassification$: Observable<Classification>;
  @Select(ClassificationSelectors.getBadgeMessage) badgeMessage$: Observable<string>;
  customFields$: Observable<CustomField[]>;
  isCreatingNewClassification: boolean = false;
  readonly lengthError = 'You have reached the maximum length for this field';
  inputOverflowMap = new Map<string, boolean>();
  validateInputOverflow: Function;
  requestInProgress: boolean;
  @ViewChild('ClassificationForm') classificationForm: NgForm;
  toggleValidationMap = new Map<string, boolean>();
  destroy$ = new Subject<void>();
  private location = inject(Location);
  private requestInProgressService = inject(RequestInProgressService);
  private domainService = inject(DomainService);
  private formsValidatorService = inject(FormsValidatorService);
  private notificationsService = inject(NotificationService);
  private importExportService = inject(ImportExportService);
  private store = inject(Store);

  ngOnInit() {
    this.customFields$ = this.store.select(EngineConfigurationSelectors.classificationsCustomisation).pipe(
      map((customisation) => customisation.information),
      getCustomFields(customFieldCount)
    );

    this.selectedClassification$.pipe(takeUntil(this.destroy$)).subscribe((classification) => {
      this.classification = { ...classification };
      this.isCreatingNewClassification = typeof this.classification.classificationId === 'undefined';
    });

    this.importExportService
      .getImportingFinished()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.store.dispatch(new SelectClassification(this.classification.classificationId));
      });

    this.requestInProgressService
      .getRequestInProgress()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.requestInProgress = value;
      });

    this.formsValidatorService.inputOverflowObservable.pipe(takeUntil(this.destroy$)).subscribe((inputOverflowMap) => {
      this.inputOverflowMap = inputOverflowMap;
    });
    this.validateInputOverflow = (inputFieldModel, maxLength) => {
      this.formsValidatorService.validateInputOverflow(inputFieldModel, maxLength);
    };
  }

  isFieldValid(field: string): boolean {
    return this.formsValidatorService.isFieldValid(this.classificationForm, field);
  }

  onSubmit() {
    this.formsValidatorService.formSubmitAttempt = true;
    trimForm(this.classificationForm);
    this.formsValidatorService
      .validateFormInformation(this.classificationForm, this.toggleValidationMap)
      .then((value) => {
        if (value) {
          this.onSave();
        }
      });
  }

  onRestore() {
    this.formsValidatorService.formSubmitAttempt = false;
    this.store
      .dispatch(new RestoreSelectedClassification(this.classification.classificationId))
      .pipe(take(1))
      .subscribe(() => {
        this.notificationsService.showSuccess('CLASSIFICATION_RESTORE');
      });
  }

  onCopy() {
    if (this.isCreatingNewClassification) {
      this.notificationsService.showError('CLASSIFICATION_COPY_NOT_CREATED');
    } else {
      this.store.dispatch(new CopyClassification());
    }
  }

  onCloseClassification() {
    this.store.dispatch(new DeselectClassification());
  }

  getCategoryIcon(category: string): Observable<Pair<string, string>> {
    return this.categoryIcons$.pipe(
      map((iconMap) =>
        iconMap[category]
          ? { left: iconMap[category], right: category }
          : { left: iconMap.missing, right: 'Category does not match with the configuration' }
      )
    );
  }

  validChanged(): void {
    this.classification.isValidInDomain = !this.classification.isValidInDomain;
  }

  masterDomainSelected(): boolean {
    return this.domainService.getSelectedDomainValue() === '';
  }

  getClassificationCustom(customNumber: number): string {
    return `custom${customNumber}`;
  }

  async onSave() {
    this.requestInProgressService.setRequestInProgress(true);
    if (typeof this.classification.classificationId === 'undefined') {
      this.store
        .dispatch(new SaveCreatedClassification(this.classification))
        .pipe(take(1))
        .subscribe(() => {
          this.selectedClassification$.pipe(take(1)).subscribe((classification) => {
            this.notificationsService.showSuccess('CLASSIFICATION_CREATE', {
              classificationKey: classification.key
            });
            this.location.go(
              this.location
                .path()
                .replace(/(classifications).*/g, `classifications/(detail:${classification.classificationId})`)
            );
          });
          this.afterRequest();
        });
    } else {
      try {
        this.store
          .dispatch(new SaveModifiedClassification(this.classification))
          .pipe(take(1))
          .subscribe(() => {
            this.afterRequest();
            this.notificationsService.showSuccess('CLASSIFICATION_UPDATE', {
              classificationKey: this.classification.key
            });
          });
      } catch (error) {
        this.afterRequest();
      }
    }
  }

  onRemoveClassification() {
    this.notificationsService.showDialog(
      'CLASSIFICATION_DELETE',
      { classificationKey: this.classification.key },
      this.removeClassificationConfirmation.bind(this)
    );
  }

  removeClassificationConfirmation() {
    this.requestInProgressService.setRequestInProgress(true);

    this.store
      .dispatch(new RemoveSelectedClassification())
      .pipe(take(1))
      .subscribe(() => {
        this.notificationsService.showSuccess('CLASSIFICATION_REMOVE', { classificationKey: this.classification.key });
        this.afterRequest();
      });
    this.location.go(this.location.path().replace(/(classifications).*/g, 'classifications'));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private afterRequest() {
    this.requestInProgressService.setRequestInProgress(false);
  }
}
