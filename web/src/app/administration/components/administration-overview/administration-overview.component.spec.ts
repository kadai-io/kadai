/*
 * Copyright [2025] [envite consulting GmbH]
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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AdministrationOverviewComponent } from './administration-overview.component';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { of, Subject } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { KadaiEngineService } from '../../../shared/services/kadai-engine/kadai-engine.service';

const domainServiceSpy: Partial<DomainService> = {
  getDomains: jest.fn().mockReturnValue(of(['domain a', 'domain b'])),
  getSelectedDomain: jest.fn().mockReturnValue(of('domain a')),
  switchDomain: jest.fn()
};

describe('AdministrationOverviewComponent', () => {
  let component: AdministrationOverviewComponent;
  let fixture: ComponentFixture<AdministrationOverviewComponent>;
  const routingAccess$ = new Subject<boolean>();
  const kadaiEngineServiceSpy: Partial<KadaiEngineService> = {
    isCustomRoutingRulesEnabled: jest.fn().mockReturnValue(routingAccess$.asObservable())
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AdministrationOverviewComponent],
      providers: [
        { provide: DomainService, useValue: domainServiceSpy },
        { provide: KadaiEngineService, useValue: kadaiEngineServiceSpy },
        provideHttpClient(),
        provideRouter([])
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdministrationOverviewComponent);
    component = fixture.componentInstance;
    routingAccess$.next(false);
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should render 3 tabs in navbar when routing access is disabled', () => {
    const navbar = fixture.debugElement.nativeElement.getElementsByClassName('administration-overview__navbar-links');
    expect(navbar).toHaveLength(3);
  });

  it('should render 4 tabs in navbar when routing access is enabled', () => {
    routingAccess$.next(true);
    fixture.detectChanges();
    const navbar = fixture.debugElement.nativeElement.getElementsByClassName('administration-overview__navbar-links');
    expect(navbar).toHaveLength(4);
  });

  it('should display current domain', () => {
    const domainElem = fixture.debugElement.nativeElement.querySelector('.administration-overview__domain');
    expect(domainElem).toBeTruthy();
    fixture.detectChanges();
    expect(domainElem.textContent).toMatch('domain a');
  });

  it('should call DomainService.switchDomain when switchDomain is invoked', () => {
    component.switchDomain('domain b');
    expect(domainServiceSpy.switchDomain).toHaveBeenCalledWith('domain b');
  });
});
