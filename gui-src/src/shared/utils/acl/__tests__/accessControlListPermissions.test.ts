import { describe, it, expect } from "vitest";
import {
  canReadResource,
  canUpdateResource,
  canDeleteResource,
  canReadOrUpdateResourceACL,
  isAmongstTheOwners,
  isAmongstTheReaders,
  isAmongstTheEditors,
  isAdmin,
  isAuthenticated,
} from "../accessControlListPermissions";
import { Session } from "next-auth";
import { AccessControlList } from "@/shared/types/AccessControlListTypes";

// Mock session data
const mockUser = {
  gazelleId: "user123",
  id: "user123",
  name: "Test User",
  groups: ["group1", "group2"],
  organization: "Test Organization",
  email: "test.user@example.com",
};

const mockAdminUser = {
  gazelleId: "admin123",
  id: "admin123",
  name: "Admin User",
  groups: ["role:gazelle_admin", "group1"],
  organization: "Test Organization",
  email: "admin.user@example.com",
};

const mockSession: Session = {
  user: mockUser,
  expires: "2026-12-31",
  access_token: "",
  refresh_token: "",
  expires_in: 0,
  id_token: "",
  error: "",
};

const mockAdminSession: Session = {
  user: mockAdminUser,
  expires: "2026-12-31",
  access_token: "",
  refresh_token: "",
  expires_in: 0,
  id_token: "",
  error: "",
};

// Mock ACL data
const publicACL: AccessControlList = {
  isPublic: true,
  owners: [],
  editors: [],
  readers: [],
};

const privateACL: AccessControlList = {
  isPublic: false,
  owners: ["owner123"],
  editors: ["editor123"],
  readers: ["reader123"],
};

const aclWithReadAccessKey: AccessControlList = {
  isPublic: false,
  readAccessKey: "validKey123",
  owners: ["owner123"],
  editors: [],
  readers: [],
};

const aclWithUserInReaders: AccessControlList = {
  isPublic: false,
  owners: [],
  editors: [],
  readers: ["user"],
};

const aclWithUserAsOwner: AccessControlList = {
  isPublic: false,
  owners: ["user123"],
  editors: [],
  readers: [],
};

const aclWithUserAsEditor: AccessControlList = {
  isPublic: false,
  owners: [],
  editors: ["user123"],
  readers: [],
};

const aclWithUserAsReader: AccessControlList = {
  isPublic: false,
  owners: [],
  editors: [],
  readers: ["user123"],
};

const aclWithGroupAsOwner: AccessControlList = {
  isPublic: false,
  owners: ["group1"],
  editors: [],
  readers: [],
};

const aclWithGroupAsEditor: AccessControlList = {
  isPublic: false,
  owners: [],
  editors: ["group2"],
  readers: [],
};

const aclWithGroupAsReader: AccessControlList = {
  isPublic: false,
  owners: [],
  editors: [],
  readers: ["group1"],
};

describe("isAuthenticated", () => {
  it("returns true for valid session", () => {
    expect(isAuthenticated(mockSession)).toBe(true);
  });

  it("returns false for null session", () => {
    expect(isAuthenticated(null)).toBe(false);
  });

  it("returns false for undefined session", () => {
    expect(isAuthenticated(undefined)).toBe(false);
  });
});

describe("isAdmin", () => {
  it("returns true for admin user", () => {
    expect(isAdmin(mockAdminSession)).toBe(true);
  });

  it("returns false for non-admin user", () => {
    expect(isAdmin(mockSession)).toBe(false);
  });

  it("returns false for null session", () => {
    expect(isAdmin(null)).toBe(false);
  });

  it("returns false for undefined session", () => {
    expect(isAdmin(undefined)).toBe(false);
  });
});

describe("isAmongstTheOwners", () => {
  it("returns true when user ID is in owners list", () => {
    expect(isAmongstTheOwners(mockSession, aclWithUserAsOwner)).toBe(true);
  });

  it("returns true when user's group is in owners list", () => {
    expect(isAmongstTheOwners(mockSession, aclWithGroupAsOwner)).toBe(true);
  });

  it("returns false when user is not in owners list", () => {
    expect(isAmongstTheOwners(mockSession, privateACL)).toBe(false);
  });

  it("returns false for null session", () => {
    expect(isAmongstTheOwners(null, aclWithUserAsOwner)).toBe(false);
  });

  it("returns false for undefined session", () => {
    expect(isAmongstTheOwners(undefined, aclWithUserAsOwner)).toBe(false);
  });
});

