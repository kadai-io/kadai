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

context.skip('KADAI Monitor', () => {
  beforeEach(() => cy.loginAs('admin'));

  it('should visit kadai tasks by status monitor page', () => {
    cy.intercept('**/monitor/task-status-report*').as('monitorData');
    cy.visitMonitor();
    cy.get('nav').find('a').contains('Tasks by Status').click();
    cy.verifyPageLoad('/tasks-status');
    cy.get('nav').find('.mat-tab-label-active').should('contain', 'Tasks by Status');
    cy.wait('@monitorData');
    cy.get('canvas.chartjs-render-monitor').should('be.visible');
  });

  it('should visit kadai tasks by priority monitor page', () => {
    cy.intercept('**/monitor/workbasket-priority-report*').as('monitorData');
    cy.visitMonitor();
    cy.get('nav').find('a').contains('Tasks by Priority').click();
    cy.verifyPageLoad('/tasks-priority');
    cy.get('nav').find('.mat-tab-label-active').should('contain', 'Tasks by Priority');
    cy.wait('@monitorData');
    cy.get('canvas.chartjs-render-monitor').should('be.visible');
  });

  it('should visit kadai workbaskets monitor page', () => {
    cy.intercept('**/monitor/workbasket-report*').as('monitorData');
    cy.visitMonitor();
    cy.get('nav').find('a').contains('Workbaskets').click();
    cy.verifyPageLoad('/workbaskets');
    cy.get('nav').find('.mat-tab-label-active').should('contain', 'Workbaskets');
    cy.wait('@monitorData');
    cy.get('canvas.chartjs-render-monitor').should('be.visible');
  });

  it('should visit kadai classifications monitor page', () => {
    cy.intercept('**/monitor/classification-report').as('monitorData');
    cy.visitMonitor();
    cy.get('nav').find('a').contains('Classifications').click();
    cy.verifyPageLoad('/classifications');
    cy.get('nav').find('.mat-tab-label-active').should('contain', 'Classifications');
    cy.wait('@monitorData');
    cy.get('canvas.chartjs-render-monitor').should('be.visible');
  });

  it('should visit kadai timestamp monitor page', () => {
    cy.intercept('**/monitor/timestamp*').as('monitorData');
    cy.visitMonitor();
    cy.get('nav').find('a').contains('Timestamp').click();
    cy.verifyPageLoad('/timestamp');
    cy.get('nav').find('.mat-tab-label-active').should('contain', 'Timestamp');
    cy.wait('@monitorData');
    cy.contains('TimestampReport').should('be.visible');
  });
});
