import { describe, it, expect, beforeEach, vi, afterEach } from "vitest";
import { saveSearch } from "../saveSearch";
import { toast } from "react-toastify";

vi.mock("react-toastify", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe("saveSearch", () => {
  const mockStorageKey = "test-storage-key";
  const mockSearchName = "My Test Search";
  const mockUrl = "/test/url";
  const mockFilters = { filter1: "value1", filter2: "value2" };

  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();

    // Mock globalThis.dispatchEvent
    vi.spyOn(globalThis, "dispatchEvent").mockImplementation(() => true);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe("Successful saves", () => {
    it("should save a search successfully with valid inputs", () => {
      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(true);
      expect(toast.success).toHaveBeenCalledWith("Search saved successfully");
      expect(globalThis.dispatchEvent).toHaveBeenCalledWith(expect.any(Event));

      const stored = JSON.parse(localStorage.getItem(mockStorageKey) || "[]");
      expect(stored).toHaveLength(1);
      expect(stored[0]).toEqual({
        name: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
      });
    });

    it("should save a search with custom success message", () => {
      const customMessage = "Custom success message";
      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
        successMessage: customMessage,
      });

      expect(result).toBe(true);
      expect(toast.success).toHaveBeenCalledWith(customMessage);
    });

    it("should append to existing searches", () => {
      const existingSearch = {
        name: "Existing Search",
        url: "/existing/url",
        filters: { existing: "filter" },
      };
      localStorage.setItem(mockStorageKey, JSON.stringify([existingSearch]));

      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(true);
      const stored = JSON.parse(localStorage.getItem(mockStorageKey) || "[]");
      expect(stored).toHaveLength(2);
      expect(stored[0]).toEqual(existingSearch);
      expect(stored[1].name).toBe(mockSearchName);
    });

    it("should trim whitespace from searchName and url", () => {
      const result = saveSearch({
        searchName: "  Trimmed Name  ",
        url: "  /trimmed/url  ",
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(true);
      const stored = JSON.parse(localStorage.getItem(mockStorageKey) || "[]");
      expect(stored[0].name).toBe("Trimmed Name");
      expect(stored[0].url).toBe("/trimmed/url");
    });
  });

  describe("Input validation", () => {
    it("should reject empty searchName", () => {
      const result = saveSearch({
        searchName: "",
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("Search name cannot be empty");
      expect(localStorage.getItem(mockStorageKey)).toBeNull();
    });

    it("should reject searchName with only whitespace", () => {
      const result = saveSearch({
        searchName: "   ",
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("Search name cannot be empty");
    });

    it("should reject empty url", () => {
      const result = saveSearch({
        searchName: mockSearchName,
        url: "",
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("URL cannot be empty");
    });

    it("should reject url with only whitespace", () => {
      const result = saveSearch({
        searchName: mockSearchName,
        url: "   ",
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("URL cannot be empty");
    });

    it("should reject empty storageKey", () => {
      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: "",
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("Invalid storage configuration");
    });

    it("should use custom error message when provided", () => {
      const customError = "Custom error message";
      const result = saveSearch({
        searchName: "",
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
        errorMessage: customError,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith(customError);
    });
  });

  describe("Duplicate detection", () => {
    it("should reject duplicate search names (exact match)", () => {
      localStorage.setItem(mockStorageKey, JSON.stringify([{ name: mockSearchName, url: "/other", filters: {} }]));

      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("A search with this name already exists");
    });

    it("should reject duplicate search names (case insensitive)", () => {
      localStorage.setItem(mockStorageKey, JSON.stringify([{ name: "my test search", url: "/other", filters: {} }]));

      const result = saveSearch({
        searchName: "MY TEST SEARCH",
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("A search with this name already exists");
    });

    it("should reject duplicate names after trimming whitespace", () => {
      localStorage.setItem(mockStorageKey, JSON.stringify([{ name: "Test Search", url: "/other", filters: {} }]));

      const result = saveSearch({
        searchName: "  Test Search  ",
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("A search with this name already exists");
    });
  });

  describe("Corrupted data handling", () => {
    it("should handle corrupted JSON in localStorage", () => {
      localStorage.setItem(mockStorageKey, "invalid json {[}");

      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(true);
      const stored = JSON.parse(localStorage.getItem(mockStorageKey) || "[]");
      expect(stored).toHaveLength(1);
      expect(stored[0].name).toBe(mockSearchName);
    });

    it("should handle non-array data in localStorage", () => {
      localStorage.setItem(mockStorageKey, JSON.stringify({ notAnArray: true }));

      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(true);
      const stored = JSON.parse(localStorage.getItem(mockStorageKey) || "[]");
      expect(stored).toHaveLength(1);
      expect(stored[0].name).toBe(mockSearchName);
    });
  });

  describe("localStorage availability", () => {
    it("should handle localStorage being unavailable", () => {
      const setItemSpy = vi.spyOn(Storage.prototype, "setItem");
      setItemSpy.mockImplementationOnce(() => {
        throw new Error("localStorage is not available");
      });

      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("Unable to access local storage");
    });

    it("should handle quota exceeded error", () => {
      const setItemSpy = vi.spyOn(Storage.prototype, "setItem");
      setItemSpy.mockImplementation((key) => {
        if (key === "__storage_test__") {
          return; // Allow test key
        }
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const error: any = new Error("QuotaExceededError");
        error.name = "QuotaExceededError";
        throw error;
      });

      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(false);
      expect(toast.error).toHaveBeenCalledWith("Failed to save search");
    });
  });

  describe("Edge cases", () => {
    it("should handle empty filters object", () => {
      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: {},
        storageKey: mockStorageKey,
      });

      expect(result).toBe(true);
      const stored = JSON.parse(localStorage.getItem(mockStorageKey) || "[]");
      expect(stored[0].filters).toEqual({});
    });

    it("should handle complex nested filters", () => {
      const complexFilters = {
        nested: { deep: { value: "test" } },
        array: [1, 2, 3],
        mixed: { arr: ["a", "b"], obj: { key: "val" } },
      };

      const result = saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: complexFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(true);
      const stored = JSON.parse(localStorage.getItem(mockStorageKey) || "[]");
      expect(stored[0].filters).toEqual(complexFilters);
    });

    it("should dispatch storage event to notify other tabs", () => {
      saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(globalThis.dispatchEvent).toHaveBeenCalledTimes(1);
      const dispatchedEvent = (globalThis.dispatchEvent as any).mock.calls[0][0];
      expect(dispatchedEvent).toBeInstanceOf(Event);
      expect(dispatchedEvent.type).toBe("storage");
    });

    it("should handle special characters in search name", () => {
      const specialName = "Search & Filter <test> \"quotes\" 'apostrophe'";
      const result = saveSearch({
        searchName: specialName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(true);
      const stored = JSON.parse(localStorage.getItem(mockStorageKey) || "[]");
      expect(stored[0].name).toBe(specialName);
    });

    it("should handle unicode characters in search name", () => {
      const unicodeName = "搜索 🔍 тест";
      const result = saveSearch({
        searchName: unicodeName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(result).toBe(true);
      const stored = JSON.parse(localStorage.getItem(mockStorageKey) || "[]");
      expect(stored[0].name).toBe(unicodeName);
    });
  });

  describe("Error logging", () => {
    it("should log errors to console when save fails", () => {
      const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
      const setItemSpy = vi.spyOn(Storage.prototype, "setItem");
      const testError = new Error("Test error");

      setItemSpy.mockImplementation((key) => {
        if (key !== "__storage_test__") {
          throw testError;
        }
      });

      saveSearch({
        searchName: mockSearchName,
        url: mockUrl,
        filters: mockFilters,
        storageKey: mockStorageKey,
      });

      expect(consoleSpy).toHaveBeenCalledWith("Failed to save search:", testError);

      consoleSpy.mockRestore();
    });
  });
});
