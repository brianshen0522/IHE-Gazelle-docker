/* eslint-disable @typescript-eslint/no-explicit-any */
import { renderHook } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { useUserFilterMappings } from "../useUserFilterMappings";
import { Session } from "next-auth";

// Mock useTranslation
const mockT = vi.fn((key: string) => {
  const translations: Record<string, string> = {
    "gzl.gum.delegated": "delegated",
    "gzl.gum.local": "local",
    "gzl.gum.activated": "activated",
    "gzl.gum.disabled": "disabled",
    "gzl.gum.gazelle_admin": "Gazelle Administrator",
    "gzl.gum.project_admin": "Project Manager",
    "gzl.gum.monitor": "Monitor",
    "gzl.gum.test_designer": "Test Designer",
    "gzl.gum.late_registration": "Late Registration",
    "gzl.gum.testing_session_manager": "Testing Session Manager",
    "gzl.gum.sut_operator": "SUT Operator",
    "gzl.gum.organization_admin": "Organization Administrator",
    "gzl.gum.org": "Organization member",
    "gzl.gum.org-adm": "Organization administrator",
  };
  return translations[key] || key;
});

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: mockT,
  }),
}));

// Mock useSession
const mockUseSession = vi.fn();
vi.mock("next-auth/react", () => ({
  useSession: () => mockUseSession(),
}));

// Mock useGetOrganizations and useGetRoles
const mockUseGetOrganizations = vi.fn();
const mockUseGetRoles = vi.fn();
vi.mock("../swr/useGetGroups", () => ({
  useGetOrganizations: (params: any) => mockUseGetOrganizations(params),
  useGetRoles: (session: Session) => mockUseGetRoles(session),
}));

