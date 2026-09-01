/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import UsersTabs from "../UsersTabs";
import { Session } from "next-auth";

// Mock next/navigation
const mockPush = vi.fn();
const mockPathname = vi.fn();
vi.mock("next/navigation", () => ({
  usePathname: () => mockPathname(),
}));

// Mock useUnsavedChanges hook
vi.mock("@/shared/context/UnsavedChangeContext", () => ({
  useUnsavedChanges: () => ({
    hasUnsavedChanges: false,
    setHasUnsavedChanges: vi.fn(),
    handleNavigation: vi.fn(),
    router: {
      push: mockPush,
      replace: vi.fn(),
      back: vi.fn(),
      forward: vi.fn(),
      refresh: vi.fn(),
      prefetch: vi.fn(),
    },
  }),
}));

// Mock permissions
const { mockCanCreateUser, mockCanManageOrganizations, mockCanEditOrganization } = vi.hoisted(() => ({
  mockCanCreateUser: vi.fn(),
  mockCanManageOrganizations: vi.fn(),
  mockCanEditOrganization: vi.fn(),
}));

vi.mock("@user-management/utils/permissions", () => ({
  canCreateUser: mockCanCreateUser,
  canManageOrganizations: mockCanManageOrganizations,
  canEditOrganization: mockCanEditOrganization,
}));

// Mock Tabs component
vi.mock("@gazelle/gazelle-component-ui", () => ({
  Tabs: ({ tabNames, onTabSelect, selectedTab }: any) =>
    React.createElement(
      "div",
      { "data-testid": "tabs" },
      tabNames.map((name: string) =>
        React.createElement(
          "button",
          {
            key: name,
            onClick: () => onTabSelect(name),
            "data-selected": selectedTab === name,
            "data-testid": `tab-${name}`,
          },
          name,
        ),
      ),
    ),
}));

