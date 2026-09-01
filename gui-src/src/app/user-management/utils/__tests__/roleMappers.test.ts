/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi } from "vitest";

// Partial mock - keep getRoleDisplayFromGroups real, mock getDisplayGroupFromGroupId
vi.mock("@user-management/utils/roleMappers", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@user-management/utils/roleMappers")>();
  return {
    ...actual,
    getDisplayGroupFromGroupId: vi.fn((groupId: string) => {
      // Use actual role ID format
      const mapping: Record<string, string> = {
        "role:gazelle_admin": "gazelle_admin",
        "role:project_admin": "project_admin",
        "role:monitor": "monitor",
        "role:test_designer": "test_designer",
        "role:late_registration": "late_registration",
        "role:testing_session_manager": "testing_session_manager",
      };
      return mapping[groupId];
    }),
  };
});

// Import after the mock
const { getRoleDisplayFromGroups } = await import("../roleMappers");

describe("getRoleDisplayFromGroups", () => {
  const mockT = vi.fn((key: string) => {
    const translations: Record<string, string> = {
      "gzl.gum.gazelle_admin": "Gazelle Administrator",
      "gzl.gum.project_admin": "Project Manager",
      "gzl.gum.monitor": "Monitor",
      "gzl.gum.test_designer": "Test Designer",
      "gzl.gum.late_registration": "Late Registration",
      "gzl.gum.testing_session_manager": "Testing Session Manager",
      "gzl.gum.custom-role": "Custom Role",
    };
    return translations[key] || key;
  });

  it("returns empty string for undefined groups", () => {
    const result = getRoleDisplayFromGroups(undefined, mockT);
    expect(result).toBe("");
  });

  it("returns empty string for empty array", () => {
    const result = getRoleDisplayFromGroups([], mockT);
    expect(result).toBe("");
  });

  it("formats single role correctly", () => {
    const result = getRoleDisplayFromGroups(["role:gazelle_admin"], mockT);
    expect(result).toBe("Gazelle Administrator");
    expect(mockT).toHaveBeenCalledWith("gzl.gum.gazelle_admin");
  });

  it("formats multiple roles correctly", () => {
    const result = getRoleDisplayFromGroups(["role:gazelle_admin", "role:project_admin"], mockT);
    expect(result).toBe("Gazelle Administrator, Project Manager");
  });

  it("filters out org: prefixed groups", () => {
    const result = getRoleDisplayFromGroups(["role:gazelle_admin", "org:some-org", "role:monitor"], mockT);
    expect(result).toBe("Gazelle Administrator, Monitor");
  });

  it("filters out empty strings", () => {
    const result = getRoleDisplayFromGroups(["role:gazelle_admin", "", "role:monitor"], mockT);
    expect(result).toBe("Gazelle Administrator, Monitor");
  });

  it("filters out undefined values", () => {
    const result = getRoleDisplayFromGroups(["role:gazelle_admin", undefined as any, "role:monitor"], mockT);
    expect(result).toBe("Gazelle Administrator, Monitor");
  });

  it("handles unknown roles by translating them", () => {
    const result = getRoleDisplayFromGroups(["custom-role"], mockT);
    expect(result).toBe("Custom Role");
    expect(mockT).toHaveBeenCalledWith("gzl.gum.custom-role");
  });

  it("handles roles without mapped group IDs", () => {
    const result = getRoleDisplayFromGroups(["unknown-role-id"], mockT);
    expect(result).toBe("gzl.gum.unknown-role-id");
    expect(mockT).toHaveBeenCalledWith("gzl.gum.unknown-role-id");
  });

  it("combines filtering and formatting", () => {
    const groups = ["role:gazelle_admin", "org:org-123", "", "role:project_admin", undefined as any, "role:monitor"];
    const result = getRoleDisplayFromGroups(groups, mockT);
    expect(result).toBe("Gazelle Administrator, Project Manager, Monitor");
  });

  it("calls translation function for each role", () => {
    mockT.mockClear();
    getRoleDisplayFromGroups(["role:gazelle_admin", "role:project_admin", "role:monitor"], mockT);
    expect(mockT).toHaveBeenCalledTimes(3);
    expect(mockT).toHaveBeenCalledWith("gzl.gum.gazelle_admin");
    expect(mockT).toHaveBeenCalledWith("gzl.gum.project_admin");
    expect(mockT).toHaveBeenCalledWith("gzl.gum.monitor");
  });
});
