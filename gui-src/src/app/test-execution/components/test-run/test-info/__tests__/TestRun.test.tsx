/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import TestRun from "../TestRun";
import { TestModel } from "../../../../types/TestModel";
import { TestRunExecution } from "../../../../types/TestRunExecution";

// Mock dependencies
vi.mock("@gazelle/gazelle-component-ui", () => ({
  InfoRow: ({ label, value }: any) => (
    <div data-testid="info-row">
      <span data-testid="info-row-label">{label}</span>
      <span data-testid="info-row-value">{value}</span>
    </div>
  ),
  TagSection: ({ items, labelKey }: any) => (
    <div data-testid="tag-section">
      <span data-testid="tag-label">{labelKey}</span>
      {items.map((item: string) => (
        <span key={item} data-testid={`tag-${item}`}>
          {item}
        </span>
      ))}
    </div>
  ),
}));

vi.mock("@/shared/hooks/useDateFormat", () => ({
  __esModule: true,
  default: () => vi.fn((date: string | number) => `Formatted: ${date}`),
}));

vi.mock("../LastExecution", () => ({
  __esModule: true,
  default: ({ testRun, reportUrl }: any) => (
    <div data-testid="last-execution">
      <span data-testid="last-execution-id">{testRun.id}</span>
      {reportUrl && <span data-testid="last-execution-report-url">{reportUrl}</span>}
    </div>
  ),
}));

vi.mock("../../../utils/getReportUrl", () => ({
  getReportUrl: ({ testReportUrl }: any) => testReportUrl || null,
}));

vi.mock("@/shared/components/ACL/AclDisplay", () => ({
  AclDisplay: ({ acl, itemId }: any) => (
    <div data-testid="acl-display">
      <span data-testid="acl-item-id">{itemId}</span>
      <span data-testid="acl-public">{acl?.isPublic ? "public" : "private"}</span>
    </div>
  ),
}));

