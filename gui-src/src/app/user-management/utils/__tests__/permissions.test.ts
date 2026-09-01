import { describe, it, expect } from "vitest";
import { Session } from "next-auth";
import {
  canCreateUser,
  canManageUsers,
  canManageOrganizations,
  canEditOrganization,
  isGazelleAdmin,
  isOnlyOrgaAdmin,
  cantDesignTests,
  canEditRole,
  GAZELLE_ADMIN,
  PROJECT_ADMIN,
  TESTING_SESSION_MANAGER,
  MONITOR,
  TEST_DESIGNER,
  LATE_REGISTRATION,
  SUT_OPERATOR,
} from "../permissions";

const createSession = (groups: string[]) =>
  ({
    user: {
      gazelleId: "user-123",
      id: "user-123",
      organization: "org-123",
      email: "test@example.com",
      name: "Test User",
      groups,
    },
    expires: "2026-12-31",
  }) as unknown as Session;

describe("User Management Permissions", () => {
  describe("canCreateUser", () => {
    it("returns true for Gazelle Admin", () => {
      const session = {
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

      expect(canCreateUser(session)).toBe(true);
    });

    it("returns true for Project Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:project_admin"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canCreateUser(session)).toBe(true);
    });

    it("returns true for Testing Session Manager", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:testing_session_manager"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canCreateUser(session)).toBe(true);
    });

    it("returns true for Organization Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["org-adm:org-123"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canCreateUser(session)).toBe(true);
    });

    it("returns false for Monitor", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:monitor"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canCreateUser(session)).toBe(false);
    });

    it("returns false for null session", () => {
      expect(canCreateUser(null)).toBe(false);
    });
  });

  describe("canManageUsers", () => {
    it("returns true for Gazelle Admin", () => {
      const session = {
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

      expect(canManageUsers(session)).toBe(true);
    });

    it("returns true for Project Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:project_admin"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canManageUsers(session)).toBe(true);
    });

    it("returns true for Testing Session Manager", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:testing_session_manager"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canManageUsers(session)).toBe(true);
    });

    it("returns true for Organization Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["org-adm:org-123"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canManageUsers(session)).toBe(true);
    });

    it("returns false for Monitor", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:monitor"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canManageUsers(session)).toBe(false);
    });

    it("returns false for null session", () => {
      expect(canManageUsers(null)).toBe(false);
    });
  });

  describe("canManageOrganizations", () => {
    it("returns true for Gazelle Admin", () => {
      const session = {
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

      expect(canManageOrganizations(session)).toBe(true);
    });

    it("returns true for Project Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:project_admin"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canManageOrganizations(session)).toBe(true);
    });

    it("returns true for Testing Session Manager", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:testing_session_manager"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canManageOrganizations(session)).toBe(true);
    });

    it("returns false for Organization Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["org-adm:org-123"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canManageOrganizations(session)).toBe(false);
    });

    it("returns false for Monitor", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:monitor"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canManageOrganizations(session)).toBe(false);
    });

    it("returns false for null session", () => {
      expect(canManageOrganizations(null)).toBe(false);
    });

    it("returns true when user has multiple roles including Project Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:project_admin", "role:monitor", "org-adm:org-123"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canManageOrganizations(session)).toBe(true);
    });
  });

  describe("canEditOrganization", () => {
    it("returns true for Organization Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["org-adm:org-123"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canEditOrganization(session)).toBe(true);
    });

    it("returns false for Gazelle Admin", () => {
      const session = {
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

      expect(canEditOrganization(session)).toBe(false);
    });

    it("returns false for Project Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:project_admin"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canEditOrganization(session)).toBe(false);
    });

    it("returns false for Testing Session Manager", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:testing_session_manager"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canEditOrganization(session)).toBe(false);
    });

    it("returns false for Monitor", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:monitor"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canEditOrganization(session)).toBe(false);
    });

    it("returns false for null session", () => {
      expect(canEditOrganization(null)).toBe(false);
    });

    it("returns true when user has org admin for multiple organizations", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["org-adm:org-123", "org-adm:org-456"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(canEditOrganization(session)).toBe(true);
    });
  });

  describe("isGazelleAdmin", () => {
    it("returns true for Gazelle Admin", () => {
      const session = {
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

      expect(isGazelleAdmin(session)).toBe(true);
    });

    it("returns false for non-admin users", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:project_admin"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(isGazelleAdmin(session)).toBe(false);
    });

    it("returns false for null session", () => {
      expect(isGazelleAdmin(null)).toBe(false);
    });
  });

  describe("isOnlyOrgaAdmin", () => {
    it("returns true for only Organization Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["org-adm:org-123"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(isOnlyOrgaAdmin(session)).toBe(true);
    });

    it("returns false for Gazelle Admin with Org Admin", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:gazelle_admin", "org-adm:org-123"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(isOnlyOrgaAdmin(session)).toBe(false);
    });

    it("returns false for users without Org Admin role", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:monitor"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(isOnlyOrgaAdmin(session)).toBe(false);
    });

    it("returns false for null session", () => {
      expect(isOnlyOrgaAdmin(null)).toBe(false);
    });
  });

  describe("cantDesignTests", () => {
    it("returns false for Gazelle Admin (can design)", () => {
      const session = {
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

      expect(cantDesignTests(session)).toBe(false);
    });

    it("returns false for Test Designer (can design)", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:test_designer"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(cantDesignTests(session)).toBe(false);
    });

    it("returns false for Testing Session Manager (can design)", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:testing_session_manager"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(cantDesignTests(session)).toBe(false);
    });

    it("returns true for Monitor (cannot design)", () => {
      const session = {
        user: {
          gazelleId: "user-123",
          id: "user-123",
          organization: "org-123",
          email: "test@example.com",
          name: "Test User",
          groups: ["role:monitor"],
        },
        expires: "2026-12-31",
      } as unknown as Session;

      expect(cantDesignTests(session)).toBe(true);
    });

    it("returns true for null session", () => {
      expect(cantDesignTests(null)).toBe(true);
    });
  });

  describe("canEditRole", () => {
    it("returns false for delegated users", () => {
      const session = createSession([GAZELLE_ADMIN]);

      expect(canEditRole(PROJECT_ADMIN,  false, true, session)).toBe(false);
    });

    it("allows Gazelle Admin to edit every non-gazelle role on another user", () => {
      const session = createSession([GAZELLE_ADMIN]);

      expect(canEditRole(GAZELLE_ADMIN,  false, false, session)).toBe(true);
      expect(canEditRole(PROJECT_ADMIN,  false, false, session)).toBe(true);
      expect(canEditRole(TESTING_SESSION_MANAGER,  false, false, session)).toBe(true);
      expect(canEditRole(MONITOR,  false, false, session)).toBe(true);
      expect(canEditRole(TEST_DESIGNER,  false, false, session)).toBe(true);
      expect(canEditRole(LATE_REGISTRATION,  false, false, session)).toBe(true);
      expect(canEditRole(SUT_OPERATOR,  false, false, session)).toBe(true);
      expect(canEditRole("org-adm:org-123",  false, false, session)).toBe(true);
    });

    it("prevents Gazelle Admin from editing its own gazelle admin role", () => {
      const session = createSession([GAZELLE_ADMIN]);

      expect(canEditRole(GAZELLE_ADMIN,  true, false, session)).toBe(false);
      expect(canEditRole(PROJECT_ADMIN,  true, false, session)).toBe(true);
    });

    it("allows Project Admin to edit every role except gazelle admin and project admin", () => {
      const session = createSession([PROJECT_ADMIN]);

      expect(canEditRole(GAZELLE_ADMIN,  false, false, session)).toBe(false);
      expect(canEditRole(PROJECT_ADMIN,  false, false, session)).toBe(false);
      expect(canEditRole(TESTING_SESSION_MANAGER,  false, false, session)).toBe(true);
      expect(canEditRole(MONITOR,  false, false, session)).toBe(true);
      expect(canEditRole(TEST_DESIGNER, false, false, session)).toBe(true);
      expect(canEditRole(LATE_REGISTRATION, false, false, session)).toBe(true);
      expect(canEditRole(SUT_OPERATOR, false, false, session)).toBe(true);
      expect(canEditRole("org-adm:org-123", false, false, session)).toBe(true);
    });

    it("allows Testing Session Manager to edit every role except gazelle admin, project admin and tsm", () => {
      const session = createSession([TESTING_SESSION_MANAGER]);

      expect(canEditRole(GAZELLE_ADMIN,  false, false, session)).toBe(false);
      expect(canEditRole(PROJECT_ADMIN,  false, false, session)).toBe(false);
      expect(canEditRole(TESTING_SESSION_MANAGER,  false, false, session)).toBe(false);
      expect(canEditRole(MONITOR,  false, false, session)).toBe(true);
      expect(canEditRole(TEST_DESIGNER,  false, false, session)).toBe(true);
      expect(canEditRole(LATE_REGISTRATION,  false, false, session)).toBe(true);
      expect(canEditRole(SUT_OPERATOR,  false, false, session)).toBe(true);
      expect(canEditRole("org-adm:org-123",  false, false, session)).toBe(true);
    });

    it("allows Organization Admin to edit sut operator and org admin roles on another user only", () => {
      const session = createSession(["org-adm:org-123"]);

      expect(canEditRole(SUT_OPERATOR,  false, false, session)).toBe(true);
      expect(canEditRole("org-adm:org-123",  false, false, session)).toBe(true);
      expect(canEditRole("org-adm:org-456",  false, false, session)).toBe(true);
      expect(canEditRole(MONITOR,  false, false, session)).toBe(false);
      expect(canEditRole("org:org-123",  false, false, session)).toBe(false);
      expect(canEditRole(SUT_OPERATOR,  false, true, session)).toBe(false);
    });

    it("returns false for users without admin roles", () => {
      const session = createSession([MONITOR]);

      expect(canEditRole(SUT_OPERATOR,  false, false, session)).toBe(false);
    });
  });
});
