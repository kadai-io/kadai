/*
 * Copyright [2026] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law of an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Routes } from '@angular/router';
import { Component } from '@angular/core';
import { TaskListComponent } from './task-list.component';
import { provideHttpClient } from '@angular/common/http';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { beforeEach, describe, expect, it } from 'vitest';

@Component({
  selector: 'kadai-dummy-detail',
  template: 'dummydetail'
})
export class DummyDetailComponent {}

describe('TaskListComponent', () => {
  let component: TaskListComponent;
  let fixture: ComponentFixture<TaskListComponent>;

  const routes: Routes = [{ path: '*', component: DummyDetailComponent }];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskListComponent],
      providers: [provideHttpClient(), provideAngularSvgIcon(), provideRouter(routes)]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
