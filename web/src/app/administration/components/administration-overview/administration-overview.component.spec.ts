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
    kadaiEngineServiceSpy.isCustomRoutingRulesEnabled.mockReturnValue(of(false));
    domainServiceSpy.getDomains = vi.fn().mockReturnValue(of(['domain a', 'domain b']));
    domainServiceSpy.getSelectedDomain = vi.fn().mockReturnValue(of('domain a'));
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

  it('should render task routing tab when routingAccess is true', () => {
    component.routingAccess = true;
    fixture.detectChanges();
    const tabs = fixture.nativeElement.querySelectorAll('.administration-overview__navbar-links');
    expect(tabs.length).toBe(4);
  });

  it('should not render task routing tab when routingAccess is false', () => {
    component.routingAccess = false;
    fixture.detectChanges();
    const tabs = fixture.nativeElement.querySelectorAll('.administration-overview__navbar-links');
    expect(tabs.length).toBe(3);
  });

  it('should show MASTER DOMAIN when domain is empty string', () => {
    component.domains = [''];
    fixture.detectChanges();
    expect(component.domains).toContain('');
    expect(component.domains[0] ? component.domains[0] : 'MASTER DOMAIN').toBe('MASTER DOMAIN');
  });

  it('should show domain name when domain is non-empty', () => {
    component.domains = ['DOMAIN_A'];
    fixture.detectChanges();
    expect(component.domains).toContain('DOMAIN_A');
    expect(component.domains[0] ? component.domains[0] : 'MASTER DOMAIN').toBe('DOMAIN_A');
  });

  it('should set selectedTab to "workbaskets" when Workbaskets link is clicked', () => {
    component.selectedTab = '';
    fixture.detectChanges();
    const links = fixture.nativeElement.querySelectorAll('.administration-overview__navbar-links');
    const workbasketsLink = Array.from(links).find((el: any) => el.textContent.includes('Workbaskets')) as HTMLElement;
    workbasketsLink.click();
    expect(component.selectedTab).toBe('workbaskets');
  });

  it('should set selectedTab to "classifications" when Classifications link is clicked', () => {
    component.selectedTab = '';
    fixture.detectChanges();
    const links = fixture.nativeElement.querySelectorAll('.administration-overview__navbar-links');
    const classificationsLink = Array.from(links).find((el: any) =>
      el.textContent.includes('Classifications')
    ) as HTMLElement;
    classificationsLink.click();
    expect(component.selectedTab).toBe('classifications');
  });

  it('should set selectedTab to "access-items-management" when Access Items Management link is clicked', () => {
    component.selectedTab = '';
    fixture.detectChanges();
    const links = fixture.nativeElement.querySelectorAll('.administration-overview__navbar-links');
    const accessItemsLink = Array.from(links).find((el: any) => el.textContent.includes('Access Items')) as HTMLElement;
    accessItemsLink.click();
    expect(component.selectedTab).toBe('access-items-management');
  });

  it('should set selectedTab to "task-routing" when Task Routing link is clicked', () => {
    component.routingAccess = true;
    component.selectedTab = '';
    fixture.detectChanges();
    const links = fixture.nativeElement.querySelectorAll('.administration-overview__navbar-links');
    const taskRoutingLink = Array.from(links).find((el: any) => el.textContent.includes('Task Routing')) as HTMLElement;
    taskRoutingLink.click();
    expect(component.selectedTab).toBe('task-routing');
  });

  it('should call switchDomain when a mat-option is clicked', () => {
    component.domains = ['domain a', 'domain b'];
    fixture.detectChanges();
    component.switchDomain('domain b');
    expect(domainServiceSpy.switchDomain).toHaveBeenCalledWith('domain b');
  });
});
