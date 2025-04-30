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

import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { StartupService } from 'app/shared/services/startup/startup.service';

@Injectable({
  providedIn: 'root'
})
export class RoutingUploadService {
  private httpClient = inject(HttpClient);
  private startupService = inject(StartupService);

  get url(): string {
    return this.startupService.getKadaiRestUrl() + '/v1/routing-rules/default';
  }

  uploadRoutingRules(file: File) {
    const formData = new FormData();
    formData.append('excelRoutingFile', file);
    const headers = new HttpHeaders().set('Content-Type', 'multipart/form-data');
    return this.httpClient.put(this.url, formData, { headers });
  }
}
