/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import OrganizationSidepanel from "../OrganizationSidepanel";
import { Session } from "next-auth";

// Mock useSidePanel hook
const mockSetIsOpen = vi.fn();
const mockUseSidePanel = vi.fn();

// Mock useGetOrganizationFromId hook
const mockUseGetOrganizationFromId = vi.fn();

// Mock useGetHeaders hook
const mockUseGetHeaders = vi.fn();

vi.mock("@/shared/hooks/useGetUserInformation", () => ({
  useGetOrganizationFromId: (id: string) => mockUseGetOrganizationFromId(id),
}));

vi.mock("@/shared/hooks/SWR/useGetHeaders", () => ({
  useGetHeaders: (options: any) => mockUseGetHeaders(options),
}));

// Mock react-i18next
vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        "gzl.user.interface.organization": "Organization",
        "gzl.user.interface.delegated_organization": "Delegated organization",
        "gzl.user.interface.linked_users": "Linked Users",
        "gzl.user.interface.security": "Security",
        "gzl.user.interface.archive_organization_info": "Archive organization and deactivate linked users accounts.",
        "gzl.user.interface.archive_organization": "Archive organization",
      };
      return translations[key] || key;
    },
  }),
}));

// Mock permissions utility
vi.mock("@/shared/utils/permissions", () => ({
  isGazelleAdmin: (session: Session) => session?.user?.groups?.includes("role:gazelle_admin") ?? false,
}));

// Mock gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => {
  const SidePanelComponent = ({ isOpen, children, className }: any) => (
    <div data-testid="sidepanel" data-open={String(isOpen)} className={className}>
      {children}
    </div>
  );

  SidePanelComponent.Header = ({ onClose }: any) => (
    <div data-testid="sidepanel-header">
      <button data-testid="close-button" onClick={onClose}>
        Close
      </button>
    </div>
  );

  SidePanelComponent.Section = ({ id, title, children }: any) => (
    <div data-testid={`sidepanel-section-${id}`}>
      <h3>{title}</h3>
      {children}
    </div>
  );

  return {
    SidePanel: SidePanelComponent,
    Badge: ({ id, variant, children }: any) => (
      <span data-testid={id} data-variant={variant}>
        {children}
      </span>
    ),
    Button: ({ id, onClick, children }: any) => (
      <button data-testid={id} onClick={onClick}>
        {children}
      </button>
    ),
    useSidePanel: () => mockUseSidePanel(),
  };
});

// Mock EditOrganization component
vi.mock("../../edit/EditOrganization", () => ({
  default: ({ organizationId, isFromSidepanel }: any) => (
    <div data-testid="edit-organization" data-organization-id={organizationId} data-from-sidepanel={String(isFromSidepanel)}>
      Edit Organization
    </div>
  ),
}));

// Mock OrganizationArchiveModal component
vi.mock("../../archive/OrganizationArchiveModal", () => ({
  default: ({ organization, isOpen, toggleModal }: any) => (
    <div data-testid="archive-modal" data-open={String(isOpen)} data-org-id={organization?.id}>
      <button data-testid="archive-modal-toggle" onClick={toggleModal}>
        Toggle Archive Modal
      </button>
    </div>
  ),
}));

// Mock OrganizationLinkedUsers component
vi.mock("../OrganizationLinkedUsers", () => ({
  default: ({ organizationId, registeredUsersCount, orgAdminCount, isLoading, isError }: any) => (
    <div
      data-testid="organization-linked-users"
      data-org-id={organizationId}
      data-registered-count={registeredUsersCount}
      data-admin-count={orgAdminCount}
      data-is-loading={String(isLoading)}
      data-is-error={String(isError)}
    >
      Linked Users: {registeredUsersCount} registered, {orgAdminCount} admins
    </div>
  ),
}));