describe("isAmongstTheEditors", () => {
  it("returns true when user ID is in editors list", () => {
    expect(isAmongstTheEditors(mockSession, aclWithUserAsEditor)).toBe(true);
  });

  it("returns true when user's group is in editors list", () => {
    expect(isAmongstTheEditors(mockSession, aclWithGroupAsEditor)).toBe(true);
  });

  it("returns false when user is not in editors list", () => {
    expect(isAmongstTheEditors(mockSession, privateACL)).toBe(false);
  });

  it("returns false for null session", () => {
    expect(isAmongstTheEditors(null, aclWithUserAsEditor)).toBe(false);
  });

  it("returns false for undefined session", () => {
    expect(isAmongstTheEditors(undefined, aclWithUserAsEditor)).toBe(false);
  });
});

describe("isAmongstTheReaders", () => {
  it("returns true when user ID is in readers list", () => {
    expect(isAmongstTheReaders(mockSession, aclWithUserAsReader)).toBe(true);
  });

  it("returns true when user's group is in readers list", () => {
    expect(isAmongstTheReaders(mockSession, aclWithGroupAsReader)).toBe(true);
  });

  it("returns false when user is not in readers list", () => {
    expect(isAmongstTheReaders(mockSession, privateACL)).toBe(false);
  });

  it("returns false for null session", () => {
    expect(isAmongstTheReaders(null, aclWithUserAsReader)).toBe(false);
  });

  it("returns false for undefined session", () => {
    expect(isAmongstTheReaders(undefined, aclWithUserAsReader)).toBe(false);
  });
});

describe("canReadResource", () => {
  describe("public access", () => {
    it("allows unauthenticated users to read public resources", () => {
      expect(canReadResource(null, publicACL)).toBe(true);
      expect(canReadResource(undefined, publicACL)).toBe(true);
    });

    it("allows authenticated users to read public resources", () => {
      expect(canReadResource(mockSession, publicACL)).toBe(true);
    });
  });

  describe("access key", () => {
    it("allows access with valid access key (unauthenticated)", () => {
      expect(canReadResource(null, aclWithReadAccessKey, "validKey123")).toBe(true);
    });

    it("allows access with valid access key (authenticated)", () => {
      expect(canReadResource(mockSession, aclWithReadAccessKey, "validKey123")).toBe(true);
    });

    it("denies access with invalid access key", () => {
      expect(canReadResource(null, aclWithReadAccessKey, "invalidKey")).toBe(false);
    });

    it("denies access without access key when required", () => {
      expect(canReadResource(null, aclWithReadAccessKey)).toBe(false);
    });
  });

  describe("any authenticated user", () => {
    it("allows any authenticated user when 'user' is in readers", () => {
      expect(canReadResource(mockSession, aclWithUserInReaders)).toBe(true);
    });

    it("denies unauthenticated users when 'user' is in readers", () => {
      expect(canReadResource(null, aclWithUserInReaders)).toBe(false);
    });
  });

  describe("specific users", () => {
    it("allows owners to read", () => {
      expect(canReadResource(mockSession, aclWithUserAsOwner)).toBe(true);
    });

    it("allows editors to read", () => {
      expect(canReadResource(mockSession, aclWithUserAsEditor)).toBe(true);
    });

    it("allows readers to read", () => {
      expect(canReadResource(mockSession, aclWithUserAsReader)).toBe(true);
    });

    it("denies users not in any list", () => {
      expect(canReadResource(mockSession, privateACL)).toBe(false);
    });
  });

  describe("groups", () => {
    it("allows users with owner group to read", () => {
      expect(canReadResource(mockSession, aclWithGroupAsOwner)).toBe(true);
    });

    it("allows users with editor group to read", () => {
      expect(canReadResource(mockSession, aclWithGroupAsEditor)).toBe(true);
    });

    it("allows users with reader group to read", () => {
      expect(canReadResource(mockSession, aclWithGroupAsReader)).toBe(true);
    });
  });

  describe("admin users", () => {
    it("allows admin to read any resource", () => {
      expect(canReadResource(mockAdminSession, privateACL)).toBe(true);
    });
  });

  describe("unauthenticated users", () => {
    it("denies unauthenticated users for private resources", () => {
      expect(canReadResource(null, privateACL)).toBe(false);
      expect(canReadResource(undefined, privateACL)).toBe(false);
    });
  });
});

