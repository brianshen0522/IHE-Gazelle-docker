/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import TestExecutionHistory from "../TestExecutionHistory";
import { TestReportSummary } from "@/app/test-execution/types/TestRunExecution";

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

vi.mock("@/shared/hooks/useDateFormat", () => ({
  default: () => (date: string) => new Date(date).toLocaleString(),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  Tooltip: ({ children }: any) => <div data-testid="tooltip">{children}</div>,
}));

vi.mock("@/shared/components/report/ResultBadge", () => ({
  ResultBadge: ({ result }: any) => <span data-testid="result-badge">{result}</span>,
}));

vi.mock("../utils/getReportUrl", () => ({
  getReportUrl: ({ testReportUrl, testSessionId }: any) => {
    const reportId = testReportUrl?.match(/\/items\/([^/]+)/)?.[1] || "report-id";
    return testSessionId ? `/test-execution/report/${reportId}?testSessionId=${testSessionId}` : `/test-execution/report/${reportId}`;
  },
}));

describe("TestExecutionHistory", () => {
  const mockTestReportSummaries: TestReportSummary[] = [
    {
      id: "summary-1",
      result: "PASSED",
      dateTime: "2026-01-01T10:00:00.000Z",
      location: "http://api.example.com/items/report-1",
    },
    {
      id: "summary-2",
      result: "FAILED",
      dateTime: "2026-01-02T11:00:00.000Z",
      location: "http://api.example.com/items/report-2",
    },
    {
      id: "summary-3",
      result: "UNDEFINED",
      dateTime: "2026-01-03T12:00:00.000Z",
      location: "http://api.example.com/items/report-3",
    },
  ];

  it("should render information text", () => {
    render(<TestExecutionHistory testReportSummaries={mockTestReportSummaries} />);

    expect(screen.getByText("gzl.user.interface.test_exec_history_information")).toBeInTheDocument();
  });

  it("should render all test report summaries in reverse order", () => {
    render(<TestExecutionHistory testReportSummaries={mockTestReportSummaries} />);

    const links = screen.getAllByRole("link");
    expect(links).toHaveLength(3);

    // Should be reversed: summary-3, summary-2, summary-1
    expect(links[0]).toHaveAttribute("href", "/test-execution/reports/report-3");
    expect(links[1]).toHaveAttribute("href", "/test-execution/reports/report-2");
    expect(links[2]).toHaveAttribute("href", "/test-execution/reports/report-1");
  });

  it("should render links with correct attributes", () => {
    render(<TestExecutionHistory testReportSummaries={mockTestReportSummaries} />);

    const links = screen.getAllByRole("link");

    links.forEach((link) => {
      expect(link).toHaveAttribute("target", "_blank");
      expect(link).toHaveAttribute("rel", "noopener noreferrer");
      expect(link).toHaveClass("text-blue", "hover:text-visited_link", "underline");
    });
  });

  it("should render formatted dates", () => {
    render(<TestExecutionHistory testReportSummaries={mockTestReportSummaries} />);

    const links = screen.getAllByRole("link");

    // Check that dates are formatted (using the mock formatter)
    expect(links[0]).toHaveTextContent(new Date("2026-01-03T12:00:00.000Z").toLocaleString());
    expect(links[1]).toHaveTextContent(new Date("2026-01-02T11:00:00.000Z").toLocaleString());
    expect(links[2]).toHaveTextContent(new Date("2026-01-01T10:00:00.000Z").toLocaleString());
  });

  it("should render result badges", () => {
    render(<TestExecutionHistory testReportSummaries={mockTestReportSummaries} />);

    const badges = screen.getAllByTestId("result-badge");
    expect(badges).toHaveLength(3);

    // Should be reversed
    expect(badges[0]).toHaveTextContent("UNDEFINED");
    expect(badges[1]).toHaveTextContent("FAILED");
    expect(badges[2]).toHaveTextContent("PASSED");
  });

  it("should include testSessionId in URL when provided", () => {
    render(<TestExecutionHistory testReportSummaries={mockTestReportSummaries} testSessionId="session-123" />);

    const links = screen.getAllByRole("link");

    links.forEach((link) => {
      expect(link.getAttribute("href")).toContain("testSessionId=session-123");
    });
  });

  it("should not include testSessionId in URL when not provided", () => {
    render(<TestExecutionHistory testReportSummaries={mockTestReportSummaries} />);

    const links = screen.getAllByRole("link");

    links.forEach((link) => {
      expect(link.getAttribute("href")).not.toContain("testSessionId");
    });
  });

  it("should render empty list when no summaries provided", () => {
    render(<TestExecutionHistory testReportSummaries={[]} />);

    const links = screen.queryAllByRole("link");
    expect(links).toHaveLength(0);
  });

  it("should use grid layout with two columns", () => {
    const { container } = render(<TestExecutionHistory testReportSummaries={mockTestReportSummaries} />);

    const gridElement = container.querySelector(".grid");
    expect(gridElement).toBeInTheDocument();
    expect(gridElement).toHaveClass("grid-cols-[auto_auto]");
  });
});
