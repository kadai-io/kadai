import typescriptEslint from '@typescript-eslint/eslint-plugin';
import typescriptEslintTslint from '@typescript-eslint/eslint-plugin-tslint';
import tsParser from '@typescript-eslint/parser';
import globals from 'globals';
import eslintPluginPrettierRecommended
  from 'eslint-plugin-prettier/recommended';

export default [
  {
    files: ['**/*.js', '**/*.ts'],
    plugins: {
      '@typescript-eslint': typescriptEslint,
      '@typescript-eslint/tslint': typescriptEslintTslint
    },
    languageOptions: {
      parser: tsParser,
      ecmaVersion: 2018,
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node
      },
      parserOptions: {
        project: './tsconfig.json',
        errorOnTypeScriptSyntacticAndSemanticIssues: true
      }
    }
  },
  eslintPluginPrettierRecommended
];
