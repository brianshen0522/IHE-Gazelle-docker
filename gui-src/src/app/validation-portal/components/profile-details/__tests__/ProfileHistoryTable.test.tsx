/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, Mock } from "vitest";
import "@testing-library/jest-dom";
import ProfileHistoryTable from "../ProfileHistoryTable";
import { useSession } from "next-auth/react";
import { useGetData } from "@shared/hooks/SWR/useGetData";
import { ValidationHistory } from "@validation-portal/types/ValidationProfile";

// Mock dependencies
vi.mock("next-auth/react");
vi.mock("@shared/hooks/SWR/useGetData");
vi.mock("@/shared/hooks/useDateFormat", () => ({
  __esModule: true,
  default: () => vi.fn((date: string | number) => `Formatted: ${date}`),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  Skeleton: ({ className }: any) => <div data-testid="skeleton" className={className} />,
}));

vi.mock("../HistoryRow", () => ({
  __esModule: true,
  default: ({ entry, reportHref, formatDate }: any) => (
    <tr data-testid={`history-row-${entry.reportId}`}>
      <td>{formatDate(entry.executionDate)}</td>
      <td>{entry.result}</td>
      <td>{reportHref}</td>
    </tr>
  ),
}));

describe("ProfileHistoryTable", () => {
  const mockUseSession = useSession as Mock;
  const mockUseGetData = useGetData as Mock;

  const mockSession = {
    user: {
      gazelleId: "user-123",
      name: "Test User",
    },
    access_token: "test-token",
  };

  const mockHistoryData: ValidationHistory[] = [
    {
      reportId: "report-1",
      executionDate: "2024-01-15T10:30:00Z",
      result: "PASSED",
    },
    {
      reportId: "report-2",
      executionDate: "2024-01-14T09:20:00Z",
      result: "FAILED",
    },
    {
      reportId: "report-3",
      executionDate: "2024-01-13T14:45:00Z",
      result: "UNKNOWN",
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders login message when user is not logged in", () => {
    mockUseSession.mockReturnValue({ data: null });
    mockUseGetData.mockReturnValue({
      data: [],
      isLoading: false,
      error: undefined,
    });

    render(<ProfileHistoryTable profileId="profile-123" />);

    expect(screen.getByText("Log in to access your history of past validation requests.")).toBeInTheDocument();
  });

  it("renders skeleton while loading", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseGetData.mockReturnValue({
      data: [],
      isLoading: true,
      error: undefined,
    });

    render(<ProfileHistoryTable profileId="profile-123" />);

    expect(screen.getByTestId("skeleton")).toBeInTheDocument();
    expect(screen.getByTestId("skeleton")).toHaveClass("h-32");
  });

  it("renders error message when there is an error", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseGetData.mockReturnValue({
      data: [],
      isLoading: false,
      error: new Error("Failed to fetch"),
    });

    render(<ProfileHistoryTable profileId="profile-123" />);

    expect(screen.getByText("An error occurred while loading history.")).toBeInTheDocument();
  });

  it("renders empty message when there is no history data", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseGetData.mockReturnValue({
      data: [],
      isLoading: false,
      error: undefined,
    });

    render(<ProfileHistoryTable profileId="profile-123" />);

    expect(
      screen.getByText("You haven't used this validation profile yet. Execute a validation to see the history below."),
    ).toBeInTheDocument();
  });

  it("renders history table with data", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseGetData.mockReturnValue({
      data: mockHistoryData,
      isLoading: false,
      error: undefined,
    });

    render(<ProfileHistoryTable profileId="profile-123" />);

    expect(
      screen.getByText("The table below lists your last 10 validation reports using this validation profile (all versions)."),
    ).toBeInTheDocument();
    expect(screen.getByText("Execution Date")).toBeInTheDocument();
    expect(screen.getByText("Result")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Access all reports" })).toHaveAttribute(
      "href",
      "/reports?type=VALIDATION_REPORT&validation_profile_id=profile-123",
    );

    // Check that all history rows are rendered
    expect(screen.getByTestId("history-row-report-1")).toBeInTheDocument();
    expect(screen.getByTestId("history-row-report-2")).toBeInTheDocument();
    expect(screen.getByTestId("history-row-report-3")).toBeInTheDocument();
  });

  it("calls useGetData with correct parameters", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseGetData.mockReturnValue({
      data: mockHistoryData,
      isLoading: false,
      error: undefined,
    });

    render(<ProfileHistoryTable profileId="test-profile-456" />);

    expect(mockUseGetData).toHaveBeenCalledWith({
      searchParameters: {
        profileId: "test-profile-456",
        owner: "user-123",
      },
      field: "date",
      sortOrder: "desc",
      offset: 0,
      limit: 10,
      baseUrl: "/gazelle/validation-portal/api",
      apiFolder: "history",
    });
  });

  it("passes profileId with special characters as query parameter", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseGetData.mockReturnValue({
      data: mockHistoryData,
      isLoading: false,
      error: undefined,
    });

    const profileIdWithSpecialChars = "https://hl7.org/fhir/StructureDefinition/Account|4.0.1";
    render(<ProfileHistoryTable profileId={profileIdWithSpecialChars} />);

    expect(screen.getByRole("link", { name: "Access all reports" })).toHaveAttribute(
      "href",
      "/reports?type=VALIDATION_REPORT&validation_profile_id=https%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FAccount%7C4.0.1",
    );

    expect(mockUseGetData).toHaveBeenCalledWith(
      expect.objectContaining({
        searchParameters: {
          profileId: profileIdWithSpecialChars,
          owner: "user-123",
        },
        apiFolder: "history",
      }),
    );
  });

  it("uses empty string for owner when gazelleId is not available", () => {
    mockUseSession.mockReturnValue({
      data: {
        user: {
          name: "Test User",
          // gazelleId is missing
        },
        access_token: "test-token",
      },
    });
    mockUseGetData.mockReturnValue({
      data: [],
      isLoading: false,
      error: undefined,
    });

    render(<ProfileHistoryTable profileId="profile-123" />);

    expect(mockUseGetData).toHaveBeenCalledWith(
      expect.objectContaining({
        searchParameters: {
          profileId: "profile-123",
          owner: "",
        },
      }),
    );
  });

  it("renders correct report links for each history entry", () => {
    mockUseSession.mockReturnValue({ data: mockSession });
    mockUseGetData.mockReturnValue({
      data: mockHistoryData,
      isLoading: false,
      error: undefined,
    });

    render(<ProfileHistoryTable profileId="profile-123" />);

    expect(screen.getByText("/validation-portal/reports/report-1")).toBeInTheDocument();
    expect(screen.getByText("/validation-portal/reports/report-2")).toBeInTheDocument();
    expect(screen.getByText("/validation-portal/reports/report-3")).toBeInTheDocument();
  });
});
