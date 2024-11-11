/*
 * Copyright [2024] [envite consulting GmbH]
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

import { CommonModule } from '@angular/common';
import { inject, NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpHeaders,
  HttpInterceptorFn,
  HttpRequest,
  HttpXsrfTokenExtractor,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { RouterModule } from '@angular/router';
import { AlertModule } from 'ngx-bootstrap/alert';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { provideHotToastConfig } from '@ngneat/hot-toast';
import { AccordionModule } from 'ngx-bootstrap/accordion';

/**
 * Components
 */
import { SpinnerComponent } from 'app/shared/components/spinner/spinner.component';
import { MasterAndDetailComponent } from 'app/shared/components/master-and-detail/master-and-detail.component';
import { KadaiTreeComponent } from 'app/administration/components/tree/tree.component';
import { IconTypeComponent } from 'app/administration/components/type-icon/icon-type.component';
import { FieldErrorDisplayComponent } from 'app/shared/components/field-error-display/field-error-display.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { SortComponent } from './components/sort/sort.component';
import { PaginationComponent } from './components/pagination/pagination.component';
import { ProgressSpinnerComponent } from './components/progress-spinner/progress-spinner.component';
import { TypeAheadComponent } from './components/type-ahead/type-ahead.component';

/**
 * Pipes
 */
import { MapValuesPipe } from './pipes/map-values.pipe';
import { RemoveNoneTypePipe } from './pipes/remove-empty-type.pipe';
import { SpreadNumberPipe } from './pipes/spread-number.pipe';
import { OrderBy } from './pipes/order-by.pipe';
import { MapToIterable } from './pipes/map-to-iterable.pipe';
import { NumberToArray } from './pipes/number-to-array.pipe';
import { DateTimeZonePipe } from './pipes/date-time-zone.pipe';

/**
 * Services
 */
import { DialogPopUpComponent } from './components/popup/dialog-pop-up.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { WorkbasketFilterComponent } from './components/workbasket-filter/workbasket-filter.component';
import { TaskFilterComponent } from './components/task-filter/task-filter.component';
import { WorkbasketService } from 'app/shared/services/workbasket/workbasket.service';
import { ClassificationsService } from 'app/shared/services/classifications/classifications.service';
import { ObtainMessageService } from './services/obtain-message/obtain-message.service';
import { AccessIdsService } from './services/access-ids/access-ids.service';
import { DragAndDropDirective } from './directives/drag-and-drop.directive';
import { ResizableWidthDirective } from './directives/resizable-width.directive';
import { TreeModule } from '@ali-hm/angular-tree-component';
import { environment } from '../../environments/environment';
import { tap } from 'rxjs/operators';
import { RequestInProgressService } from './services/request-in-progress/request-in-progress.service';
import { NotificationService } from './services/notifications/notification.service';

const MODULES = [
  CommonModule,
  FormsModule,
  AlertModule.forRoot(),
  TypeaheadModule.forRoot(),
  AccordionModule.forRoot(),
  BsDatepickerModule.forRoot(),
  AngularSvgIconModule,
  MatDialogModule,
  MatButtonModule,
  RouterModule,
  TreeModule,
  MatAutocompleteModule
];

const DECLARATIONS = [
  SpinnerComponent,
  MasterAndDetailComponent,
  KadaiTreeComponent,
  TypeAheadComponent,
  MapValuesPipe,
  RemoveNoneTypePipe,
  SpreadNumberPipe,
  DateTimeZonePipe,
  NumberToArray,
  OrderBy,
  MapToIterable,
  SortComponent,
  IconTypeComponent,
  FieldErrorDisplayComponent,
  PaginationComponent,
  ProgressSpinnerComponent,
  DialogPopUpComponent,
  WorkbasketFilterComponent,
  TaskFilterComponent,
  DragAndDropDirective,
  ResizableWidthDirective
];

export const httpClientInterceptor: HttpInterceptorFn = (request: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const requestInProgressService = inject(RequestInProgressService);
  const tokenExtractor = inject(HttpXsrfTokenExtractor);
  const notificationService = inject(NotificationService);

  let req = request.clone();
  if (req.headers.get('Content-Type') === 'multipart/form-data') {
    const headers = new HttpHeaders();
    req = req.clone({ headers });
  } else {
    req = req.clone({ setHeaders: { 'Content-Type': 'application/hal+json' } });
  }
  let token = tokenExtractor.getToken() as string;
  if (token !== null) {
    req = req.clone({ setHeaders: { 'X-XSRF-TOKEN': token } });
  }
  if (!environment.production) {
    req = req.clone({ headers: req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=') });
  }
  return next(req).pipe(
    tap(
      () => {},
      (error) => {
        requestInProgressService.setRequestInProgress(false);
        if (
          error.status !== 404 &&
          (!(error instanceof HttpErrorResponse) || error.url.indexOf('environment-information.json') === -1)
        ) {
          const { key, messageVariables } = error.error.error || {
            key: 'FALLBACK',
            messageVariables: {}
          };
          notificationService.showError(key, messageVariables);
        }
      }
    )
  );
};

@NgModule({
  declarations: [DECLARATIONS],
  imports: [
    MODULES,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule
  ],
  exports: [DECLARATIONS],
  providers: [
    AccessIdsService,
    ClassificationsService,
    WorkbasketService,
    ObtainMessageService,
    provideHttpClient(withInterceptors([httpClientInterceptor])),
    provideHotToastConfig({
      style: {
        'max-width': '520px'
      }
    })
  ]
})
export class SharedModule {}
