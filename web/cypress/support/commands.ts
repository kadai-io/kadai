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

/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      loginAsAdmin(): Chainable<void>;
      verifyPageLoad(path: string): Chainable<void>;
      visitTestWorkbasket(): Chainable<void>;
      visitTestClassification(): Chainable<void>;
      visitMonitor(): Chainable<void>;
      visitWorkbasketsInformationPage(): Chainable<void>;
      visitWorkbasketsAccessPage(): Chainable<void>;
      visitWorkbasketsDistributionTargetsPage(): Chainable<void>;
      saveWorkbaskets(): Chainable<void>;
      undoWorkbaskets(): Chainable<void>;
    }
  }
}

Cypress.Commands.add('visitWorkbasketsInformationPage', () => {
  cy.get('mat-tab-header').contains('Information').click();
});

Cypress.Commands.add('visitWorkbasketsAccessPage', () => {
  cy.get('mat-tab-header').contains('Access').click();
});

Cypress.Commands.add('visitWorkbasketsDistributionTargetsPage', () => {
  cy.get('mat-tab-header').contains('Distribution Targets').click();
  cy.get('#dual-list-Right').find('mat-list-option', { timeout: 15000 }).should('have.length.gte', 1);
  cy.get('#dual-list-Left').find('mat-list-option', { timeout: 15000 }).should('have.length.gte', 1);
});

Cypress.Commands.add('saveWorkbaskets', () => {
  cy.get('.workbasket-details__save-button').click();
  cy.get('.hot-toast-bar-base-wrapper', { timeout: 8000 }).should('exist');
  cy.get('body').then(($body) => {
    if ($body.find('.hot-toast-close-btn').length > 0) {
      cy.get('.hot-toast-close-btn').trigger('click', { force: true, multiple: true });
    }
  });
  cy.get('.hot-toast-bar-base-wrapper', { timeout: 15000 }).should('not.exist');
});

Cypress.Commands.add('undoWorkbaskets', () => {
  cy.get('button').contains('Undo Changes').click();
});

Cypress.Commands.add('verifyPageLoad', (path: string) => {
  cy.location('hash', { timeout: 10000 }).should('include', path);
});

Cypress.Commands.add('visitTestWorkbasket', () => {
  cy.visit(Cypress.expose('appUrl') + Cypress.expose('adminUrl') + '/workbaskets');
  cy.verifyPageLoad('/workbaskets');
  cy.get('mat-selection-list', { timeout: 10000 }).should('be.visible');
  cy.get('mat-selection-list').contains(Cypress.expose('testValueWorkbasketSelectionName')).click();
  cy.visitWorkbasketsInformationPage();
});

Cypress.Commands.add('visitTestClassification', () => {
  cy.visit(Cypress.expose('appUrl') + Cypress.expose('adminUrl') + '/classifications');
  cy.verifyPageLoad('/classifications');

  cy.get('kadai-administration-tree').contains(Cypress.expose('testValueClassificationSelectionName')).click();
});

Cypress.Commands.add('visitMonitor', () => {
  cy.visit(Cypress.expose('appUrl') + '/monitor');
  cy.verifyPageLoad('/monitor');
});

Cypress.Commands.add('loginAsAdmin', () => {
  cy.visit(Cypress.expose('loginUrl') + '/login');
  cy.location('pathname', { timeout: 10000 }).should('include', '/login');

  cy.get('#username').type('admin');
  cy.get('#password').type('admin');
  cy.get('#login-submit').click();

  cy.verifyPageLoad('/kadai/administration/workbaskets');
});
