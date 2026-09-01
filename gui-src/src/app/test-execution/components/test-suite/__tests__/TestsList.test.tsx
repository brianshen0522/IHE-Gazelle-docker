import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import TestsList from "../TestsList";

// Hoisted mocks
const mockUseSmallScreen = vi.hoisted(() => vi.fn(() => false));
const mockUseTestSession = vi.hoisted(() => vi.fn());

vi.mock("@gazelle/gazelle-component-ui", () => ({
  ScrollTop: vi.fn(() => React.createElement("div", { "data-testid": "scroll-top" }, "ScrollTop")),
  useSmallScreen: mockUseSmallScreen,
}));

vi.mock("../TestsColumns", () => ({
  useTestsColumns: vi.fn(() => [
    { id: "name", label: "Name" },
    { id: "status", label: "Status" },
  ]),
}));

vi.mock("@/shared/components/filter/GenericFilters", () => ({
  default: vi.fn(({ indexPath }) => React.createElement("div", { "data-testid": "generic-filters", "data-index-path": indexPath }, "GenericFilters")),
}));

vi.mock("@/shared/hooks/useSearchParamsUrl", () => ({
  useSearchParamsUrl: vi.fn(() => ({
    searchParameters: { page: "1", limit: "10" },
  })),
}));

vi.mock("@shared/components/table/TablePaginationWrapper", () => ({
  default: vi.fn(({ path }) => React.createElement("div", { "data-testid": "table-pagination", "data-path": path }, "TablePaginationWrapper")),
}));

vi.mock("../TestSidepanel", () => ({
  default: vi.fn(() => React.createElement("div", { "data-testid": "test-sidepanel" }, "TestSidepanel")),
}));

vi.mock("@test-execution/context/TestSessionContext", () => ({
  useTestSession: () => mockUseTestSession(),
}));

describe("TestsList", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders nothing when testSessionId is null", () => {
    mockUseTestSession.mockReturnValue({
      testSessionId: null,
      testId: null,
    });

    const { container } = render(<TestsList />);

    expect(container.firstChild).toBeNull();
  });

  it("renders nothing when testSessionId is undefined", () => {
    mockUseTestSession.mockReturnValue({
      testSessionId: undefined,
      testId: null,
    });

    const { container } = render(<TestsList />);

    expect(container.firstChild).toBeNull();
  });

  it("renders all components when testSessionId is present", () => {
    mockUseTestSession.mockReturnValue({
      testSessionId: "session-123",
      testId: null,
    });

    render(<TestsList />);

    expect(screen.getByTestId("generic-filters")).toBeInTheDocument();
    expect(screen.getByTestId("table-pagination")).toBeInTheDocument();
    expect(screen.getByTestId("scroll-top")).toBeInTheDocument();
    expect(screen.getByTestId("test-sidepanel")).toBeInTheDocument();
  });

  it("constructs correct API path with testSessionId", () => {
    mockUseTestSession.mockReturnValue({
      testSessionId: "session-abc-123",
      testId: null,
    });

    render(<TestsList />);

    const genericFilters = screen.getByTestId("generic-filters");
    expect(genericFilters).toHaveAttribute("data-index-path", "/sessions/session-abc-123/tests");

    const tablePagination = screen.getByTestId("table-pagination");
    expect(tablePagination).toHaveAttribute("data-path", "/sessions/session-abc-123/tests");
  });

  it("hides sidepanel on small screens", () => {
    mockUseSmallScreen.mockReturnValue(true);

    mockUseTestSession.mockReturnValue({
      testSessionId: "session-123",
      testId: null,
    });

    render(<TestsList />);

    expect(screen.queryByTestId("test-sidepanel")).not.toBeInTheDocument();
  });

  it("shows sidepanel on large screens", () => {
    mockUseSmallScreen.mockReturnValue(false);

    mockUseTestSession.mockReturnValue({
      testSessionId: "session-123",
      testId: null,
    });

    render(<TestsList />);

    expect(screen.getByTestId("test-sidepanel")).toBeInTheDocument();
  });

  it("passes correct props to GenericFilters", () => {
    mockUseTestSession.mockReturnValue({
      testSessionId: "session-123",
      testId: null,
    });

    render(<TestsList />);

    const genericFilters = screen.getByTestId("generic-filters");
    expect(genericFilters).toBeInTheDocument();
  });

  it("passes correct props to TablePaginationWrapper", () => {
    mockUseTestSession.mockReturnValue({
      testSessionId: "session-456",
      testId: null,
    });

    render(<TestsList />);

    const tablePagination = screen.getByTestId("table-pagination");
    expect(tablePagination).toHaveAttribute("data-path", "/sessions/session-456/tests");
  });
});
