/* eslint-disable @typescript-eslint/no-explicit-any */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import UserManagementHeader from "../UserManagementHeader";
import { Session } from "next-auth";

// Mock child components
vi.mock("@/shared/components/layout/ContentHeaderWrapper", () => ({
  default: ({ id, title, breadcrumbs }: any) => (
    <div data-testid="content-header" data-id={id} data-title={title}>
      {title}
      {breadcrumbs && (
        <div data-testid="breadcrumbs">
          {breadcrumbs.map((bc: any) => (
            <span key={id} data-url={bc.url}>
              {bc.label}
            </span>
          ))}
        </div>
      )}
    </div>
  ),
}));

vi.mock("../UsersTabs", () => ({
  default: () => <div data-testid="users-tabs">UsersTabs</div>,
}));

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

describe("UserManagementHeader", () => {
  it("renders ContentHeaderWrapper with correct props", () => {
    render(<UserManagementHeader id="test-header" title="gzl.user.interface.user_management" session={mockSession} />);

    const header = screen.getByTestId("content-header");
    expect(header).toBeInTheDocument();
    expect(header).toHaveAttribute("data-id", "test-header");
    expect(header).toHaveAttribute("data-title", "User management");
  });

  it("renders UsersTabs component", () => {
    render(<UserManagementHeader id="test-header" title="gzl.user.interface.user_management" session={mockSession} />);

    expect(screen.getByTestId("users-tabs")).toBeInTheDocument();
  });

  it("passes custom breadcrumbs to ContentHeaderWrapper when provided", () => {
    const customBreadcrumbs = [
      { label: "Home", url: "/home" },
      { label: "User Management", url: "" },
      { label: "Users", url: "" },
    ];

    render(
      <UserManagementHeader id="test-header" title="gzl.user.interface.user_management" breadcrumbs={customBreadcrumbs} session={mockSession} />,
    );

    const breadcrumbs = screen.getByTestId("breadcrumbs");
    expect(breadcrumbs).toBeInTheDocument();

    const spans = breadcrumbs.querySelectorAll("span");
    expect(spans).toHaveLength(3);
    expect(spans[0]).toHaveTextContent("Home");
    expect(spans[0]).toHaveAttribute("data-url", "/home");
    expect(spans[1]).toHaveTextContent("User Management");
    expect(spans[1]).toHaveAttribute("data-url", "");
    expect(spans[2]).toHaveTextContent("Users");
    expect(spans[2]).toHaveAttribute("data-url", "");
  });

  it("does not pass breadcrumbs when not provided", () => {
    render(<UserManagementHeader id="test-header" title="gzl.user.interface.user_management" session={mockSession} />);

    expect(screen.queryByTestId("breadcrumbs")).not.toBeInTheDocument();
  });

  it("wraps content in div with correct className", () => {
    const { container } = render(<UserManagementHeader id="test-header" title="gzl.user.interface.user_management" session={mockSession} />);

    const wrapper = container.querySelector(".px-6");
    expect(wrapper).toBeInTheDocument();
  });

  it("translates title using t function", () => {
    render(<UserManagementHeader id="test-header" title="gzl.user.interface.user_management" session={mockSession} />);

    const header = screen.getByTestId("content-header");
    expect(header).toHaveTextContent("User management");
  });
});