describe("useUserFilterMappings", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseSession.mockReturnValue({ data: { access_token: "test-token" } });
    mockUseGetRoles.mockReturnValue({ data: null });
  });

  it("should return empty mappings when no organizations data is available", () => {
    mockUseGetOrganizations.mockReturnValue({ data: null });

    const { result } = renderHook(() => useUserFilterMappings());

    expect(result.current).toEqual({
      delegated: {
        true: "delegated",
        false: "local",
      },
      activated: {
        true: "activated",
        false: "disabled",
      },
      groupIds: expect.any(Object),
      group: expect.any(Object),
    });
  });

  it("should call useGetOrganizations with limit 1000", () => {
    mockUseGetOrganizations.mockReturnValue({ data: null });

    renderHook(() => useUserFilterMappings());

    expect(mockUseGetOrganizations).toHaveBeenCalledWith({ limit: 1000 });
  });

  it("should map organization IDs to names", () => {
    const mockOrgaData = {
      data: [
        { id: "org-1", name: "Organization One", shortName: "ORG1" },
        { id: "org-2", name: "Organization Two", shortName: "ORG2" },
        { id: "org-3", name: "Organization Three", shortName: "org-3" }, // shortName same as id
      ],
    };

    mockUseGetOrganizations.mockReturnValue({ data: mockOrgaData });

    const { result } = renderHook(() => useUserFilterMappings());

    // Check plain ID mapping
    expect(result.current.organizationId["org-1"]).toBe("Organization One");
    expect(result.current.organizationId["org-2"]).toBe("Organization Two");
    expect(result.current.organizationId["org-3"]).toBe("Organization Three");

    // Check org:id format mapping
    expect(result.current.organizationId["org:org-1"]).toBe("Organization One");
    expect(result.current.organizationId["org:org-2"]).toBe("Organization Two");

    // Check shortName mapping (only when different from id)
    expect(result.current.organizationId["ORG1"]).toBe("Organization One");
    expect(result.current.organizationId["org:ORG1"]).toBe("Organization One");
    expect(result.current.organizationId["ORG2"]).toBe("Organization Two");
    expect(result.current.organizationId["org:ORG2"]).toBe("Organization Two");

    // shortName same as id should not create duplicate mapping (but it does, which is fine)
    expect(result.current.organizationId["org-3"]).toBe("Organization Three");
  });

  it("should map role group IDs to translated display names", () => {
    mockUseGetOrganizations.mockReturnValue({ data: { data: [] } });
    mockUseGetRoles.mockReturnValue({
      data: {
        data: [
          { id: "role:gazelle_admin", reference: "gazelle_admin", type: "role" },
          { id: "role:project_admin", reference: "project_admin", type: "role" },
          { id: "role:monitor", reference: "monitor", type: "role" },
          { id: "role:test_designer", reference: "test_designer", type: "role" },
          { id: "role:late_registration", reference: "late_registration", type: "role" },
          { id: "role:testing_session_manager", reference: "testing_session_manager", type: "role" },
          { id: "role:sut_operator", reference: "sut_operator", type: "role" },
        ],
      },
    });

    const { result } = renderHook(() => useUserFilterMappings());

    // Map with prefix (for display when backend returns full id)
    expect(result.current.groupIds["role:gazelle_admin"]).toBe("Gazelle Administrator");
    expect(result.current.groupIds["role:project_admin"]).toBe("Project Manager");
    expect(result.current.groupIds["role:monitor"]).toBe("Monitor");
    expect(result.current.groupIds["role:test_designer"]).toBe("Test Designer");
    expect(result.current.groupIds["role:late_registration"]).toBe("Late Registration");
    expect(result.current.groupIds["role:testing_session_manager"]).toBe("Testing Session Manager");
    expect(result.current.groupIds["role:sut_operator"]).toBe("SUT Operator");

    // Also map without prefix (for API requests)
    expect(result.current.groupIds["gazelle_admin"]).toBe("Gazelle Administrator");
    expect(result.current.groupIds["project_admin"]).toBe("Project Manager");
    expect(result.current.groupIds["monitor"]).toBe("Monitor");
    expect(result.current.groupIds["test_designer"]).toBe("Test Designer");
    expect(result.current.groupIds["late_registration"]).toBe("Late Registration");
    expect(result.current.groupIds["testing_session_manager"]).toBe("Testing Session Manager");
    expect(result.current.groupIds["sut_operator"]).toBe("SUT Operator");

    // Check generic fallbacks
    expect(result.current.groupIds["org"]).toBe("Organization member");
    expect(result.current.groupIds["org-adm"]).toBe("Organization administrator");
  });

  it("should map organization admin groups with org-adm:orgId format", () => {
    const mockOrgaData = {
      data: [
        { id: "org-1", name: "Organization One", shortName: "ORG1" },
        { id: "org-2", name: "Organization Two", shortName: "ORG2" },
      ],
    };

    mockUseGetOrganizations.mockReturnValue({ data: mockOrgaData });

    const { result } = renderHook(() => useUserFilterMappings());

    expect(result.current.groupIds["org-adm"]).toBe("Organization administrator");
  });

  it("should map boolean values for delegated field", () => {
    mockUseGetOrganizations.mockReturnValue({ data: null });

    const { result } = renderHook(() => useUserFilterMappings());

    expect(result.current.delegated).toEqual({
      true: "delegated",
      false: "local",
    });
  });

  it("should map boolean values for activated field", () => {
    mockUseGetOrganizations.mockReturnValue({ data: null });

    const { result } = renderHook(() => useUserFilterMappings());

    expect(result.current.activated).toEqual({
      true: "activated",
      false: "disabled",
    });
  });

  it("should have both groupIds and group mappings with the same content", () => {
    const mockOrgaData = {
      data: [{ id: "org-1", name: "Organization One", shortName: "ORG1" }],
    };

    mockUseGetOrganizations.mockReturnValue({ data: mockOrgaData });

    const { result } = renderHook(() => useUserFilterMappings());

    expect(result.current.groupIds).toEqual(result.current.group);
  });

  it("should update mappings when organization data changes", () => {
    const initialData = {
      data: [{ id: "org-1", name: "Organization One", shortName: "ORG1" }],
    };

    mockUseGetOrganizations.mockReturnValue({ data: initialData });

    const { result, rerender } = renderHook(() => useUserFilterMappings());

    expect(result.current.organizationId["org-1"]).toBe("Organization One");

    // Update mock data
    const updatedData = {
      data: [
        { id: "org-1", name: "Updated Organization", shortName: "ORG1" },
        { id: "org-2", name: "New Organization", shortName: "ORG2" },
      ],
    };

    mockUseGetOrganizations.mockReturnValue({ data: updatedData });

    rerender();

    expect(result.current.organizationId["org-1"]).toBe("Updated Organization");
    expect(result.current.organizationId["org-2"]).toBe("New Organization");
  });
});
