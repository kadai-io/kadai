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

import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpHeaders,
  HttpInterceptor,
  HttpRequest,
  HttpXsrfTokenExtractor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { environment } from 'environments/environment';
import { tap } from 'rxjs/operators';
import { NotificationService } from '../notifications/notification.service';

@Injectable()
export class HttpClientInterceptor implements HttpInterceptor {
  constructor(
    private requestInProgressService: RequestInProgressService,
    private tokenExtractor: HttpXsrfTokenExtractor,
    private notificationService: NotificationService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let req = request.clone();
    if (req.headers.get('Content-Type') === 'multipart/form-data') {
      const headers = new HttpHeaders();
      req = req.clone({ headers });
    } else {
      req = req.clone({ setHeaders: { 'Content-Type': 'application/hal+json' } });
    }
    let token = this.tokenExtractor.getToken() as string;
    if (token !== null) {
      req = req.clone({ setHeaders: { 'X-XSRF-TOKEN': token } });
    }
    if (!environment.production) {
      req = req.clone({ headers: req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=') });
    }
    return next.handle(req).pipe(
      tap(
        () => {},
        (error) => {
          this.requestInProgressService.setRequestInProgress(false);
          if (
            error.status !== 404 &&
            (!(error instanceof HttpErrorResponse) || error.url.indexOf('environment-information.json') === -1)
          ) {
            const { key, messageVariables } = error.error.error || {
              key: 'FALLBACK',
              messageVariables: {}
            };
            this.notificationService.showError(key, messageVariables);
          }
        }
      )
    );
  }
}
