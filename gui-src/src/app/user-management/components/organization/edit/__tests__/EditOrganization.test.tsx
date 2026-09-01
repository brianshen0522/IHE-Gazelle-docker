/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import EditOrganization from "../EditOrganization";
import { Session } from "next-auth";

// Mock next/navigation
vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    refresh: vi.fn(),
  }),
  usePathname: () => "/user-management/organization/edit",
}));

// Mock validation functions
vi.mock("@/app/user-management/utils/validation", () => ({
  validateShortName: (name: string) => name.length <= 32,
  validateUrl: (url: string) => url.startsWith("http://") || url.startsWith("https://") || url === "",
}));

// Mock useGetOrganizationFromId hook
const mockUseGetOrganizationFromId = vi.fn();
vi.mock("@/shared/hooks/useGetUserInformation", () => ({
  useGetOrganizationFromId: (id: string) => mockUseGetOrganizationFromId(id),
}));

// Mock child components
vi.mock("@/app/user-management/components/UserManagementHeader", () => ({
  default: ({ id, title, breadcrumbs }: any) => (
    <div data-testid="user-management-header" data-id={id}>
      <h1>{title}</h1>
      <nav>
        {breadcrumbs.map((bc: any) => (
          <span key={id}>{bc.label}</span>
        ))}
      </nav>
    </div>
  ),
}));

vi.mock("../EditOrganizationForm", () => ({
  default: ({ organizationId, isFromSidepanel }: any) => (
    <div data-testid="edit-organization-form" data-organization-id={organizationId} data-from-sidepanel={String(isFromSidepanel)}>
      Edit Form
    </div>
  ),
}));

vi.mock("../DelegatedOrgaDetails", () => ({
  default: ({ name, shortName }: any) => (
    <div data-testid="delegated-orga-details">
      <div>Name: {name}</div>
      <div>Short Name: {shortName}</div>
    </div>
  ),
}));

vi.mock("../../archive/OrganizationArchived", () => ({
  default: ({ name, shortName }: any) => (
    <div data-testid="organization-archived">
      <div>Name: {name}</div>
      <div>Short Name: {shortName}</div>
    </div>
  ),
}));

// Mock useUnsavedChanges context
vi.mock("@/shared/context/UnsavedChangeContext", () => ({
  useUnsavedChanges: () => ({
    hasUnsavedChanges: false,
    setHasUnsavedChanges: vi.fn(),
    handleNavigation: vi.fn(),
  }),
}));

