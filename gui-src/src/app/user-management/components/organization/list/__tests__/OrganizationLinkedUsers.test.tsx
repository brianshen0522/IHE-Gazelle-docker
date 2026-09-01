/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import OrganizationLinkedUsers from "../OrganizationLinkedUsers";

// Mock next/link
vi.mock("next/link", () => ({
  default: ({ href, children, className }: any) => (
    <a href={href} className={className} data-testid="linked-users-link">
      {children}
    </a>
  ),
}));

// Mock lucide-react
vi.mock("lucide-react", () => ({
  ExternalLink: () => <span data-testid="external-link-icon" />,
}));

// Mock react-i18next
vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        "gzl.user.interface.registered_users": "Registered Users",
        "gzl.user.interface.organization_administrators": "Organization Administrators",
        "gzl.user.interface.error_loading_linked_users": "Error loading linked users",
      };
      return translations[key] || key;
    },
  }),
}));

// Mock @gazelle/gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Skeleton: ({ className }: any) => <div data-testid="skeleton" className={className} />,
}));

describe("OrganizationLinkedUsers", () => {
  const defaultProps = {
    organizationId: "Test Organization",
    registeredUsersCount: 10,
    orgAdminCount: 2,
    isLoading: false,
    isError: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Loading state", () => {
    it("renders skeleton when isLoading is true", () => {
      render(<OrganizationLinkedUsers {...defaultProps} isLoading={true} />);

      const skeleton = screen.getByTestId("skeleton");
      expect(skeleton).toBeInTheDocument();
      expect(skeleton).toHaveClass("h-4", "w-1/2");
    });

    it("does not render links when loading", () => {
      render(<OrganizationLinkedUsers {...defaultProps} isLoading={true} />);

      expect(screen.queryByTestId("linked-users-link")).not.toBeInTheDocument();
    });
  });

  describe("Error state", () => {
    it("renders error message when isError is true", () => {
      render(<OrganizationLinkedUsers {...defaultProps} isError={true} />);

      const errorMessage = screen.getByText("Error loading linked users");
      expect(errorMessage).toBeInTheDocument();
      expect(errorMessage).toHaveClass("text-red");
    });

    it("does not render links when error", () => {
      render(<OrganizationLinkedUsers {...defaultProps} isError={true} />);

      expect(screen.queryByTestId("linked-users-link")).not.toBeInTheDocument();
    });

    it("does not render skeleton when error", () => {
      render(<OrganizationLinkedUsers {...defaultProps} isError={true} />);

      expect(screen.queryByTestId("skeleton")).not.toBeInTheDocument();
    });
  });

  describe("Normal state with registered users", () => {
    it("renders registered users link with correct count", () => {
      render(<OrganizationLinkedUsers {...defaultProps} />);

      const links = screen.getAllByTestId("linked-users-link");
      expect(links[0]).toHaveTextContent("10 registered users");
    });

    it("renders registered users link with correct href", () => {
      render(<OrganizationLinkedUsers {...defaultProps} />);

      const links = screen.getAllByTestId("linked-users-link");
      const registeredUsersLink = links[0];
      expect(registeredUsersLink).toHaveAttribute("href", "/user-management/users?organizationId=Test%20Organization");
    });

    it("renders registered users link with correct styling", () => {
      render(<OrganizationLinkedUsers {...defaultProps} />);

      const links = screen.getAllByTestId("linked-users-link");
      const registeredUsersLink = links[0];
      expect(registeredUsersLink).toHaveClass("flex", "items-center", "gap-1", "text-blue", "hover:text-visited_link", "underline");
    });

    it("renders external link icon for registered users link", () => {
      render(<OrganizationLinkedUsers {...defaultProps} />);

      const icons = screen.getAllByTestId("external-link-icon");
      expect(icons.length).toBeGreaterThan(0);
    });
  });

  describe("Organization administrators", () => {
    it("renders org admin link when orgAdminCount > 0", () => {
      render(<OrganizationLinkedUsers {...defaultProps} orgAdminCount={2} />);

      const links = screen.getAllByTestId("linked-users-link");
      expect(links[1]).toHaveTextContent("2 organization administrators");
    });

    it("renders org admin link with correct href", () => {
      render(<OrganizationLinkedUsers {...defaultProps} orgAdminCount={2} />);

      const links = screen.getAllByTestId("linked-users-link");
      const orgAdminLink = links[1];
      expect(orgAdminLink).toHaveAttribute("href", "/user-management/users?organizationId=Test%20Organization&group=org-adm");
    });

    it("does not render org admin link when orgAdminCount is 0", () => {
      render(<OrganizationLinkedUsers {...defaultProps} orgAdminCount={0} />);

      expect(screen.queryByText("organization administrators")).not.toBeInTheDocument();
      const links = screen.getAllByTestId("linked-users-link");
      expect(links).toHaveLength(1); // Only registered users link
    });

    it("renders both links when orgAdminCount > 0", () => {
      render(<OrganizationLinkedUsers {...defaultProps} orgAdminCount={3} />);

      const links = screen.getAllByTestId("linked-users-link");
      expect(links).toHaveLength(2);
    });
  });

  describe("URL encoding", () => {
    it("properly encodes organization name with special characters", () => {
      render(<OrganizationLinkedUsers {...defaultProps} organizationId="Test & Organization" />);

      const links = screen.getAllByTestId("linked-users-link");
      const registeredUsersLink = links[0];
      expect(registeredUsersLink).toHaveAttribute("href", "/user-management/users?organizationId=Test%20%26%20Organization");
    });

    it("properly encodes organization name with spaces", () => {
      render(<OrganizationLinkedUsers {...defaultProps} organizationId="My Test Org" />);

      const links = screen.getAllByTestId("linked-users-link");
      const registeredUsersLink = links[0];
      expect(registeredUsersLink).toHaveAttribute("href", "/user-management/users?organizationId=My%20Test%20Org");
    });

    it("properly encodes organization name with unicode characters", () => {
      render(<OrganizationLinkedUsers {...defaultProps} organizationId="Tëst Örgåñizatïon" />);

      const links = screen.getAllByTestId("linked-users-link");
      const registeredUsersLink = links[0];
      expect(registeredUsersLink).toHaveAttribute("href", "/user-management/users?organizationId=T%C3%ABst%20%C3%96rg%C3%A5%C3%B1izat%C3%AFon");
    });
  });

  describe("Edge cases", () => {
    it("handles zero registered users count", () => {
      render(<OrganizationLinkedUsers {...defaultProps} registeredUsersCount={0} />);

      const links = screen.getAllByTestId("linked-users-link");
      expect(links[0]).toHaveTextContent("0 registered users");
    });

    it("handles large user counts", () => {
      render(<OrganizationLinkedUsers {...defaultProps} registeredUsersCount={9999} orgAdminCount={500} />);

      const links = screen.getAllByTestId("linked-users-link");
      expect(links[0]).toHaveTextContent("9999 registered users");
      expect(links[1]).toHaveTextContent("500 organization administrators");
    });

    it("handles empty organization name", () => {
      render(<OrganizationLinkedUsers {...defaultProps} organizationId="" />);

      const links = screen.getAllByTestId("linked-users-link");
      const registeredUsersLink = links[0];
      expect(registeredUsersLink).toHaveAttribute("href", "/user-management/users?organizationId=");
    });
  });

  describe("Component rendering order", () => {
    it("renders registered users link before org admin link", () => {
      render(<OrganizationLinkedUsers {...defaultProps} orgAdminCount={2} />);

      const links = screen.getAllByTestId("linked-users-link");
      expect(links[0]).toHaveAttribute("href", expect.stringContaining("organizationId=Test%20Organization"));
      expect(links[0]).not.toHaveAttribute("href", expect.stringContaining("group=org-adm"));
      expect(links[1]).toHaveAttribute("href", expect.stringContaining("group=org-adm"));
    });
  });
});
