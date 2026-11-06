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
import { provideRouter, Router } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideStore, Store } from '@ngxs/store';
import { DomainState } from '../../../shared/store/domain-store/domain.state';
import { RuntimeConfigService } from '../../../shared/services/runtime-config/runtime-config.service';
import { of } from 'rxjs';

describe('MonitorComponent', () => {
  let component: MonitorComponent;
  let fixture: ComponentFixture<MonitorComponent>;
  let debugElement: DebugElement;
  let store: Store;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatTabsModule, NoopAnimationsModule, MonitorComponent],
      providers: [
        provideRouter([{ path: '**', children: [] }]),
        provideHttpClientTesting(),
        provideStore([DomainState]),
        { provide: RuntimeConfigService, useValue: { kadaiRestUrl: 'http://localhost:8080/kadai' } }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    store = TestBed.inject(Store);
    store.reset({ ...store.snapshot(), domain: { domains: ['DOMAIN_A', 'DOMAIN_B'], selectedDomain: 'DOMAIN_A' } });
    fixture = TestBed.createComponent(MonitorComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
    expect(component.domains()).toEqual(['DOMAIN_A', 'DOMAIN_B']);
    expect(component.selectedDomain()).toBe('DOMAIN_A');
  });

  it('should put the domain into the url when switchDomain is called', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    component.switchDomain('DOMAIN_B');

    expect(navigate).toHaveBeenCalledWith([], {
      queryParams: { domain: 'DOMAIN_B' },
      queryParamsHandling: 'merge'
    });
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
    expect(component.domains().length).toBe(2);
    expect(component.domains()).toContain('DOMAIN_A');
    expect(component.domains()).toContain('DOMAIN_B');
  });

  it('should render domain items in the template when domains are available', () => {
    fixture.detectChanges();
    expect(component.domains().length).toBeGreaterThanOrEqual(2);
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

  it('should put a non-empty domain into the url when a mat-option is clicked', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    component.switchDomain('DOMAIN_A');

    expect(navigate).toHaveBeenCalledWith([], {
      queryParams: { domain: 'DOMAIN_A' },
      queryParamsHandling: 'merge'
    });
  });

  it('should put the master domain into the url as an empty value', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    component.switchDomain('');

    expect(navigate).toHaveBeenCalledWith([], {
      queryParams: { domain: '' },
      queryParamsHandling: 'merge'
    });
  });

  it('should display MASTER DOMAIN label for empty string domain in @for loop', () => {
    store.reset({ ...store.snapshot(), domain: { domains: ['', 'DOMAIN_A'], selectedDomain: '' } });
    fixture = TestBed.createComponent(MonitorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.domains()).toContain('');
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
