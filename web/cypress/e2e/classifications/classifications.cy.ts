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

describe('KADAI Classifications', () => {
  beforeEach(() => cy.loginAsAdmin());

  it('should be possible to edit the service level of a classification', () => {
    const editedValue = 'P99D';

    cy.visitTestClassification();

    cy.get('#classification-service-level').clear({ force: true }).type(editedValue);
    cy.get('button').contains('Save').click();

    cy.get('#classification-service-level').should('have.value', editedValue);
  });

  it('should be able to visit classifications and filter by manual', () => {
    cy.visit(Cypress.expose('appUrl') + Cypress.expose('adminUrl') + '/classifications');
    cy.verifyPageLoad('/classifications');

    cy.get('button[mattooltip="Filter Category"]')
      .click()
      .then(() => {
        cy.get('.mat-mdc-menu-content').contains('MANUAL').click();
        cy.get('tree-node-collection').find('tree-node').should('have.length', 8);
      });
  });

  it('should be possible to edit the name of a classification', () => {
    const editedValue = 'CY-TEST';

    cy.visitTestClassification();

    cy.get('#classification-name').scrollIntoView().clear().type(editedValue);
    cy.get('button').contains('Save').click();

    cy.get('#classification-name').should('have.value', editedValue);
  });

  it('should be possible to edit the category of a classification', () => {
    cy.visitTestClassification();

    cy.get('ng-form').find('mat-form-field mat-select[required]').click({ force: true });
    cy.wait(Cypress.expose('dropdownWait'));
    cy.get('mat-option').contains('PROCESS').click();
    cy.get('button').contains('Save').click();

    cy.get('ng-form').find('mat-form-field mat-select[required]').contains('PROCESS').should('be.visible');

    cy.get('ng-form').find('mat-form-field mat-select[required]').click({ force: true });
    cy.wait(Cypress.expose('dropdownWait'));
    cy.get('mat-option').contains('EXTERNAL').should('be.visible').click();

    cy.get('button').contains('Save').click();
  });

  it('should be possible to edit the description of a classification', () => {
    const editedValue = 'CY-TEST-DESC';

    cy.visitTestClassification();

    cy.get('#classification-description').clear({ force: true }).type(editedValue);
    cy.get('button').contains('Save').click();

    cy.get('#classification-description').should('have.value', editedValue);
  });

  it('should be possible to edit the custom classification', () => {
    cy.visitTestClassification();

    cy.get('#classification-custom-1').clear({ force: true }).type(Cypress.expose('testValueClassifications'));

    cy.get('button').contains('Save').click();

    cy.get('#classification-custom-1').should('have.value', Cypress.expose('testValueClassifications'));
  });

  it('should be possible to edit the application entry point', () => {
    cy.visitTestClassification();

    cy.get('#classification-application-entry-point')
      .clear({ force: true })
      .type(Cypress.expose('testValueClassifications'));
    cy.get('button').contains('Save').click();

    cy.get('#classification-application-entry-point').should('have.value', Cypress.expose('testValueClassifications'));
  });
});
