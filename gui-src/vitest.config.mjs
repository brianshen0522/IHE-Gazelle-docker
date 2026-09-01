import path from "node:path";
import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    environment: "jsdom",
    setupFiles: "./__tests__/setupTests.ts",
    globals: true,
    coverage: {
      provider: "v8",
      reporter: ["text", "json", "html", "cobertura", "lcov"],
      include: [
        "src/app/**/components/**/*.{ts,tsx}",
        "src/app/**/hooks/**/*.{ts,tsx}",
        "src/app/**/(components)/**/*.{ts,tsx}",
        "src/app/**/(hooks)/**/*.{ts,tsx}",
        "src/shared/**/components/**/*.{ts,tsx}",
        "src/shared/hooks/**/*.{ts,tsx}",
        "src/shared/utils/**/*.{ts,tsx}",
      ],
      exclude: ["src/app/**/Types.{ts,tsx}", "src/app/**/types.{ts,tsx}", "src/shared/**/types.{ts,tsx}", "**/*.d.ts"],
      reportsDirectory: "./coverage",
    },
    alias: {
      "@": path.resolve(__dirname, "src"),
      "@shared": path.resolve(__dirname, "src/shared"),
      "@maestro": path.resolve(__dirname, "src/shared/services/maestro"),
      "@auth": path.resolve(__dirname, "src/shared/components/auth"),
      "@hooks": path.resolve(__dirname, "src/shared/hooks"),
      "@home": path.resolve(__dirname, "src/app/home"),
      "@test-execution": path.resolve(__dirname, "src/app/test-execution"),
      "@message-capture": path.resolve(__dirname, "src/app/message-capture"),
      "@user-management": path.resolve(__dirname, "src/app/user-management"),
      "@validation-portal": path.resolve(__dirname, "src/app/validation-portal"),
      "@/assets": path.resolve(__dirname, "src/app/assets"),
    },
  },
});
