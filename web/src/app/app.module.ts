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

/**
 * Modules
 */
import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgxsModule } from '@ngxs/store';
import { NgxsReduxDevtoolsPluginModule } from '@ngxs/devtools-plugin';
import { AlertModule } from 'ngx-bootstrap/alert';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { SharedModule } from 'app/shared/shared.module';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/**
 * Services
 */
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { OrientationService } from 'app/shared/services/orientation/orientation.service';
import { SelectedRouteService } from 'app/shared/services/selected-route/selected-route';
import { DomainService } from 'app/shared/services/domain/domain.service';
import { StartupService } from 'app/shared/services/startup/startup.service';
import { MasterAndDetailService } from 'app/shared/services/master-and-detail/master-and-detail.service';
import { WindowRefService } from 'app/shared/services/window/window.service';
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
import { NavBarComponent } from 'app/shared/components/nav-bar/nav-bar.component';
import { UserInformationComponent } from 'app/shared/components/user-information/user-information.component';
import { NoAccessComponent } from 'app/shared/components/no-access/no-access.component';
import { FormsValidatorService } from './shared/services/forms-validator/forms-validator.service';
import { NotificationService } from './shared/services/notifications/notification.service';
import { SidenavService } from './shared/services/sidenav/sidenav.service';
import { SidenavListComponent } from 'app/shared/components/sidenav-list/sidenav-list.component';
/**
 * Components
 */
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
/**
 * Store
 */
import { ClassificationCategoriesService } from './shared/services/classification-categories/classification-categories.service';
import { environment } from '../environments/environment';
import { STATES } from './shared/store';
import { NgxsRouterPluginModule } from '@ngxs/router-plugin';
import { TreeModule } from '@ali-hm/angular-tree-component';
import { provideHttpClient, withXsrfConfiguration } from '@angular/common/http';

const DECLARATIONS = [AppComponent, NavBarComponent, UserInformationComponent, NoAccessComponent, SidenavListComponent];

const MODULES = [
  TabsModule.forRoot(),
  AlertModule.forRoot(),
  BrowserModule,
  FormsModule,
  AppRoutingModule,
  AngularSvgIconModule.forRoot(),
  BrowserAnimationsModule,
  ReactiveFormsModule,
  TreeModule,
  SharedModule,
  MatSidenavModule,
  MatCheckboxModule,
  MatGridListModule,
  MatListModule,
  MatButtonModule,
  MatIconModule,
  MatSelectModule,
  MatToolbarModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  NgxsModule.forRoot(STATES, { developmentMode: !environment.production }),
  NgxsReduxDevtoolsPluginModule.forRoot({ disabled: environment.production, maxAge: 25 }),
  NgxsRouterPluginModule.forRoot()
];

const PROVIDERS = [
  WindowRefService,
  DomainService,
  RequestInProgressService,
  OrientationService,
  SelectedRouteService,
  StartupService,
  MasterAndDetailService,
  KadaiEngineService,
  FormsValidatorService,
  NotificationService,
  ClassificationCategoriesService,
  SidenavService,
  {
    provide: APP_INITIALIZER,
    useFactory: startupServiceFactory,
    deps: [StartupService],
    multi: true
  },
  provideHttpClient(withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' }))
];

@NgModule({
  declarations: DECLARATIONS,
  imports: MODULES,
  providers: PROVIDERS,
  bootstrap: [AppComponent]
})
export class AppModule {}

export function startupServiceFactory(startupService: StartupService): () => Promise<any> {
  return (): Promise<any> => startupService.load();
}