// Mock gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Skeleton: ({ className }: any) => (
    <div data-testid="skeleton" className={className}>
      Loading...
    </div>
  ),
  Input: ({ id, value, setValue, label, name, readonly, disabled }: any) => (
    <div data-testid={`input-${id}`}>
      <label>{label}</label>
      <input
        data-testid={`input-field-${id}`}
        name={name}
        value={value}
        onChange={(e) => setValue?.(e.target.value)}
        readOnly={readonly}
        disabled={disabled}
      />
    </div>
  ),
  Button: ({ id, children, disabled, type, onClick }: any) => (
    <button data-testid={id} disabled={disabled} type={type} onClick={onClick}>
      {children}
    </button>
  ),
  formatSegmentLabel: (segment: string) =>
    segment
      .split("-")
      .map((word: string) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(" "),
  ContentHeader: ({ id, title, breadcrumbs }: any) => (
    <div data-testid="content-header" data-id={id}>
      {title && <h2>{title}</h2>}
      {breadcrumbs?.map((bc: any) => (
        <span key={id}>{bc.label}</span>
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

describe("EditOrganization", () => {
  const mockSession: Session = {
    user: {
      gazelleId: "user-123",
      id: "user-123",
      organization: "org-123",
      email: "test@example.com",
      name: "Test User",
      groups: [],
    },
    expires: "2026-12-31",
  } as unknown as Session;

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseGetOrganizationFromId.mockReturnValue({
      data: {
        data: {
          id: "org-123",
          name: "Test Organization",
          shortname: "test-org",
          delegated: false,
        },
      },
      isLoading: false,
    });
  });

  describe("Loading state", () => {
    it("renders skeleton when loading", () => {
      mockUseGetOrganizationFromId.mockReturnValue({
        data: null,
        isLoading: true,
      });

      render(<EditOrganization session={mockSession} />);

      expect(screen.getByTestId("skeleton")).toBeInTheDocument();
    });
  });

  describe("Full page mode (not from sidepanel)", () => {
    it("renders UserManagementHeader when not from sidepanel", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      expect(screen.getByTestId("user-management-header")).toBeInTheDocument();
    });

    it("renders with background and padding styling when not from sidepanel", () => {
      const { container } = render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      const contentDiv = container.querySelector(".bg-white.p-6.m-8.rounded-lg.shadow-md");
      expect(contentDiv).toBeInTheDocument();
    });

    it("renders organization details heading when not from sidepanel", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      expect(screen.getByText("Organization details")).toBeInTheDocument();
    });

    it("renders EditOrganizationForm with correct props", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      const form = screen.getByTestId("edit-organization-form");
      expect(form).toHaveAttribute("data-organization-id", "org-123");
    });
  });

  describe("Sidepanel mode", () => {
    it("does not render UserManagementHeader when from sidepanel", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={true} />);

      expect(screen.queryByTestId("user-management-header")).not.toBeInTheDocument();
    });

    it("renders with minimal styling when from sidepanel", () => {
      const { container } = render(<EditOrganization session={mockSession} isFromSidepanel={true} />);

      expect(container.querySelector(".bg-white.p-6.m-8.rounded-lg.shadow-md")).not.toBeInTheDocument();
    });

    it("does not render organization details heading when from sidepanel", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={true} />);

      expect(screen.queryByText("gzl.user.interface.organization_details")).not.toBeInTheDocument();
    });
  });

  describe("Organization ID handling", () => {
    it("uses provided organizationId prop over session organization", () => {
      render(<EditOrganization session={mockSession} organizationId="custom-org-456" />);

      expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("custom-org-456");
    });

    it("falls back to session organization when no organizationId prop", () => {
      render(<EditOrganization session={mockSession} />);

      expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("org-123");
    });

    it("uses empty string when no session and no organizationId", () => {
      render(<EditOrganization session={null} />);

      expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("");
    });
  });

  describe("Delegated Organization", () => {
    beforeEach(() => {
      mockUseGetOrganizationFromId.mockReturnValue({
        data: {
          data: {
            id: "org-123",
            name: "Test Organization",
            shortname: "test-org",
            delegated: true,
          },
        },
        isLoading: false,
      });
    });

    it("renders DelegatedOrgaDetails when organization is delegated", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      expect(screen.getByTestId("delegated-orga-details")).toBeInTheDocument();
    });

    it("does not render EditOrganizationForm when organization is delegated", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      expect(screen.queryByTestId("edit-organization-form")).not.toBeInTheDocument();
    });

    it("passes correct props to DelegatedOrgaDetails", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      const delegatedDetails = screen.getByTestId("delegated-orga-details");
      expect(delegatedDetails).toBeInTheDocument();
      expect(delegatedDetails).toHaveTextContent("test-org");
    });
  });

  describe("Archived Organization", () => {
    beforeEach(() => {
      mockUseGetOrganizationFromId.mockReturnValue({
        data: {
          data: {
            id: "org-123",
            name: "Test Organization",
            shortname: "test-org",
            delegated: false,
            archived: true,
          },
        },
        isLoading: false,
      });
    });

    it("renders OrganizationArchived when organization is archived", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      expect(screen.getByTestId("organization-archived")).toBeInTheDocument();
    });

    it("does not render EditOrganizationForm when organization is archived", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      expect(screen.queryByTestId("edit-organization-form")).not.toBeInTheDocument();
    });

    it("does not render DelegatedOrgaDetails when organization is archived", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      expect(screen.queryByTestId("delegated-orga-details")).not.toBeInTheDocument();
    });

    it("passes correct props to OrganizationArchived", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      const archivedDetails = screen.getByTestId("organization-archived");
      expect(archivedDetails).toBeInTheDocument();
      expect(archivedDetails).toHaveTextContent("test-org");
    });

    it("renders archived state in sidepanel mode", () => {
      render(<EditOrganization session={mockSession} isFromSidepanel={true} />);

      expect(screen.getByTestId("organization-archived")).toBeInTheDocument();
      expect(screen.queryByTestId("edit-organization-form")).not.toBeInTheDocument();
    });
  });

  describe("Priority of states", () => {
    it("shows delegated state over archived state when both are true", () => {
      mockUseGetOrganizationFromId.mockReturnValue({
        data: {
          data: {
            id: "org-123",
            name: "Test Organization",
            shortname: "test-org",
            delegated: true,
            archived: true,
          },
        },
        isLoading: false,
      });

      render(<EditOrganization session={mockSession} isFromSidepanel={false} />);

      expect(screen.getByTestId("delegated-orga-details")).toBeInTheDocument();
      expect(screen.queryByTestId("organization-archived")).not.toBeInTheDocument();
      expect(screen.queryByTestId("edit-organization-form")).not.toBeInTheDocument();
    });
  });
});
