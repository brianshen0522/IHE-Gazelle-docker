/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import OrganizationFormFields from "../OrganizationFormFields";

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
  Input: ({ id, value, setValue, label, name, type, placeholder, required, isValidInput, validationMessage }: any) => (
    <div data-testid={`input-container-${id}`}>
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
}));

describe("OrganizationFormFields", () => {
  const mockSetName = vi.fn();
  const mockSetShortName = vi.fn();

  const defaultProps = {
    name: "",
    setName: mockSetName,
    shortName: "",
    setShortName: mockSetShortName,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders all three input fields", () => {
    render(<OrganizationFormFields {...defaultProps} />);

    expect(screen.getByTestId("input-field-name")).toBeInTheDocument();
    expect(screen.getByLabelText(/Short name/i)).toBeInTheDocument();
  });

  it("displays asterisks for required fields", () => {
    render(<OrganizationFormFields {...defaultProps} />);

    expect(screen.getByLabelText("Name*")).toBeInTheDocument();
    expect(screen.getByLabelText("Short name*")).toBeInTheDocument();
  });

  it("calls setName when name input changes", () => {
    render(<OrganizationFormFields {...defaultProps} />);

    const nameInput = screen.getByTestId("input-field-name");
    fireEvent.change(nameInput, { target: { value: "ACME Corporation" } });

    expect(mockSetName).toHaveBeenCalledWith("ACME Corporation");
  });

  it("calls setShortName when short name input changes", () => {
    render(<OrganizationFormFields {...defaultProps} />);

    const shortNameInput = screen.getByTestId("input-field-shortName");
    fireEvent.change(shortNameInput, { target: { value: "acme" } });

    expect(mockSetShortName).toHaveBeenCalledWith("acme");
  });

  it("displays validation message for invalid short name", () => {
    const props = {
      ...defaultProps,
      shortName: "this-is-a-very-long-short-name-that-exceeds-the-32-character-limit",
    };
    render(<OrganizationFormFields {...props} />);

    expect(screen.getByTestId("validation-shortName")).toBeInTheDocument();
  });

  it("does not display validation for valid inputs", () => {
    const props = {
      ...defaultProps,
      name: "ACME Corp",
      shortName: "acme",
    };
    render(<OrganizationFormFields {...props} />);

    expect(screen.queryByTestId("validation-name")).not.toBeInTheDocument();
    expect(screen.queryByTestId("validation-shortName")).not.toBeInTheDocument();
  });

  it("has correct input types", () => {
    render(<OrganizationFormFields {...defaultProps} />);

    expect(screen.getByTestId("input-field-name")).toHaveAttribute("type", "text");
    expect(screen.getByTestId("input-field-shortName")).toHaveAttribute("type", "text");
  });

  it("has correct name attributes for form submission", () => {
    render(<OrganizationFormFields {...defaultProps} />);

    expect(screen.getByTestId("input-field-name")).toHaveAttribute("name", "name");
    expect(screen.getByTestId("input-field-shortName")).toHaveAttribute("name", "shortName");
  });
});
