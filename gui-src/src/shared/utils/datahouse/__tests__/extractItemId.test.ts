import { describe, it, expect } from "vitest";
import { extractItemIdFromReportUrl } from "../extractItemId";

describe("extractItemIdFromReportUrl", () => {
  it("extracts item ID from valid datahouse URL", () => {
    const url = "https://example.com/datahouse/items/abc123def456";
    expect(extractItemIdFromReportUrl(url)).toBe("abc123def456");
  });

  it("extracts item ID from URL with additional path segments", () => {
    const url = "/datahouse/items/report-789/details";
    expect(extractItemIdFromReportUrl(url)).toBe("report-789");
  });

  it("extracts item ID from URL with query parameters", () => {
    const url = "https://example.com/items/item-xyz?tab=details&view=full";
    expect(extractItemIdFromReportUrl(url)).toBe("item-xyz");
  });

  it("returns undefined when URL is undefined", () => {
    expect(extractItemIdFromReportUrl()).toBeUndefined();
  });

  it("returns undefined when URL is empty string", () => {
    expect(extractItemIdFromReportUrl("")).toBeUndefined();
  });

  it("returns undefined when URL does not contain /items/ pattern", () => {
    expect(extractItemIdFromReportUrl("https://example.com/reports/test")).toBeUndefined();
  });

  it("returns undefined when URL has /items/ but no ID after it", () => {
    expect(extractItemIdFromReportUrl("https://example.com/items/")).toBeUndefined();
  });

  it("handles URLs with special characters in item ID", () => {
    const url = "/items/abc-123_456";
    expect(extractItemIdFromReportUrl(url)).toBe("abc-123_456");
  });

  it("extracts only the first segment after /items/", () => {
    const url = "/api/v1/items/12345/attachments/67890";
    expect(extractItemIdFromReportUrl(url)).toBe("12345");
  });

  it("handles relative URLs", () => {
    const url = "/items/relative-id";
    expect(extractItemIdFromReportUrl(url)).toBe("relative-id");
  });

  it("handles absolute URLs with protocol", () => {
    const url = "https://gazelle.example.com/datahouse/items/full-url-id";
    expect(extractItemIdFromReportUrl(url)).toBe("full-url-id");
  });
});
