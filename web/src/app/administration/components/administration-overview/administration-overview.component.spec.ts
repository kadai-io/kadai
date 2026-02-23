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
import { AdministrationOverviewComponent } from './administration-overview.component';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { KadaiEngineService } from '../../../shared/services/kadai-engine/kadai-engine.service';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideHttpClientTesting } from '@angular/common/http/testing';

const domainServiceSpy: Partial<DomainService> = {
  getDomains: vi.fn().mockReturnValue(of(['domain a', 'domain b'])),
  getSelectedDomain: vi.fn().mockReturnValue(of('domain a')),
  switchDomain: vi.fn()
};

const kadaiEngineServiceSpy = {
  isCustomRoutingRulesEnabled: vi.fn().mockReturnValue(of(false))
};

describe('AdministrationOverviewComponent', () => {
  let component: AdministrationOverviewComponent;
  let fixture: ComponentFixture<AdministrationOverviewComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [AdministrationOverviewComponent],
      providers: [
        { provide: DomainService, useValue: domainServiceSpy },
        { provide: KadaiEngineService, useValue: kadaiEngineServiceSpy },
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdministrationOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should render 3 tabs in navbar', () => {
    const navbar = fixture.debugElement.nativeElement.getElementsByClassName('administration-overview__navbar-links');
    expect(navbar).toHaveLength(3);
  });

  it('should display current domain', () => {
    const domainElem = fixture.debugElement.nativeElement.querySelector('.administration-overview__domain');
    expect(domainElem).toBeTruthy();

    fixture.detectChanges();
    expect(domainElem.textContent).toMatch('domain a');
  });

  it('should call getDomains on init and populate domains', () => {
    expect(domainServiceSpy.getDomains).toHaveBeenCalled();
    expect(component.domains).toEqual(['domain a', 'domain b']);
  });

  it('should call getSelectedDomain on init and set selectedDomain', () => {
    expect(domainServiceSpy.getSelectedDomain).toHaveBeenCalled();
    expect(component.selectedDomain).toBe('domain a');
  });

  it('should call isCustomRoutingRulesEnabled on init', () => {
    expect(kadaiEngineServiceSpy.isCustomRoutingRulesEnabled).toHaveBeenCalled();
    expect(component.routingAccess).toBe(false);
  });

  it('should call switchDomain when switchDomain is invoked', () => {
    component.switchDomain('domain b');
    expect(domainServiceSpy.switchDomain).toHaveBeenCalledWith('domain b');
  });

  it('should set routingAccess to true when routing rules enabled', async () => {
    kadaiEngineServiceSpy.isCustomRoutingRulesEnabled.mockReturnValue(of(true));
    fixture = TestBed.createComponent(AdministrationOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.routingAccess).toBe(true);
  });
});
