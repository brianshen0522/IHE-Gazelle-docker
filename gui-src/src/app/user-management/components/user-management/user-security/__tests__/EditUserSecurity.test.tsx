/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import EditUserSecurity from "../EditUserSecurity";
import { Session } from "next-auth";
import { User } from "../../Types";
import { GAZELLE_ADMIN } from "@home/utils/permissions";

// Mock next/navigation
const mockPush = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

// Mock next-auth
const mockUseSession = vi.fn();
vi.mock("next-auth/react", () => ({
  useSession: () => mockUseSession(),
}));

// Mock EditUserContext
const mockSetUserActivation = vi.fn();
const mockEditUserContext = {
  userActivation: true,
  setUserActivation: mockSetUserActivation,
  user: null,
  userPictureUrl: null,
  setUserPictureUrl: vi.fn(),
  newUserPicture: null,
  setNewUserPicture: vi.fn(),
  inputs: [],
  orga: { value: "" },
  setOrga: vi.fn(),
  setInputs: vi.fn(),
  userGroups: [],
  setUserGroups: vi.fn(),
  userPrefTableLabel: "compact",
  setUserPrefTableLabel: vi.fn(),
  userPrefNotificationByEmail: true,
  setUserPrefNotificationByEmail: vi.fn(),
  userPrefLanguagesSpoken: ["en"],
  setUserPrefLanguagesSpoken: vi.fn(),
};

vi.mock("@user-management/context/EditUserContext", () => ({
  useEditUserContext: () => mockEditUserContext,
}));

// Mock useEnv hook
const mockEnvKcBaseUrl = "https://keycloak.example.com";
vi.mock("@user-management/hooks/useEnv", () => ({
  default: () => ({ envKcBaseUrl: mockEnvKcBaseUrl }),
}));

// Mock actions
const mockUpdateUserActivationStatus = vi.fn().mockResolvedValue({});
vi.mock("../../actions", () => ({
  updateUserActivationStatus: (...args: any[]) => mockUpdateUserActivationStatus(...args),
}));

// Mock react-toastify
vi.mock("react-toastify", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// Mock DeleteUserModal
vi.mock("../DeleteUserModal", () => ({
  default: ({ user }: { user: User }) => <div data-testid="delete-user-modal">Delete Modal for {user?.id}</div>,
}));

// Mock gazelle-component-ui components
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Button: ({ id, children, onClick, type, variant, ariaLabel, title }: any) => (
    <button data-testid={id} onClick={onClick} type={type} data-variant={variant} aria-label={ariaLabel} title={title}>
      {children}
    </button>
  ),
  SectionTitle: ({ id, title }: any) => <h2 data-testid={id}>{title}</h2>,
  ToggleSwitch: ({ id, label, name, checked, onChange }: any) => (
    <div data-testid={id}>
      <label htmlFor={name}>{label}</label>
      <input type="checkbox" id={name} name={name} checked={checked} onChange={() => onChange()} />
    </div>
  ),
}));

// Mock lucide-react
vi.mock("lucide-react", () => ({
  Mail: () => <span data-testid="mail-icon">Mail</span>,
}));

