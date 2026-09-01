import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import { OrganizationCell } from "./OrganizationCell";

// Mock the hook
const mockUseGetOrganizationFromId = vi.fn();

vi.mock("@/shared/hooks/useGetUserInformation", () => ({
  useGetOrganizationFromId: (id: string) => mockUseGetOrganizationFromId(id),
}));

describe("OrganizationCell", () => {
  const orgaMapping = new Map<string, string>([
    ["org-1", "Organization One"],
    ["org-2", "Organization Two"],
  ]);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders organization name from mapping (old orga)", () => {
    // When the org is in the mapping, the hook should be called with empty string
    mockUseGetOrganizationFromId.mockReturnValue({
      data: null,
      isError: false,
      isLoading: false,
    });

    render(<OrganizationCell organizationId="org-1" orgaMapping={orgaMapping} />);

    const cell = screen.getByText("Organization One");
    expect(cell).toBeInTheDocument();
    expect(cell).toHaveAttribute("title", "Organization One");
    expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("");
  });

  it("fetches organization name from hook (new orga)", () => {
    mockUseGetOrganizationFromId.mockReturnValue({
      data: { data: { name: "New Organization", id: "org-new" } },
      isError: false,
      isLoading: false,
    });

    render(<OrganizationCell organizationId="org-new" orgaMapping={orgaMapping} />);

    const cell = screen.getByText("New Organization");
    expect(cell).toBeInTheDocument();
    expect(cell).toHaveAttribute("title", "New Organization");
    expect(mockUseGetOrganizationFromId).toHaveBeenCalledWith("org-new");
  });

  it("falls back to organizationId when name is not available", () => {
    mockUseGetOrganizationFromId.mockReturnValue({
      data: null,
      isError: false,
      isLoading: false,
    });

    render(<OrganizationCell organizationId="org-unknown" orgaMapping={orgaMapping} />);

    const cell = screen.getByText("org-unknown");
    expect(cell).toBeInTheDocument();
    expect(cell).toHaveAttribute("title", "org-unknown");
  });

  it("displays tooltip with organization name", () => {
    mockUseGetOrganizationFromId.mockReturnValue({
      data: null,
      isError: false,
      isLoading: false,
    });

    render(<OrganizationCell organizationId="org-2" orgaMapping={orgaMapping} />);

    const cell = screen.getByText("Organization Two");
    expect(cell).toHaveAttribute("title", "Organization Two");
  });

  it("handles loading state gracefully", () => {
    mockUseGetOrganizationFromId.mockReturnValue({
      data: null,
      isError: false,
      isLoading: true,
    });

    render(<OrganizationCell organizationId="org-loading" orgaMapping={orgaMapping} />);

    const cell = screen.getByText("org-loading");
    expect(cell).toBeInTheDocument();
  });
});