describe("canUpdateResource", () => {
  it("allows owners to update", () => {
    expect(canUpdateResource(mockSession, aclWithUserAsOwner)).toBe(true);
  });

  it("allows editors to update", () => {
    expect(canUpdateResource(mockSession, aclWithUserAsEditor)).toBe(true);
  });

  it("denies readers to update", () => {
    expect(canUpdateResource(mockSession, aclWithUserAsReader)).toBe(false);
  });

  it("allows users with owner group to update", () => {
    expect(canUpdateResource(mockSession, aclWithGroupAsOwner)).toBe(true);
  });

  it("allows users with editor group to update", () => {
    expect(canUpdateResource(mockSession, aclWithGroupAsEditor)).toBe(true);
  });

  it("allows admin to update", () => {
    expect(canUpdateResource(mockAdminSession, privateACL)).toBe(true);
  });

  it("denies unauthenticated users to update", () => {
    expect(canUpdateResource(null, aclWithUserAsOwner)).toBe(false);
    expect(canUpdateResource(undefined, aclWithUserAsOwner)).toBe(false);
  });

  it("denies users not in owners or editors list", () => {
    expect(canUpdateResource(mockSession, privateACL)).toBe(false);
  });
});

describe("canDeleteResource", () => {
  it("allows owners to delete", () => {
    expect(canDeleteResource(mockSession, aclWithUserAsOwner)).toBe(true);
  });

  it("denies editors to delete", () => {
    expect(canDeleteResource(mockSession, aclWithUserAsEditor)).toBe(false);
  });

  it("denies readers to delete", () => {
    expect(canDeleteResource(mockSession, aclWithUserAsReader)).toBe(false);
  });

  it("allows users with owner group to delete", () => {
    expect(canDeleteResource(mockSession, aclWithGroupAsOwner)).toBe(true);
  });

  it("denies users with editor group to delete", () => {
    expect(canDeleteResource(mockSession, aclWithGroupAsEditor)).toBe(false);
  });

  it("allows admin to delete", () => {
    expect(canDeleteResource(mockAdminSession, privateACL)).toBe(true);
  });

  it("denies unauthenticated users to delete", () => {
    expect(canDeleteResource(null, aclWithUserAsOwner)).toBe(false);
    expect(canDeleteResource(undefined, aclWithUserAsOwner)).toBe(false);
  });

  it("denies users not in owners list", () => {
    expect(canDeleteResource(mockSession, privateACL)).toBe(false);
  });
});

describe("canReadOrUpdateResourceACL", () => {
  it("allows owners to read/update ACL", () => {
    expect(canReadOrUpdateResourceACL(mockSession, aclWithUserAsOwner)).toBe(true);
  });

  it("denies editors to read/update ACL", () => {
    expect(canReadOrUpdateResourceACL(mockSession, aclWithUserAsEditor)).toBe(false);
  });

  it("denies readers to read/update ACL", () => {
    expect(canReadOrUpdateResourceACL(mockSession, aclWithUserAsReader)).toBe(false);
  });

  it("allows users with owner group to read/update ACL", () => {
    expect(canReadOrUpdateResourceACL(mockSession, aclWithGroupAsOwner)).toBe(true);
  });

  it("denies users with editor group to read/update ACL", () => {
    expect(canReadOrUpdateResourceACL(mockSession, aclWithGroupAsEditor)).toBe(false);
  });

  it("allows admin to read/update ACL", () => {
    expect(canReadOrUpdateResourceACL(mockAdminSession, privateACL)).toBe(true);
  });

  it("denies unauthenticated users to read/update ACL", () => {
    expect(canReadOrUpdateResourceACL(null, aclWithUserAsOwner)).toBe(false);
    expect(canReadOrUpdateResourceACL(undefined, aclWithUserAsOwner)).toBe(false);
  });

  it("denies users not in owners list", () => {
    expect(canReadOrUpdateResourceACL(mockSession, privateACL)).toBe(false);
  });
});
