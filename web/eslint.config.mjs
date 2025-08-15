import typescriptEslint from '@typescript-eslint/eslint-plugin';
import tsParser from '@typescript-eslint/parser';
import globals from 'globals';
import eslintPluginPrettierRecommended
  from 'eslint-plugin-prettier/recommended';
import eslintPluginImport from 'eslint-plugin-import';

export default [
  {
    files: ['**/*.js', '**/*.ts'],
    plugins: {
      '@typescript-eslint': typescriptEslint
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
  eslintPluginPrettierRecommended,
  eslintPluginImport.flatConfigs.recommended,
  {
    rules: {
      'import/named': 'off',
      'import/no-unresolved': 'off'
    }
  }
];
