/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor, act } from "@testing-library/react";
import { useAclUsers } from "../useAclUsers";
import { FormattedMember } from "@/shared/types/AccessControlListTypes";
import { Session } from "next-auth";

// Create hoisted mock functions
const { mockUseGetUserSummary, mockUseSearchGroupsWithResolvedOrganizationName, mockFormatUsers, mockFormatGroups } = vi.hoisted(() => ({
  mockUseGetUserSummary: vi.fn(),
  mockUseSearchGroupsWithResolvedOrganizationName: vi.fn(),
  mockFormatUsers: vi.fn((users) => users || []),
  mockFormatGroups: vi.fn((groups) => groups || []),
}));

// Mock dependencies
vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

vi.mock("@gazelle/gazelle-component-ui", () => ({
  formatUsers: mockFormatUsers,
  formatGroups: mockFormatGroups,
}));

vi.mock("@shared/hooks/SWR/useGetUserSummary", () => ({
  useGetUserSummary: mockUseGetUserSummary,
}));

vi.mock("@message-capture/hooks/useGetGroupsWithOrganizationNames", () => ({
  useSearchGroupsWithResolvedOrganizationName: mockUseSearchGroupsWithResolvedOrganizationName,
}));

vi.mock("@shared/utils/acl/aclFormatters", () => ({
  formatWithTranslation: vi.fn((value) => value),
}));

