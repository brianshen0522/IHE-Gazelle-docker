/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import TestRunHistoryRow from "../TestRunHistoryRow";
import * as nextNavigation from "next/navigation";
import * as useDateFormatHook from "@hooks/useDateFormat";

const mockPush = vi.fn();

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

vi.mock("@hooks/useDateFormat", () => ({
  default: () => (dateTime: string) => `formatted-${dateTime}`,
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  Badge: ({ id, variant, children }: any) => (
    <span data-testid={id} data-variant={variant}>
      {children}
    </span>
  ),
  Tooltip: ({ id, children }: any) => <div data-testid={id}>{children}</div>,
}));

describe("TestRunHistoryRow", () => {
  const mockUseRouter = vi.spyOn(nextNavigation, "useRouter");
  const mockUseDateFormat = vi.spyOn(useDateFormatHook, "default");

  const baseEntry = {
    id: "exec-1",
    testId: "test-1",
    testSessionId: "session-1",
    testReportSummaries: [
      {
        dateTime: "2026-01-01T10:00:00.000Z",
        result: "FAILED",
      },
    ],
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseRouter.mockReturnValue({ push: mockPush } as any);
    mockUseDateFormat.mockReturnValue((dateTime: string | number) => `formatted-${dateTime}` as any);
  });

  it("renders execution id, tooltip and last report data", () => {
    render(
      <table>
        <tbody>
          <TestRunHistoryRow
            entry={baseEntry as any}
            testRunExecutionHref="/test-execution/test-run-execution?executionId=exec-1"
            tooltipText="View test run"
          />
        </tbody>
      </table>,
    );

    expect(screen.getByText("exec-1")).toBeInTheDocument();
    expect(screen.getByTestId("history-link-test-1")).toHaveTextContent("View test run");
    expect(screen.getByText("formatted-2026-01-01T10:00:00.000Z")).toBeInTheDocument();
    expect(screen.getByTestId("result-test-1")).toHaveTextContent("FAILED");
    expect(screen.getByTestId("result-test-1")).toHaveAttribute("data-variant", "failed");
  });

  it("shows execution in progress when there are no summaries", () => {
    render(
      <table>
        <tbody>
          <TestRunHistoryRow
            entry={{ ...baseEntry, testReportSummaries: [] } as any}
            testRunExecutionHref="/test-execution/test-run-execution?executionId=exec-1"
            tooltipText="View test run"
          />
        </tbody>
      </table>,
    );

    expect(screen.getByText("gzl.texec.history.execution_in_progress")).toBeInTheDocument();
    expect(screen.getByTestId("result-test-1")).toHaveTextContent("UNDEFINED");
    expect(screen.getByTestId("result-test-1")).toHaveAttribute("data-variant", "default");
  });

  it("uses success badge variant for successful result", () => {
    render(
      <table>
        <tbody>
          <TestRunHistoryRow
            entry={
              {
                ...baseEntry,
                testReportSummaries: [
                  {
                    dateTime: "2026-01-01T10:00:00.000Z",
                    result: "PASSED",
                  },
                ],
              } as any
            }
            testRunExecutionHref="/test-execution/test-run-execution?executionId=exec-1"
            tooltipText="View test run"
          />
        </tbody>
      </table>,
    );

    expect(screen.getByTestId("result-test-1")).toHaveAttribute("data-variant", "success");
    expect(screen.getByTestId("result-test-1")).toHaveTextContent("PASSED");
  });

  it("navigates to execution page when clicked", () => {
    render(
      <table>
        <tbody>
          <TestRunHistoryRow
            entry={baseEntry as any}
            testRunExecutionHref="/test-execution/test-run-execution?executionId=exec-1"
            tooltipText="View test run"
          />
        </tbody>
      </table>,
    );

    fireEvent.click(screen.getByText("exec-1"));

    expect(mockPush).toHaveBeenCalledWith("/test-execution/test-run-execution?executionId=exec-1");
  });
});
