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
import { Router, NavigationStart, RouterEvent } from '@angular/router';
import { MasterAndDetailService } from 'app/shared/services/master-and-detail/master-and-detail.service';

@Component({
  selector: 'kadai-shared-master-and-detail',
  templateUrl: './master-and-detail.component.html',
  styleUrls: ['./master-and-detail.component.scss']
})
export class MasterAndDetailComponent implements OnInit {
  private classifications = 'classifications';
  private workbaskets = 'workbaskets';
  private tasks = 'tasks';
  private detailRoutes: Array<string> = ['/workbaskets/(detail', 'classifications/(detail', 'tasks/(detail'];

  showDetail = false;
  currentRoute = '';
  constructor(
    private router: Router,
    private masterAndDetailService: MasterAndDetailService
  ) {}

  ngOnInit(): void {
    this.showDetail = this.showDetails();
    this.masterAndDetailService.setShowDetail(this.showDetail);
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.showDetail = this.showDetails(event);
        this.masterAndDetailService.setShowDetail(this.showDetail);
      }
    });
  }

  private showDetails(event?: RouterEvent): boolean {
    if (!event) {
      return this.checkUrl(this.router.url);
    }
    return this.checkUrl(event.url);
  }

  private checkUrl(url: string): boolean {
    this.checkRoute(url);
    return this.detailRoutes.some((routeDetail) => url.indexOf(routeDetail) !== -1);
  }

  private checkRoute(url: string) {
    if (url.indexOf(this.workbaskets) !== -1) {
      this.currentRoute = this.workbaskets;
    } else if (url.indexOf(this.classifications) !== -1) {
      this.currentRoute = this.classifications;
    } else if (url.indexOf(this.tasks) !== -1) {
      this.currentRoute = this.tasks;
    }
  }
}
