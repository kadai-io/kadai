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
import { RouterModule } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
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
      imports: [MatTabsModule, RouterModule, RouterTestingModule, NoopAnimationsModule, MonitorComponent],
      providers: [
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

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should set domains from getDomains subscription', () => {
    expect(component.domains).toEqual(['DOMAIN_A', 'DOMAIN_B']);
  });

  it('should set selectedDomain from getSelectedDomain subscription', () => {
    expect(component.selectedDomain).toBe('DOMAIN_A');
  });

  it('should call domainService.switchDomain when switchDomain is called', () => {
    component.switchDomain('DOMAIN_B');
    expect(domainServiceMock.switchDomain).toHaveBeenCalledWith('DOMAIN_B');
  });
});
