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
      loginAs(username: string): Chainable<void>;
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
  cy.get('mat-tab-header').contains('Information').click({ force: true });
});

Cypress.Commands.add('visitWorkbasketsAccessPage', () => {
  cy.get('mat-tab-header').contains('Access').click({ force: true });
});

Cypress.Commands.add('visitWorkbasketsDistributionTargetsPage', () => {
  cy.get('mat-tab-header').contains('Distribution Targets').click({ force: true });
});

Cypress.Commands.add('saveWorkbaskets', () => {
  cy.get('button').contains('Save').click();
});

Cypress.Commands.add('undoWorkbaskets', () => {
  cy.get('button').contains('Undo Changes').click();
});

Cypress.Commands.add('verifyPageLoad', (path: string) => {
  cy.location('hash', { timeout: 10000 }).should('include', path);
});

Cypress.Commands.add('visitTestWorkbasket', () => {
  cy.visit(Cypress.env('appUrl') + Cypress.env('adminUrl') + '/workbaskets');
  cy.verifyPageLoad('/workbaskets');

  // since the list is loaded dynamically, we need to explicitly wait 1000ms for the results
  // in order to avoid errors regarding detached DOM elements although it is a bad practice
  cy.wait(1000);
  cy.get('mat-selection-list').contains(Cypress.env('testValueWorkbasketSelectionName')).should('exist').click();
  cy.visitWorkbasketsInformationPage();
});

Cypress.Commands.add('visitTestClassification', () => {
  cy.visit(Cypress.env('appUrl') + Cypress.env('adminUrl') + '/classifications');
  cy.verifyPageLoad('/classifications');

  cy.get('kadai-administration-tree')
    .contains(Cypress.env('testValueClassificationSelectionName'))
    .should('exist')
    .click();
});

Cypress.Commands.add('visitMonitor', () => {
  cy.visit(Cypress.env('appUrl') + '/monitor');
  cy.wait(1000);
  cy.verifyPageLoad('/monitor');
});

Cypress.Commands.add('loginAs', (username: string) => {
  if (Cypress.env('isLocal')) {
    cy.log('Local development - No need for testing login functionality');
  } else {
    cy.visit(Cypress.env('loginUrl') + '/login');
    // not calling verifyPageLoad as we cannot verify via hash in this case
    cy.location('pathname', { timeout: 10000 }).should('include', '/login');

    cy.get('#username').type('admin').should('have.value', 'admin');
    cy.get('#password').type('admin').should('have.value', 'admin');
    cy.get('#login-submit').click();

    cy.verifyPageLoad('/kadai/administration/workbaskets');
  }
});
