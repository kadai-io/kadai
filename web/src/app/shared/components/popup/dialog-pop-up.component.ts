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
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent } from '@angular/material/dialog';
import { ObtainMessageService } from '../../services/obtain-message/obtain-message.service';
import { messageTypes } from '../../services/obtain-message/message-types';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'kadai-shared-dialog-pop-up',
  templateUrl: './dialog-pop-up.component.html',
  styleUrls: ['./dialog-pop-up.component.scss'],
  imports: [MatDialogContent, MatDialogActions, MatButton, MatDialogClose]
})
export class DialogPopUpComponent implements OnInit {
  message: string;
  callback: Function;
  isDataSpecified: boolean;
  private data = inject(MAT_DIALOG_DATA);
  private obtainMessageService = inject(ObtainMessageService);

  ngOnInit() {
    this.isDataSpecified = this.data?.message && this.data?.callback;
    if (this.isDataSpecified) {
      this.message = this.data.message;
      this.callback = this.data.callback;
    } else {
      this.message = this.obtainMessageService.getMessage('POPUP_CONFIGURATION', {}, messageTypes.DIALOG);
    }
  }
}
