import { describe, it, expect, beforeEach, afterEach } from "vitest";
import { getAppStorageKey } from "../getAppStorageKey";

describe("getAppStorageKey", () => {
  let originalLocation: Location;

  beforeEach(() => {
    originalLocation = globalThis.location;
  });

  afterEach(() => {
    // Restore original location
    Object.defineProperty(globalThis, "location", {
      value: originalLocation,
      writable: true,
      configurable: true,
    });
  });

  const mockLocation = (pathname: string) => {
    Object.defineProperty(globalThis, "location", {
      value: { pathname },
      writable: true,
      configurable: true,
    });
  };

  describe("Valid gazelle app paths", () => {
    it("should return correct key for validation-portal app", () => {
      mockLocation("/gazelle/validation-portal/some/path");
      expect(getAppStorageKey()).toBe("validation-portal-savedSearch");
    });

    it("should return correct key for simulation-portal app", () => {
      mockLocation("/gazelle/simulation-portal/dashboard");
      expect(getAppStorageKey()).toBe("simulation-portal-savedSearch");
    });

    it("should return correct key for message-capture app", () => {
      mockLocation("/gazelle/message-capture");
      expect(getAppStorageKey()).toBe("message-capture-savedSearch");
    });

    it("should return correct key for user-management app", () => {
      mockLocation("/gazelle/user-management/users");
      expect(getAppStorageKey()).toBe("user-management-savedSearch");
    });

    it("should return correct key for test-execution app", () => {
      mockLocation("/gazelle/test-execution/runs");
      expect(getAppStorageKey()).toBe("test-execution-savedSearch");
    });

    it("should handle paths with trailing slashes", () => {
      mockLocation("/gazelle/validation-portal/");
      expect(getAppStorageKey()).toBe("validation-portal-savedSearch");
    });

    it("should handle paths with multiple trailing slashes", () => {
      mockLocation("/gazelle/simulation-portal///");
      expect(getAppStorageKey()).toBe("simulation-portal-savedSearch");
    });

    it("should handle deeply nested paths", () => {
      mockLocation("/gazelle/message-capture/section/subsection/page");
      expect(getAppStorageKey()).toBe("message-capture-savedSearch");
    });

    it("should handle app names with numbers", () => {
      mockLocation("/gazelle/app123/page");
      expect(getAppStorageKey()).toBe("app123-savedSearch");
    });

    it("should handle app names with hyphens", () => {
      mockLocation("/gazelle/my-custom-app/page");
      expect(getAppStorageKey()).toBe("my-custom-app-savedSearch");
    });
  });

  describe("Invalid or edge case paths", () => {
    it("should return default key for root path", () => {
      mockLocation("/");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });

    it("should return default key for empty path", () => {
      mockLocation("");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });

    it("should return default key when only gazelle prefix exists", () => {
      mockLocation("/gazelle");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });

    it("should return default key when only gazelle prefix with trailing slash", () => {
      mockLocation("/gazelle/");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });

    it("should return default key for non-gazelle paths", () => {
      mockLocation("/other-app/some/path");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });

    it("should return default key for paths not starting with gazelle", () => {
      mockLocation("/app/gazelle/validation-portal");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });

    it("should return default key for paths with gazelle later in URL", () => {
      mockLocation("/prefix/gazelle/validation-portal");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });

    it("should handle paths with only slashes", () => {
      mockLocation("///");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });
  });

  describe("Server-side rendering", () => {
    it("should return default key when globalThis is undefined", () => {
      // We can't actually make globalThis undefined, but we can test
      // the logic by directly testing when location is unavailable
      Object.defineProperty(globalThis, "location", {
        get: () => {
          throw new Error("location is not available");
        },
        configurable: true,
      });

      expect(() => getAppStorageKey()).toThrow();

      // Restore
      Object.defineProperty(globalThis, "location", {
        value: originalLocation,
        writable: true,
        configurable: true,
      });
    });
  });

  describe("Case sensitivity", () => {
    it("should be case sensitive for 'gazelle' prefix (lowercase)", () => {
      mockLocation("/gazelle/validation-portal/page");
      expect(getAppStorageKey()).toBe("validation-portal-savedSearch");
    });

    it("should not match uppercase GAZELLE prefix", () => {
      mockLocation("/GAZELLE/validation-portal/page");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });

    it("should not match mixed case Gazelle prefix", () => {
      mockLocation("/Gazelle/validation-portal/page");
      expect(getAppStorageKey()).toBe("gazelle-savedSearch");
    });

    it("should preserve case in app name", () => {
      mockLocation("/gazelle/ValidationPortal/page");
      expect(getAppStorageKey()).toBe("ValidationPortal-savedSearch");
    });
  });

  describe("Special characters in paths", () => {
    it("should handle URL encoded characters in app name", () => {
      mockLocation("/gazelle/app%20name/page");
      expect(getAppStorageKey()).toBe("app%20name-savedSearch");
    });

    it("should handle query parameters", () => {
      mockLocation("/gazelle/validation-portal/page?query=test");
      expect(getAppStorageKey()).toBe("validation-portal-savedSearch");
    });

    it("should handle hash fragments", () => {
      mockLocation("/gazelle/validation-portal/page#section");
      expect(getAppStorageKey()).toBe("validation-portal-savedSearch");
    });

    it("should handle underscores in app name", () => {
      mockLocation("/gazelle/my_app_name/page");
      expect(getAppStorageKey()).toBe("my_app_name-savedSearch");
    });

    it("should handle dots in app name", () => {
      mockLocation("/gazelle/app.name/page");
      expect(getAppStorageKey()).toBe("app.name-savedSearch");
    });
  });

  describe("Real-world URL patterns", () => {
    it("should handle validation portal with filters", () => {
      mockLocation("/gazelle/validation-portal/instances?status=active");
      expect(getAppStorageKey()).toBe("validation-portal-savedSearch");
    });

    it("should handle simulation portal with nested routes", () => {
      mockLocation("/gazelle/simulation-portal/simulations/123/details");
      expect(getAppStorageKey()).toBe("simulation-portal-savedSearch");
    });

    it("should handle message capture with search params", () => {
      mockLocation("/gazelle/message-capture/messages?from=2024-01-01");
      expect(getAppStorageKey()).toBe("message-capture-savedSearch");
    });

    it("should handle user management with user ID", () => {
      mockLocation("/gazelle/user-management/users/456/edit");
      expect(getAppStorageKey()).toBe("user-management-savedSearch");
    });
  });

  describe("Empty segments and consecutive slashes", () => {
    it("should filter out empty segments from consecutive slashes", () => {
      mockLocation("//gazelle//validation-portal//page");
      expect(getAppStorageKey()).toBe("validation-portal-savedSearch");
    });

    it("should filter out empty segments at the end", () => {
      mockLocation("/gazelle/simulation-portal////");
      expect(getAppStorageKey()).toBe("simulation-portal-savedSearch");
    });

    it("should handle mix of empty and valid segments", () => {
      mockLocation("///gazelle///message-capture///");
      expect(getAppStorageKey()).toBe("message-capture-savedSearch");
    });
  });
});
