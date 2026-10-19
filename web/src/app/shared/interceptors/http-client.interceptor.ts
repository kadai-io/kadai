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

import { inject } from '@angular/core';
import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpHeaders,
  HttpInterceptorFn,
  HttpRequest,
  HttpXsrfTokenExtractor
} from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { environment } from 'environments/environment';
import { NotificationService } from '../services/notifications/notification.service';
import { RequestInProgressService } from '../services/request-in-progress/request-in-progress.service';

interface ErrorNotification {
  key: string;
  messageVariables: object;
}

const FALLBACK_NOTIFICATION: ErrorNotification = { key: 'FALLBACK', messageVariables: {} };

export const tokenInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  let request = req;
  if (!environment.production) {
    request = req.clone({ headers: req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=') });
  }
  return next(request);
};

export const httpClientInterceptor: HttpInterceptorFn = (request: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const tokenExtractor = inject(HttpXsrfTokenExtractor);
  const notificationService = inject(NotificationService);
  const requestInProgressService = inject(RequestInProgressService);

  let req = request.clone();
  if (req.headers.get('Content-Type') === 'multipart/form-data') {
    const headers = new HttpHeaders();
    req = req.clone({ headers });
  } else {
    req = req.clone({ setHeaders: { 'Content-Type': 'application/hal+json' } });
  }
  let token = tokenExtractor.getToken();
  if (token !== null) {
    req = req.clone({ setHeaders: { 'X-XSRF-TOKEN': token } });
  }
  if (!environment.production) {
    req = req.clone({ headers: req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=') });
  }
  return next(req).pipe(
    tap({
      error: (error) => {
        requestInProgressService.setRequestInProgress(false);

        if (
          error.status !== 404 &&
          (!(error instanceof HttpErrorResponse) || (error.url ?? '').indexOf('environment-information.json') === -1)
        ) {
          const { key, messageVariables } = toErrorNotification(error);
          notificationService.showError(key, messageVariables);
        }
      }
    })
  );
};

// The error body is not guaranteed to be a KADAI error object: it can be empty (e.g. a 401 without body),
// plain text or HTML. Reading it must never throw, otherwise the original HTTP error is replaced.
function toErrorNotification(error: unknown): ErrorNotification {
  const body = error instanceof HttpErrorResponse ? error.error : undefined;
  const kadaiError = isObject(body) ? body['error'] : undefined;
  if (!isObject(kadaiError) || typeof kadaiError['key'] !== 'string') {
    return FALLBACK_NOTIFICATION;
  }
  return {
    key: kadaiError['key'],
    messageVariables: isObject(kadaiError['messageVariables']) ? kadaiError['messageVariables'] : {}
  };
}

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}
