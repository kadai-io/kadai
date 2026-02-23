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

import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { DomainService } from './domain.service';
import { SelectedRouteService } from '../selected-route/selected-route';
import { StartupService } from '../startup/startup.service';
import { RequestInProgressService } from '../request-in-progress/request-in-progress.service';
import { Subject } from 'rxjs';

const REST_URL = 'http://localhost:8080/kadai';
const DOMAINS_URL = `${REST_URL}/v1/domains`;

describe('DomainService', () => {
  let service: DomainService;
  let httpTesting: HttpTestingController;
  let selectedRouteSubject: Subject<string>;
  let mockRequestInProgressService: { setRequestInProgress: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    selectedRouteSubject = new Subject<string>();

    const mockSelectedRouteService = {
      getSelectedRoute: () => selectedRouteSubject.asObservable()
    };

    mockRequestInProgressService = {
      setRequestInProgress: vi.fn()
    };

    const mockStartupService = {
      getKadaiRestUrl: () => REST_URL
    };

    TestBed.configureTestingModule({
      providers: [
        DomainService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: SelectedRouteService, useValue: mockSelectedRouteService },
        { provide: StartupService, useValue: mockStartupService },
        { provide: RequestInProgressService, useValue: mockRequestInProgressService }
      ]
    });

    service = TestBed.inject(DomainService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('url getter', () => {
    it('should return the correct domains URL', () => {
      expect(service.url).toBe(DOMAINS_URL);
    });
  });

  describe('getDomains()', () => {
    it('should make a GET request to the domains URL', () => {
      service.getDomains().subscribe();

      const req = httpTesting.expectOne(DOMAINS_URL);
      expect(req.request.method).toBe('GET');
      req.flush(['DOMAIN_A', 'DOMAIN_B']);
    });

    it('should emit the received domains', () => {
      const domains = ['DOMAIN_A', 'DOMAIN_B'];
      let emittedDomains: string[] | undefined;

      service.getDomains().subscribe((d) => (emittedDomains = d));

      const req = httpTesting.expectOne(DOMAINS_URL);
      req.flush(domains);

      expect(emittedDomains).toEqual(domains);
    });

    it('should call selectDomain with the first domain when no domain is selected yet', () => {
      const domains = ['DOMAIN_A', 'DOMAIN_B'];
      let selectedDomain: string | undefined;

      service.getSelectedDomain().subscribe((d) => (selectedDomain = d));
      service.getDomains().subscribe();

      const req = httpTesting.expectOne(DOMAINS_URL);
      req.flush(domains);

      expect(selectedDomain).toBe('DOMAIN_A');
    });

    it('should cache the response and not make a second HTTP request', () => {
      const domains = ['DOMAIN_A'];

      service.getDomains().subscribe();
      const req = httpTesting.expectOne(DOMAINS_URL);
      req.flush(domains);

      service.getDomains().subscribe();
      httpTesting.expectNone(DOMAINS_URL);
    });

    it('should make a new HTTP request when forceRefresh is true', () => {
      const domains = ['DOMAIN_A'];

      service.getDomains().subscribe();
      const req1 = httpTesting.expectOne(DOMAINS_URL);
      req1.flush(domains);

      service.getDomains(true).subscribe();
      const req2 = httpTesting.expectOne(DOMAINS_URL);
      req2.flush(['DOMAIN_A', 'DOMAIN_B']);
    });
  });

  describe('getSelectedDomain()', () => {
    it('should return an Observable', () => {
      const result = service.getSelectedDomain();
      expect(result).toBeDefined();
      expect(typeof result.subscribe).toBe('function');
    });

    it('should emit the selected domain when switchDomain is called', () => {
      let emittedDomain: string | undefined;
      service.getSelectedDomain().subscribe((d) => (emittedDomain = d));

      service.switchDomain('DOMAIN_X');

      expect(emittedDomain).toBe('DOMAIN_X');
    });
  });

  describe('switchDomain()', () => {
    it('should update the selected domain observable', () => {
      const emitted: string[] = [];
      service.getSelectedDomain().subscribe((d) => emitted.push(d));

      service.switchDomain('DOMAIN_A');
      service.switchDomain('DOMAIN_B');

      expect(emitted).toEqual(['DOMAIN_A', 'DOMAIN_B']);
    });
  });

  describe('domainChangedComplete()', () => {
    it('should call requestInProgressService.setRequestInProgress(false)', () => {
      service.domainChangedComplete();

      expect(mockRequestInProgressService.setRequestInProgress).toHaveBeenCalledWith(false);
    });
  });

  describe('getSelectedDomainValue()', () => {
    it('should return undefined before any domain is selected', () => {
      expect(service.getSelectedDomainValue()).toBeUndefined();
    });

    it('should return the current domain value after switchDomain is called', () => {
      service.switchDomain('DOMAIN_A');

      expect(service.getSelectedDomainValue()).toBe('DOMAIN_A');
    });

    it('should return the updated domain value after multiple switchDomain calls', () => {
      service.switchDomain('DOMAIN_A');
      service.switchDomain('DOMAIN_B');

      expect(service.getSelectedDomainValue()).toBe('DOMAIN_B');
    });
  });

  describe('addMasterDomain()', () => {
    it('should add empty domain when non-empty domains exist', () => {
      const domains = ['DOMAIN_A', 'DOMAIN_B'];
      let emittedDomains: string[] | undefined;

      service.getDomains().subscribe((d) => (emittedDomains = d));
      const req = httpTesting.expectOne(DOMAINS_URL);
      req.flush(domains);

      service.addMasterDomain();

      expect(emittedDomains).toContain('');
      expect(emittedDomains).toContain('DOMAIN_A');
      expect(emittedDomains).toContain('DOMAIN_B');
    });

    it('should not add empty domain when domains are already all empty', () => {
      const domains: string[] = [];
      let emittedDomains: string[] | undefined;

      service.getDomains().subscribe((d) => (emittedDomains = d));
      const req = httpTesting.expectOne(DOMAINS_URL);
      req.flush(domains);

      service.addMasterDomain();

      expect(emittedDomains).toEqual([]);
    });
  });

  describe('removeMasterDomain()', () => {
    it('should remove empty domain when it exists in the list', () => {
      const domains = ['DOMAIN_A', 'DOMAIN_B'];
      const emittedValues: string[][] = [];

      service.getDomains().subscribe((d) => emittedValues.push(d));
      const req = httpTesting.expectOne(DOMAINS_URL);
      req.flush(domains);

      service.addMasterDomain();
      expect(emittedValues[emittedValues.length - 1]).toContain('');

      service.removeMasterDomain();
      expect(emittedValues[emittedValues.length - 1]).not.toContain('');
      expect(emittedValues[emittedValues.length - 1]).toEqual(['DOMAIN_A', 'DOMAIN_B']);
    });

    it('should not emit when no empty domain is present', () => {
      const domains = ['DOMAIN_A', 'DOMAIN_B'];
      const emittedCount: number[] = [];

      service.getDomains().subscribe(() => emittedCount.push(1));
      const req = httpTesting.expectOne(DOMAINS_URL);
      req.flush(domains);

      const countBefore = emittedCount.length;

      service.removeMasterDomain();

      expect(emittedCount.length).toBe(countBefore);
    });
  });

  describe('Constructor subscription to selectedRouteService', () => {
    it('should remove master domain and switch to first domain when route is workbaskets and current domain is empty', () => {
      const domains = ['DOMAIN_A', 'DOMAIN_B'];

      service.getDomains().subscribe();
      const req = httpTesting.expectOne(DOMAINS_URL);
      req.flush(domains);

      service.switchDomain('');

      let selectedDomain: string | undefined;
      service.getSelectedDomain().subscribe((d) => (selectedDomain = d));

      selectedRouteSubject.next('workbaskets');

      expect(selectedDomain).toBe('DOMAIN_A');
    });

    it('should add master domain when route is classifications', () => {
      const domains = ['DOMAIN_A'];
      let emittedDomains: string[] | undefined;

      service.getDomains().subscribe((d) => (emittedDomains = d));
      const req = httpTesting.expectOne(DOMAINS_URL);
      req.flush(domains);

      selectedRouteSubject.next('classifications');

      expect(emittedDomains).toContain('');
    });
  });
});
