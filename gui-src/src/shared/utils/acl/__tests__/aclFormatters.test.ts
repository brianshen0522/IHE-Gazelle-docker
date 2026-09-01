import { describe, it, expect, vi, beforeEach } from "vitest";
import type { TFunction } from "i18next";
import {
  formatWithTranslation,
  formatWithTranslationWithOrga,
  formatItemsWithTranslation,
  formatItemsWithTranslationAndOrga,
} from "../aclFormatters";

describe("aclFormatters", () => {
  const mockT = vi.fn((key: string) => {
    if (key === "gzl.gum.organization_admin") {
      return "Organization Admin";
    }
    return key;
  }) as unknown as TFunction;

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("formatWithTranslation", () => {
    it("should translate a simple name", () => {
      const input = { id: "1", name: "gzl.user" };
      const result = formatWithTranslation(input, mockT);

      expect(result).toEqual({ id: "1", name: "gzl.user" });
      expect(mockT).toHaveBeenCalledWith("gzl.user");
    });

    it("should handle organization_admin special case", () => {
      const input = { id: "1", name: "gzl.gum.organization_admin" };
      const result = formatWithTranslation(input, mockT);

      expect(result.name).toBe("Organization Admin");
      expect(mockT).toHaveBeenCalledWith("gzl.gum.organization_admin");
    });

    it("should handle organization_admin in a longer string", () => {
      const input = { id: "1", name: "prefix_gzl.gum.organization_admin_suffix" };
      const result = formatWithTranslation(input, mockT);

      expect(result.name).toContain("Organization Admin");
    });
  });

  describe("formatWithTranslationWithOrga", () => {
    it("should translate name and preserve organization", () => {
      const input = { id: "1", name: "gzl.user", organization: "Test Org" };
      const result = formatWithTranslationWithOrga(input, mockT);

      expect(result).toEqual({
        id: "1",
        name: "gzl.user",
        organization: "Test Org",
      });
    });

    it("should handle missing organization", () => {
      const input = { id: "1", name: "gzl.user", organization: "" };
      const result = formatWithTranslationWithOrga(input, mockT);

      expect(result.id).toBe("1");
      expect(result.name).toBe("gzl.user");
      expect(result.organization).toBe("");
    });
  });

  describe("formatItemsWithTranslation", () => {
    it("should format an array of items", () => {
      const input = [
        { id: "1", name: "gzl.user1" },
        { id: "2", name: "gzl.user2" },
      ];
      const result = formatItemsWithTranslation(input, mockT);

      expect(result).toHaveLength(2);
      expect(mockT).toHaveBeenCalledTimes(2);
    });

    it("should handle empty array", () => {
      const result = formatItemsWithTranslation([], mockT);

      expect(result).toEqual([]);
      expect(mockT).not.toHaveBeenCalled();
    });
  });

  describe("formatItemsWithTranslationAndOrga", () => {
    it("should format an array of items with organizations", () => {
      const input = [
        { id: "1", name: "gzl.user1", organization: "Org 1" },
        { id: "2", name: "gzl.user2", organization: "Org 2" },
      ];
      const result = formatItemsWithTranslationAndOrga(input, mockT);

      expect(result).toHaveLength(2);
      expect(result[0].organization).toBe("Org 1");
      expect(result[1].organization).toBe("Org 2");
    });

    it("should handle empty array", () => {
      const result = formatItemsWithTranslationAndOrga([], mockT);

      expect(result).toEqual([]);
      expect(mockT).not.toHaveBeenCalled();
    });
  });
});
