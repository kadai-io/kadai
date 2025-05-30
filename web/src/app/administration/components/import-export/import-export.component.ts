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

import { Component, inject, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ClassificationDefinitionService } from 'app/administration/services/classification-definition.service';
import { WorkbasketDefinitionService } from 'app/administration/services/workbasket-definition.service';
import { DomainService } from 'app/shared/services/domain/domain.service';
import { KadaiType } from 'app/shared/models/kadai-type';
import { ImportExportService } from 'app/administration/services/import-export.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { Observable, Subject } from 'rxjs';
import { HotToastService } from '@ngneat/hot-toast';
import { takeUntil } from 'rxjs/operators';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';
import { AsyncPipe } from '@angular/common';

/**
 * Recommendation: Turn this component into presentational component - no logic, instead events are
 * fired back to parent components with @Output(). This way the logic of exporting/importing workbasket
 * or classification is stored in their respective container component.
 */
@Component({
  selector: 'kadai-administration-import-export',
  templateUrl: './import-export.component.html',
  styleUrls: ['./import-export.component.scss'],
  imports: [MatButton, MatTooltip, MatIcon, FormsModule, MatMenuTrigger, MatMenu, MatMenuItem, AsyncPipe]
})
export class ImportExportComponent implements OnInit, OnDestroy {
  @Input() currentSelection: KadaiType;
  @Input() parentComponent: string;
  @ViewChild('selectedFile', { static: true })
  selectedFileInput;
  domains$: Observable<string[]>;
  destroy$ = new Subject<void>();
  private domainService = inject(DomainService);
  private workbasketDefinitionService = inject(WorkbasketDefinitionService);
  private classificationDefinitionService = inject(ClassificationDefinitionService);
  private notificationService = inject(NotificationService);
  private importExportService = inject(ImportExportService);
  private hotToastService = inject(HotToastService);

  ngOnInit() {
    this.domains$ = this.domainService.getDomains();
  }

  export(domain = '') {
    if (this.currentSelection === KadaiType.WORKBASKETS) {
      this.workbasketDefinitionService.exportWorkbaskets(domain);
    } else {
      this.classificationDefinitionService.exportClassifications(domain);
    }
  }

  uploadFile() {
    const file = this.selectedFileInput.nativeElement.files[0];
    if (this.checkFormatFile(file)) {
      if (this.currentSelection === KadaiType.WORKBASKETS) {
        this.workbasketDefinitionService
          .importWorkbasket(file)
          .pipe(
            takeUntil(this.destroy$),
            this.hotToastService.observe({
              loading: 'Uploading...',
              success: 'File successfully uploaded',
              error: 'Upload failed'
            })
          )
          .subscribe({
            next: () => {
              this.importExportService.setImportingFinished(true);
            }
          });
      } else {
        this.classificationDefinitionService
          .importClassification(file)
          .pipe(
            takeUntil(this.destroy$),
            this.hotToastService.observe({
              loading: 'Uploading...',
              success: 'File successfully uploaded',
              error: 'Upload failed'
            })
          )
          .subscribe({
            next: () => {
              this.importExportService.setImportingFinished(true);
            }
          });
      }
    }
    this.resetProgress();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private checkFormatFile(file): boolean {
    const ending = file.name.match(/\.([^.]+)$/)[1];
    let check = false;
    if (ending === 'json') {
      check = true;
    } else {
      file.value = '';
      this.notificationService.showError('IMPORT_EXPORT_UPLOAD_FILE_FORMAT');
    }
    return check;
  }

  private resetProgress() {
    this.selectedFileInput.nativeElement.value = '';
  }
}
