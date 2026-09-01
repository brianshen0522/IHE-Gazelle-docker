/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import "@testing-library/jest-dom";
import DeleteUserModal from "../DeleteUserModal";
import { Session } from "next-auth";
import { User } from "../../Types";
import { GAZELLE_ADMIN } from "@home/utils/permissions";

// Mock next-auth
const mockSignOut = vi.fn();
vi.mock("next-auth/react", () => ({
  signOut: (...args: any[]) => mockSignOut(...args),
}));

// Mock actions
const mockDeleteUser = vi.fn().mockResolvedValue({});
vi.mock("../../actions", () => ({
  deleteUser: (...args: any[]) => mockDeleteUser(...args),
}));

// Mock react-toastify
vi.mock("react-toastify", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const { toast } = await import("react-toastify");

// Mock gazelle-component-ui components
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Modal: ({ id, title, size, isOpen, toggleModal, trigger, children }: any) => (
    <div data-testid={`modal-${id}`} data-is-open={isOpen}>
      <div data-testid="modal-trigger" onClick={trigger.props.onClick}>
        {trigger}
      </div>
      {isOpen && (
        <div data-testid="modal-content">
          <div data-testid="modal-title">{title}</div>
          <div data-testid="modal-size" data-size={size}>
            {children}
          </div>
          <button data-testid="modal-toggle" onClick={toggleModal}>
            Toggle
          </button>
        </div>
      )}
    </div>
  ),
  Button: ({ id, children, onClick, variant, title, ariaLabel, type }: any) => (
    <button data-testid={id} onClick={onClick} data-variant={variant} title={title} aria-label={ariaLabel} type={type}>
      {children}
    </button>
  ),
}));

// Mock lucide-react
vi.mock("lucide-react", () => ({
  Trash: () => <span data-testid="trash-icon">Trash</span>,
  TriangleAlert: () => <span data-testid="alert-icon">Alert</span>,
}));

describe("DeleteUserModal", () => {
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

  // Mock window.location.reload
  const originalLocation = globalThis.location;
  beforeEach(() => {
    vi.clearAllMocks();
    mockDeleteUser.mockResolvedValue({});
    delete (globalThis as any).location;
    (globalThis as any).location = { reload: vi.fn() };
  });

  afterEach(() => {
    globalThis.location = originalLocation;
  });

  describe("Rendering", () => {
    it("renders the delete button trigger", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);
      expect(screen.getByTestId("trash-icon")).toBeInTheDocument();
    });

    it("renders 'delete my account' text when viewing own account", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);
      expect(screen.getByText("Delete my account")).toBeInTheDocument();
    });

    it("renders 'delete account' text when admin viewing another user", () => {
      const otherUser = { ...mockUser, id: "other-user" };
      render(<DeleteUserModal user={otherUser} session={mockSession} />);
      expect(screen.getByText("Delete account")).toBeInTheDocument();
    });

    it("modal is closed by default", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);
      const modalContent = screen.queryByTestId("modal-content");
      expect(modalContent).not.toBeInTheDocument();
    });
  });

  describe("Modal Interaction", () => {
    it("opens modal when delete button is clicked", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);

      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      expect(screen.getByTestId("modal-content")).toBeInTheDocument();
    });

    it("displays warning title with alert icon", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);

      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      expect(screen.getByTestId("alert-icon")).toBeInTheDocument();
      expect(screen.getByText("Warning")).toBeInTheDocument();
    });

    it("displays user name in warning message", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);

      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      const warningText = screen.getByText(/You are about to delete the account of John Doe/);
      expect(warningText).toBeInTheDocument();
    });

    it("displays keep account button", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);

      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      expect(screen.getByText("No, keep the user account")).toBeInTheDocument();
    });

    it("displays delete account confirmation button", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);

      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      expect(screen.getByText("Yes, delete the user account")).toBeInTheDocument();
    });

    it("closes modal when keep account button is clicked", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);

      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      // Verify modal is open
      expect(screen.getByTestId("modal-content")).toBeInTheDocument();

      const keepButton = screen.getByText("No, keep the user account");
      fireEvent.click(keepButton);

      // Verify modal is closed
      expect(screen.queryByTestId("modal-content")).not.toBeInTheDocument();
    });
  });

  describe("Delete User Functionality", () => {
    it("calls deleteUser and signOut when deleting own account", async () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} account={true} />);

      // Open modal
      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      // Confirm deletion
      const confirmButton = screen.getByText("Yes, delete the user account");
      fireEvent.click(confirmButton);

      await waitFor(() => {
        expect(mockDeleteUser).toHaveBeenCalledWith("user-123");
        expect(mockSignOut).toHaveBeenCalledWith({ callbackUrl: "/", redirect: true });
        expect(toast.success).toHaveBeenCalledWith("Your account has been deleted");
      });
    });

    it("calls deleteUser and reloads page when admin deletes another user", async () => {
      const otherUser = { ...mockUser, id: "other-user" };
      render(<DeleteUserModal user={otherUser} session={mockSession} account={false} />);

      // Open modal
      const deleteButton = screen.getByText("Delete account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      // Confirm deletion
      const confirmButton = screen.getByText("Yes, delete the user account");
      fireEvent.click(confirmButton);

      await waitFor(() => {
        expect(mockDeleteUser).toHaveBeenCalledWith("other-user");
        expect(toast.success).toHaveBeenCalledWith("The account has been deleted");
      });
    });

    it("handles deletion errors with error toast", async () => {
      const error = new Error("Deletion failed");
      mockDeleteUser.mockImplementationOnce(() => {
        throw error;
      });

      const consoleErrorSpy = vi.spyOn(console, "error").mockImplementation(() => {});

      render(<DeleteUserModal user={mockUser} session={mockSession} account={false} />);

      // Open modal
      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      // Confirm deletion
      const confirmButton = screen.getByText("Yes, delete the user account");
      fireEvent.click(confirmButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(error);
        expect(toast.error).toHaveBeenCalledWith("Deletion failed");
      });

      consoleErrorSpy.mockRestore();
    });

    it("does not call deleteUser when user id is missing", async () => {
      const userWithoutId = { ...mockUser, id: "" };
      render(<DeleteUserModal user={userWithoutId} session={mockSession} account={false} />);

      // Open modal
      const deleteButton = screen.getByText("Delete account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      // Confirm deletion
      const confirmButton = screen.getByText("Yes, delete the user account");
      fireEvent.click(confirmButton);

      await waitFor(() => {
        expect(mockDeleteUser).not.toHaveBeenCalled();
      });
    });
  });

  describe("Edge Cases", () => {
    it("handles null session gracefully", () => {
      render(<DeleteUserModal user={mockUser} session={null} />);
      expect(screen.getByTestId("trash-icon")).toBeInTheDocument();
    });

    it("handles user without first/last name", () => {
      const userWithoutName = { ...mockUser, firstName: "", lastName: "" };
      render(<DeleteUserModal user={userWithoutName} session={mockSession} />);

      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      expect(screen.getByTestId("modal-content")).toBeInTheDocument();
    });

    it("modal size is set to md", () => {
      render(<DeleteUserModal user={mockUser} session={mockSession} />);

      const deleteButton = screen.getByText("Delete my account").closest("button");
      fireEvent.click(deleteButton as HTMLElement);

      const modalSize = screen.getByTestId("modal-size");
      expect(modalSize).toHaveAttribute("data-size", "md");
    });
  });
});
