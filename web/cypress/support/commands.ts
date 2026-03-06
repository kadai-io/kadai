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

function nativeTabClick(tabLabel: string) {
  // Use native dispatchEvent to bypass all Cypress coverage checks.
  // Dispatches click on the div[role="tab"] so Angular's zone-patched click handler fires,
  // the tab activates, and CDK virtual scroll renders items.
  cy.get('mat-tab-header')
    .contains('.mdc-tab__text-label', tabLabel)
    .then(($span) => {
      const tabDiv = ($span[0].closest('[role="tab"]') || $span[0]) as HTMLElement;
      tabDiv.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, composed: true }));
    });
  // Brief wait for Angular zone + CDK virtual scroll to initialize after tab activation
  cy.wait(300);
}

Cypress.Commands.add('visitWorkbasketsInformationPage', () => {
  nativeTabClick('Information');
});

Cypress.Commands.add('visitWorkbasketsAccessPage', () => {
  nativeTabClick('Access');
});

Cypress.Commands.add('visitWorkbasketsDistributionTargetsPage', () => {
  nativeTabClick('Distribution Targets');
});

Cypress.Commands.add('saveWorkbaskets', () => {
  cy.get('.workbasket-details__save-button').click();
  // Wait for the HTTP response to come back (a toast always appears after save)
  cy.get('.hot-toast-bar-base-wrapper', { timeout: 8000 }).should('exist');
  // Dismiss persistent error toasts (autoClose: false)
  cy.get('body').then(($body) => {
    if ($body.find('.hot-toast-close-btn').length > 0) {
      cy.get('.hot-toast-close-btn').click({ multiple: true, force: true });
    }
  });
  // Wait for all toasts to disappear (success toasts auto-close in 5000ms)
  cy.get('.hot-toast-bar-base-wrapper', { timeout: 8000 }).should('not.exist');
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
