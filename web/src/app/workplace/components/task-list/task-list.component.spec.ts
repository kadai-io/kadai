/*
 * Copyright [2024] [envite consulting GmbH]
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
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { Routes } from '@angular/router';
import { ChangeDetectorRef, Component } from '@angular/core';
import { WorkplaceService } from 'app/workplace/services/workplace.service';
import { TaskListComponent } from './task-list.component';
import { DateTimeZonePipe } from '../../../shared/pipes/date-time-zone.pipe';
import { MatSelectModule } from '@angular/material/select';
import { MatListModule } from '@angular/material/list';
import { MatBadgeModule } from '@angular/material/badge';
import { provideHttpClient } from '@angular/common/http';

@Component({
  selector: 'kadai-dummy-detail',
  template: 'dummydetail'
})
export class DummyDetailComponent {}

@Component({
  selector: 'svg-icon',
  template: '<p>Mock Icon Component</p>'
})
export class MockSvgIconComponent {}

describe('TaskListComponent', () => {
  let component: TaskListComponent;
  let fixture: ComponentFixture<TaskListComponent>;

  const routes: Routes = [{ path: '*', component: DummyDetailComponent }];

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, RouterTestingModule.withRoutes(routes), MatSelectModule, MatListModule, MatBadgeModule],
      declarations: [TaskListComponent, DummyDetailComponent, MockSvgIconComponent, DateTimeZonePipe],
      providers: [WorkplaceService, ChangeDetectorRef, provideHttpClient()]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
