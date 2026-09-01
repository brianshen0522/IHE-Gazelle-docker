import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { RolesCell } from "./RolesCell";

// Mock the role formatter utility
vi.mock("@user-management/components/users-list/utils/roleFormatters", () => ({
  getRoleDisplayFromGroups: vi.fn((groupIds: string[], t: (key: string) => string) => {
    if (!groupIds || groupIds.length === 0) return "";
    return groupIds.map((id) => t(`gzl.gum.${id}`)).join(", ");
  }),
}));

describe("RolesCell", () => {
  const mockT = vi.fn((key: string) => {
    const translations: Record<string, string> = {
      "gzl.gum.admin": "Administrator",
      "gzl.gum.user": "User",
      "gzl.gum.viewer": "Viewer",
    };
    return translations[key] || key;
  });

  it("renders roles with italic styling", () => {
    const { container } = render(<RolesCell groupIds={["admin", "user"]} t={mockT} />);

    const span = container.querySelector("span.italic");
    expect(span).toBeInTheDocument();
  });

  it("displays formatted roles", () => {
    render(<RolesCell groupIds={["admin", "user"]} t={mockT} />);

    const cell = screen.getByText("Administrator, User");
    expect(cell).toBeInTheDocument();
  });

  it("displays tooltip with roles", () => {
    render(<RolesCell groupIds={["admin", "viewer"]} t={mockT} />);

    const cell = screen.getByText("Administrator, Viewer");
    expect(cell).toHaveAttribute("title", "Administrator, Viewer");
  });

  it("handles empty groupIds", () => {
    render(<RolesCell groupIds={[]} t={mockT} />);

    const { container } = render(<RolesCell groupIds={[]} t={mockT} />);
    const span = container.querySelector("span.italic");
    expect(span).toBeInTheDocument();
    expect(span).toHaveTextContent("");
  });

  it("handles single role", () => {
    render(<RolesCell groupIds={["admin"]} t={mockT} />);

    const cell = screen.getByText("Administrator");
    expect(cell).toBeInTheDocument();
    expect(cell).toHaveAttribute("title", "Administrator");
  });

  it("passes correct translation function", () => {
    const customT = vi.fn((key: string) => `translated-${key}`);
    render(<RolesCell groupIds={["role1"]} t={customT} />);

    expect(customT).toHaveBeenCalled();
  });
});