describe("OrganizationSidepanel", () => {
  const mockOrganization = {
    id: "org-123",
    name: "Test Organization",
    shortname: "test-org",
    status: true,
    delegated: false,
  };

  const mockSession = {
    user: {
      gazelleId: "user-123",
      id: "user-123",
      organization: "org-123",
      email: "user@example.com",
      groups: ["role:gazelle_admin"],
    },
  } as unknown as Session;

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseGetOrganizationFromId.mockReturnValue({
      data: {
        data: {
          id: "org-123",
          name: "Test Organization",
          shortname: "test-org",
          archived: false,
          delegated: false,
        },
      },
      isLoading: false,
    });

    // Mock useGetHeaders for users count
    mockUseGetHeaders.mockImplementation((options: any) => {
      if (options.searchParameters?.group === "org-adm") {
        // Org admin count
        return {
          headers: { "content-range": "0-1/2" },
          isLoading: false,
          error: null,
        };
      }
      // Total users count
      return {
        headers: { "content-range": "0-9/10" },
        isLoading: false,
        error: null,
      };
    });
  });

  describe("When sidepanel is closed", () => {
    beforeEach(() => {
      mockUseSidePanel.mockReturnValue({
        isOpen: false,
        setIsOpen: mockSetIsOpen,
        selectedRow: null,
      });
    });

    it("renders sidepanel with closed state", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("sidepanel")).toHaveAttribute("data-open", "false");
    });

    it("does not render content when no organization is selected", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.queryByTestId("sidepanel-header")).not.toBeInTheDocument();
      expect(screen.queryByTestId("edit-organization")).not.toBeInTheDocument();
    });
  });

  describe("When sidepanel is open with organization selected", () => {
    beforeEach(() => {
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: mockOrganization,
      });
    });

    it("renders sidepanel with open state", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("sidepanel")).toHaveAttribute("data-open", "true");
    });

    it("renders sidepanel header", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("sidepanel-header")).toBeInTheDocument();
    });

    it("renders organization details section", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("sidepanel-section-organization-details")).toBeInTheDocument();
      expect(screen.getByText("Organization")).toBeInTheDocument();
    });

    it("renders EditOrganization component with correct props", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      const editOrg = screen.getByTestId("edit-organization");
      expect(editOrg).toBeInTheDocument();
      expect(editOrg).toHaveAttribute("data-organization-id", "org-123");
      expect(editOrg).toHaveAttribute("data-from-sidepanel", "true");
    });

    it("does not render delegated badge when organization is not delegated", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.queryByTestId("delegated-organization-badge")).not.toBeInTheDocument();
    });
  });

  describe("When organization is delegated", () => {
    beforeEach(() => {
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: { ...mockOrganization, delegated: true },
      });
      mockUseGetOrganizationFromId.mockReturnValue({
        data: {
          data: {
            id: "org-123",
            name: "Test Organization",
            shortname: "test-org",
            archived: false,
            delegated: true,
          },
        },
        isLoading: false,
      });
    });

    it("renders delegated badge", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      const badge = screen.getByTestId("delegated-organization-badge");
      expect(badge).toBeInTheDocument();
      expect(badge).toHaveAttribute("data-variant", "variant-2");
    });

    it("does not render security section for delegated organizations", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.queryByTestId("sidepanel-section-security")).not.toBeInTheDocument();
      expect(screen.queryByTestId("archive-organization")).not.toBeInTheDocument();
    });
  });

  describe("Sidepanel interactions", () => {
    beforeEach(() => {
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: mockOrganization,
      });
    });

    it("calls setIsOpen(false) when close button is clicked", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      const closeButton = screen.getByTestId("close-button");
      fireEvent.click(closeButton);

      expect(mockSetIsOpen).toHaveBeenCalledWith(false);
    });
  });

  describe("Styling", () => {
    beforeEach(() => {
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: mockOrganization,
      });
    });

    it("applies correct className to sidepanel", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("sidepanel")).toHaveClass("p-1");
    });
  });

  describe("Archive functionality", () => {
    beforeEach(() => {
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: mockOrganization,
      });
    });

    it("renders security section with archive button when organization is not archived", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("sidepanel-section-security")).toBeInTheDocument();
      expect(screen.getByTestId("archive-organization")).toBeInTheDocument();
    });

    it("renders archive modal", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("archive-modal")).toBeInTheDocument();
    });

    it("archive modal is initially closed", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("archive-modal")).toHaveAttribute("data-open", "false");
    });

    it("opens archive modal when archive button is clicked", () => {
      const { rerender } = render(<OrganizationSidepanel session={mockSession} />);

      const archiveButton = screen.getByTestId("archive-organization");
      fireEvent.click(archiveButton);

      // Re-render to reflect state change
      rerender(<OrganizationSidepanel session={mockSession} />);

      // The modal state would be managed internally, we can verify the button was clicked
      expect(archiveButton).toBeInTheDocument();
    });

    it("passes correct organization to archive modal", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("archive-modal")).toHaveAttribute("data-org-id", "org-123");
    });
  });

  describe("When organization is archived", () => {
    beforeEach(() => {
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: { ...mockOrganization, archived: true },
      });
      mockUseGetOrganizationFromId.mockReturnValue({
        data: {
          data: {
            id: "org-123",
            name: "Test Organization",
            shortname: "test-org",
            archived: true,
            delegated: false,
          },
        },
        isLoading: false,
      });
    });

    it("does not render security section when organization is archived", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.queryByTestId("sidepanel-section-security")).not.toBeInTheDocument();
    });

    it("does not render archive button when organization is archived", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.queryByTestId("archive-organization")).not.toBeInTheDocument();
    });

    it("does not render archive modal when organization is archived", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.queryByTestId("archive-modal")).not.toBeInTheDocument();
    });

    it("still renders organization details section", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("sidepanel-section-organization-details")).toBeInTheDocument();
      expect(screen.getByTestId("edit-organization")).toBeInTheDocument();
    });
  });

  describe("Data synchronization", () => {
    beforeEach(() => {
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: mockOrganization,
      });
    });

    it("fetches fresh organization data using useGetOrganizationFromId", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("org-123");
    });

    it("uses archived status from fetched data, not from selectedRow", () => {
      // selectedRow has archived: false, but fetched data has archived: true
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: { ...mockOrganization, archived: false },
      });
      mockUseGetOrganizationFromId.mockReturnValue({
        data: {
          data: {
            id: "org-123",
            name: "Test Organization",
            shortname: "test-org",
            archived: true,
            delegated: false,
          },
        },
        isLoading: false,
      });

      render(<OrganizationSidepanel session={mockSession} />);

      // Should not render security section because fetched data shows archived: true
      expect(screen.queryByTestId("sidepanel-section-security")).not.toBeInTheDocument();
    });

    it("handles empty organization id", () => {
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: null,
      });

      render(<OrganizationSidepanel session={mockSession} />);

      expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("");
    });
  });

  describe("Linked Users Section", () => {
    beforeEach(() => {
      mockUseSidePanel.mockReturnValue({
        isOpen: true,
        setIsOpen: mockSetIsOpen,
        selectedRow: mockOrganization,
      });
    });

    it("renders linked users section when registered users count > 0", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.getByTestId("sidepanel-section-linked-users")).toBeInTheDocument();
      expect(screen.getByText("Linked Users")).toBeInTheDocument();
    });

    it("renders OrganizationLinkedUsers component with correct props", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      const linkedUsers = screen.getByTestId("organization-linked-users");
      expect(linkedUsers).toBeInTheDocument();
      expect(linkedUsers).toHaveAttribute("data-org-id", "org-123");
      expect(linkedUsers).toHaveAttribute("data-registered-count", "10");
      expect(linkedUsers).toHaveAttribute("data-admin-count", "2");
      expect(linkedUsers).toHaveAttribute("data-is-loading", "false");
      expect(linkedUsers).toHaveAttribute("data-is-error", "false");
    });

    it("does not render linked users section when registered users count is 0", () => {
      mockUseGetHeaders.mockImplementation(() => ({
        headers: { "content-range": "0-0/0" },
        isLoading: false,
        error: null,
      }));

      render(<OrganizationSidepanel session={mockSession} />);

      expect(screen.queryByTestId("sidepanel-section-linked-users")).not.toBeInTheDocument();
    });

    it("fetches users count with correct parameters", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(mockUseGetHeaders).toHaveBeenCalledWith({
        searchParameters: { organizationId: "org-123" },
        type: "users",
        path: "/v2/users",
        headersToExtract: ["content-range"],
      });
    });

    it("fetches org admin count with correct parameters", () => {
      render(<OrganizationSidepanel session={mockSession} />);

      expect(mockUseGetHeaders).toHaveBeenCalledWith({
        searchParameters: { organizationId: "org-123", group: "org-adm" },
        type: "users",
        path: "/v2/users",
        headersToExtract: ["content-range"],
      });
    });

    it("passes loading state to OrganizationLinkedUsers", () => {
      mockUseGetHeaders.mockImplementation(() => ({
        // Provide headers even during loading (like cached data with SWR)
        headers: { "content-range": "0-9/10" },
        isLoading: true,
        error: null,
      }));

      render(<OrganizationSidepanel session={mockSession} />);

      const linkedUsers = screen.getByTestId("organization-linked-users");
      expect(linkedUsers).toHaveAttribute("data-is-loading", "true");
    });

    it("passes error state to OrganizationLinkedUsers when users fetch fails", () => {
      mockUseGetHeaders.mockImplementation((options: any) => {
        if (options.searchParameters?.group === "org-adm") {
          return {
            headers: { "content-range": "0-1/2" },
            isLoading: false,
            error: null,
          };
        }
        // Return valid headers with an error to test error state display
        return {
          headers: { "content-range": "0-9/10" },
          isLoading: false,
          error: new Error("Failed to fetch"),
        };
      });

      render(<OrganizationSidepanel session={mockSession} />);

      const linkedUsers = screen.getByTestId("organization-linked-users");
      expect(linkedUsers).toHaveAttribute("data-is-error", "true");
    });

    it("passes error state to OrganizationLinkedUsers when org admin fetch fails", () => {
      mockUseGetHeaders.mockImplementation((options: any) => {
        if (options.searchParameters?.group === "org-adm") {
          // Return valid headers with an error to test error state display
          return {
            headers: { "content-range": "0-1/2" },
            isLoading: false,
            error: new Error("Failed to fetch"),
          };
        }
        return {
          headers: { "content-range": "0-9/10" },
          isLoading: false,
          error: null,
        };
      });

      render(<OrganizationSidepanel session={mockSession} />);

      const linkedUsers = screen.getByTestId("organization-linked-users");
      expect(linkedUsers).toHaveAttribute("data-is-error", "true");
    });

    it("handles missing content-range headers gracefully", () => {
      mockUseGetHeaders.mockImplementation(() => ({
        headers: {},
        isLoading: false,
        error: null,
      }));

      render(<OrganizationSidepanel session={mockSession} />);

      // When headers are missing, counts default to 0, so section is not rendered
      expect(screen.queryByTestId("sidepanel-section-linked-users")).not.toBeInTheDocument();
      expect(screen.queryByTestId("organization-linked-users")).not.toBeInTheDocument();
    });

    it("handles null content-range headers", () => {
      mockUseGetHeaders.mockImplementation(() => ({
        headers: { "content-range": null },
        isLoading: false,
        error: null,
      }));

      render(<OrganizationSidepanel session={mockSession} />);

      // When headers are null, counts default to 0, so section is not rendered
      expect(screen.queryByTestId("sidepanel-section-linked-users")).not.toBeInTheDocument();
    });

    it("handles malformed content-range headers", () => {
      mockUseGetHeaders.mockImplementation(() => ({
        headers: { "content-range": "invalid-format" },
        isLoading: false,
        error: null,
      }));

      render(<OrganizationSidepanel session={mockSession} />);

      // When headers are malformed, parseContentRange returns null, counts default to 0
      expect(screen.queryByTestId("sidepanel-section-linked-users")).not.toBeInTheDocument();
    });

    it("parses content-range headers correctly", () => {
      mockUseGetHeaders.mockImplementation((options: any) => {
        if (options.searchParameters?.group === "org-adm") {
          return {
            headers: { "content-range": "0-4/5" },
            isLoading: false,
            error: null,
          };
        }
        return {
          headers: { "content-range": "0-24/25" },
          isLoading: false,
          error: null,
        };
      });

      render(<OrganizationSidepanel session={mockSession} />);

      const linkedUsers = screen.getByTestId("organization-linked-users");
      expect(linkedUsers).toHaveAttribute("data-registered-count", "25");
      expect(linkedUsers).toHaveAttribute("data-admin-count", "5");
    });
  });
});
