/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import EditOrganizationForm from "../EditOrganizationForm";

// Mock Form from next/form
vi.mock("next/form", () => ({
  default: ({ children, action, className }: any) => (
    <form onSubmit={action} className={className} data-testid="form">
      {children}
    </form>
  ),
}));

// Mock react-toastify
vi.mock("react-toastify", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// Mock swr
vi.mock("swr", () => ({
  mutate: vi.fn(),
}));

// Mock useActionState
const mockFormAction = vi.fn();
vi.mock("react", async () => {
  const actual = await vi.importActual("react");
  return {
    ...actual,
    useActionState: () => [{ success: false, message: "" }, mockFormAction, false],
  };
});

// Mock validation functions
vi.mock("@/app/user-management/utils/validation", () => ({
  validateOrganizationName: vi.fn((value: string) => {
    if (!value) return true;
    return value.length <= 255;
  }),
  validateShortName: vi.fn((name: string) => name.length <= 32),
  validateUrl: vi.fn((url: string) => {
    if (url === "") return true;
    return url.startsWith("http://") || url.startsWith("https://");
  }),
}));

// Mock editOrganizationAction
vi.mock("../../actions", () => ({
  editOrganizationAction: vi.fn(),
}));

// Mock gazelle-component-ui
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Button: ({ id, children, disabled, type, variant, onClick }: any) => (
    <button data-testid={id} disabled={disabled} type={type} data-variant={variant} onClick={onClick}>
      {children}
    </button>
  ),
  Input: ({ id, value, setValue, label, name, readonly, disabled, isValidInput, validationMessage }: any) => (
    <div data-testid={`input-${id}`} data-valid={isValidInput === undefined ? undefined : String(isValidInput)}>
      <label>{label}</label>
      <input
        data-testid={`input-field-${id}`}
        name={name}
        value={value}
        onChange={(e) => setValue(e.target.value)}
        readOnly={readonly}
        disabled={disabled}
      />
      {isValidInput === false && <span data-testid={`validation-${id}`}>{validationMessage}</span>}
    </div>
  ),
}));

// Mock lucide-react
vi.mock("lucide-react", () => ({
  Globe: () => <svg data-testid="globe-icon" />,
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

describe("EditOrganizationForm", () => {
  const mockOrganizationData = {
    data: {
      id: "org-123",
      name: "Test Organization",
      shortname: "test-org",
      url: "https://example.com",
      delegated: true,
      archived: false,
      lastUpdateTimestamp: new Date(1690000000000),
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Form rendering", () => {
    it("renders all form fields", () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      expect(screen.getByTestId("input-name")).toBeInTheDocument();
      expect(screen.getByTestId("input-shortName")).toBeInTheDocument();
    });

    it("renders save button", () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      expect(screen.getByTestId("save-organization")).toBeInTheDocument();
    });

    it("renders mandatory fields message", () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      expect(screen.getByText(/mandatory/i)).toBeInTheDocument();
    });

    it("includes hidden organizationId field", () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      const form = screen.getByTestId("form");
      const hiddenInput = form.querySelector('input[name="organizationId"]');
      expect(hiddenInput).toHaveValue("org-123");
    });
  });

  describe("Form field initialization", () => {
    it("loads organization data into form fields", async () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      await waitFor(() => {
        expect(screen.getByTestId("input-field-name")).toHaveValue("Test Organization");
        expect(screen.getByTestId("input-field-shortName")).toHaveValue("test-org");
      });
    });
  });

  describe("Short name field", () => {
    it("is readonly and disabled", () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      const shortNameInput = screen.getByTestId("input-field-shortName");
      expect(shortNameInput).toHaveAttribute("readonly");
      expect(shortNameInput).toBeDisabled();
    });
  });

  describe("Form field updates", () => {
    it("updates legal name when input changes", async () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      const nameInput = screen.getByTestId("input-field-name");
      fireEvent.change(nameInput, { target: { value: "New Organization Name" } });

      await waitFor(() => {
        expect(nameInput).toHaveValue("New Organization Name");
      });
    });
  });

  describe("Unsaved changes tracking", () => {
    beforeEach(() => {
      mockSetHasUnsavedChanges.mockClear();
    });

    it("does not set unsaved changes when form loads with initial data", async () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      await waitFor(() => {
        expect(screen.getByTestId("input-field-name")).toHaveValue("Test Organization");
      });

      // Should not have been called with true since there are no changes
      expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(false);
    });

    it("sets unsaved changes when name is modified", async () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      const nameInput = screen.getByTestId("input-field-name");

      await waitFor(() => {
        expect(nameInput).toHaveValue("Test Organization");
      });

      mockSetHasUnsavedChanges.mockClear();
      fireEvent.change(nameInput, { target: { value: "New Organization Name" } });

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });
  });

  describe("Save button state", () => {
    it("is disabled when no changes are made", () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      expect(screen.getByTestId("save-organization")).toBeDisabled();
    });

    it("is enabled when name changes", async () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      const nameInput = screen.getByTestId("input-field-name");

      // Wait for initial load
      await waitFor(() => {
        expect(nameInput).toHaveValue("Test Organization");
      });

      fireEvent.change(nameInput, { target: { value: "New Organization Name" } });

      await waitFor(() => {
        expect(screen.getByTestId("save-organization")).not.toBeDisabled();
      });
    });

    it("is disabled when name is empty", async () => {
      render(<EditOrganizationForm organizationId="org-123" organizationData={mockOrganizationData} />);

      const nameInput = screen.getByTestId("input-field-name");

      await waitFor(() => {
        expect(nameInput).toHaveValue("Test Organization");
      });

      fireEvent.change(nameInput, { target: { value: "" } });

      await waitFor(() => {
        expect(screen.getByTestId("save-organization")).toBeDisabled();
      });
    });
  });
});
