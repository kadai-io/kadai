/*
 * Copyright [2025] [envite consulting GmbH]
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

import { defineConfig } from 'cypress';

export default defineConfig({
  retries: 2,
  viewportWidth: 1280,
  viewportHeight: 720,

  env: {
    appUrl: 'http://localhost:4200/#/kadai',
    adminUrl: '/administration',
    dropdownWait: 80,
    testValueClassificationSelectionName: 'L10303',
    testValueClassifications: 'CY-TEST-CLASSIFICATIONS',
    testValueWorkbasketSelectionName: 'basxet0',
    testValueWorkbaskets: 'CY-TEST-WORKBASKETS',
    isLocal: true,
    isHistoryEnabled: false
  },

  component: {
    devServer: {
      framework: 'angular',
      bundler: 'webpack'
    },
    specPattern: '**/*.cy.ts'
  },

  e2e: {
    setupNodeEvents(on, config) {
      // implement node event listeners here
    }
  }
});
