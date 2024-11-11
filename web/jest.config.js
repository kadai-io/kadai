const { pathsToModuleNameMapper } = require('ts-jest');
const { compilerOptions } = require('./tsconfig');

const esModules = ['lodash-es'].join('|');

module.exports = {
  preset: 'jest-preset-angular',
  roots: ['<rootDir>/src'],
  modulePaths: ['<rootDir>'],
  moduleDirectories: ['node_modules', 'src'],
  testMatch: ['**/+(*.)+(spec).+(ts)'],
  setupFilesAfterEnv: ['<rootDir>/src/test.ts'],
  transform: {
    '^.+\\.mjs$': [
      'jest-preset-angular',
      {
        tsconfig: '<rootDir>/tsconfig.spec.json'
      }
    ]
  },
  transformIgnorePatterns: [`node_modules/(?!.*\\.mjs$|${esModules})`],
  collectCoverage: true,
  coverageReporters: ['text'],
  coverageDirectory: 'coverage/kadai-web',
  moduleNameMapper: pathsToModuleNameMapper(compilerOptions.paths || {}, {
    prefix: `<rootDir>/${compilerOptions.baseUrl}/`
  })
};
