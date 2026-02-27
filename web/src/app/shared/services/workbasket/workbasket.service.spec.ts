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
    });

    it('should call domainChangedComplete after domain emission', () => {
      service.getWorkBasketsSummary(true).subscribe();

      httpMock.expectOne((r) => r.url.includes('/v1/workbaskets')).flush({ workbaskets: [] });

      expect(mockDomainService.domainChangedComplete).toHaveBeenCalled();
    });
  });
});
