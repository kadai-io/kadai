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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AppComponent } from './app.component';
import { RouterTestingModule } from '@angular/router/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { KadaiEngineService } from './shared/services/kadai-engine/kadai-engine.service';
import { RequestInProgressService } from './shared/services/request-in-progress/request-in-progress.service';
import { SelectedRouteService } from './shared/services/selected-route/selected-route';
import { SidenavService } from './shared/services/sidenav/sidenav.service';
import { of, Subject } from 'rxjs';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { OrientationService } from './shared/services/orientation/orientation.service';
import { WindowRefService } from './shared/services/window/window.service';
import { Router } from '@angular/router';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let httpController: HttpTestingController;

  const kadaiEngineServiceMock = {
    getVersion: vi.fn().mockReturnValue(of({ version: '1.0.0' })),
    logout: vi.fn().mockReturnValue(of(null)),
    isHistoryProviderEnabled: vi.fn().mockReturnValue(of(false)),
    isCustomRoutingRulesEnabled: vi.fn().mockReturnValue(of(false)),
    hasRole: vi.fn().mockReturnValue(false),
    getUserInformation: vi.fn().mockResolvedValue(undefined),
    currentUserInfo: null
  };

  const requestInProgressServiceMock = {
    getRequestInProgress: vi.fn().mockReturnValue(of(false)),
    setRequestInProgress: vi.fn()
  };

  const selectedRouteServiceMock = {
    selectedRouteTriggered: new Subject<string>(),
    selectRoute: vi.fn(),
    getSelectedRoute: vi.fn().mockReturnValue(of('workplace'))
  };

  const sidenavServiceMock = {
    toggleSidenav: vi.fn(),
    setSidenav: vi.fn(),
    state: false
  };

  const orientationServiceMock = {
    onResize: vi.fn()
  };

  const windowRefServiceMock = {
    nativeWindow: { location: { href: '' } }
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [AppComponent, RouterTestingModule],
      providers: [
        { provide: KadaiEngineService, useValue: kadaiEngineServiceMock },
        { provide: RequestInProgressService, useValue: requestInProgressServiceMock },
        { provide: SelectedRouteService, useValue: selectedRouteServiceMock },
        { provide: SidenavService, useValue: sidenavServiceMock },
        { provide: OrientationService, useValue: orientationServiceMock },
        { provide: WindowRefService, useValue: windowRefServiceMock },
        provideAngularSvgIcon(),
        provideNoopAnimations(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    httpController = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  afterEach(() => {
    httpController.match(() => true).forEach((req) => req.flush(''));
    vi.restoreAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle toggle property when toggleSidenav is called', () => {
    expect(component.toggle).toBe(false);
    component.toggleSidenav();
    expect(component.toggle).toBe(true);
    component.toggleSidenav();
    expect(component.toggle).toBe(false);
  });

  it('should call sidenavService.toggleSidenav when toggleSidenav is called', () => {
    component.toggleSidenav();
    expect(sidenavServiceMock.toggleSidenav).toHaveBeenCalled();
  });

  it('should call kadaiEngineService.getVersion on init', () => {
    expect(kadaiEngineServiceMock.getVersion).toHaveBeenCalled();
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    const nextSpy = vi.spyOn(component.destroy$, 'next');
    const completeSpy = vi.spyOn(component.destroy$, 'complete');

    component.ngOnDestroy();

    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should set version from getVersion response', () => {
    expect(component.version()).toBe('1.0.0');
  });

  it('should call orientationService.onResize when onResize is called', () => {
    component.onResize();
    expect(orientationServiceMock.onResize).toHaveBeenCalled();
  });

  it('should trigger onResize when window resize event fires', () => {
    orientationServiceMock.onResize.mockClear();
    window.dispatchEvent(new Event('resize'));
    expect(orientationServiceMock.onResize).toHaveBeenCalled();
  });

  it('should call kadaiEngineService.logout when logout is called', () => {
    component.logout();
    expect(kadaiEngineServiceMock.logout).toHaveBeenCalled();
  });

  it('should call selectedRouteService.selectRoute on NavigationEnd event', async () => {
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/');
    expect(selectedRouteServiceMock.selectRoute).toHaveBeenCalled();
  });

  it('should show progress bar when requestInProgress is true', () => {
    const localFixture = TestBed.createComponent(AppComponent);
    const localComponent = localFixture.componentInstance;
    (localComponent as any).requestInProgress = () => true;
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const progressBar = localFixture.nativeElement.querySelector('mat-progress-bar');
    expect(progressBar).toBeTruthy();
  });

  it('should not show progress bar when requestInProgress is false', () => {
    const localFixture = TestBed.createComponent(AppComponent);
    const localComponent = localFixture.componentInstance;
    (localComponent as any).requestInProgress = () => false;
    localFixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const progressBar = localFixture.nativeElement.querySelector('mat-progress-bar');
    expect(progressBar).toBeNull();
  });

  it('should call toggleSidenav when menu button inside sidenav drawer is clicked', async () => {
    sidenavServiceMock.toggleSidenav.mockClear();
    await component.sidenav().open();
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const menuButton = fixture.nativeElement.querySelector('.navbar_button-toggle');
    expect(menuButton).toBeTruthy();
    menuButton.click();
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    expect(sidenavServiceMock.toggleSidenav).toHaveBeenCalled();
  });

  it('should call logout when logout button inside sidenav drawer is clicked', async () => {
    kadaiEngineServiceMock.logout.mockClear();
    await component.sidenav().open();
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const logoutButton = fixture.nativeElement.querySelector('[aria-controls="logout"]');
    expect(logoutButton).toBeTruthy();
    logoutButton.click();
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    expect(kadaiEngineServiceMock.logout).toHaveBeenCalled();
  });
});
