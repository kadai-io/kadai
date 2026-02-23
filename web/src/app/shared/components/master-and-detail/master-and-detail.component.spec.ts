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
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MasterAndDetailComponent } from './master-and-detail.component';
import { MasterAndDetailService } from 'app/shared/services/master-and-detail/master-and-detail.service';
import { provideRouter } from '@angular/router';

describe('MasterAndDetailComponent', () => {
  let component: MasterAndDetailComponent;
  let fixture: ComponentFixture<MasterAndDetailComponent>;
  let masterAndDetailServiceMock: { setShowDetail: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    masterAndDetailServiceMock = {
      setShowDetail: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [MasterAndDetailComponent],
      providers: [provideRouter([]), { provide: MasterAndDetailService, useValue: masterAndDetailServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(MasterAndDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize showDetail to false when on a non-detail route', () => {
    expect(component.showDetail).toBe(false);
  });

  it('should call masterAndDetailService.setShowDetail on ngOnInit', () => {
    expect(masterAndDetailServiceMock.setShowDetail).toHaveBeenCalled();
  });

  it('should call masterAndDetailService.setShowDetail with false for a non-detail url', () => {
    expect(masterAndDetailServiceMock.setShowDetail).toHaveBeenCalledWith(false);
  });

  it('should have currentRoute as empty string initially when url does not contain known routes', () => {
    expect(component.currentRoute).toBe('');
  });
});
