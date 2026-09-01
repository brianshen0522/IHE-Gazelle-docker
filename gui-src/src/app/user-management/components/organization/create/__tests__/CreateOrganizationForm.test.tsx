/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import CreateOrganizationForm from "../CreateOrganizationForm";

// Mock next/form
vi.mock("next/form", () => ({
  default: ({ children, action, className }: { children: React.ReactNode; action: (formData: FormData) => void; className?: string }) => (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        const formData = new FormData(e.currentTarget);
        action(formData);
      }}
      className={className}
    >
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

// Mock the organization actions
const { mockCreateOrganizationAction } = vi.hoisted(() => ({
  mockCreateOrganizationAction: vi.fn(),
}));

vi.mock("../../actions", () => ({
  createOrganizationAction: mockCreateOrganizationAction,
}));

// Mock validation functions
vi.mock("@/app/user-management/utils/validation", () => ({
  validateOrganizationName: vi.fn((value: string) => {
    if (!value) return true;
    return value.length <= 255;

  }),
  validateShortName: vi.fn((value: string) => {
    if (!value) return true;
    return value.length <= 32;
  }),
  validateUrl: vi.fn((value: string) => {
    if (!value) return true;
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
  Button: ({ id, children, disabled, type, variant }: any) => (
    <button data-testid={id} disabled={disabled} type={type} data-variant={variant}>
      {children}
    </button>
  ),
  NoticeBanner: ({ children, color }: any) => (
    <div data-testid="notice-banner" data-color={color}>
      {children}
    </div>
  ),
}));

// Mock OrganizationFormFields
vi.mock("../OrganizationFormFields", () => ({
  default: ({ name, setName, shortName, setShortName }: any) => (
    <div data-testid="organization-form-fields">
      <input data-testid="name-input" value={name} onChange={(e) => setName(e.target.value)} name="name" />
      <input data-testid="short-name-input" value={shortName} onChange={(e) => setShortName(e.target.value)} name="shortName" />
    </div>
  ),
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

describe("CreateOrganizationForm", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the form with OrganizationFormFields component", () => {
    render(<CreateOrganizationForm />);

    expect(screen.getByTestId("organization-form-fields")).toBeInTheDocument();
    expect(screen.getByTestId("save-organization")).toBeInTheDocument();
  });

  it("submit button is disabled when required fields are empty", () => {
    render(<CreateOrganizationForm />);

    const submitButton = screen.getByTestId("save-organization");
    expect(submitButton).toBeDisabled();
  });

  it("submit button is enabled when all required fields are valid", async () => {
    render(<CreateOrganizationForm />);

    const nameInput = screen.getByTestId("name-input");
    const shortNameInput = screen.getByTestId("short-name-input");
    const submitButton = screen.getByTestId("save-organization");

    fireEvent.change(nameInput, { target: { value: "ACME Corporation" } });
    fireEvent.change(shortNameInput, { target: { value: "acme" } });

    await waitFor(() => {
      expect(submitButton).not.toBeDisabled();
    });
  });

  it("submit button is disabled when short name is invalid", async () => {
    render(<CreateOrganizationForm />);

    const nameInput = screen.getByTestId("name-input");
    const shortNameInput = screen.getByTestId("short-name-input");
    const submitButton = screen.getByTestId("save-organization");

    fireEvent.change(nameInput, { target: { value: "ACME Corporation" } });
    fireEvent.change(shortNameInput, { target: { value: "this-is-a-very-long-short-name-that-exceeds-the-32-character-limit" } });

    await waitFor(() => {
      expect(submitButton).toBeDisabled();
    });
  });

  it("clears validation message when user starts typing", async () => {
    const { rerender } = render(<CreateOrganizationForm />);

    const nameInput = screen.getByTestId("name-input");
    fireEvent.change(nameInput, { target: { value: "Test" } });

    rerender(<CreateOrganizationForm />);

    expect(screen.queryByTestId("notice-banner")).not.toBeInTheDocument();
  });

  it("displays correct button text based on pending state", () => {
    render(<CreateOrganizationForm />);

    const submitButton = screen.getByTestId("save-organization");
    expect(submitButton).toHaveTextContent(/create organization/i);
  });

  it("displays mandatory inputs message", () => {
    render(<CreateOrganizationForm />);

    expect(screen.getByText(/attributes followed by an asterisk.*are mandatory/i)).toBeInTheDocument();
  });

  describe("Unsaved changes tracking", () => {
    beforeEach(() => {
      mockSetHasUnsavedChanges.mockClear();
    });

    it("sets unsaved changes when name is entered", async () => {
      render(<CreateOrganizationForm />);

      const nameInput = screen.getByTestId("name-input");
      fireEvent.change(nameInput, { target: { value: "ACME Corporation" } });

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });

    it("sets unsaved changes when short name is entered", async () => {
      render(<CreateOrganizationForm />);

      const shortNameInput = screen.getByTestId("short-name-input");
      fireEvent.change(shortNameInput, { target: { value: "acme" } });

      await waitFor(() => {
        expect(mockSetHasUnsavedChanges).toHaveBeenCalledWith(true);
      });
    });
  });
});
