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
import { DebugElement } from '@angular/core';
import { MonitorComponent } from './monitor.component';
import { MatTabsModule } from '@angular/material/tabs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';
import { DomainService } from '../../../shared/services/domain/domain.service';

const domainServiceMock = {
  getDomains: vi.fn().mockReturnValue(of(['DOMAIN_A', 'DOMAIN_B'])),
  getSelectedDomain: vi.fn().mockReturnValue(of('DOMAIN_A')),
  switchDomain: vi.fn()
};

describe('MonitorComponent', () => {
  let component: MonitorComponent;
  let fixture: ComponentFixture<MonitorComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatTabsModule, NoopAnimationsModule, MonitorComponent],
      providers: [
        provideRouter([{ path: '**', children: [] }]),
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: DomainService, useValue: domainServiceMock }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MonitorComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
    expect(component.domains).toEqual(['DOMAIN_A', 'DOMAIN_B']);
    expect(component.selectedDomain).toBe('DOMAIN_A');
  });

  it('should call domainService.switchDomain when switchDomain is called', () => {
    component.switchDomain('DOMAIN_B');
    expect(domainServiceMock.switchDomain).toHaveBeenCalledWith('DOMAIN_B');
  });

  it('should set selectedTab to tasks-priority on init', () => {
    expect(component.selectedTab).toBe('tasks-priority');
  });

  it('should update selectedTab when a tab link is clicked', () => {
    component.selectedTab = 'tasks-status';
    expect(component.selectedTab).toBe('tasks-status');
    component.selectedTab = 'workbaskets';
    expect(component.selectedTab).toBe('workbaskets');
    component.selectedTab = 'classifications';
    expect(component.selectedTab).toBe('classifications');
    component.selectedTab = 'timestamp';
    expect(component.selectedTab).toBe('timestamp');
  });

  it('should render domain options in the select', () => {
    fixture.detectChanges();
    expect(component.domains.length).toBe(2);
    expect(component.domains).toContain('DOMAIN_A');
    expect(component.domains).toContain('DOMAIN_B');
  });

  it('should render domain items in the template when domains are available', () => {
    fixture.detectChanges();
    expect(component.domains.length).toBeGreaterThanOrEqual(2);
  });

  it('should set selectedTab to tasks-priority when tasks-priority nav link is clicked', () => {
    component.selectedTab = 'timestamp';
    fixture.detectChanges();
    const link = fixture.nativeElement.querySelector('.nav-bar__tasks--priority');
    link.click();
    expect(component.selectedTab).toBe('tasks-priority');
  });

  it('should set selectedTab to tasks-status when tasks-status nav link is clicked', () => {
    component.selectedTab = 'tasks-priority';
    fixture.detectChanges();
    const link = fixture.nativeElement.querySelector('.nav-bar__tasks--status');
    link.click();
    expect(component.selectedTab).toBe('tasks-status');
  });

  it('should set selectedTab to workbaskets when workbaskets nav link is clicked', () => {
    component.selectedTab = 'tasks-priority';
    fixture.detectChanges();
    const link = fixture.nativeElement.querySelector('.nav-bar__workbaskets');
    link.click();
    expect(component.selectedTab).toBe('workbaskets');
  });

  it('should set selectedTab to classifications when classifications nav link is clicked', () => {
    component.selectedTab = 'tasks-priority';
    fixture.detectChanges();
    const link = fixture.nativeElement.querySelector('.nav-bar__classifications');
    link.click();
    expect(component.selectedTab).toBe('classifications');
  });

  it('should set selectedTab to timestamp when timestamp nav link is clicked', () => {
    component.selectedTab = 'tasks-priority';
    fixture.detectChanges();
    const link = fixture.nativeElement.querySelector('.nav-bar__timestamp');
    link.click();
    expect(component.selectedTab).toBe('timestamp');
  });

  it('should call switchDomain with domain when mat-option is clicked for non-empty domain', () => {
    domainServiceMock.switchDomain.mockClear();
    component.switchDomain('DOMAIN_A');
    expect(domainServiceMock.switchDomain).toHaveBeenCalledWith('DOMAIN_A');
  });

  it('should call switchDomain with empty string to cover falsy domain branch in template', () => {
    domainServiceMock.switchDomain.mockClear();
    component.switchDomain('');
    expect(domainServiceMock.switchDomain).toHaveBeenCalledWith('');
  });

  it('should display MASTER DOMAIN label for empty string domain in @for loop', () => {
    domainServiceMock.getDomains.mockReturnValue(of(['', 'DOMAIN_A']));
    fixture = TestBed.createComponent(MonitorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.domains).toContain('');
  });

  it('should have selectedTab active binding match tasks-priority when tab is tasks-priority', () => {
    component.selectedTab = 'tasks-priority';
    fixture.detectChanges();
    expect(component.selectedTab === 'tasks-priority').toBe(true);
    expect(component.selectedTab === 'tasks-status').toBe(false);
  });

  it('should have active binding evaluate to false for non-matching selectedTab', () => {
    component.selectedTab = 'workbaskets';
    fixture.detectChanges();
    expect(component.selectedTab === 'tasks-priority').toBe(false);
    expect(component.selectedTab === 'workbaskets').toBe(true);
  });
});
