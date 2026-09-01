import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import UsersWrapper from "../UsersWrapper";
import { Session } from "next-auth";

// Mock permissions using vi.hoisted to avoid hoisting issues
const { mockCanManageUsers } = vi.hoisted(() => ({
  mockCanManageUsers: vi.fn(),
}));

vi.mock("@user-management/utils/permissions", () => ({
  canManageUsers: mockCanManageUsers,
}));

// Mock useSearchParamsUrl hook
vi.mock("@/shared/hooks/useSearchParamsUrl", () => ({
  useSearchParamsUrl: () => ({
    searchParameters: new URLSearchParams(),
  }),
}));

// Mock useUserFilterMappings hook
vi.mock("@user-management/hooks/useUserFilterMappings", () => ({
  useUserFilterMappings: () => ({
    organizationId: { "org-1": "Organization One" },
    groupIds: { "role:gazelle_admin": "Gazelle Administrator" },
    group: { "role:gazelle_admin": "Gazelle Administrator" },
    delegated: { true: "Delegated", false: "Local" },
    activated: { true: "Activated", false: "Disabled" },
  }),
}));

// Mock child components
vi.mock("@/app/user-management/components/users-list/Filters", () => ({
  default: () => React.createElement("div", { "data-testid": "filters" }, "Filters"),
}));

vi.mock("@/app/user-management/components/user-management/create/CreateUser", () => ({
  default: () => React.createElement("div", { "data-testid": "create-user" }, "Create User"),
}));

vi.mock("@/app/user-management/components/users-list/UsersList", () => ({
  default: () => React.createElement("div", { "data-testid": "users-list" }, "Users List"),
}));

vi.mock("@/shared/components/auth/Unauthorized", () => ({
  default: () => React.createElement("div", { "data-testid": "unauthorized" }, "Unauthorized"),
}));

vi.mock("@/app/user-management/components/UserManagementHeader", () => ({
  default: () => React.createElement("div", { "data-testid": "user-management-header" }, "Header"),
}));

vi.mock("@/shared/components/filter/GenericFilters", () => ({
  default: () => React.createElement("div", { "data-testid": "generic-filters" }, "Filters"),
}));

vi.mock("react-toastify", () => ({
  ToastContainer: () => React.createElement("div", { "data-testid": "toast-container" }, "Toast"),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  SidePanelProvider: ({ children }: { children: React.ReactNode }) => React.createElement("div", { "data-testid": "side-panel-provider" }, children),
}));

describe("UsersWrapper", () => {
  const mockSession = {
    user: {
      gazelleId: "user-123",
      id: "user-123",
      organization: "org-123",
      email: "test@example.com",
      name: "Test User",
      groups: [],
    },
    expires: "2026-12-31",
  } as unknown as Session;

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders nothing when session is null", () => {
    const { container } = render(<UsersWrapper session={null} />);
    expect(container.querySelector(".flex")).toBeInTheDocument();
    expect(screen.queryByTestId("filters")).not.toBeInTheDocument();
  });

  it("renders Filters when session exists", () => {
    mockCanManageUsers.mockReturnValue(true);

    render(<UsersWrapper session={mockSession} />);
    expect(screen.getByTestId("generic-filters")).toBeInTheDocument();
  });

  it("renders UserManagementHeader when session exists", () => {
    mockCanManageUsers.mockReturnValue(true);

    render(<UsersWrapper session={mockSession} />);
    expect(screen.getByTestId("user-management-header")).toBeInTheDocument();
  });

  it("renders UsersList when user can manage users", () => {
    mockCanManageUsers.mockReturnValue(true);

    render(<UsersWrapper session={mockSession} />);
    expect(screen.getByTestId("users-list")).toBeInTheDocument();
  });

  it("renders Unauthorized when user cannot manage users", () => {
    mockCanManageUsers.mockReturnValue(false);

    render(<UsersWrapper session={mockSession} />);
    expect(screen.getByTestId("unauthorized")).toBeInTheDocument();
    expect(screen.queryByTestId("users-list")).not.toBeInTheDocument();
  });

  it("renders all main components when user can manage users", () => {
    mockCanManageUsers.mockReturnValue(true);

    render(<UsersWrapper session={mockSession} />);
    expect(screen.getByTestId("generic-filters")).toBeInTheDocument();
    expect(screen.getByTestId("user-management-header")).toBeInTheDocument();
    expect(screen.getByTestId("users-list")).toBeInTheDocument();
  });
});
