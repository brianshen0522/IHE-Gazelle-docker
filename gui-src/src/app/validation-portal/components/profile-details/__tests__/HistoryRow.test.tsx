/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import HistoryRow from "../HistoryRow";
import { ValidationHistory } from "../../../types/ValidationProfile";

// Mock dependencies
vi.mock("next/link", () => ({
  __esModule: true,
  default: ({ href, children, className, title, ref }: any) => (
    <a href={href} className={className} title={title} ref={ref}>
      {children}
    </a>
  ),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  Badge: ({ id, variant, children }: any) => (
    <span data-testid={id} data-variant={variant}>
      {children}
    </span>
  ),
  Tooltip: ({ id, position, children }: any) => (
    <div data-testid={id} data-position={position}>
      {children}
    </div>
  ),
}));

describe("HistoryRow", () => {
  const mockFormatDate = vi.fn((date: string | number) => `Formatted: ${date}`);

  const mockEntry: ValidationHistory = {
    reportId: "report-123",
    executionDate: "2024-01-15T10:30:00Z",
    result: "PASSED",
  };

  const defaultProps = {
    entry: mockEntry,
    formatDate: mockFormatDate,
    reportHref: "/validation-portal/reports/report-123" as any,
    tooltipText: "View validation report",
    linkTitle: "Click to view report details",
  };

  it("renders the history row with formatted date", () => {
    render(<HistoryRow {...defaultProps} />);

    expect(screen.getByText("Formatted: 2024-01-15T10:30:00Z")).toBeInTheDocument();
    expect(mockFormatDate).toHaveBeenCalledWith("2024-01-15T10:30:00Z");
  });

  it("renders link with correct href and title", () => {
    render(<HistoryRow {...defaultProps} />);

    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("href", "/validation-portal/reports/report-123");
    expect(link).toHaveAttribute("title", "Click to view report details");
  });

  it("renders tooltip with correct text", () => {
    render(<HistoryRow {...defaultProps} />);

    const tooltip = screen.getByTestId(`history-link-${mockEntry.reportId}`);
    expect(tooltip).toBeInTheDocument();
    expect(tooltip).toHaveTextContent("View validation report");
    expect(tooltip).toHaveAttribute("data-position", "bottom");
  });

  it("renders badge with PASSED result as success variant", () => {
    render(<HistoryRow {...defaultProps} />);

    const badge = screen.getByTestId(`result-${mockEntry.reportId}`);
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveAttribute("data-variant", "success");
    expect(badge).toHaveTextContent("PASSED");
  });

  it("renders badge with FAILED result as failed variant", () => {
    const failedEntry: ValidationHistory = {
      ...mockEntry,
      result: "FAILED",
    };

    render(<HistoryRow {...defaultProps} entry={failedEntry} />);

    const badge = screen.getByTestId(`result-${failedEntry.reportId}`);
    expect(badge).toHaveAttribute("data-variant", "failed");
    expect(badge).toHaveTextContent("FAILED");
  });

  it("renders badge with UNKNOWN result as unknown variant", () => {
    const unknownEntry: ValidationHistory = {
      ...mockEntry,
      result: "UNKNOWN",
    };

    render(<HistoryRow {...defaultProps} entry={unknownEntry} />);

    const badge = screen.getByTestId(`result-${unknownEntry.reportId}`);
    expect(badge).toHaveAttribute("data-variant", "unknown");
    expect(badge).toHaveTextContent("UNKNOWN");
  });

  it("renders table cells with correct structure", () => {
    const { container } = render(<HistoryRow {...defaultProps} />);

    const row = container.querySelector("tr");
    expect(row).toBeInTheDocument();
    expect(row).toHaveClass("hover:bg-lightpurple/20");

    const cells = container.querySelectorAll("td");
    expect(cells).toHaveLength(2);
    expect(cells[0]).toHaveClass("px-4", "py-3", "whitespace-nowrap");
    expect(cells[1]).toHaveClass("px-4", "py-3", "whitespace-nowrap");
  });

  it("applies correct CSS classes to link", () => {
    render(<HistoryRow {...defaultProps} />);

    const link = screen.getByRole("link");
    expect(link).toHaveClass("text-blue", "hover:text-visited_link", "hover:underline", "transition-colors", "duration-150");
  });
});
