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

import { Component, OnInit } from '@angular/core';
import { SelectedRouteService } from 'app/shared/services/selected-route/selected-route';
import { Subject } from 'rxjs';
import { expandRight } from 'app/shared/animations/expand.animation';
import { SidenavService } from '../../services/sidenav/sidenav.service';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'kadai-shared-nav-bar',
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.scss'],
  animations: [expandRight],
  standalone: false
})
export class NavBarComponent implements OnInit {
  selectedRoute = '';
  titleAdministration = 'Administration';
  titleMonitor = 'Monitor';
  titleWorkplace = 'Workplace';
  titleHistory = 'History';
  titleSettings = 'Settings';
  toggle: boolean = false;
  title = '';

  destroy$ = new Subject();

  constructor(
    private selectedRouteService: SelectedRouteService,
    private sidenavService: SidenavService
  ) {}

  ngOnInit() {
    this.selectedRouteService
      .getSelectedRoute()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value: string) => {
        this.selectedRoute = value;
        this.setTitle(value);
      });
  }

  toggleSidenav() {
    this.toggle = !this.toggle;
    this.sidenavService.toggleSidenav();
  }

  setTitle(value: string = '') {
    if (value.includes('administration')) {
      this.title = this.titleAdministration;
    } else if (value.includes('monitor')) {
      this.title = this.titleMonitor;
    } else if (value.includes('workplace')) {
      this.title = this.titleWorkplace;
    } else if (value.includes('history')) {
      this.title = this.titleHistory;
    } else if (value.includes('settings')) {
      this.title = this.titleSettings;
    }
  }
}
