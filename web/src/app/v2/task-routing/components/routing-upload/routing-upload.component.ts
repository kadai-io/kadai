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

import { Component, inject, OnInit } from '@angular/core';
import { RoutingUploadService } from '@task-routing/services/routing-upload.service';
import { NotificationService } from 'app/shared/services/notifications/notification.service';
import { HotToastService } from '@ngneat/hot-toast';

@Component({
  selector: 'kadai-routing-upload',
  templateUrl: './routing-upload.component.html',
  styleUrls: ['./routing-upload.component.scss'],
  imports: []
})
export class RoutingUploadComponent implements OnInit {
  file: File | null = null;
  private routingUploadService = inject(RoutingUploadService);
  private toastService = inject(HotToastService);
  private notificationService = inject(NotificationService);

  ngOnInit(): void {}

  onFileChanged(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.file = input.files[0];
    this.upload(input.files);
  }

  upload(fileList?: FileList) {
    if (typeof fileList !== 'undefined') {
      this.file = fileList[0];
    }
    this.routingUploadService.uploadRoutingRules(this.file).subscribe({
      next: (res: { amountOfImportedRow: number; result: string }) => this.toastService.success(res.result),
      error: (err) => {
        this.notificationService.showError(err.error.message.key);
        this.clearInput();
      },
      complete: () => this.clearInput()
    });
  }

  clearInput() {
    const inputElement = document.getElementById('routingUpload') as HTMLInputElement;
    inputElement.value = '';
    this.file = null;
  }
}