describe("TestRun", () => {
  const createMockTestModel = (overrides?: Partial<TestModel>): TestModel => ({
    id: "test-model-1",
    name: "Test Model Name",
    summary: "Test Summary",
    description: "Test Description",
    tags: ["tag1", "tag2"],
    testRoles: [],
    version: "1.0.0",
    ...overrides,
  });

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

  describe("Component Rendering", () => {
    it("should return null when testModel is undefined", () => {
      const testRun = createMockExecution();
      const { container } = render(<TestRun testModel={undefined} testRun={testRun} />);

      expect(container.firstChild).toBeNull();
    });

    it("should render test model information when testModel is provided", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution();

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.getAllByTestId("info-row")).toHaveLength(5); // test_run_id, last_execution_date, owner ,test_name, test_summary
    });

    it("should render testSessionId when provided", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution();
      const testSessionId = "session-123";

      render(<TestRun testModel={testModel} testSessionId={testSessionId} testRun={testRun} />);

      // Should have 6 info rows: test_run_id, test_session, last_execution_date, owner, test_name, test_summary
      expect(screen.getAllByTestId("info-row")).toHaveLength(6);
    });
  });

  describe("LastExecution Component", () => {
    it("should not render LastExecution component when there are no testReportSummaries", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution();

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.queryByTestId("last-execution")).toBeNull();
    });

    it("should render LastExecution component when testReportSummaries exist", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution({
        testReportSummaries: [
          {
            id: "summary-1",
            result: "PASSED",
            dateTime: "2026-01-01T10:00:00Z",
            location: "http://example.com/report/1",
          },
        ],
      });

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.getByTestId("last-execution")).toBeInTheDocument();
      expect(screen.getByTestId("last-execution-id")).toHaveTextContent("exec-1");
    });

    it("should pass report URL to LastExecution when testReportUrl is available", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution({
        testReportUrl: "http://example.com/report/1",
        testReportSummaries: [
          {
            id: "summary-1",
            result: "PASSED",
            dateTime: "2026-01-01T10:00:00Z",
            location: "http://example.com/report/summary",
          },
        ],
      });

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.getByTestId("last-execution-report-url")).toHaveTextContent("http://example.com/report/1");
    });

    it("should pass report URL to LastExecution from testReportSummaries when testReportUrl is not available", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution({
        testReportSummaries: [
          {
            id: "summary-1",
            result: "PASSED",
            dateTime: "2026-01-01",
            location: "http://example.com/report/summary/1",
          },
        ],
      });

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.getByTestId("last-execution-report-url")).toHaveTextContent("http://example.com/report/summary/1");
    });
  });

  describe("Last Execution Date", () => {
    it("should display formatted last execution date from testReportSummaries", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution({
        testReportSummaries: [
          {
            id: "summary-1",
            result: "PASSED",
            dateTime: "2026-01-01T10:00:00Z",
            location: "http://example.com/report/1",
          },
        ],
      });

      render(<TestRun testModel={testModel} testRun={testRun} />);

      const infoRows = screen.getAllByTestId("info-row-value");
      const hasFormattedDate = infoRows.some((row) => row.textContent?.includes("Formatted: 2026-01-01T10:00:00Z"));
      expect(hasFormattedDate).toBe(true);
    });

    it("should display empty string when no testReportSummaries are available", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution();

      render(<TestRun testModel={testModel} testRun={testRun} />);

      const infoRows = screen.getAllByTestId("info-row-value");
      // One of the rows should have empty value (last execution date)
      const hasEmptyValue = infoRows.some((row) => row.textContent === "");
      expect(hasEmptyValue).toBe(true);
    });

    it("should use the last testReportSummary dateTime when multiple are available", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution({
        testReportSummaries: [
          {
            id: "summary-1",
            result: "PASSED",
            dateTime: "2026-01-01T10:00:00Z",
            location: "http://example.com/report/1",
          },
          {
            id: "summary-2",
            result: "FAILED",
            dateTime: "2026-01-02T15:00:00Z",
            location: "http://example.com/report/2",
          },
        ],
      });

      render(<TestRun testModel={testModel} testRun={testRun} />);

      const infoRows = screen.getAllByTestId("info-row-value");
      // Should use the last summary's dateTime
      const hasLatestDate = infoRows.some((row) => row.textContent?.includes("Formatted: 2026-01-02T15:00:00Z"));
      expect(hasLatestDate).toBe(true);
    });
  });

  describe("Tags Display", () => {
    it("should display tags section when tags are available", () => {
      const testModel = createMockTestModel({
        tags: ["integration", "regression", "smoke"],
      });
      const testRun = createMockExecution();

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.getByTestId("tag-section")).toBeInTheDocument();
      expect(screen.getByTestId("tag-integration")).toHaveTextContent("integration");
      expect(screen.getByTestId("tag-regression")).toHaveTextContent("regression");
      expect(screen.getByTestId("tag-smoke")).toHaveTextContent("smoke");
    });

    it("should not display tags section when tags array is empty", () => {
      const testModel = createMockTestModel({
        tags: [],
      });
      const testRun = createMockExecution();

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.queryByTestId("tag-section")).toBeNull();
    });

    it("should not display tags section when tags is undefined", () => {
      const testModel = createMockTestModel({
        tags: undefined as any,
      });
      const testRun = createMockExecution();

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.queryByTestId("tag-section")).toBeNull();
    });
  });

  describe("Report URL Generation", () => {
    it("should prioritize testReportUrl over testReportSummaries location", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution({
        testReportUrl: "http://example.com/report/direct",
        testReportSummaries: [
          {
            id: "summary-1",
            result: "PASSED",
            dateTime: "2026-01-01",
            location: "http://example.com/report/summary",
          },
        ],
      });

      render(<TestRun testModel={testModel} testRun={testRun} />);

      // Should use testReportUrl, not summary location
      expect(screen.getByTestId("last-execution-report-url")).toHaveTextContent("http://example.com/report/direct");
    });

    it("should fallback to testReportSummaries location when testReportUrl is not available", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution({
        testReportSummaries: [
          {
            id: "summary-1",
            result: "PASSED",
            dateTime: "2026-01-01",
            location: "http://example.com/report/summary",
          },
        ],
      });

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.getByTestId("last-execution-report-url")).toHaveTextContent("http://example.com/report/summary");
    });

    it("should return undefined when neither testReportUrl nor testReportSummaries are available", () => {
      const testModel = createMockTestModel();
      const testRun = createMockExecution();

      render(<TestRun testModel={testModel} testRun={testRun} />);

      expect(screen.queryByTestId("last-execution-report-url")).toBeNull();
    });
  });
});
