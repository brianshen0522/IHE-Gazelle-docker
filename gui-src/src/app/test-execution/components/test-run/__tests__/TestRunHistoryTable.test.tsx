/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { Session } from "next-auth";
import TestRunHistoryTable from "../TestRunHistoryTable";
import * as nextAuthReact from "next-auth/react";
import * as useTestSessionHook from "../../../context/TestSessionContext";
import * as useGetDataHook from "@hooks/SWR/useGetData";

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

vi.mock("../TestRunHistoryRow", () => ({
  default: ({ entry, testRunExecutionHref, tooltipText }: any) => (
    <tr data-testid={`history-row-${entry.id}`} onClick={() => mockPush(testRunExecutionHref)}>
      <td>{entry.id}</td>
      <td>{tooltipText}</td>
    </tr>
  ),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  Skeleton: ({ className }: any) => <div data-testid="skeleton" className={className} />,
  Badge: ({ children }: any) => <span data-testid="badge">{children}</span>,
  Tooltip: ({ children }: any) => <div data-testid="tooltip">{children}</div>,
}));

describe("TestRunHistoryTable", () => {
  const mockSession: Session = {
    access_token: "test-token",
    user: {
      gazelleId: "user-1",
      organization: "org-1",
    },
    expires: "2026-12-31",
  } as Session;

  const mockUseSession = vi.spyOn(nextAuthReact, "useSession");
  const mockUseTestSession = vi.spyOn(useTestSessionHook, "useTestSession");
  const mockUseGetData = vi.spyOn(useGetDataHook, "useGetData");

  const mockHistoryData = [
    {
      id: "exec-2",
      testId: "test-1",
      testSessionId: "session-1",
      testReportSummaries: [
        {
          dateTime: "2026-01-02T10:00:00.000Z",
          result: "PASSED",
        },
      ],
    },
    {
      id: "exec-1",
      testId: "test-1",
      testSessionId: "session-1",
      testReportSummaries: [
        {
          dateTime: "2026-01-01T10:00:00.000Z",
          result: "FAILED",
        },
      ],
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseSession.mockReturnValue({ data: mockSession } as any);
    mockUseTestSession.mockReturnValue({
      testSessionId: "session-1",
    } as any);
  });

  it("shows login prompt when session is missing", () => {
    mockUseSession.mockReturnValue({ data: null } as any);

    render(<TestRunHistoryTable testId="test-1" />);

    expect(screen.getByText("gzl.texec.history.login_to_view_history")).toBeInTheDocument();
  });

  it("shows skeleton when loading", () => {
    mockUseGetData.mockReturnValue({
      data: undefined,
      isLoading: true,
      error: undefined,
    } as any);

    render(<TestRunHistoryTable testId="test-1" />);

    expect(screen.getByTestId("skeleton")).toBeInTheDocument();
  });

  it("shows error message when loading history fails", () => {
    mockUseGetData.mockReturnValue({
      data: undefined,
      isLoading: false,
      error: new Error("Failed to load history"),
    } as any);

    render(<TestRunHistoryTable testId="test-1" />);

    expect(screen.getByText("gzl.user.interface.error_loading_history")).toBeInTheDocument();
  });

  it("shows empty state when no history is available", () => {
    mockUseGetData.mockReturnValue({
      data: [],
      isLoading: false,
      error: undefined,
    } as any);

    render(<TestRunHistoryTable testId="test-1" />);

    expect(screen.getByText("gzl.texec.history.no_history_available")).toBeInTheDocument();
  });

  it("renders history table with rows", () => {
    mockUseGetData.mockReturnValue({
      data: mockHistoryData,
      isLoading: false,
      error: undefined,
    } as any);

    render(<TestRunHistoryTable testId="test-1" />);

    expect(screen.getByText("gzl.texec.history.description")).toBeInTheDocument();
    expect(screen.getByText("gzl.texec.history.id")).toBeInTheDocument();
    expect(screen.getByText("gzl.texec.history.last_execution_result")).toBeInTheDocument();

    expect(screen.getByTestId("history-row-exec-2")).toBeInTheDocument();
    expect(screen.getByTestId("history-row-exec-1")).toBeInTheDocument();
  });

  it("passes correct navigation href to history row", () => {
    mockUseGetData.mockReturnValue({
      data: mockHistoryData.slice(0, 1),
      isLoading: false,
      error: undefined,
    } as any);

    render(<TestRunHistoryTable testId="test-1" />);

    fireEvent.click(screen.getByTestId("history-row-exec-2"));

    expect(mockPush).toHaveBeenCalledWith("/test-execution/test-run-execution?executionId=exec-2");
  });
});
