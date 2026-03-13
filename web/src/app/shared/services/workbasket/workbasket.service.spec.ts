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
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';
import { WorkbasketService } from './workbasket.service';
import { DomainService } from '../domain/domain.service';
import { environment } from 'environments/environment';
import { Workbasket } from '../../models/workbasket';
import { WorkbasketAccessItems } from '../../models/workbasket-access-items';
import { WorkbasketAccessItemsRepresentation } from '../../models/workbasket-access-items-representation';
import { Direction, WorkbasketQuerySortParameter } from '../../models/sorting';

describe('WorkbasketService', () => {
  let service: WorkbasketService;
  let httpMock: HttpTestingController;

  const mockDomainService = {
    getSelectedDomain: vi.fn().mockReturnValue(of('DOMAIN_A')),
    domainChangedComplete: vi.fn()
  };

  beforeEach(() => {
    environment.kadaiRestUrl = 'http://test';
    vi.clearAllMocks();
    mockDomainService.getSelectedDomain.mockReturnValue(of('DOMAIN_A'));

    TestBed.configureTestingModule({
      providers: [
        WorkbasketService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: DomainService, useValue: mockDomainService }
      ]
    });

    service = TestBed.inject(WorkbasketService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getWorkBasket', () => {
    it('should make a GET request to /v1/workbaskets/{id}', () => {
      service.getWorkBasket('wb-123').subscribe();

      const req = httpMock.expectOne('http://test/v1/workbaskets/wb-123');
      expect(req.request.method).toBe('GET');
      req.flush({ workbasketId: 'wb-123' });
    });
  });

  describe('getAllWorkBaskets', () => {
    it('should make a GET request with required-permission=OPEN', () => {
      service.getAllWorkBaskets().subscribe();

      const req = httpMock.expectOne('http://test/v1/workbaskets?required-permission=OPEN');
      expect(req.request.method).toBe('GET');
      req.flush({ workbaskets: [] });
    });
  });

  describe('createWorkbasket', () => {
    it('should make a POST request to /v1/workbaskets', () => {
      const workbasket: Workbasket = { workbasketId: 'new-wb', name: 'New WB', key: 'KEY1' };
      service.createWorkbasket(workbasket).subscribe();

      const req = httpMock.expectOne('http://test/v1/workbaskets');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(workbasket);
      req.flush({ workbasketId: 'new-wb' });
    });
  });

  describe('updateWorkbasket', () => {
    it('should make a PUT request to the provided URL', () => {
      const workbasket: Workbasket = { workbasketId: 'wb-upd', name: 'Updated WB' };
      const url = 'http://test/v1/workbaskets/wb-upd';
      service.updateWorkbasket(url, workbasket).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(workbasket);
      req.flush({ workbasketId: 'wb-upd' });
    });

    it('should handle error response via catchError', () => {
      const workbasket: Workbasket = { workbasketId: 'wb-err', name: 'Error WB' };
      const url = 'http://test/v1/workbaskets/wb-err';
      let errorReceived: any;

      service.updateWorkbasket(url, workbasket).subscribe({
        error: (err) => (errorReceived = err)
      });

      const req = httpMock.expectOne(url);
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });

      expect(errorReceived).toBeDefined();
    });
  });

  describe('markWorkbasketForDeletion', () => {
    it('should make a DELETE request to the provided URL', () => {
      const url = 'http://test/v1/workbaskets/wb-del';
      service.markWorkbasketForDeletion(url).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('DELETE');
      req.flush(null, { status: 202, statusText: 'Accepted' });
    });
  });

  describe('getWorkBasketAccessItems', () => {
    it('should make a GET request to the provided URL', () => {
      const url = 'http://test/v1/workbaskets/wb-1/accessItems';
      service.getWorkBasketAccessItems(url).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('GET');
      req.flush({ accessItems: [] });
    });
  });

  describe('createWorkBasketAccessItem', () => {
    it('should make a POST request to the provided URL', () => {
      const url = 'http://test/v1/workbaskets/wb-1/accessItems';
      const accessItem: WorkbasketAccessItems = { accessId: 'user1' } as WorkbasketAccessItems;
      service.createWorkBasketAccessItem(url, accessItem).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(accessItem);
      req.flush({});
    });
  });

  describe('updateWorkBasketAccessItem', () => {
    it('should make a PUT request to the provided URL', () => {
      const url = 'http://test/v1/workbaskets/wb-1/accessItems';
      const items: WorkbasketAccessItemsRepresentation = { accessItems: [] } as WorkbasketAccessItemsRepresentation;
      service.updateWorkBasketAccessItem(url, items).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('PUT');
      req.flush({});
    });
  });

  describe('getWorkBasketsDistributionTargets', () => {
    it('should make a GET request to the provided URL', () => {
      const url = 'http://test/v1/workbaskets/wb-1/distribution-targets';
      service.getWorkBasketsDistributionTargets(url).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('GET');
      req.flush({ distributionTargets: [] });
    });
  });

  describe('updateWorkBasketsDistributionTargets', () => {
    it('should make a PUT request to the provided URL with array of ids', () => {
      const url = 'http://test/v1/workbaskets/wb-1/distribution-targets';
      const ids = new Set(['wb-2', 'wb-3']);
      service.updateWorkBasketsDistributionTargets(url, ids).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(['wb-2', 'wb-3']);
      req.flush({});
    });
  });

  describe('removeDistributionTarget', () => {
    it('should make a DELETE request to the provided URL', () => {
      const url = 'http://test/v1/workbaskets/wb-2/distribution-targets/wb-1';
      service.removeDistributionTarget(url).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  describe('selectWorkBasket', () => {
    it('should emit the provided id via workBasketSelected subject', () => {
      let emitted: string;
      service.getSelectedWorkBasket().subscribe((id) => (emitted = id));

      service.selectWorkBasket('wb-selected');

      expect(emitted).toBe('wb-selected');
    });

    it('should emit undefined when called without argument', () => {
      let emitted: string = 'initial';
      service.getSelectedWorkBasket().subscribe((id) => (emitted = id));

      service.selectWorkBasket();

      expect(emitted).toBeUndefined();
    });
  });

  describe('getSelectedWorkBasket', () => {
    it('should return an observable', () => {
      const obs = service.getSelectedWorkBasket();
      expect(obs).toBeDefined();
      expect(typeof obs.subscribe).toBe('function');
    });
  });

  describe('expandWorkbasketActionToolbar', () => {
    it('should emit true via workbasketActionToolbarExpanded subject', () => {
      let emitted: boolean;
      service.getWorkbasketActionToolbarExpansion().subscribe((val) => (emitted = val));

      service.expandWorkbasketActionToolbar(true);

      expect(emitted).toBe(true);
    });

    it('should emit false via workbasketActionToolbarExpanded subject', () => {
      let emitted: boolean;
      service.getWorkbasketActionToolbarExpansion().subscribe((val) => (emitted = val));

      service.expandWorkbasketActionToolbar(false);

      expect(emitted).toBe(false);
    });
  });

  describe('getWorkbasketActionToolbarExpansion', () => {
    it('should return an observable', () => {
      const obs = service.getWorkbasketActionToolbarExpansion();
      expect(obs).toBeDefined();
      expect(typeof obs.subscribe).toBe('function');
    });
  });

  describe('triggerWorkBasketSaved', () => {
    it('should emit a numeric timestamp via workBasketSaved subject', () => {
      let emitted: number;
      service.workbasketSavedTriggered().subscribe((val) => (emitted = val));

      service.triggerWorkBasketSaved();

      expect(typeof emitted).toBe('number');
      expect(emitted).toBeGreaterThan(0);
    });
  });

  describe('workbasketSavedTriggered', () => {
    it('should return an observable', () => {
      const obs = service.workbasketSavedTriggered();
      expect(obs).toBeDefined();
      expect(typeof obs.subscribe).toBe('function');
    });
  });

  describe('getWorkBasketsSummary', () => {
    it('should make a GET request when forceRequest is true', () => {
      service.getWorkBasketsSummary(true).subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('/v1/workbaskets'));
      expect(req.request.method).toBe('GET');
      req.flush({ workbaskets: [] });
    });

    it('should call domainService.getSelectedDomain when forceRequest is true', () => {
      service.getWorkBasketsSummary(true).subscribe();

      httpMock.expectOne((r) => r.url.includes('/v1/workbaskets')).flush({ workbaskets: [] });

      expect(mockDomainService.getSelectedDomain).toHaveBeenCalled();
      expect(mockDomainService.domainChangedComplete).toHaveBeenCalled();
    });

    it('should return cached observable when forceRequest is false and cache exists', () => {
      service.getWorkBasketsSummary(true).subscribe();
      httpMock.expectOne((r) => r.url.includes('/v1/workbaskets')).flush({ workbaskets: [] });

      const result = service.getWorkBasketsSummary(false);
      httpMock.expectNone((r) => r.url.includes('/v1/workbaskets'));
      expect(result).toBeDefined();
    });

    it('should return cached observable when forceRequest defaults to false', () => {
      const result = service.getWorkBasketsSummary();
      expect(result).toBeDefined();
      httpMock.expectNone((r) => r.url.includes('/v1/workbaskets'));
    });

    it('should include query string parameters when filterParameter is provided', () => {
      service.getWorkBasketsSummary(true, { name: ['TestWB'] }).subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('/v1/workbaskets'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('name=TestWB');
      req.flush({ workbaskets: [] });
    });

    it('should include sort parameter in query string when sortParameter is provided', () => {
      service
        .getWorkBasketsSummary(true, undefined, {
          'sort-by': WorkbasketQuerySortParameter.NAME,
          order: Direction.ASC
        })
        .subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('/v1/workbaskets'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('sort-by=NAME');
      req.flush({ workbaskets: [] });
    });

    it('should include paging parameter in query string when pagingParameter is provided', () => {
      service.getWorkBasketsSummary(true, undefined, undefined, { page: 2, 'page-size': 10 }).subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('/v1/workbaskets'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('page=2');
      req.flush({ workbaskets: [] });
    });
  });

  describe('getWorkBasketsDistributionTargets with parameters', () => {
    it('should include filter parameters in the URL', () => {
      const url = 'http://test/v1/workbaskets/wb-1/distribution-targets';
      service.getWorkBasketsDistributionTargets(url, { name: ['Target'] }).subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('distribution-targets'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('name=Target');
      req.flush({ distributionTargets: [] });
    });

    it('should include sort and paging parameters in the URL', () => {
      const url = 'http://test/v1/workbaskets/wb-1/distribution-targets';
      service
        .getWorkBasketsDistributionTargets(
          url,
          undefined,
          { 'sort-by': WorkbasketQuerySortParameter.NAME, order: Direction.DESC },
          { page: 1, 'page-size': 5 }
        )
        .subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('distribution-targets'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('sort-by=NAME');
      expect(req.request.url).toContain('page=1');
      req.flush({ distributionTargets: [] });
    });
  });

  describe('handleError (via updateWorkbasket)', () => {
    it('should propagate error message from plain Error object', () => {
      const url = 'http://test/v1/workbaskets/wb-err2';
      const workbasket: Workbasket = { workbasketId: 'wb-err2', name: 'Error WB' };
      let errorReceived: any;

      service.updateWorkbasket(url, workbasket).subscribe({ error: (err) => (errorReceived = err) });

      const req = httpMock.expectOne(url);
      req.flush({ message: 'Something went wrong' }, { status: 400, statusText: 'Bad Request' });

      expect(errorReceived).toBeDefined();
    });

    it('should use error.message when available in the non-Response error branch', () => {
      const url = 'http://test/v1/workbaskets/wb-msg-err';
      const workbasket: Workbasket = { workbasketId: 'wb-msg-err', name: 'Msg Error WB' };
      let errorReceived: any;

      service.updateWorkbasket(url, workbasket).subscribe({ error: (err) => (errorReceived = err) });

      const req = httpMock.expectOne(url);
      req.flush(null, { status: 503, statusText: 'Service Unavailable' });

      expect(errorReceived).toBeDefined();
      expect(typeof errorReceived).toBe('string');
    });

    it('should call error.toString() when error has no message property', () => {
      const url = 'http://test/v1/workbaskets/wb-tostring-err';
      const workbasket: Workbasket = { workbasketId: 'wb-tostring-err', name: 'ToString Error WB' };
      let errorReceived: any;

      const { throwError } = require('rxjs');
      const httpClient = (service as any).httpClient;
      vi.spyOn(httpClient, 'put').mockReturnValueOnce(throwError(() => ({ toString: () => 'plain error string' })));

      service.updateWorkbasket(url, workbasket).subscribe({ error: (err) => (errorReceived = err) });

      expect(errorReceived).toBe('plain error string');
    });

    it('should handle error instanceof Response — status and statusText branch', () => {
      const url = 'http://test/v1/workbaskets/wb-resp-err';
      const workbasket: Workbasket = { workbasketId: 'wb-resp-err', name: 'Response Error WB' };
      let errorReceived: any;

      const httpClient = (service as any).httpClient;
      const mockResponse = new Response(JSON.stringify({ detail: 'error' }), {
        status: 422,
        statusText: 'Unprocessable Entity'
      });
      const { throwError } = require('rxjs');
      vi.spyOn(httpClient, 'put').mockReturnValueOnce(throwError(() => mockResponse));

      service.updateWorkbasket(url, workbasket).subscribe({ error: (err) => (errorReceived = err) });

      expect(errorReceived).toBeDefined();
      expect(typeof errorReceived).toBe('string');
      expect(errorReceived).toContain('422');
    });
  });
});
