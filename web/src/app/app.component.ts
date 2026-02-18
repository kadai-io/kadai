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

import { Component, HostListener, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { Subject } from 'rxjs';
import { FormsValidatorService } from 'app/shared/services/forms-validator/forms-validator.service';
import { SidenavService } from './shared/services/sidenav/sidenav.service';
import { RequestInProgressService } from './shared/services/request-in-progress/request-in-progress.service';
import { OrientationService } from './shared/services/orientation/orientation.service';
import { SelectedRouteService } from './shared/services/selected-route/selected-route';
import { KadaiEngineService } from './shared/services/kadai-engine/kadai-engine.service';
import { WindowRefService } from 'app/shared/services/window/window.service';
import { environment } from 'environments/environment';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { takeUntil } from 'rxjs/operators';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { UserInformationComponent } from './shared/components/user-information/user-information.component';
import { SidenavListComponent } from './shared/components/sidenav-list/sidenav-list.component';
import { NavBarComponent } from './shared/components/nav-bar/nav-bar.component';

import { MatProgressBar } from '@angular/material/progress-bar';

@Component({
  selector: 'kadai-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    MatIconButton,
    MatIcon,
    UserInformationComponent,
    SidenavListComponent,
    MatSidenavContent,
    NavBarComponent,
    MatProgressBar,
    RouterOutlet
  ]
})
export class AppComponent implements OnInit, OnDestroy {
  workbasketsRoute = true;
  selectedRoute = '';
  requestInProgress = false;
  version: string;
  toggle: boolean = false;
  destroy$ = new Subject<void>();
  @ViewChild('sidenav') public sidenav: MatSidenav;
  private router = inject(Router);
  private requestInProgressService = inject(RequestInProgressService);
  private orientationService = inject(OrientationService);
  private selectedRouteService = inject(SelectedRouteService);
  private formsValidatorService = inject(FormsValidatorService);
  private sidenavService = inject(SidenavService);
  private kadaiEngineService = inject(KadaiEngineService);
  private window = inject(WindowRefService);

  @HostListener('window:resize')
  onResize() {
    this.orientationService.onResize();
  }

  ngOnInit() {
    this.router.events.pipe(takeUntil(this.destroy$)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.selectedRouteService.selectRoute(event);
        this.formsValidatorService.formSubmitAttempt = false;
      }
    });

    this.requestInProgressService
      .getRequestInProgress()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value: boolean) => {
        setTimeout(() => {
          this.requestInProgress = value;
        });
      });

    this.selectedRouteService
      .getSelectedRoute()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value: string) => {
        if (value.indexOf('classifications') !== -1) {
          this.workbasketsRoute = false;
        }
        this.selectedRoute = value;
      });

    this.kadaiEngineService
      .getVersion()
      .pipe(takeUntil(this.destroy$))
      .subscribe((restVersion) => {
        this.version = restVersion.version;
      });
  }

  logout() {
    this.kadaiEngineService.logout();
    this.window.nativeWindow.location.href = environment.kadaiLogoutUrl;
  }

  toggleSidenav() {
    this.toggle = !this.toggle;
    this.sidenavService.toggleSidenav();
  }

  ngAfterViewInit(): void {
    this.sidenavService.setSidenav(this.sidenav);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
