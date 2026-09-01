/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import CreateUser from "../CreateUser";
import { Session } from "next-auth";

// Mock Next.js navigation
const mockPush = vi.fn();
const mockPathname = vi.fn();

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: mockPush,
  }),
  usePathname: () => mockPathname(),
}));

// Mock useTranslation
vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: any) => key,
    i18n: { language: "en" },
  }),
}));

// Mock useUnsavedChanges
vi.mock("@/shared/context/UnsavedChangeContext", () => ({
  useUnsavedChanges: () => ({
    handleNavigation: vi.fn(),
    setUnsavedChanges: vi.fn(),
    hasUnsavedChanges: false,
  }),
}));

// Mock canCreateUser permission
vi.mock("@user-management/utils/permissions", () => ({
  canCreateUser: vi.fn(() => true),
  canManageUsers: vi.fn(() => true),
  canManageOrganizations: vi.fn(() => true),
  canEditOrganization: vi.fn(() => true),
}));

// Mock CreateUserForm
vi.mock("../CreateUserForm", () => ({
  default: () => React.createElement("div", { "data-testid": "create-user-form" }, "Create User Form"),
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
  });

  it("renders UserManagementHeader with title", () => {
    render(<CreateUser session={mockSession} />);
    expect(screen.getAllByText("gzl.user.interface.user_management")[0]).toBeInTheDocument();
  });

  it("renders CreateUserForm", () => {
    render(<CreateUser session={mockSession} />);
    expect(screen.getByTestId("create-user-form")).toBeInTheDocument();
  });

  it("renders with null session", () => {
    render(<CreateUser session={null} />);
    expect(screen.getAllByText("gzl.user.interface.user_management")[0]).toBeInTheDocument();
    expect(screen.getByTestId("create-user-form")).toBeInTheDocument();
  });

  it("renders with proper layout structure", () => {
    const { container } = render(<CreateUser session={mockSession} />);
    const mainContainer = container.querySelector(".flex.flex-col.gap-4.bg-white.p-6.m-8.rounded-lg.shadow-md");
    expect(mainContainer).toBeInTheDocument();
  });
});