describe("useAclUsers", () => {
  const mockSession: Session = {
    user: {
      gazelleId: "user-123",
      name: "Test User",
      email: "test@example.com",
      id: "",
      groups: [],
      organization: ""
    },
    access_token: "mock-token",
    expires: "2099-01-01",
    refresh_token: "",
    expires_in: 0,
    id_token: "",
    error: ""
  };

  const mockOwners: FormattedMember[] = [{ id: "user-123", name: "Owner User", organization: "Org 1" }];
  const mockEditors: FormattedMember[] = [{ id: "user-456", name: "Editor User", organization: "Org 2" }];
  const mockViewers: FormattedMember[] = [{ id: "user-789", name: "Viewer User", organization: "Org 3" }];

  beforeEach(() => {
    vi.clearAllMocks();

    // Default mock implementations
    mockUseGetUserSummary.mockReturnValue({
      data: { users: [] },
      isLoading: false,
      error: null,
      mutate: vi.fn(),
    } as any);

    mockUseSearchGroupsWithResolvedOrganizationName.mockReturnValue({
      data: { resolvedData: [] },
      isLoading: false,
      error: null,
      mutate: vi.fn(),
    } as any);
  });

  it("should initialize with empty arrays when no data", () => {
    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    expect(result.current.availableUsers).toEqual([]);
    expect(result.current.availableGroups).toEqual([]);
    expect(result.current.offset).toBe(0);
    expect(result.current.searchTerm).toBe("");
  });

  it("should fetch users when enabled", () => {
    renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    expect(mockUseGetUserSummary).toHaveBeenCalledWith({
      offset: 0,
      limit: 100,
      search: "",
      enabled: true,
    });
  });

  it("should not fetch users when disabled", () => {
    renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: false,
      }),
    );

    expect(mockUseGetUserSummary).toHaveBeenCalledWith({
      offset: 0,
      limit: 100,
      search: "",
      enabled: false,
    });
  });

  it("should filter out existing members from available users", () => {
    const mockUsers = [
      { id: "user-123", name: "Owner User" }, // Already an owner
      { id: "user-456", name: "Editor User" }, // Already an editor
      { id: "user-999", name: "New User" }, // Not in ACL
    ];

    mockUseGetUserSummary.mockReturnValue({
      data: { users: mockUsers },
      isLoading: false,
      error: null,
      mutate: vi.fn(),
    } as any);

    mockFormatUsers.mockReturnValue(mockUsers);

    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    expect(result.current.availableUsers).toHaveLength(1);
    expect(result.current.availableUsers[0].id).toBe("user-999");
  });

  it("should filter out existing members from available groups", () => {
    const mockGroups = [
      { id: "user-123", name: "Owner Group" }, // Already an owner
      { id: "group-999", name: "New Group" }, // Not in ACL
    ];

    mockUseSearchGroupsWithResolvedOrganizationName.mockReturnValue({
      data: { resolvedData: mockGroups },
      isLoading: false,
      error: null,
      mutate: vi.fn(),
    } as any);

    mockFormatGroups.mockReturnValue(mockGroups);

    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    expect(result.current.availableGroups).toHaveLength(1);
    expect(result.current.availableGroups[0].id).toBe("group-999");
  });

  it("should filter out users with null or undefined id", () => {
    const mockUsers = [
      { id: "user-999", name: "Valid User" },
      { id: null, name: "Invalid User 1" },
      { id: undefined, name: "Invalid User 2" },
      { name: "Invalid User 3" }, // Missing id
    ];

    mockUseGetUserSummary.mockReturnValue({
      data: { users: mockUsers },
      isLoading: false,
      error: null,
      mutate: vi.fn(),
    } as any);

    mockFormatUsers.mockReturnValue(mockUsers as any);

    const { result } = renderHook(() =>
      useAclUsers({
        owners: [],
        editors: [],
        viewers: [],
        session: mockSession,
        enabled: true,
      }),
    );

    expect(result.current.availableUsers).toHaveLength(1);
    expect(result.current.availableUsers[0].id).toBe("user-999");
  });

  it("should update offset when setOffset is called", async () => {
    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    expect(result.current.offset).toBe(0);

    act(() => {
      result.current.setOffset(10);
    });

    await waitFor(() => {
      expect(result.current.offset).toBe(10);
    });
  });

  it("should update offset with function callback", async () => {
    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    act(() => {
      result.current.setOffset((prev) => prev + 10);
    });

    await waitFor(() => {
      expect(result.current.offset).toBe(10);
    });
  });

  it("should update searchTerm when setSearchTerm is called", async () => {
    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    expect(result.current.searchTerm).toBe("");

    act(() => {
      result.current.setSearchTerm("test search");
    });

    await waitFor(() => {
      expect(result.current.searchTerm).toBe("test search");
    });
  });

  it("should return isLoading true when users are loading", () => {
    mockUseGetUserSummary.mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
      mutate: vi.fn(),
    } as any);

    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    expect(result.current.isLoading).toBe(true);
  });

  it("should return isLoading true when groups are loading", () => {
    mockUseSearchGroupsWithResolvedOrganizationName.mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
      mutate: vi.fn(),
    } as any);

    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    expect(result.current.isLoading).toBe(true);
  });

  it("should return isLoading false when both users and groups are loaded", () => {
    mockUseGetUserSummary.mockReturnValue({
      data: { users: [] },
      isLoading: false,
      error: null,
      mutate: vi.fn(),
    } as any);

    mockUseSearchGroupsWithResolvedOrganizationName.mockReturnValue({
      data: { resolvedData: [] },
      isLoading: false,
      error: null,
      mutate: vi.fn(),
    } as any);

    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    expect(result.current.isLoading).toBe(false);
  });

  it("should handle search term in user query", async () => {
    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    act(() => {
      result.current.setSearchTerm("john");
    });

    await waitFor(() => {
      expect(mockUseGetUserSummary).toHaveBeenCalledWith(
        expect.objectContaining({
          search: "john",
        }),
      );
    });
  });

  it("should handle pagination offset in queries", async () => {
    const { result } = renderHook(() =>
      useAclUsers({
        owners: mockOwners,
        editors: mockEditors,
        viewers: mockViewers,
        session: mockSession,
        enabled: true,
      }),
    );

    act(() => {
      result.current.setOffset(20);
    });

    await waitFor(() => {
      expect(mockUseGetUserSummary).toHaveBeenCalledWith(
        expect.objectContaining({
          offset: 20,
        }),
      );
    });
  });

  it("should handle empty owners, editors, and viewers", () => {
    const mockUsers = [
      { id: "user-1", name: "User 1" },
      { id: "user-2", name: "User 2" },
    ];

    mockUseGetUserSummary.mockReturnValue({
      data: { users: mockUsers },
      isLoading: false,
      error: null,
      mutate: vi.fn(),
    } as any);

    mockFormatUsers.mockReturnValue(mockUsers);

    const { result } = renderHook(() =>
      useAclUsers({
        owners: [],
        editors: [],
        viewers: [],
        session: mockSession,
        enabled: true,
      }),
    );

    // All users should be available since no one has roles
    expect(result.current.availableUsers).toHaveLength(2);
  });
});
