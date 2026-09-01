import typescriptEslint from "@typescript-eslint/eslint-plugin";
import typescriptParser from "@typescript-eslint/parser";
import reactHooks from "eslint-plugin-react-hooks";

const eslintConfig = [
  {
    files: ["**/*.ts", "**/*.tsx", "**/*.js", "**/*.jsx"],
    plugins: {
      "@typescript-eslint": typescriptEslint,
      "react-hooks": reactHooks,
    },
    languageOptions: {
      parser: typescriptParser,
      parserOptions: {
        ecmaVersion: "latest",
        sourceType: "module",
        ecmaFeatures: {
          jsx: true,
        },
      },
    },
    rules: {
      "prefer-const": "error",
      eqeqeq: "error",
      "no-unused-vars": "off",
      "@typescript-eslint/no-unused-vars": "warn",
      "no-trailing-spaces": "error",
      "no-unreachable": "error",
      "default-case": "error",
      "no-duplicate-imports": "error",
      "no-console": ["error", { allow: ["warn", "error"] }],
      "@typescript-eslint/no-explicit-any": "warn",
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn",
    },
  },
];

export default eslintConfig;
