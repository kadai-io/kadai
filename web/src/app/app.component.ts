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

import { Component, inject, OnDestroy, OnInit, viewChild } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { Store } from '@ngxs/store';
import { DomainSelectors } from './shared/store/domain-store/domain.selectors';
import { SelectDomain } from './shared/store/domain-store/domain.actions';
import { DOMAIN_QUERY_PARAM } from './shared/store/domain-store/domain.state';
import { firstValueFrom, of, Subject } from 'rxjs';
import { SidenavService } from './shared/services/sidenav/sidenav.service';
import { RequestInProgressService } from './shared/services/request-in-progress/request-in-progress.service';
import { OrientationService } from './shared/services/orientation/orientation.service';
import { SelectedRouteService } from './shared/services/selected-route/selected-route';
import { KadaiEngineService } from './shared/services/kadai-engine/kadai-engine.service';
import { WindowRefService } from 'app/shared/services/window/window.service';
import { RuntimeConfigService } from 'app/shared/services/runtime-config/runtime-config.service';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { catchError, map, takeUntil } from 'rxjs/operators';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { UserInformationComponent } from './shared/components/user-information/user-information.component';
import { SidenavListComponent } from './shared/components/sidenav-list/sidenav-list.component';
import { NavBarComponent } from './shared/components/nav-bar/nav-bar.component';
import { MatProgressBar } from '@angular/material/progress-bar';
import { toSignal } from '@angular/core/rxjs-interop';

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
  ],
  host: {
    '(window:resize)': 'onResize()'
  }
})
export class AppComponent implements OnInit, OnDestroy {
  requestInProgress = toSignal(inject(RequestInProgressService).getRequestInProgress(), { initialValue: false });
  version = toSignal(
    inject(KadaiEngineService)
      .getVersion()
      .pipe(
        map((r) => r.version),
        catchError(() => of(''))
      ),
    { initialValue: '' }
  );
  toggle: boolean = false;
  destroy$ = new Subject<void>();
  public sidenav = viewChild<MatSidenav>('sidenav');
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private store = inject(Store);
  private orientationService = inject(OrientationService);
  private sidenavService = inject(SidenavService);
  private kadaiEngineService = inject(KadaiEngineService);
  private selectedRouteService = inject(SelectedRouteService);
  private runtimeConfigService = inject(RuntimeConfigService);
  private window = inject(WindowRefService);

  onResize() {
    this.orientationService.onResize();
  }

  ngOnInit() {
    this.router.events.pipe(takeUntil(this.destroy$)).subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.selectedRouteService.selectRoute(event);
      }
    });

    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const fromUrl = params[DOMAIN_QUERY_PARAM];
      const offered = this.store.selectSnapshot(DomainSelectors.domains);
      if (fromUrl === undefined || !offered.includes(fromUrl)) {
        return;
      }
      if (fromUrl !== this.store.selectSnapshot(DomainSelectors.selectedDomain)) {
        this.store.dispatch(new SelectDomain(fromUrl));
      }
    });

    // A url naming a domain that is not offered is corrected rather than obeyed, and the correction
    // replaces the entry so that going back does not walk through domains the user never chose.
    this.store
      .select(DomainSelectors.selectedDomain)
      .pipe(takeUntil(this.destroy$))
      .subscribe((domain) => {
        if (domain !== undefined && this.route.snapshot.queryParams[DOMAIN_QUERY_PARAM] !== domain) {
          this.router.navigate([], {
            queryParams: { [DOMAIN_QUERY_PARAM]: domain },
            queryParamsHandling: 'merge',
            replaceUrl: true
          });
        }
      });
  }

  async logout(): Promise<void> {
    try {
      await firstValueFrom(this.kadaiEngineService.logout());
    } finally {
      this.window.nativeWindow.location.href = this.runtimeConfigService.kadaiLogoutUrl;
    }
  }

  toggleSidenav() {
    this.toggle = !this.toggle;
    this.sidenavService.toggleSidenav();
  }

  ngAfterViewInit(): void {
    this.sidenavService.setSidenav(this.sidenav()!);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