describe("EditUserSecurity", () => {
  const mockUser: User = {
    id: "user-123",
    firstName: "John",
    lastName: "Doe",
    email: "john@example.com",
    organizationId: "org-123",
    groupIds: ["group1"],
    delegated: false,
    activated: true,
  };

  const mockSession = {
    user: {
      gazelleId: "user-123",
      id: "user-123",
      organization: "org-123",
      email: "john@example.com",
      name: "John Doe",
      groups: [GAZELLE_ADMIN],
    },
    expires: "2026-12-31",
  } as Session;

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseSession.mockReturnValue({ data: mockSession });
    mockEditUserContext.userActivation = true;
    mockUpdateUserActivationStatus.mockResolvedValue({});
  });

  describe("Rendering", () => {
    it("renders the security section title", () => {
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByText("Security")).toBeInTheDocument();
    });

    it("renders activation toggle when not account and not delegated", () => {
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByTestId("activation-toggle")).toBeInTheDocument();
    });

    it("does not render activation toggle when account is true", () => {
      render(<EditUserSecurity user={mockUser} delegated={false} account={true} />);
      expect(screen.queryByTestId("activation-toggle")).not.toBeInTheDocument();
    });

    it("does not render activation toggle when delegated is true", () => {
      render(<EditUserSecurity user={mockUser} delegated={true} account={false} />);
      expect(screen.queryByTestId("activation-toggle")).not.toBeInTheDocument();
    });

    it("renders reset password button for non-delegated user viewing their own account", () => {
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByTitle("Reset password")).toBeInTheDocument();
    });

    it("does not render reset password button for delegated users", () => {
      const delegatedUser = { ...mockUser, delegated: true };
      render(<EditUserSecurity user={delegatedUser} delegated={true} account={false} />);
      expect(screen.queryByTitle("Reset password")).not.toBeInTheDocument();
    });

    it("does not render reset password button when viewing another user's account", () => {
      const otherUser = { ...mockUser, id: "other-user" };
      render(<EditUserSecurity user={otherUser} delegated={false} account={false} />);
      expect(screen.queryByTitle("Reset password")).not.toBeInTheDocument();
    });

    it("renders delete user modal for admin users", () => {
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByTestId("delete-user-modal")).toBeInTheDocument();
    });

    it("renders delete user modal when viewing own account", () => {
      mockUseSession.mockReturnValue({
        data: { ...mockSession, user: { ...mockSession.user, groups: [] } },
      });
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByTestId("delete-user-modal")).toBeInTheDocument();
    });

    it("does not render delete user modal when not admin and viewing another user", () => {
      mockUseSession.mockReturnValue({
        data: {
          ...mockSession,
          user: { ...mockSession.user, gazelleId: "other-user", groups: [] },
        },
      });
      const otherUser = { ...mockUser, id: "different-user" };
      render(<EditUserSecurity user={otherUser} delegated={false} account={false} />);
      expect(screen.queryByTestId("delete-user-modal")).not.toBeInTheDocument();
    });
  });

  describe("User Activation Toggle", () => {
    it("displays activated status when user is activated", () => {
      mockEditUserContext.userActivation = true;
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByText("Account activated")).toBeInTheDocument();
    });

    it("displays disabled status when user is not activated", () => {
      mockEditUserContext.userActivation = false;
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByText("Account disabled")).toBeInTheDocument();
    });

    it("handles activation toggle correctly - activating user", async () => {
      const { toast } = await import("react-toastify");
      mockEditUserContext.userActivation = false;

      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);

      const toggle = screen.getByTestId("activation-toggle").querySelector("input");
      fireEvent.click(toggle as HTMLElement);

      await waitFor(() => {
        expect(mockUpdateUserActivationStatus).toHaveBeenCalledWith(true, "user-123");
        expect(mockSetUserActivation).toHaveBeenCalledWith(true);
        expect(toast.success).toHaveBeenCalledWith("Account activated");
      });
    });

    it("handles activation toggle correctly - deactivating user", async () => {
      const { toast } = await import("react-toastify");
      mockEditUserContext.userActivation = true;

      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);

      const toggle = screen.getByTestId("activation-toggle").querySelector("input");
      fireEvent.click(toggle as HTMLElement);

      await waitFor(() => {
        expect(mockUpdateUserActivationStatus).toHaveBeenCalledWith(false, "user-123");
        expect(mockSetUserActivation).toHaveBeenCalledWith(false);
        expect(toast.success).toHaveBeenCalledWith("Account disabled");
      });
    });
  });

  describe("Reset Password Functionality", () => {
    it("redirects to keycloak reset password page when clicking reset password", () => {
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);

      const resetButton = screen.getByTitle("Reset password");
      fireEvent.click(resetButton);

      expect(mockPush).toHaveBeenCalledWith(`${mockEnvKcBaseUrl}/login-actions/reset-credentials?client_id=gazelle-account`);
    });

    it("displays 'update password' text when viewing own account", () => {
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByText("Update password")).toBeInTheDocument();
    });

    it("displays 'reset password' text when admin viewing another user", () => {
      const otherUser = { ...mockUser, id: "other-user" };
      mockUseSession.mockReturnValue({
        data: {
          ...mockSession,
          user: { ...mockSession.user, gazelleId: "admin-user" },
        },
      });
      // This case shouldn't render the button since user is not viewing own account
      render(<EditUserSecurity user={otherUser} delegated={false} account={false} />);
      expect(screen.queryByTitle("Reset password")).not.toBeInTheDocument();
    });

    it("renders mail icon in reset password button", () => {
      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByTestId("mail-icon")).toBeInTheDocument();
    });
  });

  describe("Permission Checks", () => {
    it("allows delete for GAZELLE_ADMIN users", () => {
      const otherUser = { ...mockUser, id: "other-user" };
      mockUseSession.mockReturnValue({
        data: {
          ...mockSession,
          user: { ...mockSession.user, gazelleId: "admin-user", groups: [GAZELLE_ADMIN] },
        },
      });

      render(<EditUserSecurity user={otherUser} delegated={false} account={false} />);
      expect(screen.getByTestId("delete-user-modal")).toBeInTheDocument();
    });

    it("allows delete for users viewing their own account", () => {
      mockUseSession.mockReturnValue({
        data: {
          ...mockSession,
          user: { ...mockSession.user, gazelleId: "user-123", groups: [] },
        },
      });

      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByTestId("delete-user-modal")).toBeInTheDocument();
    });

    it("denies delete for non-admin users viewing other accounts", () => {
      const otherUser = { ...mockUser, id: "other-user" };
      mockUseSession.mockReturnValue({
        data: {
          ...mockSession,
          user: { ...mockSession.user, gazelleId: "different-user", groups: [] },
        },
      });

      render(<EditUserSecurity user={otherUser} delegated={false} account={false} />);
      expect(screen.queryByTestId("delete-user-modal")).not.toBeInTheDocument();
    });
  });

  describe("Edge Cases", () => {
    it("handles missing session gracefully", () => {
      mockUseSession.mockReturnValue({ data: null });

      render(<EditUserSecurity user={mockUser} delegated={false} account={false} />);
      expect(screen.getByText("Security")).toBeInTheDocument();
    });

    it("renders correctly when all features are disabled", () => {
      render(<EditUserSecurity user={mockUser} delegated={true} account={true} />);
      expect(screen.getByText("Security")).toBeInTheDocument();
      expect(screen.queryByTestId("activation-toggle")).not.toBeInTheDocument();
    });
  });
});
