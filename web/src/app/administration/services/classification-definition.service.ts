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

import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KadaiDate } from 'app/shared/util/kadai.date';
import { BlobGenerator } from 'app/shared/util/blob-generator';
import { Classification } from '../../shared/models/classification';
import { StartupService } from '../../shared/services/startup/startup.service';
import { take } from 'rxjs/operators';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ClassificationDefinitionService {
  private httpClient = inject(HttpClient);
  private startupService = inject(StartupService);

  get url(): string {
    return this.startupService.getKadaiRestUrl() + '/v1/classification-definitions';
  }

  // GET
  exportClassifications(domain: string): Observable<Classification[]> {
    const domainRequest = domain === '' ? domain : `?domain=${domain}`;
    const classificationDefObservable = this.httpClient.get<Classification[]>(this.url + domainRequest).pipe(take(1));
    classificationDefObservable.subscribe((classificationDefinitions) =>
      BlobGenerator.saveFile(classificationDefinitions, `Classifications_${KadaiDate.getDate()}.json`)
    );
    return classificationDefObservable;
  }

  importClassification(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    const headers = new HttpHeaders().set('Content-Type', 'multipart/form-data');
    return this.httpClient.post(this.url, formData, { headers });
  }
}
