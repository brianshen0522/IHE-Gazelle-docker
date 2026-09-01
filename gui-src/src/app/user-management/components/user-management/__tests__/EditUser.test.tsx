/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import EditUser from "../EditUser";
import { Session } from "next-auth";

// Mock next/navigation
const mockPush = vi.fn();
const mockBack = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush, back: mockBack }),
}));

// Mock next-auth
const mockUseSession = vi.fn();
vi.mock("next-auth/react", () => ({
  useSession: () => mockUseSession(),
}));

// Mock EditUserContext
const mockEditUserContext = {
  user: {
    id: "user-123",
    email: "test@example.com",
    delegated: false,
  },
  userPictureUrl: "",
  newUserPicture: null,
  inputs: [
    { name: "firstName", value: "John" },
    { name: "lastName", value: "Doe" },
    { name: "email", value: "test@example.com" },
  ],
  orga: { value: "org-123" },
  userActivation: true,
  userGroups: [],
  userPrefTableLabel: "compact",
  userPrefNotificationByEmail: true,
  userPrefLanguagesSpoken: ["en"],
};

vi.mock("@user-management/context/EditUserContext", () => ({
  useEditUserContext: () => mockEditUserContext,
}));

// Mock hooks
vi.mock("@user-management/hooks/useFilteredParams", () => ({
  useFilteredParams: () => ({ filteredParams: {} }),
}));

vi.mock("@/shared/hooks/SWR/useGetData", () => ({
  useGetData: () => ({ mutateData: vi.fn() }),
}));

vi.mock("@/app/user-management/hooks/swr/useGetUser", () => ({
  useGetUserById: () => ({ mutate: vi.fn(), key: "user-key" }),
  useGetUserPicture: () => ({ data: null, isError: false, key: "picture-key", mutate: vi.fn() }),
  useGetUserPreferencesById: () => ({ data: null, isError: false, isLoading: false }),
}));

vi.mock("@shared/context/UnsavedChangeContext", () => ({
  useUnsavedChanges: () => ({
    hasUnsavedChanges: false,
    setHasUnsavedChanges: vi.fn(),
    handleNavigation: (callback: () => void) => callback(),
  }),
}));

// Mock action functions
vi.mock("../editUserActions", () => ({
  handleFormSubmit: vi.fn((e) => e.preventDefault()),
  validateInputs: vi.fn(() => false),
}));

// Mock child components
vi.mock("../EditUserSecurity", () => ({
  default: ({ user }: any) => <div data-testid="edit-user-security">Security for {user.id}</div>,
}));

vi.mock("../ProfilePicture", () => ({
  default: () => <div data-testid="profile-picture">Profile Picture</div>,
}));

vi.mock("@user-management/components/user-management/EditUserAttributes", () => ({
  default: () => <div data-testid="edit-user-attributes">Attributes</div>,
}));

vi.mock("@user-management/components/user-management/RoleAttribution", () => ({
  default: () => <div data-testid="role-attribution">Roles</div>,
}));

vi.mock("@user-management/components/user-management/EditUserPreferences", () => ({
  default: () => <div data-testid="edit-user-preferences">Preferences</div>,
}));

// Mock gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Badge: ({ id, variant, children }: any) => (
    <div data-testid={id} data-variant={variant}>
      {children}
    </div>
  ),
  Button: ({ id, children, disabled, type, variant, onClick }: any) => (
    <button data-testid={id} disabled={disabled} type={type} data-variant={variant} onClick={onClick}>
      {children}
    </button>
  ),
  IconButton: ({ id, onClick, children, disabled, variant }: any) => (
    <button data-testid={id} onClick={onClick} disabled={disabled} data-variant={variant}>
      {children}
    </button>
  ),
  Tooltip: ({ children }: any) => <div>{children}</div>,
  SectionTitle: ({ id, title }: any) => <h2 data-testid={id}>{title}</h2>,
  ToggleSwitch: ({ id, label, name, checked, onChange }: any) => (
    <div data-testid={id}>
      <label htmlFor={name}>{label}</label>
      <input type="checkbox" id={name} name={name} checked={checked} onChange={() => onChange()} />
    </div>
  ),
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
}));

// Mock lucide-react
vi.mock("lucide-react", () => ({
  ArrowLeft: () => <span>←</span>,
  Save: () => <span>💾</span>,
  Camera: () => <span>📷</span>,
  LucideHelpCircle: () => <span>?</span>,
  Mail: () => <span data-testid="mail-icon">Mail</span>,
  Trash: () => <span data-testid="trash-icon">Trash</span>,
  TriangleAlert: () => <span data-testid="alert-icon">Alert</span>,
}));

