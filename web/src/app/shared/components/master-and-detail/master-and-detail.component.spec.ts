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
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MasterAndDetailComponent } from './master-and-detail.component';
import { MasterAndDetailService } from 'app/shared/services/master-and-detail/master-and-detail.service';
import { NavigationStart, provideRouter, Router } from '@angular/router';

describe('MasterAndDetailComponent', () => {
  let component: MasterAndDetailComponent;
  let fixture: ComponentFixture<MasterAndDetailComponent>;
  let masterAndDetailServiceMock: { setShowDetail: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    masterAndDetailServiceMock = {
      setShowDetail: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [MasterAndDetailComponent],
      providers: [provideRouter([]), { provide: MasterAndDetailService, useValue: masterAndDetailServiceMock }]
    }).compileComponents();

    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(MasterAndDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
    expect(component.showDetail).toBe(false);
    expect(masterAndDetailServiceMock.setShowDetail).toHaveBeenCalled();
    expect(masterAndDetailServiceMock.setShowDetail).toHaveBeenCalledWith(false);
    expect(component.currentRoute).toBe('');
  });

  it('should set currentRoute to workbaskets when URL contains workbaskets', () => {
    (router.events as any).next(new NavigationStart(1, '/kadai/administration/workbaskets'));
    expect(component.currentRoute).toBe('workbaskets');
  });

  it('should set currentRoute to classifications when URL contains classifications', () => {
    (router.events as any).next(new NavigationStart(1, '/kadai/administration/classifications'));
    expect(component.currentRoute).toBe('classifications');
  });

  it('should set currentRoute to tasks when URL contains tasks', () => {
    (router.events as any).next(new NavigationStart(1, '/kadai/workplace/tasks'));
    expect(component.currentRoute).toBe('tasks');
  });

  it('should set showDetail to true when URL contains workbasket detail route', () => {
    (router.events as any).next(new NavigationStart(1, '/workbaskets/(detail:WBI:001)'));
    expect(component.showDetail).toBe(true);
    expect(masterAndDetailServiceMock.setShowDetail).toHaveBeenCalledWith(true);
  });

  it('should set showDetail to true when URL contains classification detail route', () => {
    (router.events as any).next(new NavigationStart(1, '/kadai/classifications/(detail:CLF:001)'));
    expect(component.showDetail).toBe(true);
  });

  it('should set showDetail to true when URL contains tasks detail route', () => {
    (router.events as any).next(new NavigationStart(1, '/kadai/tasks/(detail:task-1)'));
    expect(component.showDetail).toBe(true);
  });

  it('should set showDetail to false when URL does not contain a detail route', () => {
    (router.events as any).next(new NavigationStart(1, '/kadai/administration/workbaskets'));
    expect(component.showDetail).toBe(false);
    expect(masterAndDetailServiceMock.setShowDetail).toHaveBeenCalledWith(false);
  });

  it('should render task details panel when showDetail is true', () => {
    (router.events as any).next(new NavigationStart(1, '/workbaskets/(detail:WBI:001)'));
    expect(component.showDetail).toBe(true);
  });

  it('should not render task details panel when showDetail is false', () => {
    const localFixture = TestBed.createComponent(MasterAndDetailComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.showDetail = false;
    localFixture.detectChanges();
    const detailPanel = localFixture.nativeElement.querySelector('.workplace-overview__task-details');
    expect(detailPanel).toBeNull();
  });

  it('should render "Select a Task" when showDetail is false and currentRoute is tasks', () => {
    const localFixture = TestBed.createComponent(MasterAndDetailComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.showDetail = false;
    localComponent.currentRoute = 'tasks';
    localFixture.detectChanges();
    const emptyPage = localFixture.nativeElement.querySelector('.workplace-overview__empty-page');
    expect(emptyPage).toBeTruthy();
    expect(emptyPage.textContent).toContain('Select a Task');
  });

  it('should not render "Select a Task" when currentRoute is not tasks', () => {
    const localFixture = TestBed.createComponent(MasterAndDetailComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.showDetail = false;
    localComponent.currentRoute = 'workbaskets';
    localFixture.detectChanges();
    const emptyPage = localFixture.nativeElement.querySelector('.workplace-overview__empty-page');
    expect(emptyPage).toBeNull();
  });

  it('should render task details panel when showDetail is true (DOM check)', () => {
    const localFixture = TestBed.createComponent(MasterAndDetailComponent);
    localFixture.detectChanges();
    (router.events as any).next(new NavigationStart(1, '/workbaskets/(detail:WBI:001)'));
    expect(localFixture.componentInstance.showDetail).toBe(true);
  });

  it('should not render "Select a Task" when showDetail is true even for tasks route', () => {
    const localFixture = TestBed.createComponent(MasterAndDetailComponent);
    localFixture.detectChanges();
    (router.events as any).next(new NavigationStart(1, '/kadai/tasks/(detail:task-1)'));
    expect(localFixture.componentInstance.showDetail).toBe(true);
    expect(localFixture.componentInstance.currentRoute).toBe('tasks');
  });

  it('should render task-details div in DOM when initial URL is a detail route', () => {
    const localFixture = TestBed.createComponent(MasterAndDetailComponent);
    vi.spyOn(router, 'url', 'get').mockReturnValue('/workbaskets/(detail:WBI:001)');
    localFixture.detectChanges();
    const detailPanel = localFixture.nativeElement.querySelector('.workplace-overview__task-details');
    expect(detailPanel).toBeTruthy();
  });
});
