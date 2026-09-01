/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import CreateUser from "../CreateUserForm";
import { Session } from "next-auth";

// Mock next/form
vi.mock("next/form", () => ({
  default: (props: any) =>
    React.createElement(
      "form",
      {
        onSubmit: (e) => {
          e.preventDefault();
          const formData = new FormData(e.currentTarget as HTMLFormElement);
          props.action(formData);
        },
        className: props.className,
      },
      props.children,
    ),
}));

// Mock next-auth
vi.mock("next-auth/react", () => ({
  useSession: () => ({
    data: {
      user: {
        organization: "org-123",
        groups: ["role:user"],
      },
    },
  }),
}));

// Mock react-toastify
vi.mock("react-toastify", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// Mock SWR
vi.mock("swr", () => ({
  mutate: vi.fn(),
}));

// Mock user actions
const mockCreateUser = vi.fn();
vi.mock("../actions", () => ({
  createUser: mockCreateUser,
}));

// Mock hooks
const mockUseGetOrganizations = vi.fn();
vi.mock("@/app/user-management/hooks/swr/useGetGroups", () => ({
  useGetOrganizations: (params: any) => mockUseGetOrganizations(params),
}));

const mockUseGetOrganizationFromId = vi.fn();
vi.mock("@/shared/hooks/useGetUserInformation", () => ({
  useGetOrganizationFromId: (orgId: string) => mockUseGetOrganizationFromId(orgId),
}));

vi.mock("@user-management/hooks/useGumConfig", () => ({
  useGumConfig: () => ({
    data: {
      data: {
        userCreationEmailNotificationEnabled: true,
      },
    },
  }),
}));

// Mock permissions
const mockIsOnlyOrgaAdmin = vi.fn();
vi.mock("@user-management/utils/permissions", () => ({
  isOnlyOrgaAdmin: () => mockIsOnlyOrgaAdmin(),
}));

// Mock validation functions
vi.mock("@user-management/utils/validation", () => ({
  validateEmail: vi.fn((value) => {
    const [local, domain] = value?.split("@") || [];
    return local && domain?.includes(".") && !domain.startsWith(".") && !domain.endsWith(".");
  }),
  validateUserNames: vi.fn((value) => {
    return value && value.length >= 2;
  }),
  validateShortName: vi.fn((value) => {
    if (!value) return true;
    return value.length <= 32;
  }),
  validateUrl: vi.fn((value) => {
    if (!value) return false;
    try {
      new URL(value);
      return true;
    } catch {
      return false;
    }
  }),
}));

// Mock gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Badge: ({ children }: any) => <div data-testid="badge">{children}</div>,
  Button: ({ id, children, disabled, type, variant, onClick }: any) => (
    <button data-testid={id} disabled={disabled} type={type} data-variant={variant} onClick={onClick}>
      {children}
    </button>
  ),
  Input: ({ id, value, setValue, label, name, type, placeholder, required, isValidInput, validationMessage }: any) => (
    <div data-testid={`input-container-${id}`}>
      <label htmlFor={id}>{label}</label>
      <input
        id={id}
        data-testid={`input-field-${name}`}
        name={name}
        type={type}
        value={value || ""}
        onChange={(e) => setValue?.(e.target.value)}
        placeholder={placeholder}
        required={required}
      />
      {isValidInput === false && <span data-testid={`validation-${id}`}>{validationMessage}</span>}
    </div>
  ),
  SelectInput: ({ id, value, handleChange, options, name, isDisabled }: any) => (
    <div data-testid={`select-${id}`}>
      <select
        data-testid={`select-field-${name}`}
        name={name}
        value={value?.value || ""}
        disabled={isDisabled}
        onChange={(e) => {
          const selected = options?.find((opt: any) => opt.value === e.target.value);
          handleChange?.(selected);
        }}
      >
        <option value="">Select...</option>
        {options?.map((opt: any) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  ),
  NoticeBanner: ({ children, color }: any) => (
    <div data-testid="notice-banner" data-color={color}>
      {children}
    </div>
  ),
  ToggleSwitch: ({ id, checked, onChange, label }: any) => (
    <div data-testid={`toggle-${id}`}>
      <label>
        <input type="checkbox" checked={checked} onChange={onChange} data-testid={`toggle-input-${id}`} />
        {label}
      </label>
    </div>
  ),
}));

