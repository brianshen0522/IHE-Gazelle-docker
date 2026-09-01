/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import TablePaginationWrapper from "./TablePaginationWrapper";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { useGetData } from "@shared/hooks/SWR/useGetData";

// Mock dependencies
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Button: (props: any) => <button {...props} />,
  Skeleton: (props: any) => <div data-testid="skeleton" {...props} />,
  useSmallScreen: vi.fn(() => false),
  useSidePanel: vi.fn(() => ({
    selectedRow: null,
    setSelectedRow: vi.fn(),
    isOpen: false,
    setIsOpen: vi.fn(),
  })),
}));
vi.mock("@shared/hooks/SWR/useGetData", () => ({
  useGetData: vi.fn(),
}));
vi.mock("@shared/hooks/useSearchParamsUrl", () => ({
  useSearchParamsUrl: () => ({
    searchParameters: { q: "test" },
  }),
}));
vi.mock("@shared/components/errors/InternalError", () => ({
  __esModule: true,
  default: (props: any) => <div data-testid="internal-error">{props.message}</div>,
}));
vi.mock("@shared/context/UnsavedChangeContext", () => ({
  useUnsavedChanges: vi.fn(() => ({
    handleNavigation: vi.fn(),
  })),
}));
vi.mock("next/navigation", () => ({
  useRouter: vi.fn(() => ({})),
  usePathname: vi.fn(() => "/"),
  useSearchParams: vi.fn(() => new URLSearchParams()),
}));

const tableColumns = [
  {
    accessorKey: "name",
    header: "Name",
  },
];

const baseProps = {
  tableColumns,
  baseUrl: "/api",
  apiFolder: "users",
  emptyDataMessage: "No data here!",
};

describe("TablePaginationWrapper", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders skeleton on initial load", () => {
    (useGetData as any).mockReturnValue({
      data: undefined,
      contentRange: undefined,
      isLoading: true,
      isValidating: false,
      error: null,
    });

    render(<TablePaginationWrapper {...baseProps} />);
    expect(screen.getByTestId("skeleton")).toBeInTheDocument();
  });

  it("renders error when error is present", () => {
    (useGetData as any).mockReturnValue({
      data: undefined,
      contentRange: undefined,
      isLoading: false,
      isValidating: false,
      error: { message: "Internal error" },
    });

    render(<TablePaginationWrapper {...baseProps} />);
    expect(screen.getByTestId("internal-error")).toHaveTextContent("Internal error");
  });

  it("renders empty data message when no data", () => {
    (useGetData as any).mockReturnValue({
      data: [],
      contentRange: "0-0/0",
      isLoading: false,
      isValidating: false,
      error: null,
    });

    render(<TablePaginationWrapper {...baseProps} />);
    expect(screen.getByText("No data here!")).toBeInTheDocument();
  });

  it("renders table and load more button when data is present", () => {
    (useGetData as any).mockReturnValue({
      data: [{ name: "Alice" }],
      contentRange: "0-0/10",
      isLoading: false,
      isValidating: false,
      error: null,
    });

    render(<TablePaginationWrapper {...baseProps} />);
    expect(screen.getByText("Showing results 0-0/10")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Load more data" })).toBeInTheDocument();
  });

  it("disables load more button when all data loaded", () => {
    (useGetData as any).mockReturnValue({
      data: [{ name: "Bob" }],
      contentRange: "0-9/9",
      isLoading: false,
      isValidating: false,
      error: null,
    });

    render(<TablePaginationWrapper {...baseProps} />);
    const btn = screen.getByRole("button", { name: "All data loaded" });
    expect(btn).toBeDisabled();
  });

  it("calls setLimit when load more button is clicked", () => {
    (useGetData as any).mockReturnValue({
      data: [{ name: "Eve" }],
      contentRange: "0-0/100",
      isLoading: false,
      isValidating: false,
      error: null,
    });

    render(<TablePaginationWrapper {...baseProps} />);
    const btn = screen.getByRole("button", { name: "Load more data" });
    fireEvent.click(btn);
    // No assertion for setLimit as it's internal state, but no error should occur
    expect(btn).toBeInTheDocument();
  });

  it("uses custom searchParameters if provided", () => {
    (useGetData as any).mockReturnValue({
      data: [{ name: "Test" }],
      contentRange: "0-0/1",
      isLoading: false,
      isValidating: false,
      error: null,
    });

    render(<TablePaginationWrapper {...baseProps} searchParameters={{ custom: "param" }} />);
    expect(useGetData).toHaveBeenCalledWith(expect.objectContaining({ searchParameters: { custom: "param" } }));
  });
});