describe("UsersTabs", () => {
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
    mockPathname.mockReturnValue("/user-management/users");
    // Set default permission mocks
    mockCanManageOrganizations.mockReturnValue(true);
    mockCanEditOrganization.mockReturnValue(true);
  });

  it("renders tabs component", () => {
    mockCanCreateUser.mockReturnValue(false);
    render(<UsersTabs session={null} />);
    expect(screen.getByTestId("tabs")).toBeInTheDocument();
  });

  it("renders all tabs when user has create permission", () => {
    mockCanCreateUser.mockReturnValue(true);
    render(<UsersTabs session={mockSession} />);

    expect(screen.getByTestId("tab-Users")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Create user")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Organizations")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Create organization")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Edit my organization")).toBeInTheDocument();
  });

  it("hides create user tab when user lacks permission", () => {
    mockCanCreateUser.mockReturnValue(false);
    render(<UsersTabs session={mockSession} />);

    expect(screen.getByTestId("tab-Users")).toBeInTheDocument();
    expect(screen.queryByTestId("tab-Create user")).not.toBeInTheDocument();
    expect(screen.getByTestId("tab-Organizations")).toBeInTheDocument();
  });

  it("hides create user tab when session is null", () => {
    mockCanCreateUser.mockReturnValue(false);
    render(<UsersTabs session={null} />);

    expect(screen.getByTestId("tab-Users")).toBeInTheDocument();
    expect(screen.queryByTestId("tab-Create user")).not.toBeInTheDocument();
  });

  it("selects Users tab when pathname is /user-management/users", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockPathname.mockReturnValue("/user-management/users");
    render(<UsersTabs session={mockSession} />);

    const usersTab = screen.getByTestId("tab-Users");
    expect(usersTab).toHaveAttribute("data-selected", "true");
  });

  it("selects Edit Organization tab when pathname is /user-management/organization/edit", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockPathname.mockReturnValue("/user-management/organization/edit");
    render(<UsersTabs session={mockSession} />);

    const editOrgTab = screen.getByTestId("tab-Edit my organization");
    expect(editOrgTab).toHaveAttribute("data-selected", "true");
  });

  it("selects Create Organization tab when pathname is /user-management/organization/create", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockPathname.mockReturnValue("/user-management/organization/create");
    render(<UsersTabs session={mockSession} />);

    const createOrgTab = screen.getByTestId("tab-Create organization");
    expect(createOrgTab).toHaveAttribute("data-selected", "true");
  });

  it("navigates to users path when Users tab is clicked", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockPathname.mockReturnValue("/user-management/organization/edit");
    render(<UsersTabs session={mockSession} />);

    const usersTab = screen.getByTestId("tab-Users");
    fireEvent.click(usersTab);

    expect(mockPush).toHaveBeenCalledWith("/user-management/users");
  });

  it("navigates to create user path when Create User tab is clicked", () => {
    mockCanCreateUser.mockReturnValue(true);
    mockPathname.mockReturnValue("/user-management/users");
    render(<UsersTabs session={mockSession} />);

    const createUserTab = screen.getByTestId("tab-Create user");
    fireEvent.click(createUserTab);

    expect(mockPush).toHaveBeenCalledWith("/user-management/user/create");
  });

  it("navigates to organizations list path when Organizations tab is clicked", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockPathname.mockReturnValue("/user-management/users");
    render(<UsersTabs session={mockSession} />);

    const listOrgTab = screen.getByTestId("tab-Organizations");
    fireEvent.click(listOrgTab);

    expect(mockPush).toHaveBeenCalledWith("/user-management/organization/list");
  });

  it("navigates to edit organization path when Edit Organization tab is clicked", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockPathname.mockReturnValue("/user-management/users");
    render(<UsersTabs session={mockSession} />);

    const editOrgTab = screen.getByTestId("tab-Edit my organization");
    fireEvent.click(editOrgTab);

    expect(mockPush).toHaveBeenCalledWith("/user-management/organization/edit");
  });

  it("navigates to create organization path when Create Organization tab is clicked", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockPathname.mockReturnValue("/user-management/users");
    render(<UsersTabs session={mockSession} />);

    const createOrgTab = screen.getByTestId("tab-Create organization");
    fireEvent.click(createOrgTab);

    expect(mockPush).toHaveBeenCalledWith("/user-management/organization/create");
  });

  it("hides organization tabs when user lacks organization management permission", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockCanManageOrganizations.mockReturnValue(false);
    mockCanEditOrganization.mockReturnValue(false);
    render(<UsersTabs session={mockSession} />);

    expect(screen.getByTestId("tab-Users")).toBeInTheDocument();
    expect(screen.queryByTestId("tab-Organizations")).not.toBeInTheDocument();
    expect(screen.queryByTestId("tab-Create organization")).not.toBeInTheDocument();
    expect(screen.queryByTestId("tab-Edit my organization")).not.toBeInTheDocument();
  });

  it("shows only organization tabs when user can manage organizations but not users", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockCanManageOrganizations.mockReturnValue(true);
    mockCanEditOrganization.mockReturnValue(true);
    render(<UsersTabs session={mockSession} />);

    expect(screen.queryByTestId("tab-Create user")).not.toBeInTheDocument();
    expect(screen.getByTestId("tab-Organizations")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Create organization")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Edit my organization")).toBeInTheDocument();
  });

  it("shows all tabs except create user when user has all permissions except create user", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockCanManageOrganizations.mockReturnValue(true);
    mockCanEditOrganization.mockReturnValue(true);
    render(<UsersTabs session={mockSession} />);

    expect(screen.getByTestId("tab-Users")).toBeInTheDocument();
    expect(screen.queryByTestId("tab-Create user")).not.toBeInTheDocument();
    expect(screen.getByTestId("tab-Organizations")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Create organization")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Edit my organization")).toBeInTheDocument();
  });

  it("shows edit organization tab for org admin but not list/create tabs", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockCanManageOrganizations.mockReturnValue(false);
    mockCanEditOrganization.mockReturnValue(true);
    render(<UsersTabs session={mockSession} />);

    expect(screen.getByTestId("tab-Users")).toBeInTheDocument();
    expect(screen.queryByTestId("tab-Create user")).not.toBeInTheDocument();
    expect(screen.queryByTestId("tab-Organizations")).not.toBeInTheDocument();
    expect(screen.queryByTestId("tab-Create organization")).not.toBeInTheDocument();
    expect(screen.getByTestId("tab-Edit my organization")).toBeInTheDocument();
  });

  it("hides edit organization tab when user lacks edit organization permission", () => {
    mockCanCreateUser.mockReturnValue(false);
    mockCanManageOrganizations.mockReturnValue(true);
    mockCanEditOrganization.mockReturnValue(false);
    render(<UsersTabs session={mockSession} />);

    expect(screen.getByTestId("tab-Users")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Organizations")).toBeInTheDocument();
    expect(screen.getByTestId("tab-Create organization")).toBeInTheDocument();
    expect(screen.queryByTestId("tab-Edit my organization")).not.toBeInTheDocument();
  });
});