// Mock OrganizationFormFields
vi.mock("@/app/user-management/components/organization/create/OrganizationFormFields", () => ({
  default: ({ name, setName, shortName, setShortName }: any) => (
    <div data-testid="organization-form-fields">
      <input data-testid="input-field-name" value={name} onChange={(e) => setName(e.target.value)} name="name" />
      <input data-testid="input-field-shortName" value={shortName} onChange={(e) => setShortName(e.target.value)} name="shortName" />
    </div>
  ),
}));

// Mock lucide-react icons
vi.mock("lucide-react", () => ({
  UserPlus: () => <span>UserPlus</span>,
  Info: () => <span>Info</span>,
}));

// Mock useUnsavedChanges hook
const mockSetHasUnsavedChanges = vi.fn();
vi.mock("@/shared/context/UnsavedChangeContext", () => ({
  useUnsavedChanges: () => ({
    hasUnsavedChanges: false,
    setHasUnsavedChanges: mockSetHasUnsavedChanges,
    handleNavigation: vi.fn(),
    router: {
      push: vi.fn(),
      replace: vi.fn(),
      back: vi.fn(),
      forward: vi.fn(),
      refresh: vi.fn(),
      prefetch: vi.fn(),
    },
  }),
}));

// Mock useDebounce hook
vi.mock("@/shared/hooks/useDebounce", () => ({
  useDebounce: (value: any) => value, // Return value immediately for testing
}));

