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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { provideRouter, Routes } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { TaskProcessingComponent } from './task-processing.component';

@Component({
  selector: 'kadai-dummy-detail',
  template: 'dummydetail'
})
class DummyDetailComponent {}

const routes: Routes = [{ path: 'workplace/tasks', component: DummyDetailComponent }];

describe('TaskProcessingComponent', () => {
  let component: TaskProcessingComponent;
  let fixture: ComponentFixture<TaskProcessingComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TaskProcessingComponent],
      providers: [provideRouter(routes), provideHttpClient()]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskProcessingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
