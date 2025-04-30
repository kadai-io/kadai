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
import { MatDialog } from '@angular/material/dialog';
import { DialogPopUpComponent } from '../../components/popup/dialog-pop-up.component';
import { HotToastService } from '@ngneat/hot-toast';
import { ObtainMessageService } from '../obtain-message/obtain-message.service';
import { messageTypes } from '../obtain-message/message-types';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private popup = inject(MatDialog);
  private toastService = inject(HotToastService);
  private obtainMessageService = inject(ObtainMessageService);

  generateToastId(errorKey: string, messageVariables: object): string {
    let id = errorKey;
    for (const [replacementKey, value] of Object.entries(messageVariables)) {
      id = id.concat(replacementKey, value);
    }
    return id;
  }

  showError(errorKey: string, messageVariables: object = {}) {
    this.toastService.error(this.obtainMessageService.getMessage(errorKey, messageVariables, messageTypes.ERROR), {
      dismissible: true,
      autoClose: false,
      id: this.generateToastId(errorKey, messageVariables)
    });
  }

  showSuccess(successKey: string, messageVariables: object = {}) {
    this.toastService.success(
      this.obtainMessageService.getMessage(successKey, messageVariables, messageTypes.SUCCESS),
      {
        duration: 5000
      }
    );
  }

  showInformation(informationKey: string, messageVariables: object = {}) {
    this.toastService.show(
      `
    <span class="material-icons">info</span> ${this.obtainMessageService.getMessage(
      informationKey,
      messageVariables,
      messageTypes.INFORMATION
    )}
  `,
      // prevents duplicated toast because of double call in task-master
      // TODO: delete while frontend refactoring
      { id: 'empty-workbasket' }
    );
  }

  showWarning(warningKey: string, messageVariables: object = {}) {
    this.toastService.warning(this.obtainMessageService.getMessage(warningKey, messageVariables, messageTypes.WARNING));
  }

  showDialog(key: string, messageVariables: object = {}, callback: Function) {
    const message = this.obtainMessageService.getMessage(key, messageVariables, messageTypes.DIALOG);

    const ref = this.popup.open(DialogPopUpComponent, {
      data: { message: message, callback },
      backdropClass: 'backdrop',
      position: { top: '5em' },
      autoFocus: true,
      maxWidth: '50em'
    });
    ref.beforeClosed().subscribe((call) => {
      if (typeof call === 'function') {
        call();
      }
    });
    return ref;
  }
}
