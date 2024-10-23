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
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { WorkbasketDefinition } from 'app/shared/models/workbasket-definition';
import { KadaiDate } from 'app/shared/util/kadai.date';
import { BlobGenerator } from 'app/shared/util/blob-generator';
import { take } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { StartupService } from '../../shared/services/startup/startup.service';

@Injectable()
export class WorkbasketDefinitionService {
  constructor(
    private httpClient: HttpClient,
    private startupService: StartupService
  ) {}

  get url(): string {
    return this.startupService.getKadaiRestUrl() + '/v1/workbasket-definitions';
  }

  // GET
  exportWorkbaskets(domain: string): Observable<WorkbasketDefinition[]> {
    const domainRequest = domain === '' ? domain : `?domain=${domain}`;
    const workbasketDefObservable = this.httpClient.get<WorkbasketDefinition[]>(this.url + domainRequest).pipe(take(1));
    workbasketDefObservable.subscribe((workbasketDefinitions) =>
      BlobGenerator.saveFile(workbasketDefinitions, `Workbaskets_${KadaiDate.getDate()}.json`)
    );
    return workbasketDefObservable;
  }

  importWorkbasket(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    const headers = new HttpHeaders().set('Content-Type', 'multipart/form-data');
    return this.httpClient.post(this.url, formData, { headers });
  }
}
