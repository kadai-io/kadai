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
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideStore, Store } from '@ngxs/store';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { DomainState, SELECTED_DOMAIN_STORAGE_KEY } from './domain.state';
import { DomainSelectors } from './domain.selectors';
import { FetchDomains, SelectDomain } from './domain.actions';
import { RuntimeConfigService } from '../../services/runtime-config/runtime-config.service';

const REST_URL = 'http://localhost:8080/kadai';
const DOMAINS_URL = `${REST_URL}/v1/domains`;

describe('DomainState', () => {
  let store: Store;
  let httpTesting: HttpTestingController;

  const configureTestingModule = () => {
    TestBed.configureTestingModule({
      providers: [
        provideStore([DomainState]),
        provideHttpClientTesting(),
        { provide: RuntimeConfigService, useValue: { kadaiRestUrl: REST_URL } }
      ]
    });
    store = TestBed.inject(Store);
    httpTesting = TestBed.inject(HttpTestingController);
  };

  const selectedDomain = () => store.selectSnapshot(DomainSelectors.selectedDomain);
  const domains = () => store.selectSnapshot(DomainSelectors.domains);

  beforeEach(() => {
    sessionStorage.clear();
    configureTestingModule();
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should fetch the domains and expose them', () => {
    store.dispatch(new FetchDomains()).subscribe();
    httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A', 'DOMAIN_B']);

    expect(domains()).toEqual(['DOMAIN_A', 'DOMAIN_B']);
  });

  it('should select the first domain when nothing is selected yet', () => {
    store.dispatch(new FetchDomains()).subscribe();
    httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A', 'DOMAIN_B']);

    expect(selectedDomain()).toBe('DOMAIN_A');
  });

  it('should not fetch again once the domains are loaded', () => {
    store.dispatch(new FetchDomains()).subscribe();
    httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A']);

    store.dispatch(new FetchDomains()).subscribe();

    httpTesting.expectNone(DOMAINS_URL);
  });

  it('should fetch again when a refresh is forced', () => {
    store.dispatch(new FetchDomains()).subscribe();
    httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A']);

    store.dispatch(new FetchDomains(true)).subscribe();

    httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A', 'DOMAIN_B']);
    expect(domains()).toEqual(['DOMAIN_A', 'DOMAIN_B']);
  });

  it('should keep the master domain when the domains are fetched again', () => {
    store.dispatch(new FetchDomains()).subscribe();
    httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A', 'DOMAIN_B', '']);
    store.dispatch(new SelectDomain('')).subscribe();

    store.dispatch(new FetchDomains(true)).subscribe();
    httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A', 'DOMAIN_B', '']);

    expect(selectedDomain()).toBe('');
  });

  it('should keep the selected domain when the request fails', () => {
    store.dispatch(new FetchDomains()).subscribe();
    httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A', 'DOMAIN_B']);
    store.dispatch(new SelectDomain('DOMAIN_B')).subscribe();

    store.dispatch(new FetchDomains(true)).subscribe({ error: () => undefined });
    httpTesting.expectOne(DOMAINS_URL).error(new ProgressEvent('failed'));

    expect(selectedDomain()).toBe('DOMAIN_B');
  });

  describe('persistence across a reload', () => {
    const reloadStore = () => {
      TestBed.resetTestingModule();
      configureTestingModule();
    };

    it('should store the selected domain', () => {
      store.dispatch(new SelectDomain('DOMAIN_B')).subscribe();

      expect(sessionStorage.getItem(SELECTED_DOMAIN_STORAGE_KEY)).toBe('DOMAIN_B');
    });

    it.each([
      ['a regular domain', 'DOMAIN_B'],
      ['the master domain', '']
    ])('should restore %s instead of falling back to the first one', (_, stored) => {
      sessionStorage.setItem(SELECTED_DOMAIN_STORAGE_KEY, stored);
      reloadStore();

      store.dispatch(new FetchDomains()).subscribe();
      httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A', 'DOMAIN_B', '']);

      expect(selectedDomain()).toBe(stored);
    });

    it('should fall back to the first domain when the stored one is no longer offered', () => {
      sessionStorage.setItem(SELECTED_DOMAIN_STORAGE_KEY, 'DOMAIN_GONE');
      reloadStore();

      store.dispatch(new FetchDomains()).subscribe();
      httpTesting.expectOne(DOMAINS_URL).flush(['DOMAIN_A', 'DOMAIN_B']);

      expect(selectedDomain()).toBe('DOMAIN_A');
    });
  });

  it('should not select anything when no domain is offered', () => {
    store.dispatch(new FetchDomains()).subscribe();
    httpTesting.expectOne(DOMAINS_URL).flush([]);

    expect(selectedDomain()).toBeUndefined();
  });
});
