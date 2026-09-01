/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import LastExecution from "../LastExecution";
import { TestRunExecution } from "../../../../types/TestRunExecution";

// Mock components
vi.mock("@/shared/components/report/ResultBadge", () => ({
  ResultBadge: (props: any) => React.createElement("span", { "data-testid": `result-badge-${props.result}` }, props.result),
}));

vi.mock("lucide-react", () => ({
  FileCheck: () => React.createElement("svg", { "data-testid": "file-check-icon" }),
}));

vi.mock("next/link", () => ({
  default: (props: any) => React.createElement("a", { href: props.href, ...props }, props.children),
}));

describe("LastExecution", () => {
  const createMockExecution = (overrides?: Partial<TestRunExecution>): TestRunExecution =>
    ({
      id: "exec-1",
      testId: "test-1",
      testSessionId: "session-1",
      testSuiteId: "suite-1",
      testSpecification: "{}",
      testName: "Test Execution",
      status: "NOT_STARTED",
      startedAt: "2026-01-01",
      executedBy: "user-1",
      inputs: [],
      ...overrides,
    }) as TestRunExecution;

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Test Result Display - testReport (fresh execution)", () => {
    it("should display test result from testReport when available", () => {
      const testRun = createMockExecution({
        testReport: {
          uuid: "report-1",
          result: "PASSED",
          dateTime: "2026-01-01",
          testService: {
            serviceIdentification: { version: "1.0", name: "Test Service" },
            disclaimer: "Test disclaimer",
          },
          testSuiteName: "Test Suite",
          testCounters: { total: 10, passed: 10, failed: 0, undefined: 0, unexpectedErrors: 0 },
          testRunReports: [],
        },
      });

      render(<LastExecution testRun={testRun} />);

      expect(screen.getByTestId("result-badge-PASSED")).toBeInTheDocument();
    });

    it("should display failed result from testReport", () => {
      const testRun = createMockExecution({
        testReport: {
          uuid: "report-1",
          result: "FAILED",
          dateTime: "2026-01-01",
          testService: {
            serviceIdentification: { version: "1.0", name: "Test Service" },
            disclaimer: "Test disclaimer",
          },
          testSuiteName: "Test Suite",
          testCounters: { total: 10, passed: 5, failed: 5, undefined: 0, unexpectedErrors: 0 },
          testRunReports: [],
        },
      });

      render(<LastExecution testRun={testRun} />);

      expect(screen.getByTestId("result-badge-FAILED")).toBeInTheDocument();
    });
  });

  describe("Test Result Display - testReportSummaries (existing execution)", () => {
    it("should fallback to last testReportSummary result when testReport is not available", () => {
      const testRun = createMockExecution({
        testReportSummaries: [
          {
            id: "summary-1",
            result: "PASSED",
            dateTime: "2026-01-01",
            location: "http://example.com/report/1",
          },
          {
            id: "summary-2",
            result: "FAILED",
            dateTime: "2026-01-02",
            location: "http://example.com/report/2",
          },
        ],
      });

      render(<LastExecution testRun={testRun} />);

      // Should display the last summary result (FAILED)
      expect(screen.getByTestId("result-badge-FAILED")).toBeInTheDocument();
    });

    it("should display PASSED from testReportSummary", () => {
      const testRun = createMockExecution({
        testReportSummaries: [
          {
            id: "summary-1",
            result: "PASSED",
            dateTime: "2026-01-01",
            location: "http://example.com/report/1",
          },
        ],
      });

      render(<LastExecution testRun={testRun} />);

      expect(screen.getByTestId("result-badge-PASSED")).toBeInTheDocument();
    });

    it("should display UNDEFINED from testReportSummary", () => {
      const testRun = createMockExecution({
        testReportSummaries: [
          {
            id: "summary-1",
            result: "UNDEFINED",
            dateTime: "2026-01-01",
            location: "http://example.com/report/1",
          },
        ],
      });

      render(<LastExecution testRun={testRun} />);

      expect(screen.getByTestId("result-badge-UNDEFINED")).toBeInTheDocument();
    });

    it("should not display result badge when neither testReport nor testReportSummaries are available", () => {
      const testRun = createMockExecution();

      render(<LastExecution testRun={testRun} />);

      // Should not render any result badge when no test result is available
      expect(screen.queryByTestId(/result-badge-/)).toBeNull();
    });

    it("should prioritize testReport over testReportSummaries when both are available", () => {
      const testRun = createMockExecution({
        testReport: {
          uuid: "report-1",
          result: "PASSED",
          dateTime: "2026-01-01",
          testService: {
            serviceIdentification: { version: "1.0", name: "Test Service" },
            disclaimer: "Test disclaimer",
          },
          testSuiteName: "Test Suite",
          testCounters: { total: 10, passed: 10, failed: 0, undefined: 0, unexpectedErrors: 0 },
          testRunReports: [],
        },
        testReportSummaries: [
          {
            id: "summary-1",
            result: "FAILED",
            dateTime: "2026-01-01",
            location: "http://example.com/report/1",
          },
        ],
      });

      render(<LastExecution testRun={testRun} />);

      // Should display testReport result (PASSED), not summary result (FAILED)
      expect(screen.getByTestId("result-badge-PASSED")).toBeInTheDocument();
      expect(screen.queryByTestId("result-badge-FAILED")).toBeNull();
    });
  });

  describe("Report URL Link", () => {
    it("should display report link when reportUrl is provided", () => {
      const testRun = createMockExecution();
      const reportUrl = "/test-execution/report/123";

      render(<LastExecution testRun={testRun} reportUrl={reportUrl} />);

      const link = screen.getByRole("link");
      expect(link).toHaveAttribute("href", reportUrl);
      expect(link).toHaveAttribute("target", "_blank");
      expect(link).toHaveAttribute("rel", "noopener noreferrer");
    });

    it("should not display report link when reportUrl is not provided", () => {
      const testRun = createMockExecution();

      render(<LastExecution testRun={testRun} />);

      expect(screen.queryByRole("link")).toBeNull();
    });
  });
});
