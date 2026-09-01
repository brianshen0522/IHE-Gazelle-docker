/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import OrganizationsList from "../OrganizationsList";
import { Session } from "next-auth";

// Helper to create mock search params
const createMockSearchParams = (initialParams: Record<string, string> = {}) => {
  const params = new Map(Object.entries(initialParams));
  return {
    get: (key: string) => params.get(key) ?? null,
    getAll: (key: string) => (params.has(key) ? [params.get(key)!] : []),
    has: (key: string) => params.has(key),
    keys: () => params.keys(),
    values: () => params.values(),
    entries: () => params.entries(),
    forEach: (callback: (value: string, key: string) => void) => params.forEach(callback),
    toString: () => "",
    size: params.size,
  };
};

// Mock next/navigation
vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    refresh: vi.fn(),
  }),
  usePathname: () => "/user-management/organization/list",
  useSearchParams: () => createMockSearchParams(),
}));

// Mock useSmallScreen hook
const mockUseSmallScreen = vi.fn();

// Mock useUnsavedChanges context
vi.mock("@/shared/context/UnsavedChangeContext", () => ({
  useUnsavedChanges: () => ({
    hasUnsavedChanges: false,
    setHasUnsavedChanges: vi.fn(),
    handleNavigation: vi.fn(),
  }),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  ScrollTop: () => <div data-testid="scroll-top">Scroll Top</div>,
  Skeleton: ({ className }: any) => (
    <div data-testid="skeleton" className={className}>
      Loading...
    </div>
  ),
  Filter: () => <div data-testid="filter">Filter Component</div>,
  useSmallScreen: () => mockUseSmallScreen(),
  useSidePanel: () => ({
    isOpen: false,
    setIsOpen: vi.fn(),
  }),
  formatSegmentLabel: (segment: string) =>
    segment
      .split("-")
      .map((word: string) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(" "),
  ContentHeader: ({ id, title, breadcrumbs }: any) => (
    <div data-testid="content-header" data-id={id}>
      {title && <h2>{title}</h2>}
      {breadcrumbs?.map((bc: any) => (
        <span key={bc.label || bc.url}>{bc.label}</span>
      ))}
    </div>
  ),
  Tabs: ({ tabs, activeTab, onTabChange }: any) => (
    <div data-testid="tabs">
      {tabs?.map((tab: any) => (
        <button key={tab.id} onClick={() => onTabChange?.(tab.id)} data-active={activeTab === tab.id}>
          {tab.label}
        </button>
      ))}
    </div>
  ),
}));

// Mock UserManagementHeader
vi.mock("@/app/user-management/components/UserManagementHeader", () => ({
  default: ({ id, title, breadcrumbs }: any) => (
    <div data-testid="user-management-header" data-id={id}>
      <h1>{title}</h1>
      <nav>
        {breadcrumbs.map((bc: any) => (
          <span key={bc.label || bc.url}>{bc.label}</span>
        ))}
      </nav>
    </div>
  ),
}));

// Mock TablePaginationWrapper
vi.mock("@/shared/components/table/TablePaginationWrapper", () => ({
  default: ({ tableColumns, type, path, emptyDataMessage }: any) => (
    <div data-testid="table-pagination-wrapper">
      <div data-testid="table-type">{type}</div>
      <div data-testid="table-path">{path}</div>
      <div data-testid="empty-message">{emptyDataMessage}</div>
      <div data-testid="columns-count">{tableColumns?.length || 0}</div>
    </div>
  ),
}));

// Mock OrganizationSidepanel
vi.mock("../OrganizationSidepanel", () => ({
  default: () => <div data-testid="organization-sidepanel">Organization Sidepanel</div>,
}));

// Mock GenericFilters
vi.mock("@/shared/components/filter/GenericFilters", () => ({
  default: () => <div data-testid="generic-filters">Generic Filters</div>,
}));

// Mock useOrganizationsColumns
const mockColumns = [
  { id: "status", header: "Status" },
  { id: "name", header: "Name" },
  { id: "shortname", header: "Short Name" },
  { id: "action", header: "Action" },
];

vi.mock("../OrganizationsColumns", () => ({
  useOrganizationsColumns: () => mockColumns,
}));

describe("OrganizationsList", () => {
  const mockSession = {
    user: {
      gazelleId: "user-123",
      id: "user-123",
      organization: "org-123",
      email: "test@example.com",
      name: "Test User",
      groups: ["role:gazelle_admin"],
    },
    expires: "2026-12-31",
  } as unknown as Session;

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseSmallScreen.mockReturnValue(false);
  });

  describe("Component rendering", () => {
    it("renders UserManagementHeader", () => {
      render(<OrganizationsList session={mockSession} />);

      expect(screen.getByTestId("user-management-header")).toBeInTheDocument();
      expect(screen.getByTestId("user-management-header")).toHaveAttribute("data-id", "list-organization-header");
    });

    it("renders correct breadcrumbs", () => {
      render(<OrganizationsList session={mockSession} />);

      const header = screen.getByTestId("user-management-header");
      expect(header).toHaveTextContent("Home");
      expect(header).toHaveTextContent("User management");
      expect(header).toHaveTextContent("Organizations");
    });

    it("renders TablePaginationWrapper", () => {
      render(<OrganizationsList session={mockSession} />);

      expect(screen.getByTestId("table-pagination-wrapper")).toBeInTheDocument();
    });

    it("passes correct props to TablePaginationWrapper", () => {
      render(<OrganizationsList session={mockSession} />);

      expect(screen.getByTestId("table-type")).toHaveTextContent("organizations");
      expect(screen.getByTestId("table-path")).toHaveTextContent("/organizations");
      expect(screen.getByTestId("empty-message")).toHaveTextContent("There are no organizations available.");
      expect(screen.getByTestId("columns-count")).toHaveTextContent("4");
    });

    it("renders ScrollTop component", () => {
      render(<OrganizationsList session={mockSession} />);

      expect(screen.getByTestId("scroll-top")).toBeInTheDocument();
    });
  });

  describe("Responsive behavior", () => {
    it("renders sidepanel on large screens", () => {
      mockUseSmallScreen.mockReturnValue(false);
      render(<OrganizationsList session={mockSession} />);

      expect(screen.getByTestId("organization-sidepanel")).toBeInTheDocument();
    });

    it("does not render sidepanel on small screens", () => {
      mockUseSmallScreen.mockReturnValue(true);
      render(<OrganizationsList session={mockSession} />);

      expect(screen.queryByTestId("organization-sidepanel")).not.toBeInTheDocument();
    });
  });

  describe("Layout structure", () => {
    it("applies correct CSS classes for layout", () => {
      const { container } = render(<OrganizationsList session={mockSession} />);

      const mainContainer = container.querySelector(".flex.flex-grow.overflow-hidden.p-2");
      expect(mainContainer).toBeInTheDocument();

      const innerContainer = container.querySelector(".flex.flex-col.flex-grow.overflow-hidden.gap-2");
      expect(innerContainer).toBeInTheDocument();
    });
  });
});