describe("CreateUser", () => {
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

    // Default mock implementation for useGetOrganizations
    mockUseGetOrganizations.mockReturnValue({
      data: {
        data: [
          { id: "org-1", name: "Organization 1" },
          { id: "org-2", name: "Organization 2" },
          { id: "org-123", name: "Organization 123" },
        ],
      },
      isLoading: false,
    });

    // Default mock implementation for useGetOrganizationFromId
    mockUseGetOrganizationFromId.mockReturnValue({
      data: {
        data: { id: "org-123", name: "Organization 123" },
      },
      isLoading: false,
    });

    // Default mock implementation for isOnlyOrgaAdmin
    mockIsOnlyOrgaAdmin.mockReturnValue(false);
  });

  it("renders the form", () => {
    render(<CreateUser session={mockSession} />);

    expect(screen.getByTestId("Create account")).toBeInTheDocument();
  });

  it("renders all user input fields", () => {
    render(<CreateUser session={mockSession} />);

    expect(screen.getByTestId("input-field-firstName")).toBeInTheDocument();
    expect(screen.getByTestId("input-field-lastName")).toBeInTheDocument();
    expect(screen.getByTestId("input-field-email")).toBeInTheDocument();
  });

  it("renders organization toggle switch", () => {
    render(<CreateUser session={mockSession} />);

    expect(screen.getByTestId("toggle-create_new_organization")).toBeInTheDocument();
  });

  it("shows SelectInput by default when toggle is off", () => {
    render(<CreateUser session={mockSession} />);

    expect(screen.getByTestId("select-field-organizationId")).toBeInTheDocument();
    expect(screen.queryByTestId("organization-form-fields")).not.toBeInTheDocument();
  });

  it("shows OrganizationFormFields when toggle is on", async () => {
    render(<CreateUser session={mockSession} />);

    const toggle = screen.getByTestId("toggle-input-create_new_organization");
    fireEvent.click(toggle);

    await waitFor(() => {
      expect(screen.getByTestId("organization-form-fields")).toBeInTheDocument();
      expect(screen.queryByTestId("select-field-organizationId")).not.toBeInTheDocument();
    });
  });

  it("submit button is disabled when required user fields are empty", () => {
    render(<CreateUser session={mockSession} />);

    const submitButton = screen.getByTestId("Create account");
    expect(submitButton).toBeDisabled();
  });

  it("submit button is enabled when selecting existing org and all user fields valid", async () => {
    render(<CreateUser session={mockSession} />);

    fireEvent.change(screen.getByTestId("input-field-firstName"), { target: { value: "John" } });
    fireEvent.change(screen.getByTestId("input-field-lastName"), { target: { value: "Doe" } });
    fireEvent.change(screen.getByTestId("input-field-email"), { target: { value: "john@example.com" } });
    fireEvent.change(screen.getByTestId("select-field-organizationId"), { target: { value: "org-1" } });

    await waitFor(() => {
      const submitButton = screen.getByTestId("Create account");
      expect(submitButton).not.toBeDisabled();
    });
  });

  it("submit button is enabled when creating new org with all valid fields", async () => {
    render(<CreateUser session={mockSession} />);

    fireEvent.change(screen.getByTestId("input-field-firstName"), { target: { value: "John" } });
    fireEvent.change(screen.getByTestId("input-field-lastName"), { target: { value: "Doe" } });
    fireEvent.change(screen.getByTestId("input-field-email"), { target: { value: "john@example.com" } });

    const toggle = screen.getByTestId("toggle-input-create_new_organization");
    fireEvent.click(toggle);

    await waitFor(() => {
      expect(screen.getByTestId("organization-form-fields")).toBeInTheDocument();
    });

    fireEvent.change(screen.getByTestId("input-field-name"), { target: { value: "ACME Corp" } });
    fireEvent.change(screen.getByTestId("input-field-shortName"), { target: { value: "acme" } });

    await waitFor(() => {
      const submitButton = screen.getByTestId("Create account");
      expect(submitButton).not.toBeDisabled();
    });
  });

  it("displays email notification message when enabled", () => {
    render(<CreateUser session={mockSession} />);

    expect(screen.getByText(/once creation is completed.*email will be sent/i)).toBeInTheDocument();
  });

  describe("Unsaved changes tracking", () => {
    beforeEach(() => {
      mockSetHasUnsavedChanges.mockClear();
    });

    it("sets unsaved changes when first name is entered", async () => {
      render(<CreateUser session={mockSession} />);

      fireEvent.change(screen.getByTestId("input-field-firstName"), { target: { value: "John" } });

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });

    it("sets unsaved changes when last name is entered", async () => {
      render(<CreateUser session={mockSession} />);

      fireEvent.change(screen.getByTestId("input-field-lastName"), { target: { value: "Doe" } });

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });

    it("sets unsaved changes when email is entered", async () => {
      render(<CreateUser session={mockSession} />);

      fireEvent.change(screen.getByTestId("input-field-email"), { target: { value: "john@example.com" } });

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });

    it("sets unsaved changes when organization is selected", async () => {
      render(<CreateUser session={mockSession} />);

      fireEvent.change(screen.getByTestId("select-field-organizationId"), { target: { value: "org-1" } });

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });

    it("sets unsaved changes when organization toggle is changed", async () => {
      render(<CreateUser session={mockSession} />);

      const toggle = screen.getByTestId("toggle-input-create_new_organization");
      fireEvent.click(toggle);

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });

    it("sets unsaved changes when organization legal name is entered", async () => {
      render(<CreateUser session={mockSession} />);

      const toggle = screen.getByTestId("toggle-input-create_new_organization");
      fireEvent.click(toggle);

      await waitFor(() => {
        fireEvent.change(screen.getByTestId("input-field-name"), { target: { value: "ACME Corp" } });
      });

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });

    it("sets unsaved changes when organization short name is entered", async () => {
      render(<CreateUser session={mockSession} />);

      const toggle = screen.getByTestId("toggle-input-create_new_organization");
      fireEvent.click(toggle);

      await waitFor(() => {
        fireEvent.change(screen.getByTestId("input-field-shortName"), { target: { value: "acme" } });
      });

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });
  });

  describe("Organization admin search behavior", () => {
    const orgAdminSession = {
      user: {
        gazelleId: "user-456",
        id: "user-456",
        organization: "org-123",
        email: "orgadmin@example.com",
        name: "Org Admin",
        groups: ["role:organization_admin"],
      },
      expires: "2026-12-31",
    } as unknown as Session;

    beforeEach(() => {
      mockSetHasUnsavedChanges.mockClear();
    });

    it("uses useGetOrganizationFromId for organization admins", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(true);

      render(<CreateUser session={orgAdminSession} />);

      // Verify that useGetOrganizationFromId was called with the org admin's org ID
      expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("org-123");
    });

    it("uses useGetOrganizations with search for non-organization admins", async () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(false);

      render(<CreateUser session={mockSession} />);

      // Should be called with empty string initially (no search input yet)
      expect(mockUseGetOrganizations).toHaveBeenCalledWith({
        search: "",
        archived: false,
      });
    });

    it("shows only org admin's organization in the dropdown", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(true);

      render(<CreateUser session={orgAdminSession} />);

      const select = screen.getByTestId("select-field-organizationId");
      const options = select.querySelectorAll("option");

      // Should have 2 options: default "Select..." + their org
      expect(options).toHaveLength(2);
      expect(options[1]).toHaveValue("org-123");
      expect(options[1]).toHaveTextContent("Organization 123");
    });

    it("shows all organizations for non-org admins", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(false);

      render(<CreateUser session={mockSession} />);

      const select = screen.getByTestId("select-field-organizationId");
      const options = select.querySelectorAll("option");

      // Should have 4 options: default "Select..." + 3 orgs
      expect(options).toHaveLength(4);
      expect(options[1]).toHaveValue("org-1");
      expect(options[2]).toHaveValue("org-2");
      expect(options[3]).toHaveValue("org-123");
    });

    it("org admin sees their organization even when useGetOrganizationFromId is loading", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(true);

      // Mock loading state
      mockUseGetOrganizationFromId.mockReturnValue({
        data: undefined,
        isLoading: true,
      });

      render(<CreateUser session={orgAdminSession} />);

      const select = screen.getByTestId("select-field-organizationId");
      const options = select.querySelectorAll("option");

      // Should only have the default "Select..." option when still loading
      expect(options).toHaveLength(1);
    });

    it("org admin organization is fetched directly by ID, bypassing pagination", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(true);

      render(<CreateUser session={orgAdminSession} />);

      // Verify useGetOrganizationFromId is called (this bypasses pagination)
      expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("org-123");

      // Verify the organization appears in the select
      const select = screen.getByTestId("select-field-organizationId");
      const orgOption = Array.from(select.querySelectorAll("option")).find((opt) => (opt as HTMLOptionElement).value === "org-123");

      expect(orgOption).toBeDefined();
      expect(orgOption).toHaveTextContent("Organization 123");
    });

    it("org admin can access their organization even if not in paginated results", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(true);

      // Setup: org admin's organization ID
      const orgAdminWithDifferentOrg = {
        user: {
          gazelleId: "user-789",
          id: "user-789",
          organization: "org-xyz-999", // This org is NOT in the paginated results
          email: "admin@differentorg.com",
          name: "Different Org Admin",
          groups: ["role:organization_admin"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      // Mock: useGetOrganizationFromId returns their specific org
      mockUseGetOrganizationFromId.mockReturnValue({
        data: {
          data: { id: "org-xyz-999", name: "My Specific Organization" },
        },
        isLoading: false,
      });

      // Mock: useGetOrganizations returns paginated results that DON'T include their org
      mockUseGetOrganizations.mockReturnValue({
        data: {
          data: [
            { id: "org-1", name: "Organization 1" },
            { id: "org-2", name: "Organization 2" },
            // org-xyz-999 is NOT here - simulating pagination issue
          ],
        },
        isLoading: false,
      });

      render(<CreateUser session={orgAdminWithDifferentOrg} />);

      // Verify: useGetOrganizationFromId was called with the correct org ID
      expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("org-xyz-999");

      // Verify: their organization appears in the dropdown even though not in paginated list
      const select = screen.getByTestId("select-field-organizationId");
      const options = select.querySelectorAll("option");

      expect(options).toHaveLength(2); // "Select..." + their org
      expect(options[1]).toHaveValue("org-xyz-999");
      expect(options[1]).toHaveTextContent("My Specific Organization");
    });

    it("auto-populates organization select for org admins", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(true);

      render(<CreateUser session={orgAdminSession} />);

      // The select should have the org admin's organization pre-selected
      const select = screen.getByTestId("select-field-organizationId");
      expect(select).toHaveValue("org-123");
    });

    it("disables organization select for org admins", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(true);

      render(<CreateUser session={orgAdminSession} />);

      // Org admins cannot change their organization
      const select = screen.getByTestId("select-field-organizationId");
      expect(select).toBeDisabled();
    });

    it("hides organization toggle for org admins", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(true);

      render(<CreateUser session={orgAdminSession} />);

      // Organization creation toggle should not be visible for org admins
      expect(screen.queryByTestId("toggle-create_new_organization")).not.toBeInTheDocument();
    });

    it("shows organization toggle for non-org admins", () => {
      mockIsOnlyOrgaAdmin.mockReturnValue(false);

      render(<CreateUser session={mockSession} />);

      // Organization creation toggle should be visible for non-org admins
      expect(screen.getByTestId("toggle-create_new_organization")).toBeInTheDocument();
    });
  });
});