// Mock next/image
vi.mock("next/image", () => ({
  default: ({ src, alt, ...props }: any) => <img src={src} alt={alt} {...props} />,
}));

// Mock react-toastify
vi.mock("react-toastify", () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

// Mock ProfilePicture actions
vi.mock("../actions", () => ({
  deleteUserPicture: vi.fn(),
}));

describe("EditUser", () => {
  const mockSession = {
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

  const defaultProps = {
    account: true,
    isSidePanelContext: false,
    originUrl: null,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseSession.mockReturnValue({ data: mockSession });
  });

  it("renders profile picture", () => {
    render(<EditUser {...defaultProps} />);
    expect(screen.getByTestId("profile-picture")).toBeInTheDocument();
  });

  it("renders all edit sections", () => {
    render(<EditUser {...defaultProps} />);

    expect(screen.getByTestId("edit-user-attributes")).toBeInTheDocument();
    expect(screen.getByTestId("role-attribution")).toBeInTheDocument();
    expect(screen.getByTestId("edit-user-preferences")).toBeInTheDocument();
  });

  it("renders save and cancel buttons when not in sidepanel context", () => {
    render(<EditUser {...defaultProps} />);

    expect(screen.getByText("Save")).toBeInTheDocument();
    expect(screen.getByText("Cancel")).toBeInTheDocument();
  });

  it("does not render cancel button in sidepanel context", () => {
    render(<EditUser {...defaultProps} isSidePanelContext={true} />);

    expect(screen.queryByText("Cancel")).not.toBeInTheDocument();
    expect(screen.getByText("Save")).toBeInTheDocument();
  });

  it("renders EditUserSecurity when not in sidepanel context", () => {
    render(<EditUser {...defaultProps} />);

    const securitySections = screen.getAllByTestId("Security");
    expect(securitySections.length).toBeGreaterThan(0);
  });

  it("renders EditUserSecurity at bottom when in sidepanel context", () => {
    render(<EditUser {...defaultProps} isSidePanelContext={true} />);

    const securitySections = screen.getAllByTestId("Security");
    expect(securitySections.length).toBeGreaterThan(0);
  });

  it("displays delegated account badge when user is delegated", () => {
    mockEditUserContext.user.delegated = true;
    render(<EditUser {...defaultProps} />);

    expect(
      screen.getByText("This account is delegated, only its preferences can be edited. All other attributes are read-only."),
    ).toBeInTheDocument();
    mockEditUserContext.user.delegated = false; // Reset
  });

  it("displays disabled info badge when user is not activated", () => {
    mockEditUserContext.user.delegated = false;
    mockEditUserContext.userActivation = false;
    render(<EditUser {...defaultProps} />);

    expect(screen.getByText("This account is DISABLED, attributes are read-only.")).toBeInTheDocument();
    mockEditUserContext.userActivation = true; // Reset
  });

  it("disables save button when user is not activated", () => {
    mockEditUserContext.userActivation = false;
    render(<EditUser {...defaultProps} />);

    const saveButton = screen.getByText("Save").closest("button");
    expect(saveButton).toBeDisabled();
  });

  it("shows email change warning when email differs", () => {
    mockEditUserContext.inputs = [
      { name: "firstName", value: "John" },
      { name: "lastName", value: "Doe" },
      { name: "email", value: "newemail@example.com" },
    ];
    render(<EditUser {...defaultProps} />);

    expect(
      screen.getByText(
        /You updated your email, if you save, your account will be deactivated and an email will be sent to you to validate the new email./i,
      ),
    ).toBeInTheDocument();
  });

  it("calls handleNavigation on cancel button click", () => {
    render(<EditUser {...defaultProps} />);

    const cancelButton = screen.getByText("Cancel").closest("button");
    if (cancelButton) {
      fireEvent.click(cancelButton);
    }

    expect(mockBack).toHaveBeenCalled();
  });

  it("navigates to originUrl when provided", () => {
    render(<EditUser {...defaultProps} originUrl="/custom-url" />);

    const cancelButton = screen.getByText("Cancel").closest("button");
    if (cancelButton) {
      fireEvent.click(cancelButton);
    }

    expect(mockPush).toHaveBeenCalledWith("/custom-url");
  });

  it("renders as a form element", () => {
    const { container } = render(<EditUser {...defaultProps} />);

    expect(container.querySelector("form")).toBeInTheDocument();
  });

  it("applies correct layout classes in sidepanel context", () => {
    const { container } = render(<EditUser {...defaultProps} isSidePanelContext={true} />);

    const form = container.querySelector("form");
    expect(form).toHaveClass("flex", "flex-col", "gap-8");
  });
});
