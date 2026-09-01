/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import CreateOrganization from "../CreateOrganization";
import { Session } from "next-auth";

// Mock organization actions
vi.mock("../../actions", () => ({
  createOrganizationAction: vi.fn(async () => ({
    success: false,
    message: "",
  })),
}));

// Mock next/navigation
vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
    refresh: vi.fn(),
  }),
  usePathname: () => "/user-management/organization/create",
}));

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

// Mock the UsersTabs component
vi.mock("../../UsersTabs", () => ({
  default: () => <div data-testid="users-tabs">UsersTabs</div>,
}));

// Mock ContentHeaderWrapper
vi.mock("@/shared/components/layout/ContentHeaderWrapper", () => ({
  default: ({ title, id }: { title: string; id: string }) => (
    <div data-testid={id}>
      <h1>{title}</h1>
    </div>
  ),
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
  Button: ({ id, children, disabled, type, variant, onClick }: any) => (
    <button data-testid={id} disabled={disabled} type={type} data-variant={variant} onClick={onClick}>
      {children}
    </button>
  ),
  Input: ({ id, value, setValue, label, name, type, placeholder, required, isValidInput, validationMessage }: any) => (
    <div data-testid={`input-${id}`}>
      <label htmlFor={id}>{label}</label>
      <input
        id={id}
        data-testid={`input-field-${id}`}
        name={name}
        type={type}
        value={value}
        onChange={(e) => setValue?.(e.target.value)}
        placeholder={placeholder}
        required={required}
      />
      {isValidInput === false && <span data-testid={`validation-${id}`}>{validationMessage}</span>}
    </div>
  ),
  NoticeBanner: ({ children, color }: any) => (
    <div data-testid="notice-banner" data-color={color}>
      {children}
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

describe("CreateOrganization", () => {
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
  });

  it("renders the form with all fields", () => {
    render(<CreateOrganization session={mockSession} />);

    expect(screen.getByText(/User management/i)).toBeInTheDocument();
    expect(screen.getByTestId("create-organization-header")).toBeInTheDocument();
    expect(screen.getByText(/New organization details/i)).toBeInTheDocument();
    expect(screen.getByTestId("input-field-name")).toBeInTheDocument();
    expect(screen.getByLabelText(/Short name/i)).toBeInTheDocument();
  });

  it("enables submit button only when required fields are filled and valid", () => {
    render(<CreateOrganization session={mockSession} />);

    const submitButton = screen.getByTestId("save-organization");
    const nameInput = screen.getByTestId("input-field-name") as HTMLInputElement;
    const shortNameInput = screen.getByTestId("input-field-shortName") as HTMLInputElement;
    expect(submitButton).toBeDisabled();

    fireEvent.change(nameInput, { target: { value: "Test Organization" } });
    expect(submitButton).toBeDisabled();

    fireEvent.change(shortNameInput, { target: { value: "test-org" } });
    expect(submitButton).not.toBeDisabled();

    fireEvent.change(shortNameInput, { target: { value: "this-is-a-very-long-name-that-exceeds-the-limit" } });
    expect(submitButton).toBeDisabled();

    fireEvent.change(shortNameInput, { target: { value: "test_org" } });
    expect(submitButton).not.toBeDisabled();
  });

  it("updates input values when typing", () => {
    render(<CreateOrganization session={mockSession} />);

    const nameInput = screen.getByTestId("input-field-name") as HTMLInputElement;
    const shortNameInput = screen.getByTestId("input-field-shortName") as HTMLInputElement;

    fireEvent.change(nameInput, { target: { value: "ACME Corp" } });
    fireEvent.change(shortNameInput, { target: { value: "acme" } });

    expect(nameInput.value).toBe("ACME Corp");
    expect(shortNameInput.value).toBe("acme");
  });

  it("clears inputs after typing when there are no errors", () => {
    render(<CreateOrganization session={mockSession} />);

    const nameInput = screen.getByTestId("input-field-name") as HTMLInputElement;
    const shortNameInput = screen.getByTestId("input-field-shortName") as HTMLInputElement;

    // Fill form
    fireEvent.change(nameInput, { target: { value: "Test Organization" } });
    fireEvent.change(shortNameInput, { target: { value: "test" } });

    // Verify inputs have values
    expect(nameInput.value).toBe("Test Organization");
    expect(shortNameInput.value).toBe("test");

    // Update input
    fireEvent.change(nameInput, { target: { value: "Test Organization Updated" } });
    expect(nameInput.value).toBe("Test Organization Updated");
  });

  it("accepts input changes without errors", () => {
    render(<CreateOrganization session={mockSession} />);
    const nameInput = screen.getByTestId("input-field-name") as HTMLInputElement;

    fireEvent.change(nameInput, { target: { value: "New Organization" } });
    expect(nameInput.value).toBe("New Organization");
  });

  it("displays submit button text based on pending state", () => {
    render(<CreateOrganization session={mockSession} />);
    const submitButton = screen.getByTestId("save-organization");

    expect(submitButton).toHaveTextContent("Create organization");
  });

  it("validates short name length", () => {
    render(<CreateOrganization session={mockSession} />);

    const shortNameInput = screen.getByLabelText(/Short name/i) as HTMLInputElement;
    const submitButton = screen.getByTestId("save-organization");

    const nameInput = screen.getByTestId("input-field-name") as HTMLInputElement;
    fireEvent.change(nameInput, { target: { value: "Test Organization" } });

    fireEvent.change(shortNameInput, { target: { value: "valid-name_123" } });
    expect(submitButton).not.toBeDisabled();

    fireEvent.change(shortNameInput, { target: { value: "this-is-a-very-long-short-name-that-exceeds-32-characters" } });
    expect(submitButton).toBeDisabled();

    fireEvent.change(shortNameInput, { target: { value: "short-name" } });
    expect(submitButton).not.toBeDisabled();
  });
});
